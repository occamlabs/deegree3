import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

// GDAL_DATA: /usr/share/gdal/1.10
// LD_LIBRARY_PATH: /home/schneider/gdal-1.10.1/.libs
public class AccessEcwViaImageIo {

    public static void main( String[] args )
                            throws Exception {

        File file = new File( "/home/schneider/geodata/ecw/Ortho10_2012_01.ecw" );
        Iterator<ImageReader> readerIter = ImageIO.getImageReaders( file );
        if ( !readerIter.hasNext() ) {
            System.out.println( "No reader for file!?" );
        }
        ImageReader reader = readerIter.next();
        System.out.println(reader.getNumImages( true ));
    }
}
