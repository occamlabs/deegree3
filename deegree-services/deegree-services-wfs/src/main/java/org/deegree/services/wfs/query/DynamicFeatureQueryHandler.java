package org.deegree.services.wfs.query;

import java.util.Iterator;

import org.deegree.commons.tom.ElementNode;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.IteratorFeatureInputStream;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.protocol.wfs.te.DynamicFeatureQuery;
import org.deegree.services.wfs.te.DeegreeDynamicFeatureQueryAdapter;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicFeatureQueryHandler {

    private static final Logger LOG = LoggerFactory.getLogger( DynamicFeatureQueryHandler.class );    
    
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
        LOG.info ("Performing DynamicFeatureQuery");
        final DynamicFeatureQuery query = (DynamicFeatureQuery) queryAnalyzer.getQuery( queries[0] );
        final DeegreeDynamicFeatureQueryAdapter ad = new DeegreeDynamicFeatureQueryAdapter();
        final aero.m_click.wfs_te.DynamicFeatureQuery<DynamicFeatureQuery, Filter, ProjectionClause, FeatureStore, Feature, ElementNode, TimeGeometricPrimitive, ElementNode> queryAlgorithm = new aero.m_click.wfs_te.DynamicFeatureQuery<DynamicFeatureQuery, Filter, ProjectionClause, FeatureStore, Feature, ElementNode, TimeGeometricPrimitive, ElementNode>(
                                                                                                                                                                                                                                                                                                                                                                    ad );
        final Iterator<Feature> result = queryAlgorithm.query( fs, query );
        final CloseableFeatureIterator closeableIter = new CloseableFeatureIterator( result, ad );
        return new IteratorFeatureInputStream( closeableIter );
    }
}
