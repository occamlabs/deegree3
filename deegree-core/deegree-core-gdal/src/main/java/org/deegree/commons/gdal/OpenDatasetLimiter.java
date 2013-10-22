package org.deegree.commons.gdal;

import java.util.LinkedHashMap;
import java.util.Map;

class OpenDatasetLimiter extends LinkedHashMap<GdalDataset, GdalDataset> {

    private static final long serialVersionUID = -1612939834455651011L;

    private final int maxAttached;

    private final GdalDatasetPool pool;

    OpenDatasetLimiter( int maxAttached, GdalDatasetPool pool ) {
        super( maxAttached + 1, .75F, true );
        this.maxAttached = maxAttached;
        this.pool = pool;
    }

    @Override
    public boolean removeEldestEntry( Map.Entry<GdalDataset, GdalDataset> eldest ) {
        boolean remove = size() > maxAttached;
        if ( remove ) {
            pool.requestDetach( eldest.getKey() );
        }
        return remove;
    }

    synchronized void requireOpen( GdalDataset dataset ) {
        if ( !containsKey( dataset ) ) {
            put( dataset, dataset );
        }
        get( dataset );
        dataset.attach();
    }
}
