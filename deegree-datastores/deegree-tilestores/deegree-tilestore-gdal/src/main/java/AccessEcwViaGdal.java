import static java.awt.color.ColorSpace.CS_sRGB;
import static java.awt.image.DataBuffer.TYPE_BYTE;
import static java.awt.image.Raster.createBandedRaster;
import static org.gdal.gdalconst.gdalconstConstants.CE_None;
import static org.gdal.gdalconst.gdalconstConstants.GA_ReadOnly;
import static org.gdal.gdalconst.gdalconstConstants.GDT_Byte;

import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

// GDAL_DATA: /usr/share/gdal/1.10
// LD_LIBRARY_PATH: /home/schneider/gdal-1.10.1/.libs
public class AccessEcwViaGdal {

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {
        gdal.AllRegister();

        List<Thread> threads = new ArrayList<Thread>();
        final List<Dataset> datasets = Collections.synchronizedList( new ArrayList<Dataset>() );

        for ( int threadNo = 0; threadNo < 10; threadNo++ ) {
            final int j = threadNo;
            threads.add( new Thread( new Runnable() {
                @Override
                public void run() {
                    Dataset dataset = gdal.Open( "/mnt/storage/geodata/ecw/gbo-lufo/2012/ortho10_2012_overview.ecw",
                                                 GA_ReadOnly );
                    datasets.add( dataset );
                    for ( int i = 0; i < 10; i++ ) {
                        try {
                            extractRandomRegionAndSaveImage( dataset, i, j );
                        } catch ( IOException e ) {
                            e.printStackTrace();
                        }
                    }
                }
            } ) );
        }

        for ( Thread thread : threads ) {
            thread.start();
        }

        for ( Thread thread : threads ) {
            thread.join();
        }
        
        for ( Dataset dataset : datasets ) {
            dataset.delete();
        }

        System.out.println("********************************* DELETED");

        threads.clear();
        datasets.clear();
        for ( int threadNo = 0; threadNo < 10; threadNo++ ) {
            final int j = threadNo;
            threads.add( new Thread( new Runnable() {
                @Override
                public void run() {
                    Dataset dataset = gdal.Open( "/mnt/storage/geodata/ecw/gbo-lufo/2012/ortho10_2012_overview.ecw",
                                                 GA_ReadOnly );
                    datasets.add( dataset );
                    for ( int i = 0; i < 10; i++ ) {
                        try {
                            extractRandomRegionAndSaveImage( dataset, i, j );
                        } catch ( IOException e ) {
                            e.printStackTrace();
                        }
                    }
                }
            } ) );
        }
        
        for ( Thread thread : threads ) {
            thread.start();
        }
        for ( Thread thread : threads ) {
            thread.join();
        }        
        for ( Dataset dataset : datasets ) {
            dataset.delete();
        }
        
        
//        for ( int threadNo = 0; threadNo < 27; threadNo++ ) {
//            final int j = threadNo;
//            threads.add( new Thread( new Runnable() {
//                @Override
//                public void run() {
//                    Dataset dataset = datasets.get( j );
//                    for ( int i = 0; i < 20; i++ ) {
//                        try {
//                            extractRandomRegionAndSaveImage( dataset, i, j );
//                        } catch ( IOException e ) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            } ) );
//        }
//
//        for ( Dataset dataset : datasets ) {
//            dataset.delete();
//        }

        //
        // System.out.println( "- Envelope: " + getEnvelope( dataset ) );
        //
        // for ( int i = 0; i < dataset.getRasterCount(); i++ ) {
        // Band band = dataset.GetRasterBand( i + 1 );
        // System.out.println( "- Band " + i );
        // System.out.println( " - data type: " + GetDataTypeName( band.getDataType() ) );
        // System.out.println( " - color: " + GetColorInterpretationName( band.GetColorInterpretation() ) );
        // System.out.println( " - overviews: " + band.GetOverviewCount() );
        // System.out.println( " - size: " + band.getXSize() + " x " + band.getYSize() );
        // }

        // Dataset dataset = gdal.OpenShared( "/home/markus/Development/geodata/Satellite_Provo.tif" );
        //
        // String wkt4326 = readFileToString( new File( AccessEcwViaGdal.class.getResource( "/epsg4326.wkt" ).toURI() )
        // );
        // SpatialReference crs4326 = new SpatialReference( wkt4326 );
        // String wkt28992 = readFileToString( new File( AccessEcwViaGdal.class.getResource( "/epsg28992.wkt" ).toURI()
        // ) );
        // SpatialReference crs28992 = new SpatialReference( wkt28992 );
        // CoordinateTransformation transform28992To4326 = CreateCoordinateTransformation( crs28992, crs4326 );
        // Envelope env28992 = getEnvelope( dataset );
        // double[][] corners28992 = new double[4][];
        // corners28992[0] = new double[] { env28992.getMin().get0(), env28992.getMin().get1() };
        // corners28992[1] = new double[] { env28992.getMax().get0(), env28992.getMin().get1() };
        // corners28992[2] = new double[] { env28992.getMax().get0(), env28992.getMax().get1() };
        // corners28992[3] = new double[] { env28992.getMin().get0(), env28992.getMax().get1() };
        // transform28992To4326.TransformPoints( corners28992 );
        // double minX = corners28992[0][0];
        // double maxX = corners28992[0][0];
        // double minY = corners28992[0][1];
        // double maxY = corners28992[0][1];
        // for ( double[] point : corners28992 ) {
        // double x = point[0];
        // double y = point[1];
        // if ( x < minX ) {
        // minX = x;
        // } else if ( x > maxX ) {
        // maxX = x;
        // }
        // if ( y < minY ) {
        // minY = y;
        // } else if ( y > maxY ) {
        // maxY = y;
        // }
        // }
        // Point min = new DefaultPoint( null, null, null, new double[] { minX, minY } );
        // Point max = new DefaultPoint( null, null, null, new double[] { maxX, maxY } );
        // Envelope env4326 = new DefaultEnvelope( min, max );
        // System.out.println( env4326 );
        //
        // // Dataset dataset4326 = gdal.AutoCreateWarpedVRT( dataset, wkt28992, wkt4326 );
        // // System.out.println( getEnvelope( dataset ) );
        // // System.out.println( dataset.getRasterXSize() + "," + dataset.getRasterYSize() );
        // // System.out.println( getEnvelope( dataset4326 ) );
        // // System.out.println( dataset4326.getRasterXSize() + "," + dataset4326.getRasterYSize() );
        // // System.out.println( dataset4326.GetRasterBand( 1 ).GetOverviewCount() );
        //
        // int xSizeSrc = dataset.getRasterXSize();
        // int ySizeSrc = dataset.getRasterYSize();
        // BufferedImage region = null;
        // for ( int i = 0; i < 10; i++ ) {
        // // region = extractRegion( dataset, 0, 0, 650000, 205000, 6500, 2050 );
        // region = extractRegion( dataset, 0, 0, 65000, 205000, 1600, 1200 );
        // }
        //
        // long before = System.currentTimeMillis();
        // List<Thread> threads = new ArrayList<Thread>();
        // for ( int i = 0; i < 1; i++ ) {
        // final int threadNo = i + 1;
        // Thread thread = new Thread( new Runnable() {
        // @Override
        // public void run() {
        // for ( int i = 0; i < 100; i++ ) {
        // Dataset dataset = gdal.OpenShared( "/mnt/storage/geodata/ecw/Ortho10_2012_01.ecw" );
        // // try {
        // // extractRandomRegionAndSaveImage( dataset, i, threadNo );
        // // } catch ( IOException e ) {
        // // e.printStackTrace();
        // // }
        // dataset.delete();
        // }
        // }
        // } );
        // thread.start();
        // threads.add( thread );
        // }
        // for ( Thread thread : threads ) {
        // thread.join();
        // }
        // System.out.println("HUHU");
        // long after = System.currentTimeMillis();
        // System.out.println( "total: " + ( after - before ) + " [ms]" );
        // //// dataset4326.delete();
        // // dataset.delete();
        // //
        // // File outputfile = new File( "/tmp/output.jpg" );
        // // ImageIO.write( region, "jpg", outputfile );
    }

