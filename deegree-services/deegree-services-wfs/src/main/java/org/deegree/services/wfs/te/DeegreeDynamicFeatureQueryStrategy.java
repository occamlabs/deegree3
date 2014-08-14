package org.deegree.services.wfs.te;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.SelectionClause;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.te.DynamicFeatureFilter;
import org.deegree.filter.te.SnapshotGeneration;
import org.deegree.filter.te.TransformationClause;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.te.DynamicFeatureQuery;
import org.deegree.time.primitive.TimeGeometricPrimitive;

import aero.m_click.wfs_te.model.Interpretation;
import aero.m_click.wfs_te.strategy.DynamicFeatureQueryStrategy;

public class DeegreeDynamicFeatureQueryStrategy
                                               implements
                                               DynamicFeatureQueryStrategy<DynamicFeatureQuery, Filter, ProjectionClause, FeatureStore, Feature, ElementNode, TimeGeometricPrimitive, Property>,
                                               Closeable {

    private static final String AIXM_51_NS = "http://www.aixm.aero/schema/5.1";

    private static final QName VALID_TIME = new QName( GML3_2_NS, "validTime" );

    private static final QName INTERPRETATION = new QName( AIXM_51_NS, "interpretation" );

    private static final QName SEQUENCE_NUMBER = new QName( AIXM_51_NS, "sequenceNumber" );

    private static final QName CORRECTION_NUMBER = new QName( AIXM_51_NS, "correctionNumber" );

    private final XPathEvaluator<Feature> xpathEvaluator = getXpathEvaluator();

    private FeatureInputStream features;

    @Override
    public void addTimeSlice( final Feature feature, final ElementNode timeSlice ) {
        final List<Property> props = feature.getProperties();
        final Property timeSliceProp = createTimeSliceProperty( feature, timeSlice );
        props.add( timeSliceProp );
    }

    @Override
    public void addTimeSlice( Feature arg0, TimeGeometricPrimitive arg1, Interpretation arg2, Integer arg3,
                              Integer arg4, Iterable<Property> arg5 ) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean anyInteracts( TimeGeometricPrimitive arg0, TimeGeometricPrimitive arg1 ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Feature copyWithoutTimeSlices( final Feature feature ) {
        final List<Property> props = new ArrayList<Property>();
        for ( final Property prop : feature.getProperties() ) {
            if ( !isTimeSliceProperty( prop ) ) {
                props.add( prop );
            }
        }
        return feature.getType().newFeature( feature.getId(), props, null );
    }

    @Override
    public QName getName( final Property property ) {
        return property.getName();
    }

    @Override
    public Iterable<ElementNode> getTimeSlices( final Feature feature ) {
        final List<ElementNode> timeSlices = new ArrayList<ElementNode>();
        for ( final Property prop : feature.getProperties() ) {
            if ( isTimeSliceProperty( prop ) ) {
                timeSlices.add( (ElementNode) prop.getChildren().get( 0 ) );
            }
        }
        return timeSlices;
    }

    @Override
    public boolean laxDuring( final TimeGeometricPrimitive arg0, final TimeGeometricPrimitive arg1 ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Integer getCorrectionNumber( final ElementNode timeSlice ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Interpretation getInterpretation( final ElementNode timeSlice ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Property> getNonSpecialProperties( final ElementNode timeSlice ) {
        return null;
    }

    @Override
    public Integer getSequenceNumber( final ElementNode timeSlice ) {
        for ( final TypedObjectNode child : timeSlice.getChildren() ) {
            if ( child instanceof ElementNode ) {
                final ElementNode childEl = (ElementNode) child;
                if ( SEQUENCE_NUMBER.equals( childEl.getName() ) ) {
                    if ( !childEl.getChildren().isEmpty() && childEl.getChildren().get( 0 ) instanceof PrimitiveValue ) {
                        return parseInt( childEl.getChildren().get( 0 ).toString() );
                    }
                }
            }
        }
        return null;
    }

    @Override
    public TimeGeometricPrimitive getValidTime( final ElementNode timeSlice ) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean applyFilter( final Feature feature, final Filter filter ) {
        try {
            return filter.evaluate( feature, xpathEvaluator );
        } catch ( FilterEvaluationException e ) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    @Override
    public boolean getDynamicFilterEvaluateSchedules( final DynamicFeatureQuery query ) {
        final SelectionClause selectionClause = query.getSelectionClause();
        if ( !( selectionClause instanceof DynamicFeatureFilter ) ) {
            return false;
        }
        final Boolean evaluateSchedules = ( (DynamicFeatureFilter) selectionClause ).getEvaluateSchedules();
        return toPrimitiveNullSafe( evaluateSchedules );
    }

    @Override
    public TimeGeometricPrimitive getDynamicFilterSnapshotTime( final DynamicFeatureQuery query ) {
        final SelectionClause selectionClause = query.getSelectionClause();
        if ( !( selectionClause instanceof DynamicFeatureFilter ) ) {
            return ( (DynamicFeatureFilter) selectionClause ).getTimeIndicator();
        }
        return null;
    }

    @Override
    public boolean getEvaluateSchedules( final DynamicFeatureQuery query ) {
        final TransformationClause transformation = query.getTransformation();
        if ( !( transformation instanceof SnapshotGeneration ) ) {
            return false;
        }
        final Boolean evaluateSchedules = ( (SnapshotGeneration) transformation ).getEvaluateSchedule();
        return toPrimitiveNullSafe( evaluateSchedules );
    }

    @Override
    public Iterable<ProjectionClause> getProjections( final DynamicFeatureQuery query ) {
        return asList( query.getProjectionClauses() );
    }

    @Override
    public TimeGeometricPrimitive getSnapshotTime( final DynamicFeatureQuery query ) {
        final TransformationClause transformation = query.getTransformation();
        if ( !( transformation instanceof SnapshotGeneration ) ) {
            return null;
        }
        return ( (SnapshotGeneration) transformation ).getSnapshotTime();
    }

    @Override
    public Filter getStaticFilter( final DynamicFeatureQuery query ) {
        final SelectionClause selectionClause = query.getSelectionClause();
        if ( selectionClause instanceof DynamicFeatureFilter ) {
            return ( (DynamicFeatureFilter) selectionClause ).getFeatureFilter();
        } else if ( selectionClause instanceof Filter ) {
            return (Filter) selectionClause;
        }
        return null;
    }

    @Override
    public Iterator<Feature> queryStatic( final FeatureStore store, final DynamicFeatureQuery query, final Filter filter ) {
        final Query fsQuery = buildFeatureStoreQuery( query, filter );
        try {
            features = store.query( fsQuery );
            return features.iterator();
        } catch ( Exception e ) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    @Override
    public void close() {
        if ( features != null ) {
            features.close();
        }
        features = null;
    }

    PropertyType getTimeSlicePropertyType( final FeatureType ft ) {
        for ( final PropertyType pt : ft.getPropertyDeclarations() ) {
            if ( isTimeSliceProperty( pt ) ) {
                return pt;
            }
        }
        throw new RuntimeException( "Feature type " + ft.getName() + " does not have a timeSlice property!?" );
    }

    @SuppressWarnings("unchecked")
    private XPathEvaluator<Feature> getXpathEvaluator() {
        final XPathEvaluator<?> evaluator = new TypedObjectNodeXPathEvaluator();
        return (XPathEvaluator<Feature>) evaluator;
    }

    private Property createTimeSliceProperty( final Feature feature, final TypedObjectNode timeSlice ) {
        final PropertyType pt = getTimeSlicePropertyType( feature.getType() );
        return new GenericProperty( pt, timeSlice );
    }

    boolean isTimeSliceProperty( final PropertyType pt ) {
        // TODO perform check based on schema, not name
        return pt.getName().getLocalPart().equals( "timeSlice" );
    }

    private boolean isTimeSliceProperty( final Property prop ) {
        return isTimeSliceProperty( prop.getType() );
    }

    private Query buildFeatureStoreQuery( final DynamicFeatureQuery query, final Filter filter ) {
        final TypeName[] typeNames = query.getTypeNames();
        final SortProperty[] sortBy = query.getSortBy();
        return new Query( typeNames, filter, sortBy, -1, -1, -1 );
    }

    private boolean toPrimitiveNullSafe( final Boolean b ) {
        if ( b == null ) {
            return false;
        }
        return b.booleanValue();
    }
}
