package de.mclick.ows10;

import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.http.auth.AuthScope.ANY;
import static org.apache.http.entity.ContentType.TEXT_XML;
import static org.deegree.commons.xml.stax.XMLStreamUtils.copy;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mclick.AppSettings;

public class WfsInsertEventServiceNotifier implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger( WfsInsertEventServiceNotifier.class );

    private static final QName WFS_200_TRANSACTION_REQUEST = new QName( WFS_200_NS, "Transaction" );

    private static final QName WFS_200_TRANSACTION_RESPONSE = new QName( WFS_200_NS, "TransactionResponse" );

    private static final QName WFS_200_TOTAL_INSERTED = new QName( WFS_200_NS, "totalInserted" );

    private static final QName WFS_200_INSERT = new QName( WFS_200_NS, "Insert" );

    private static final String NS_SOAP = "http://www.w3.org/2003/05/soap-envelope";

    private static final String NS_WSA = "http://www.w3.org/2005/08/addressing";

    private static final String NS_WSNT = "http://docs.oasis-open.org/wsn/b-2";

    private static final String ACTION_NOTIFY = "http://docs.oasis-open.org/wsn/bw-2/NotificationConsumer/Notify";

    private static final String ROLE_ANONYMOUS = "http://www.w3.org/2005/08/addressing/role/anonymous";

    private String urlBroker;

    private String httpUserName;

    private String httpPassword;

    @Override
    public void init( FilterConfig filterConfig )
                            throws ServletException {
        LOG.info( "Initializing WfsInsertEventServiceNotifier" );
        try {
            DeegreeWorkspace ws = getActiveWorkspace( filterConfig.getServletContext().getContextPath() );
            AppSettings settings = getAppSettings( ws.getLocation() );
            urlBroker = settings.get( "ows10.eventService.url" );
            LOG.info( " - url: " + urlBroker );
            httpUserName = settings.get( "ows10.eventService.username" );
            LOG.info( " - username: " + httpUserName );
            httpPassword = settings.get( "ows10.eventService.password" );
            LOG.info( " - password: " + httpPassword );
        } catch ( Exception e ) {
            String msg = "Error reading settings.ini: " + e.getLocalizedMessage();
            LOG.error( msg, e );
            throw new ServletException( msg, e );
        }
    }

    private DeegreeWorkspace getActiveWorkspace( final String ctxPath )
                            throws ServletException, IOException {
        File wsRoot = new File( DeegreeWorkspace.getWorkspaceRoot() );
        if ( !wsRoot.exists() || !wsRoot.isDirectory() ) {
            String msg = "Workspace root directory ('" + wsRoot + "') does not exist or does not denote a directory.";
            LOG.error( msg );
            throw new IOException( msg );
        }
        String wsName = null;
        String webappName = ctxPath;
        if ( webappName.startsWith( "/" ) ) {
            webappName = webappName.substring( 1 );
        }
        File file = new File( wsRoot, webappName );
        LOG.debug( "Matching by webapp name ('" + file + "'). Checking for workspace directory '" + file + "'" );
        if ( file.exists() ) {
            wsName = webappName;
            LOG.info( "Active workspace determined by matching webapp name (" + webappName
                      + ") with available workspaces." );
        } else {
            String msg = "No workspace with webapp name (" + webappName + ") available.";
            LOG.error( msg );
            throw new IOException( msg );
        }
        DeegreeWorkspace ws = DeegreeWorkspace.getInstance( wsName, null );
        LOG.info( "Using workspace '{}' at '{}'", ws.getName(), ws.getLocation() );
        return ws;
    }

    private AppSettings getAppSettings( final File location )
                            throws ServletException {
        try {
            return new AppSettings( new File( location, "../../" ) );
        } catch ( IOException e ) {
            throw new ServletException( "Error accessing application settings (settings.ini):" + e.getMessage() );
        }
    }

    @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
                            throws IOException, ServletException {

        if ( isPostRequest( (HttpServletRequest) request ) ) {
            LOG.info( "Buffering request for potential Event Service notification" );
            final File requestPayloadFile = File.createTempFile( "fixm_wfs_request", ".xml" );
            final File responsePayloadFile = File.createTempFile( "fixm_wfs_response", ".xml" );
            final StreamBufferStore requestPayloadBuffer = new StreamBufferStore( 20 * 1024 * 1024, requestPayloadFile );
            final StreamBufferStore responsePayloadBuffer = new StreamBufferStore( 20 * 1024 * 1024,
                                                                                   responsePayloadFile );
            try {
                request = new BufferedHttpServletRequest( (HttpServletRequest) request, requestPayloadBuffer );
                if ( isWfsTransactionRequest( requestPayloadBuffer ) ) {
                    LOG.info( "Buffering response for potential Event Service notification" );
                    response = new BufferingHttpServletResponse( (HttpServletResponse) response, responsePayloadBuffer );
                    chain.doFilter( request, response );
                    requestPayloadBuffer.close();
                    responsePayloadBuffer.close();
                    if ( isNotifyingRequired( responsePayloadBuffer ) ) {
                        notifyEventService( requestPayloadBuffer );
                    }
                } else {
                    LOG.info( "Not buffering response: Not a WFS transaction request" );
                    chain.doFilter( request, response );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            } finally {
                deleteQuietly( requestPayloadFile );
                deleteQuietly( responsePayloadFile );
            }
        } else {
            LOG.info( "Not buffering request/response: Not a POST request" );
            chain.doFilter( request, response );
        }
    }

    private boolean isPostRequest( final HttpServletRequest request ) {
        if ( "POST".equals( request.getMethod() ) ) {
            return true;
        }
        return false;
    }

    private boolean isWfsTransactionRequest( StreamBufferStore requestPayloadBuffer ) {
        try {
            final InputStream inputStream = requestPayloadBuffer.getInputStream();
            final XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( inputStream );
            try {
                skipStartDocument( xmlStream );
                if ( xmlStream.isStartElement() && WFS_200_TRANSACTION_REQUEST.equals( xmlStream.getName() ) ) {
                    LOG.info( "Detected WFS 2.0.0 transaction request" );
                    return true;
                }
            } finally {
                if ( xmlStream != null ) {
                    xmlStream.close();
                    inputStream.close();
                }
            }
        } catch ( Exception e ) {
            // nothing to do
        }
        return false;
    }

    private boolean isNotifyingRequired( final StreamBufferStore responsePayloadBuffer )
                            throws IOException, XMLStreamException, FactoryConfigurationError {
        final InputStream inputStream = responsePayloadBuffer.getInputStream();
        final XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( inputStream );
        try {
            skipStartDocument( xmlStream );
            if ( xmlStream.isStartElement() && WFS_200_TRANSACTION_RESPONSE.equals( xmlStream.getName() ) ) {
                LOG.info( "Detected WFS 2.0.0 transaction response" );
                while ( xmlStream.getEventType() != END_DOCUMENT ) {
                    nextElement( xmlStream );
                    if ( xmlStream.isStartElement() && WFS_200_TOTAL_INSERTED.equals( xmlStream.getName() ) ) {
                        final String numInsertedString = xmlStream.getElementText();
                        if ( numInsertedString != null ) {
                            int numInserted = Integer.parseInt( numInsertedString.trim() );
                            if ( numInserted > 0 ) {
                                LOG.info( "Inserted features: " + numInserted );
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
            }
        } finally {
            if ( xmlStream != null ) {
                xmlStream.close();
                inputStream.close();
            }
        }
        return false;
    }

    private void notifyEventService( StreamBufferStore requestPayloadBuffer )
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        LOG.info( "Notifying Event Service" );
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final XMLOutputFactory of = XMLOutputFactory.newInstance();
        of.setProperty( IS_REPAIRING_NAMESPACES, "true" );
        final XMLStreamWriter xmlWriter = of.createXMLStreamWriter( bos );

        try {
            xmlWriter.writeStartDocument( "UTF-8", "1.0" );
            xmlWriter.writeStartElement( "soap", "Envelope", NS_SOAP );
            xmlWriter.writeNamespace( "soap", NS_SOAP );
            xmlWriter.writeNamespace( "wsa", NS_WSA );
            xmlWriter.writeNamespace( "wsnt", NS_WSNT );

            xmlWriter.writeStartElement( "soap", "Header", NS_SOAP );
            writeElement( xmlWriter, "wsa", "To", NS_WSA, urlBroker );
            writeElement( xmlWriter, "wsa", "Action", NS_WSA, ACTION_NOTIFY );
            final String messageId = "uuid:" + UUID.randomUUID();
            writeElement( xmlWriter, "wsa", "MessageID", NS_WSA, messageId );
            xmlWriter.writeStartElement( "wsa", "From", NS_WSA );
            writeElement( xmlWriter, "wsa", "Address", NS_WSA, ROLE_ANONYMOUS );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement( "soap", "Body", NS_SOAP );
            xmlWriter.writeStartElement( "wsnt", "Notify", NS_WSNT );
            xmlWriter.writeStartElement( "wsnt", "NotificationMessage", NS_WSNT );
            xmlWriter.writeStartElement( "wsnt", "Topic", NS_WSNT );
            xmlWriter.writeAttribute( "Dialect", "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple" );
            xmlWriter.writeCharacters( "aviation" );
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement( "wsnt", "Message", NS_WSNT );
            copyGmlFeatures( requestPayloadBuffer, xmlWriter );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();

            xmlWriter.writeEndElement();
            xmlWriter.writeEndDocument();

            LOG.info( bos.toString() );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( bos.toString() );
            }

            final DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.getCredentialsProvider().setCredentials( ANY,
                                                                new UsernamePasswordCredentials( httpUserName,
                                                                                                 httpPassword ) );
            final HttpPost httpPost = new HttpPost( urlBroker );
            httpPost.setEntity( new ByteArrayEntity( bos.toByteArray(), TEXT_XML ) );
            final HttpResponse httpResponse = httpClient.execute( httpPost );
            if ( httpResponse.getStatusLine().getStatusCode() != 204 ) {
                LOG.error( "Notifying of Event Service failed: " + httpResponse.getStatusLine().getStatusCode() + ": "
                           + httpResponse.getStatusLine().getReasonPhrase() );
            } else {
                LOG.info( "Event Service notified successfully" );
            }
        } finally {
            if ( xmlWriter != null ) {
                xmlWriter.close();
                bos.close();
            }
        }
    }

    private void copyGmlFeatures( final StreamBufferStore requestPayloadBuffer, final XMLStreamWriter xmlWriter )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        final InputStream inputStream = requestPayloadBuffer.getInputStream();
        final XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( inputStream );
        try {
            skipStartDocument( xmlReader );
            while ( xmlReader.getEventType() != END_DOCUMENT ) {
                if ( xmlReader.isStartElement() && WFS_200_INSERT.equals( xmlReader.getName() ) ) {
                    LOG.info( "Detected WFS 2.0.0 insert element" );
                    nextElement( xmlReader );
                    while ( !( xmlReader.isEndElement() && WFS_200_INSERT.equals( xmlReader.getName() ) ) ) {
                        if ( xmlReader.isStartElement() ) {
                            copy( xmlWriter, xmlReader );
                        } else {
                            nextElement( xmlReader );
                        }
                    }
                } else {
                    xmlReader.next();
                }
            }
        } finally {
            if ( xmlReader != null ) {
                xmlReader.close();
                inputStream.close();
            }
        }
    }

    private void writeElement( final XMLStreamWriter xmlWriter, final String prefix, final String localName,
                               final String ns, final String text )
                            throws XMLStreamException {
        xmlWriter.writeStartElement( prefix, localName, ns );
        xmlWriter.writeCharacters( text );
        xmlWriter.writeEndElement();
    }

    @Override
    public void destroy() {
        LOG.info( "Destroying WfsInsertEventServiceNotifier" );
    }

}
