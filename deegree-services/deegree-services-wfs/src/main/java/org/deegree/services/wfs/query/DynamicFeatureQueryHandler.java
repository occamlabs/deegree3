package org.deegree.services.wfs.query;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.protocol.wfs.te.DynamicFeatureQuery;

import aero.m_click.wfs_te.DynamicFeatureQueryAlgorithm;
import aero.m_click.wfs_te.FeatureSelectionAlgorithm;

public class DynamicFeatureQueryHandler {

    public FeatureInputStream query( final Query[] queries, final FeatureStore fs, final QueryAnalyzer queryAnalyzer )
                            throws FeatureStoreException, FilterEvaluationException {
        if ( isDynamicQuery( queries, queryAnalyzer ) ) {
            return performDynamicQuery( queries, fs, queryAnalyzer );
        }
        return fs.query( queries );
    }

    private boolean isDynamicQuery( final Query[] queries, final QueryAnalyzer queryAnalyzer ) {
        for ( final Query query : queries ) {
            if ( queryAnalyzer.getQuery( query ) instanceof DynamicFeatureQuery ) {
                return true;
            }
        }
        return false;
    }

    private FeatureInputStream performDynamicQuery( final Query[] queries, final FeatureStore fs,
                                                    final QueryAnalyzer queryAnalyzer )
                            throws FeatureStoreException, FilterEvaluationException {
        final DynamicFeatureQuery query = (DynamicFeatureQuery) queryAnalyzer.getQuery( queries[0] );
        final DynamicFeatureQueryAlgorithm queryAlgorithm = new DynamicFeatureQueryAlgorithm();
        final FeatureSelectionAlgorithm selectorAlgorithm = new FeatureSelectionAlgorithm( store );
        queryAlgorithm.query( query, selectorAlgorithm );
    }
}