    private static Dataset extractRegion( Dataset dataset, int xoff, int yoff, int xsize, int ysize, int pixelsX,
                                          int pixelsY ) {
        int numBands = dataset.getRasterCount();
        int buf_type = gdalconstConstants.GDT_Byte;
        byte[] buffer = new byte[pixelsX * pixelsY * numBands];
        if ( dataset.ReadRaster( xoff, yoff, xsize, ysize, pixelsX, pixelsY, buf_type, buffer, null, 0 ) != CE_None ) {
            throw new RuntimeException( "Error reading raster." );
        }
        Driver vrtDriver = gdal.GetDriverByName( "MEM" );
        Dataset region = vrtDriver.Create( "/tmp/whatever", pixelsX, pixelsY, numBands );
        region.SetProjection( dataset.GetProjection() );
        if ( region.WriteRaster( 0, 0, pixelsX, pixelsY, pixelsX, pixelsY, buf_type, buffer, null, 0 ) != CE_None ) {
            throw new RuntimeException( "Error writing raster." );
        }
        return region;
    }

    private static void save( Dataset dataset, String fileName, String driverName ) {
        Driver driver = gdal.GetDriverByName( driverName );
        Dataset target = driver.CreateCopy( fileName, dataset );
        target.delete();
    }

    private static void printRegisteredDrivers() {
        for ( int i = 0; i < gdal.GetDriverCount(); i++ ) {
            Driver driver = gdal.GetDriver( i );
            System.out.println( driver.getLongName() + ": " + driver.GetDescription() );
        }
    }

