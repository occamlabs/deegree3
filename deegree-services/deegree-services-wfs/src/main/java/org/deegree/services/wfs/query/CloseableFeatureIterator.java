package org.deegree.services.wfs.query;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.CloseableIterator;
import org.deegree.feature.Feature;

class CloseableFeatureIterator implements CloseableIterator<Feature> {

    private final Iterator<Feature> result;

    private final Closeable closeable;

    CloseableFeatureIterator( final Iterator<Feature> result, final Closeable closeable ) {
        this.result = result;
        this.closeable = closeable;
    }

    @Override
    public boolean hasNext() {
        return result.hasNext();
    }

    @Override
    public Feature next() {
        return result.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        try {
            closeable.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    @Override
    public List<Feature> getAsListAndClose() {
        final LinkedList<Feature> list = new LinkedList<Feature>();
        while ( hasNext() ) {
            list.add( next() );
        }
        close();
        return list;
    }

    @Override
    public Collection<Feature> getAsCollectionAndClose( Collection<Feature> collection ) {
        while ( hasNext() ) {
            collection.add( next() );
        }
        close();
        return collection;
    }
}
