import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.coverageio.gdal.ecw.ECWReader;
import org.geotools.geometry.GeneralEnvelope;

// GDAL_DATA: /usr/share/gdal/1.10
// LD_LIBRARY_PATH: /home/schneider/gdal-1.10.1/.libs
public class AccessEcwViaGeoTools {

    public static void main( String[] args )
                            throws Exception {

        File file = new File( "/home/schneider/geodata/ecw/RGBImage.ecw" );
        ECWReader reader = new ECWReader( file );
        System.out.println("- Envelope: " + getEnvelope( reader ));

        GridCoverage2D coverage = reader.read( null );
        System.out.println(coverage.getNumOverviews());
        RenderedImage renderedImage = coverage.getRenderedImage();

        File outputfile = new File( "/tmp/image.jpg" );
        ImageIO.write( renderedImage, "jpg", outputfile );       
    }

    private static GeneralEnvelope getEnvelope( ECWReader reader ) {
        GeneralEnvelope gtEnvelope = reader.getOriginalEnvelope();
        return gtEnvelope;
    }
}
