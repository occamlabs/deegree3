package org.deegree.services.wfs.te;

import java.util.Iterator;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.filter.FilterEvaluationException;

import aero.m_click.wfs_te.model.DynamicFeatureQuery;
import aero.m_click.wfs_te.model.Feature;
import aero.m_click.wfs_te.model.StaticFeatureStore;
import aero.m_click.wfs_te.model.filter.StaticFilter;

public class StaticFeatureStoreAdapter extends AbstractAdapter<FeatureStore> implements StaticFeatureStore {

    public StaticFeatureStoreAdapter( final FeatureStore fs ) {
        super( fs );
    }

    @Override
    public Iterator<Feature> queryStatic( final DynamicFeatureQuery query, final StaticFilter filter ) {
        final Query fsQuery = getFeatureStoreQuery( query, filter );
        try {
            final FeatureInputStream result = adaptee.query( fsQuery );
        } catch ( FeatureStoreException e ) {
        } catch ( FilterEvaluationException e ) {
        }
    }

    private Query getFeatureStoreQuery( final DynamicFeatureQuery query, final StaticFilter filter ) {
        // TODO Auto-generated method stub
        return null;
    }
}
