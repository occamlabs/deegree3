package org.deegree.commons.gdal;

import static java.util.Collections.synchronizedMap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.workspace.Destroyable;
import org.deegree.workspace.Initializable;
import org.deegree.workspace.Workspace;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.osr.SpatialReference;

/**
 * TODO add class documentation here
 * 
 * <p>
 * Due to restrictions in GDAL/GDAL plugins, standard pooling doesn't work here. One has to use a unique instance <i>per
 * thread</i>. If a GDAL <code>dataset</code> is used by different threads (subsequently, not in parallel), access
 * violations can occur that crash the VM.
 * </p>
 * 
 * @author <a href="mailto:name@company.com">Your Name</a>
 * 
 * @since 3.4
 */
public class GdalSettings implements Initializable, Destroyable {

    private ExecutorService threadPool;

    private final ThreadLocal<GdalDatasetThreadPoolCache> cache = new ThreadLocal<GdalDatasetThreadPoolCache>();

    private final Map<File, ICRS> gdalFileToCrs = synchronizedMap( new HashMap<File, ICRS>() );

    private final Map<File, Envelope> gdalFileToEnvelope = synchronizedMap( new HashMap<File, Envelope>() );

    private final Map<Integer, SpatialReference> epsgCodeToSpatialReference = synchronizedMap( new HashMap<Integer, SpatialReference>() );

    private int threadPoolSize = 8;

    private int maxAttachedDatasetsPerThread = 10;

    @Override
    public void destroy( Workspace workspace ) {
        threadPool.shutdown();
        gdalFileToCrs.clear();
        gdalFileToEnvelope.clear();
    }

    @Override
    public void init( Workspace workspace ) {
        gdal.AllRegister();
        gdal.SetConfigOption( "GDAL_CACHEMAX", "20" );
        gdal.SetConfigOption( "ECW_CACHE_MAXMEM", "" + 1024 * 1024 * 1024 );
        threadPool = Executors.newFixedThreadPool( threadPoolSize );
    }

    public void registerDatasetCrs( File file, ICRS crs ) {
        gdalFileToCrs.put( file, crs );
        GdalDataset dataset = null;
        try {
            dataset = new GdalDataset( file, crs );
            gdalFileToEnvelope.put( file, dataset.getEnvelope() );
        } catch ( Exception e ) {
            throw new IllegalArgumentException( e.getMessage(), e );
        } finally {
            if ( dataset != null ) {
                dataset.detach();
            }
        }
    }

    public ICRS getCrs( File file ) {
        return gdalFileToCrs.get( file );
    }

    public SpatialReference getCrsAsWkt( int epsgCode ) {
        SpatialReference sr = epsgCodeToSpatialReference.get( epsgCode );
        if ( sr == null ) {
            synchronized ( this ) {
                sr = new SpatialReference();
                int importFromEPSG = sr.ImportFromEPSG( epsgCode );
                if ( importFromEPSG != 0 ) {
                    throw new RuntimeException( "Cannot import EPSG:" + epsgCode + " from GDAL." );
                }               
                epsgCodeToSpatialReference.put( epsgCode, sr );
            }
        }
        return sr;
    }

    public Envelope getEnvelope( File gdalFile ) {
        return gdalFileToEnvelope.get( gdalFile );
    }

    public BufferedImage extractRegion( final File gdalFile, final Envelope region, final int pixelsX,
                                        final int pixelsY, final boolean withAlpha ) {
        try {
            final String fileName = gdalFile.getCanonicalPath();
            return threadPool.submit( new Callable<BufferedImage>() {
                @Override
                public BufferedImage call()
                                        throws Exception {
                    GdalDataset dataset = getThreadLocalDatasetCache().get( fileName );
                    BufferedImage extractRegion = dataset.extractRegion( region, pixelsX, pixelsY, withAlpha );
                    return extractRegion;
                }
            } ).get();
        } catch ( Exception e ) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    public byte[][] extractRegionRaw( final File gdalFile, final Envelope region, final int pixelsX, final int pixelsY,
                                      final boolean withAlpha ) {
        try {
            final String fileName = gdalFile.getCanonicalPath();
            return threadPool.submit( new Callable<byte[][]>() {
                @Override
                public byte[][] call()
                                        throws Exception {
                    GdalDataset dataset = getThreadLocalDatasetCache().get( fileName );
                    return dataset.extractRegionAsByteArray( region, pixelsX, pixelsY, withAlpha );
                }
            } ).get();
        } catch ( Exception e ) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    public Dataset extractRegionAsDataset( final File gdalFile, final Envelope region, final int pixelsX,
                                           final int pixelsY, final boolean withAlpha ) {
        try {
            final String fileName = gdalFile.getCanonicalPath();
            return threadPool.submit( new Callable<Dataset>() {
                @Override
                public Dataset call()
                                        throws Exception {
                    GdalDataset dataset = getThreadLocalDatasetCache().get( fileName );
                    return dataset.extractRegionAsDataset( region, pixelsX, pixelsY, withAlpha );
                }
            } ).get();
        } catch ( Exception e ) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    private GdalDatasetThreadPoolCache getThreadLocalDatasetCache() {
        GdalDatasetThreadPoolCache cache = this.cache.get();
        if ( cache == null ) {
            System.out.println( "Setting up thread-local cache for thread: " + Thread.currentThread().getName() );
            cache = new GdalDatasetThreadPoolCache( maxAttachedDatasetsPerThread, gdalFileToCrs );
            this.cache.set( cache );
        }
        cache.register( gdalFileToCrs );
        return cache;
    }   

}
