package de.mclick;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates access to parameters from app-specific <code>settings.ini</code> file.
 * 
 * @author <a href="mailto:schneider@m-click.aero>Markus Schneider</a>
 * 
 * @since 0.9
 */
public class AppSettings {

    private static final Charset CHARSET = Charset.forName( "US-ASCII" );

    private static final Logger LOG = LoggerFactory.getLogger( AppSettings.class );

    private final Map<String, String> nameToValue;

    private final File appDir;

    public AppSettings( File appDir ) throws IOException {
        this.appDir = appDir;
        nameToValue = getParams( new File( appDir, "settings.ini" ) );
    }

    public String get( String param ) {
        String value = nameToValue.get( param );
        LOG.info( param + "='" + value + "'" );
        return value;
    }

    public File getAppDir() {
        return appDir;
    }

    private Map<String, String> getParams( File file )
                            throws IOException {
        Map<String, String> nameToValue = new HashMap<String, String>();
        if ( file.exists() && file.canRead() ) {
            List<String> lines = FileUtils.readLines( file, CHARSET.name() );
            for ( String line : lines ) {
                int pos = line.indexOf( '=' );
                if ( pos != -1 ) {
                    String name = line.substring( 0, pos ).trim();
                    String value = line.substring( pos + 1 ).trim();
                    if ( value.startsWith( "\"" ) ) {
                        value = value.substring( 1 );
                    }
                    if ( value.endsWith( "\"" ) ) {
                        value = value.substring( 0, value.length() - 1 );
                    }
                    nameToValue.put( name, value );
                }
            }
        } else {
            LOG.error( "Cannot read '" + file + "'." );
        }
        return nameToValue;
    }
}
