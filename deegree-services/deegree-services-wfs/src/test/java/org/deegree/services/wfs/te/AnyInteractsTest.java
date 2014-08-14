package org.deegree.services.wfs.te;

import static java.util.Collections.emptyList;
import static org.deegree.time.position.IndeterminateValue.UNKNOWN;

import java.util.List;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimeInstant;
import org.deegree.time.primitive.GenericTimePeriod;
import org.deegree.time.primitive.RelatedTime;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;
import org.junit.Assert;
import org.junit.Test;

public class AnyInteractsTest {
    @Test
    public void instantInstant() {
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:01" ), timePrimitive( "2014-01-01T00:00:01" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:01" ), timePrimitive( "2014-01-01T00:00:02" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:02" ), timePrimitive( "2014-01-01T00:00:01" ) );
    }

    @Test
    public void instantPeriod() {
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ),
                               timePrimitive( "2014-01-01T00:00:00" ) );
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ),
                            timePrimitive( "2014-01-01T00:00:01" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ),
                               timePrimitive( "2014-01-01T00:00:02" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ),
                               timePrimitive( "2014-01-01T00:00:03" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ),
                               timePrimitive( "2014-01-01T00:00:00" ) );
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ),
                            timePrimitive( "2014-01-01T00:00:01" ) );
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ),
                            timePrimitive( "2014-01-01T00:00:02" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ),
                               timePrimitive( "2014-01-01T00:00:03" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ),
                               timePrimitive( "2014-01-01T00:00:04" ) );
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:04" ),
                               timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
    }

    @Test
    public void instantPeriodIndeterminate() {
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ),
                               timePrimitive( "2014-01-01T00:00:00" ) );
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ),
                            timePrimitive( "2014-01-01T00:00:01" ) );
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ),
                            timePrimitive( "2014-01-01T00:00:02" ) );
    }

    @Test
    public void periodPeriod() {
        // Begins
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:06" ) );
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:02", "INDETERMINATE" ) );
        // BegunBy
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:03" ) );
        // Ends
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:05" ) );
        // EndedBy
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:04", "2014-01-01T00:00:05" ) );
        // TContains
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:03", "2014-01-01T00:00:04" ) );
        // During
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:06" ) );
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        // TEquals
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ) );
        // TOverlaps
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:04", "2014-01-01T00:00:06" ) );
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:04", "INDETERMINATE" ) );
        // OverlappedBy
        assertAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        // After
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               timePrimitive( "2014-01-01T00:00:00", "2014-01-01T00:00:01" ) );
        // Before
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               timePrimitive( "2014-01-01T00:00:06", "2014-01-01T00:00:07" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               timePrimitive( "2014-01-01T00:00:06", "INDETERMINATE" ) );
        // Meets
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               timePrimitive( "2014-01-01T00:00:05", "2014-01-01T00:00:06" ) );
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               timePrimitive( "2014-01-01T00:00:05", "INDETERMINATE" ) );
        // MetBy
        assertNotAnyInteracts( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
    }

    private void assertAnyInteracts( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        Assert.assertTrue( anyInteracts( a, b ) );
    }

    private void assertNotAnyInteracts( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        Assert.assertFalse( anyInteracts( a, b ) );
    }

    private boolean anyInteracts( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        final DeegreeDynamicFeatureQueryStrategy st = new DeegreeDynamicFeatureQueryStrategy();
        return st.anyInteracts( a, b );
    }

    private TimeInstant timePrimitive( final String s ) {
        final List<Property> props = emptyList();
        final List<RelatedTime> relatedTimes = emptyList();
        TimePosition pos = null;
        if ( "INDETERMINATE".equals( s ) ) {
            pos = new TimePosition( null, null, UNKNOWN, "" );
        } else {
            pos = new TimePosition( null, null, null, s );
        }
        return new GenericTimeInstant( null, props, relatedTimes, null, pos );
    }

    private TimePeriod timePrimitive( final String t1, final String t2 ) {
        final TimeInstant begin = timePrimitive( t1 );
        final TimeInstant end = timePrimitive( t2 );
        final List<Property> props = emptyList();
        final List<RelatedTime> relatedTimes = emptyList();
        return new GenericTimePeriod( null, props, relatedTimes, null, begin, end );
    }
}
