//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.workspace.standard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceException;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ResourceLocation} backed by a file on the file system.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class DefaultResourceLocation<T extends Resource> implements ResourceLocation<T> {

    private static Logger LOG = LoggerFactory.getLogger( DefaultResourceManager.class );

    private File location;

    private ResourceIdentifier<T> identifier;

    public DefaultResourceLocation( File file, ResourceIdentifier<T> identifier ) {
        this.location = file;
        this.identifier = identifier;
    }

    @Override
    public String getNamespace() {
        InputStream fis = null;
        XMLStreamReader in = null;
        try {
            in = XMLInputFactory.newInstance().createXMLStreamReader( fis = getAsStream() );
            while ( !in.isStartElement() ) {
                in.next();
            }
            return in.getNamespaceURI();
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if ( in != null ) {
                try {
                    in.close();
                } catch ( Exception e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            IOUtils.closeQuietly( fis );
        }
        return null;
    }

    @Override
    public ResourceIdentifier<T> getIdentifier() {
        return identifier;
    }

    @Override
    public InputStream getAsStream() {
        try {
            return new FileInputStream( location );
        } catch ( FileNotFoundException e ) {
            LOG.error( "Opening of input stream for file '" + location + "' failed: " + e.getMessage() );
        }
        return null;
    }

    @Override
    public InputStream resolve( String path ) {
        URL url = null;
        try {
            url = resolveToUrl( path );
            if ( url != null ) {
                return url.openStream();
            }
        } catch ( Exception e ) {
            LOG.error( "Opening of input stream from URL '" + url + "' failed: " + e.getMessage() );
        }
        return null;
    }

    @Override
    public String toString() {
        return identifier.toString();
    }

    @Override
    public File resolveToFile( String path ) {
        try {
            return new File( resolveUrlOrFileOrRelativePath( path ) );
        } catch ( Exception e ) {
            LOG.error( "Resolving of path '" + path + "' (location: '" + location + "') to file failed: "
                                               + e.getMessage(), e );
        }
        return null;

    }

    @Override
    public URL resolveToUrl( String path ) {
        try {
            return resolveUrlOrFileOrRelativePath( path ).toURL();
        } catch ( Exception e ) {
            LOG.error( "Resolving of path '" + path + "' (location: '" + location + "') to URL failed: "
                                               + e.getMessage(), e );
        }
        return null;
    }

    private URI resolveUrlOrFileOrRelativePath( String path ) {
        try {
            URL url = new URL( path );
            return url.toURI();
        } catch ( Exception e ) {
            // not a valid URL
        }
        File file = new File( path );
        if ( file.isAbsolute() ) {
            return file.toURI();
        }
        return new File( this.location, path ).toURI();
    }

    public File getFile() {
        return location;
    }

    @Override
    public void deactivate() {
        File f = new File( location.getParentFile(), identifier.getId() + ".ignore" );
        location.renameTo( f );
        location = f;
    }

    @Override
    public void activate() {
        File f = new File( location.getParentFile(), identifier.getId() + ".xml" );
        location.renameTo( f );
        location = f;
    }

    @Override
    public void setContent( InputStream in ) {
        try {
            location.getParentFile().mkdirs();
            FileUtils.copyInputStreamToFile( in, location );
        } catch ( IOException e ) {
            throw new ResourceException( e.getLocalizedMessage(), e );
        }
    }

}