    private static void extractRandomRegionAndSaveImage( Dataset dataset, int run, int threadNo )
                            throws IOException {
        long before = System.currentTimeMillis();
        int pixelsX = 256;
        int pixelsY = 256;
        int numBands = 3;
        byte[][] bands = new byte[numBands][pixelsX * pixelsY];
        Band firstBand = dataset.GetRasterBand( 1 );
        double xPos = ( (double) ( firstBand.getXSize() - pixelsX ) ) * Math.random();
        double yPos = ( (double) ( firstBand.getYSize() - pixelsY ) ) * Math.random();

        for ( int i = 0; i < numBands; i++ ) {
            Band band = dataset.GetRasterBand( i + 1 );
            byte[] bandBytes = bands[i];
            band.ReadRaster( (int) xPos, (int) yPos, pixelsX, pixelsY, pixelsX, pixelsY, GDT_Byte, bandBytes, 0, 0 );
        }
        // saveAsJpegImage( bands, pixelsX, pixelsY, run, threadNo );
        long after = System.currentTimeMillis();
        System.out.println( threadNo + "/" + run + ": " + ( after - before ) + " [ms]" );
    }

    // private static BufferedImage extractRegion( Dataset dataset, int xPosSrc, int yPosSrc, int xSizeSrc, int
    // ySizeSrc,
    // int targetSizeX, int targetSizeY ) {
    // int numBands = 3;
    // byte[][] bands = new byte[numBands][targetSizeX * targetSizeY];
    // for ( int i = 0; i < numBands; i++ ) {
    // Band band = dataset.GetRasterBand( i + 1 );
    // byte[] bandBytes = bands[i];
    // band.ReadRaster( xPosSrc, yPosSrc, xSizeSrc, ySizeSrc, targetSizeX, targetSizeY, GDT_Byte, bandBytes );
    // }
    // BufferedImage img = createBufferedImage( bands, targetSizeX, targetSizeY );
    // return img;
    // }

    private static BufferedImage createBufferedImage( byte[][] bands, int width, int height ) {
        int numBytes = width * height * bands.length;
        DataBuffer imgBuffer = new DataBufferByte( bands, numBytes );
        int[] bankOffsets = new int[] { 0, 0, 0 };
        int[] bankIndices = new int[] { 0, 1, 2 };
        WritableRaster raster = createBandedRaster( imgBuffer, width, height, width, bankIndices, bankOffsets, null );
        ColorSpace cs = ColorSpace.getInstance( CS_sRGB );
        ColorModel cm = new ComponentColorModel( cs, false, false, ColorModel.OPAQUE, TYPE_BYTE );
        return new BufferedImage( cm, raster, false, null );
    }

    private static Envelope getEnvelope( Dataset dataset ) {
        double[] geoTransform = dataset.GetGeoTransform();
        int rasterXSize = dataset.getRasterXSize();
        int rasterYSize = dataset.getRasterYSize();
        double pixelResX = geoTransform[1];
        double pixelResY = geoTransform[5];
        double minX = geoTransform[0];
        double maxX = minX + pixelResX * rasterXSize;
        double minY = geoTransform[3];
        double maxY = minY + pixelResY * rasterYSize;
        if ( minX > maxX ) {
            double tmp = maxX;
            maxX = minX;
            minX = tmp;
        }
        if ( minY > maxY ) {
            double tmp = maxY;
            maxY = minY;
            minY = tmp;
        }
        Point min = new DefaultPoint( null, null, null, new double[] { minX, minY } );
        Point max = new DefaultPoint( null, null, null, new double[] { maxX, maxY } );
        return new DefaultEnvelope( min, max );
    }

    private static void saveAsJpegImage( byte[][] bands, int pixelsX, int pixelsY, int run, int threadNo )
                            throws IOException {
        int numBytes = pixelsX * pixelsY * bands.length;
        DataBuffer imgBuffer = new DataBufferByte( bands, numBytes );
        SampleModel sampleModel = new BandedSampleModel( TYPE_BYTE, pixelsX, pixelsY, bands.length );
        WritableRaster raster = Raster.createWritableRaster( sampleModel, imgBuffer, null );
        ColorSpace cs = ColorSpace.getInstance( ColorSpace.CS_sRGB );
        ColorModel cm = new ComponentColorModel( cs, false, false, ColorModel.OPAQUE, TYPE_BYTE );
        BufferedImage img = new BufferedImage( cm, raster, false, null );
        File outputfile = new File( "/tmp/ecw/image" + threadNo + "_" + run + ".jpg" );
        ImageIO.write( img, "jpg", outputfile );
    }

}
