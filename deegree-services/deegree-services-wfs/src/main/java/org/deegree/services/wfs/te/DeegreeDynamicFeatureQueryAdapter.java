package org.deegree.services.wfs.te;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static javax.xml.stream.XMLOutputFactory.newInstance;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GML_32;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.AppSchema;
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
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.te.DynamicFeatureQuery;
import org.deegree.time.gml.reader.GmlTimeGeometricPrimitiveReader;
import org.deegree.time.operator.AnyInteracts;
import org.deegree.time.operator.LaxDuring;
import org.deegree.time.primitive.TimeGeometricPrimitive;

import aero.m_click.wfs_te.TimeSliceComparator;
import aero.m_click.wfs_te.adapter.DynamicFeatureQueryAdapter;
import aero.m_click.wfs_te.model.Interpretation;

public class DeegreeDynamicFeatureQueryAdapter
                                              implements
                                              DynamicFeatureQueryAdapter<DynamicFeatureQuery, Filter, ProjectionClause, FeatureStore, Feature, ElementNode, TimeGeometricPrimitive, ElementNode>,
                                              Closeable {

    private static final String AIXM_51_NS = "http://www.aixm.aero/schema/5.1";

    private static final QName VALID_TIME = new QName( GML3_2_NS, "validTime" );

    private static final QName INTERPRETATION = new QName( AIXM_51_NS, "interpretation" );

    private static final QName SEQUENCE_NUMBER = new QName( AIXM_51_NS, "sequenceNumber" );

    private static final QName CORRECTION_NUMBER = new QName( AIXM_51_NS, "correctionNumber" );

    private static final Set<QName> specialProps = new HashSet<QName>( asList( VALID_TIME, INTERPRETATION,
                                                                               SEQUENCE_NUMBER, CORRECTION_NUMBER ) );

    private final XPathEvaluator<Feature> xpathEvaluator = getXpathEvaluator();

    private FeatureInputStream features;

    @Override
    public void addTimeSlice( final Feature feature, final ElementNode timeSlice ) {
        final List<Property> props = feature.getProperties();
        final Property timeSliceProp = createTimeSliceProperty( feature, timeSlice );
        props.add( timeSliceProp );
    }

    @Override
    public void addTimeSlice( final Feature feature, final TimeGeometricPrimitive validTime,
                              final Interpretation interpretation, final Integer sequenceNumber,
                              final Integer correctionNumber, final Iterable<ElementNode> nonSpecialProperties ) {
        final PropertyType timeSlicePt = getTimeSlicePropertyType( feature.getType() );
        final Map<QName, PrimitiveValue> attrs = emptyMap();
        final List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();
        final XSElementDeclaration xsType = timeSlicePt.getElementDecl();
        final AppSchema schema = feature.getType().getSchema();
        final XSComplexTypeDefinition type = (XSComplexTypeDefinition) xsType.getTypeDefinition();
        final Map<QName, XSTerm> allowedChildElementDecls = schema.getAllowedChildElementDecls( type );
        final XSElementDeclaration timeSliceElDecl = (XSElementDeclaration) allowedChildElementDecls.values().iterator().next();
        final XSComplexTypeDefinition timeSliceElType = (XSComplexTypeDefinition) timeSliceElDecl.getTypeDefinition();
        final Map<QName, XSTerm> allowedChildElementDecls2 = schema.getAllowedChildElementDecls( timeSliceElType );
        if ( validTime != null ) {
            final ElementNode validTimeEl = buildSimpleElement( VALID_TIME, validTime, allowedChildElementDecls, schema );
            children.add( validTimeEl );
        }
        if ( interpretation != null ) {
            final ElementNode interpretationEl = buildSimpleElement( INTERPRETATION, interpretation,
                                                                     allowedChildElementDecls2, schema );
            children.add( interpretationEl );
        }
        if ( sequenceNumber != null ) {
            final ElementNode sequenceNumberEl = buildSimpleElement( SEQUENCE_NUMBER, sequenceNumber,
                                                                     allowedChildElementDecls2, schema );
            children.add( sequenceNumberEl );
        }
        if ( correctionNumber != null ) {
            final ElementNode correctionNumberEl = buildSimpleElement( CORRECTION_NUMBER, correctionNumber,
                                                                       allowedChildElementDecls2, schema );
            children.add( correctionNumberEl );
        }
        for ( final ElementNode nonSpecialProperty : nonSpecialProperties ) {
            children.add( nonSpecialProperty );
        }
        final QName name = new QName( timeSliceElDecl.getNamespace(), timeSliceElDecl.getName() );
        final ElementNode timeSlice = new GenericXMLElement( name, timeSliceElDecl, attrs, children );
        addTimeSlice( feature, timeSlice );
    }

    private ElementNode buildSimpleElement( final QName name, final Object value,
                                            final Map<QName, XSTerm> allowedChildElementDecls, final AppSchema schema ) {
        final Map<QName, PrimitiveValue> attrs = emptyMap();
        final List<TypedObjectNode> children = new ArrayList<TypedObjectNode>();
        final XSTerm xsTerm = allowedChildElementDecls.get( name );
        XSElementDeclaration elementDecl = null;
        if ( xsTerm instanceof XSElementDeclaration ) {
            elementDecl = (XSElementDeclaration) xsTerm;
        }
        children.add( buildPrimitiveValue( value, xsTerm ) );
        return new GenericXMLElement( name, elementDecl, attrs, children );
    }

    private PrimitiveValue buildPrimitiveValue( final Object value, final XSTerm term ) {
        PrimitiveType pt = new PrimitiveType( STRING );
        if ( term instanceof XSElementDeclaration ) {
            final XSElementDeclaration elementDecl = (XSElementDeclaration) term;
            final XSTypeDefinition typeDefinition = elementDecl.getTypeDefinition();
            if ( typeDefinition instanceof XSSimpleTypeDefinition ) {
                pt = new PrimitiveType( (XSSimpleTypeDefinition) typeDefinition );
            }
        }
        return new PrimitiveValue( value.toString(), pt );
    }

    @Override
    public boolean anyInteracts( final TimeGeometricPrimitive a, final TimeGeometricPrimitive b ) {
        return new AnyInteracts().anyInteracts( a, b );
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
    public QName getName( final ElementNode property ) {
        return property.getName();
    }

    @Override
    public Iterator<ElementNode> getSortedTimeSlices( final Feature feature ) {
        final List<ElementNode> timeSlices = new ArrayList<ElementNode>();
        for ( final Property prop : feature.getProperties() ) {
            if ( isTimeSliceProperty( prop ) ) {
                timeSlices.add( (ElementNode) prop.getChildren().get( 0 ) );
            }
        }
        Collections.sort( timeSlices, new TimeSliceComparator<ElementNode>( this ) );
        return timeSlices.iterator();
    }

    @Override
    public boolean laxDuring( final TimeGeometricPrimitive a, final TimeGeometricPrimitive b ) {
        return new LaxDuring().laxDuring( a, b );
    }

    @Override
    public Integer getCorrectionNumber( final ElementNode timeSlice ) {
        final PrimitiveValue value = getPrimitiveValueOfChildElement( timeSlice, CORRECTION_NUMBER );
        if ( value == null ) {
            return null;
        }
        return parseInt( value.getAsText() );
    }

    @Override
    public Interpretation getInterpretation( final ElementNode timeSlice ) {
        final PrimitiveValue value = getPrimitiveValueOfChildElement( timeSlice, INTERPRETATION );
        if ( value == null ) {
            return null;
        }
        return Interpretation.valueOf( value.getAsText() );
    }

    @Override
    public Iterable<ElementNode> getNonSpecialProperties( final ElementNode timeSlice ) {
        final List<ElementNode> filteredChildren = new ArrayList<ElementNode>();
        for ( final TypedObjectNode child : timeSlice.getChildren() ) {
            if ( child instanceof ElementNode ) {
                final ElementNode childEl = (ElementNode) child;
                if ( !specialProps.contains( childEl.getName() ) ) {
                    filteredChildren.add( childEl );
                }
            }
        }
        return filteredChildren;
    }

    @Override
    public Integer getSequenceNumber( final ElementNode timeSlice ) {
        final PrimitiveValue value = getPrimitiveValueOfChildElement( timeSlice, SEQUENCE_NUMBER );
        if ( value == null ) {
            return null;
        }
        return parseInt( value.getAsText() );
    }

    @Override
    public TimeGeometricPrimitive getValidTime( final ElementNode timeSlice ) {
        final List<TypedObjectNode> children = timeSlice.getChildren();
        for ( final TypedObjectNode child : children ) {
            if ( child instanceof ElementNode ) {
                final ElementNode childEl = (ElementNode) child;
                if ( VALID_TIME.equals( childEl.getName() ) ) {
                    return parseTimeGeometricPrimitive( childEl );
                }
            }
        }
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
            return null;
        }
        return ( (DynamicFeatureFilter) selectionClause ).getTimeIndicator();
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

    private Property createTimeSliceProperty( final Feature feature, final ElementNode timeSlice ) {
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

    private PrimitiveValue getPrimitiveValueOfChildElement( final ElementNode timeSlice, final QName elName ) {
        for ( final TypedObjectNode child : timeSlice.getChildren() ) {
            if ( child instanceof ElementNode ) {
                final ElementNode childEl = (ElementNode) child;
                if ( elName.equals( childEl.getName() ) ) {
                    if ( !childEl.getChildren().isEmpty() && childEl.getChildren().get( 0 ) instanceof PrimitiveValue ) {
                        return (PrimitiveValue) childEl.getChildren().get( 0 );
                    }
                }
            }
        }
        return null;
    }

    private TimeGeometricPrimitive parseTimeGeometricPrimitive( final ElementNode childEl ) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final XMLStreamWriter xmlStream = newInstance().createXMLStreamWriter( bos );
            final GMLStreamWriter gmlWriter = createGMLStreamWriter( GML_32, xmlStream );
            gmlWriter.getFeatureWriter().export( childEl, new GmlXlinkOptions() );
            gmlWriter.close();
            final InputStream is = new ByteArrayInputStream( bos.toByteArray() );
            final XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( is );
            final GMLStreamReader gmlReader = createGMLStreamReader( GML_32, xmlReader );
            final GmlTimeGeometricPrimitiveReader timeReader = new GmlTimeGeometricPrimitiveReader( gmlReader );
            nextElement( xmlReader );
            return timeReader.read( xmlReader );
        } catch ( Exception e ) {
            throw new IllegalArgumentException( "Unable to parse gml:validTime:" + e.getMessage() );
        }
    }
}
