package org.deegree.services.wfs.te;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;

import aero.m_click.wfs_te.model.Feature;
import aero.m_click.wfs_te.model.TimeSlice;
import aero.m_click.wfs_te.model.filter.StaticFilter;

public class FeatureAdapter extends AbstractAdapter<org.deegree.feature.Feature> implements Feature {

    public FeatureAdapter( org.deegree.feature.Feature adaptee ) {
        super( adaptee );
    }

    @Override
    public Iterable<TimeSlice> getTimeSlices() {
        return new Iterable<TimeSlice>() {
            @Override
            public Iterator<TimeSlice> iterator() {
                return new Iterator<TimeSlice>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public TimeSlice next() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void remove() {
                        // TODO Auto-generated method stub
                    }
                };
            }
        };
    }

    @Override
    public boolean evaluateFilter( final StaticFilter filter ) {
        final Filter deegreeFilter = ( (StaticFilterAdapter) filter ).getAdaptee();
        final TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
        try {
            return deegreeFilter.evaluate( adaptee, evaluator );
        } catch ( FilterEvaluationException e ) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    @Override
    public FeatureAdapter createVariant( final Iterable<TimeSlice> snapshots ) {
        final List<Property> props = new ArrayList<Property>();
        for ( final Property prop : adaptee.getProperties() ) {
            if ( !isTimeSlice( prop ) ) {
                props.add( prop );
            }
        }
        for ( final TimeSlice snapshot : snapshots ) {
            props.add( ( (TimeSliceAdapter) snapshot ).getAdaptee() );
        }
        return new FeatureAdapter( adaptee.getType().newFeature( adaptee.getId(), props, null ) );
    }

    private boolean isTimeSlice( final Property prop ) {
        return ( prop.getName().getLocalPart().endsWith( "TimeSlice" ) );
    }
}
