package org.deegree.tile.persistence.gdal;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.slf4j.Logger;

public class GdalUtils {

    private static final Logger LOG = getLogger( GdalUtils.class );

    private static final ThreadLocal<Map<String, Dataset>> localDatasets = new ThreadLocal<Map<String, Dataset>>() {

        @Override
        protected Map<String, Dataset> initialValue() {
            return new HashMap<String, Dataset>();
        }
    };

    public static Dataset getDataset( String gdalFile ) {
        final Map<String, Dataset> datasets = localDatasets.get();

        if ( datasets.containsKey( gdalFile ) ) {
            LOG.debug( "Reusing dataset for {}", gdalFile );

            return datasets.get( gdalFile );
        } else {
            LOG.debug( "Opening {}", gdalFile );

            final Dataset dataset = gdal.OpenShared( gdalFile );
            datasets.put( gdalFile, dataset );
            return dataset;
        }
    }

    public static SpatialMetadata getEnvelopeAndCrs( Dataset gdalDataset, String configuredCrs )
                            throws UnknownCRSException {
        ICRS crs = null;
        if ( configuredCrs != null ) {
            crs = CRSManager.lookup( configuredCrs );
        }
        double[] geoTransform = gdalDataset.GetGeoTransform();
        int rasterXSize = gdalDataset.getRasterXSize();
        int rasterYSize = gdalDataset.getRasterYSize();
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
        Point min = new DefaultPoint( null, crs, null, new double[] { minX, minY } );
        Point max = new DefaultPoint( null, crs, null, new double[] { maxX, maxY } );
        Envelope env = new DefaultEnvelope( null, crs, null, min, max );
        return new SpatialMetadata( env, singletonList( env.getCoordinateSystem() ) );
    }

}
