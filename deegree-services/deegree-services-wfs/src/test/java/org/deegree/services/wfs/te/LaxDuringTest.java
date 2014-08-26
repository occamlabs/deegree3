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

public class LaxDuringTest {
    @Test
    public void instantInstant() {
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:01" ), timePrimitive( "2014-01-01T00:00:01" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:01" ), timePrimitive( "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02" ), timePrimitive( "2014-01-01T00:00:01" ) );
    }

    @Test
    public void periodInstant() {
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ),
                            timePrimitive( "2014-01-01T00:00:00" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ),
                            timePrimitive( "2014-01-01T00:00:01" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ),
                            timePrimitive( "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ),
                            timePrimitive( "2014-01-01T00:00:00" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ),
                            timePrimitive( "2014-01-01T00:00:01" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ),
                            timePrimitive( "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ),
                            timePrimitive( "2014-01-01T00:00:03" ) );
    }

    @Test
    public void instantPeriod() {
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:00" ),
                            timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:01" ), timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:02" ), timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:00" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:01" ),
                         timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:03" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:00" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:01" ),
                         timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:02" ),
                         timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:03" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:04" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
    }

    @Test
    public void periodPeriod() {
        // Begins
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:06" ) );
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         timePrimitive( "2014-01-01T00:00:02", "INDETERMINATE" ) );
        // Ends
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:05" ) );
        // During
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:06" ) );
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         timePrimitive( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        // TEquals
        assertLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ) );
        // BegunBy
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:03" ) );
        // EndedBy
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:04", "2014-01-01T00:00:05" ) );
        // After
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:00", "2014-01-01T00:00:01" ) );
        // Before
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:06", "2014-01-01T00:00:07" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:06", "INDETERMINATE" ) );
        // TContains
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:03", "2014-01-01T00:00:04" ) );
        // TOverlaps
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:04", "2014-01-01T00:00:06" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:04", "INDETERMINATE" ) );
        // OverlappedBy
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        // Meets
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:05", "2014-01-01T00:00:06" ) );
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:05", "INDETERMINATE" ) );
        // MetBy
        assertNotLaxDuring( timePrimitive( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            timePrimitive( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
    }

    private void assertLaxDuring( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        Assert.assertTrue( laxDuring( a, b ) );
    }

    private void assertNotLaxDuring( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        Assert.assertFalse( laxDuring( a, b ) );
    }

    private boolean laxDuring( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        final DeegreeDynamicFeatureQueryAdapter ad = new DeegreeDynamicFeatureQueryAdapter();
        return ad.laxDuring( a, b );
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
