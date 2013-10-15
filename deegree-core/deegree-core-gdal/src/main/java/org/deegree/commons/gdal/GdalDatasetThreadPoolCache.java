package org.deegree.commons.gdal;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.deegree.cs.coordinatesystems.ICRS;

class GdalDatasetThreadPoolCache {

    private final Map<String, GdalDataset> fileNameToDataset = new HashMap<String, GdalDataset>();

    private final Map<String, GdalDataset> fileNameToActiveDataset;

    GdalDatasetThreadPoolCache( final int maxSize, Map<File, ICRS> gdalFileToCrs ) {
        fileNameToActiveDataset = new LinkedHashMap<String, GdalDataset>( maxSize + 1, .75F, false ) {
            private static final long serialVersionUID = -1123297544376943430L;

            public boolean removeEldestEntry( Map.Entry<String, GdalDataset> eldest ) {
                boolean remove = size() > maxSize;
                if ( remove ) {
                    eldest.getValue().detach();
                }
                return remove;
            }
        };
        register( gdalFileToCrs );
    }

    void register( Map<File, ICRS> gdalFileToCrs ) {
        try {
            for ( Map.Entry<File, ICRS> gdalFileAndCrs : gdalFileToCrs.entrySet() ) {
                if ( !fileNameToDataset.containsKey( gdalFileAndCrs.getKey().getCanonicalPath() ) ) {
                    try {
                        GdalDataset dataset = new GdalDataset( gdalFileAndCrs.getKey(), gdalFileAndCrs.getValue() );
                        fileNameToDataset.put( gdalFileAndCrs.getKey().getCanonicalPath(), dataset );
                        fileNameToActiveDataset.put( gdalFileAndCrs.getKey().getCanonicalPath(), dataset );
                    } catch ( Exception e ) {
                        throw new IllegalArgumentException( e.getMessage(), e );
                    }
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    GdalDataset get( String fileName )
                            throws NoSuchElementException, IllegalStateException, Exception {
        GdalDataset dataset = fileNameToActiveDataset.get( fileName );
        if ( dataset == null ) {
            dataset = fileNameToDataset.get( fileName );
            if ( dataset == null ) {
                throw new IllegalArgumentException( "File " + fileName + " has not been registered." );
            }
            dataset.attach();
            fileNameToActiveDataset.put( fileName, dataset );
            fileNameToActiveDataset.get( fileName );
        }
        return dataset;
    }

    void destroy() {
        for ( GdalDataset dataset : fileNameToActiveDataset.values() ) {
            dataset.detach();
        }
    }

}
