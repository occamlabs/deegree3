package org.deegree.services.wfs.te;

import static aero.m_click.wfs_te.model.Interpretation.BASELINE;
import static aero.m_click.wfs_te.model.Interpretation.TEMPDELTA;
import static java.util.Collections.emptyList;
import static org.deegree.time.position.IndeterminateValue.UNKNOWN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.feature.Feature;
import org.deegree.feature.types.FeatureType;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimePeriod;
import org.junit.BeforeClass;
import org.junit.Test;

import aero.m_click.wfs_te.model.Interpretation;

public class DeegreeDynamicFeatureQueryAdapterTest {

    private static final QName TIME_SLICE = QName.valueOf( "{http://www.aixm.aero/schema/5.1}timeSlice" );

    private static final QName AIRSPACE_TIME_SLICE = QName.valueOf( "{http://www.aixm.aero/schema/5.1}AirspaceTimeSlice" );

    private static final QName GML_IDENTIFIER = QName.valueOf( "{http://www.opengis.net/gml/3.2}identifier" );

    private final static DeegreeDynamicFeatureQueryAdapter ad = new DeegreeDynamicFeatureQueryAdapter();

    private static Feature exampleFeature;

    private static List<ElementNode> timeSlices;

    @BeforeClass
    public static void setup()
                            throws Exception {
        exampleFeature = new Aixm51ExampleDatasetLoader().load();
        timeSlices = toList( ad.getSortedTimeSlices( exampleFeature ) );
    }

    @Test
    public void addTimeSlice() {
        final Feature feature = ad.copyWithoutTimeSlices( exampleFeature );
        final TimeGeometricPrimitive validTime = null;
        final Interpretation interpretation = BASELINE;
        final Integer sequenceNumber = 10;
        final Integer correctionNumber = 11;
        final Iterable<ElementNode> nonSpecialProperties = ad.getNonSpecialProperties( timeSlices.get( 0 ) );
        ad.addTimeSlice( feature, validTime, interpretation, sequenceNumber, correctionNumber,
                               nonSpecialProperties );
    }

    @Test
    public void copyWithoutTimeSlices() {
        final Feature copy = ad.copyWithoutTimeSlices( exampleFeature );
        assertEquals( exampleFeature.getType(), copy.getType() );
        assertEquals( exampleFeature.getId(), copy.getId() );
        assertEquals( exampleFeature.getProperties().get( 0 ), copy.getProperties().get( 0 ) );
        assertEquals( 0, toList( ad.getSortedTimeSlices( copy ) ).size() );
    }

    @Test
    public void getName() {
        final Property prop = exampleFeature.getProperties().get( 0 );
        final QName name = ad.getName( prop );
        assertEquals( GML_IDENTIFIER, name );
    }

    @Test
    public void getSortedTimeSlices() {
        final List<ElementNode> timeSlices = toList( ad.getSortedTimeSlices( exampleFeature ) );
        assertEquals( 2, timeSlices.size() );
        for ( final ElementNode timeSlice : timeSlices ) {
            assertEquals( AIRSPACE_TIME_SLICE, timeSlice.getName() );
        }
    }

    @Test
    public void getCorrectionNumber() {
        assertEquals( new Integer( 1 ), ad.getCorrectionNumber( timeSlices.get( 0 ) ) );
        assertNull( ad.getCorrectionNumber( timeSlices.get( 1 ) ) );
    }

    @Test
    public void getInterpretation() {
        assertEquals( TEMPDELTA, ad.getInterpretation( timeSlices.get( 0 ) ) );
        assertEquals( BASELINE, ad.getInterpretation( timeSlices.get( 1 ) ) );
    }

    @Test
    public void getNonSpecialProperties() {
        assertEquals( 1, toList( ad.getNonSpecialProperties( timeSlices.get( 0 ) ) ).size() );
        assertEquals( 2, toList( ad.getNonSpecialProperties( timeSlices.get( 1 ) ) ).size() );
    }

    @Test
    public void getSequenceNumber() {
        assertEquals( new Integer( 2 ), ad.getSequenceNumber( timeSlices.get( 0 ) ) );
        assertEquals( new Integer( 1 ), ad.getSequenceNumber( timeSlices.get( 1 ) ) );
    }

    @Test
    public void getValidTime() {
        final TimePeriod validTime1 = (TimePeriod) ad.getValidTime( timeSlices.get( 0 ) );
        final TimePosition begin1 = (TimePosition) validTime1.getBegin();
        assertEquals( "2012-07-10T07:00:00Z", begin1.getValue() );
        final TimePosition end1 = (TimePosition) validTime1.getEnd();
        assertEquals( "2012-07-10T07:16:00Z", end1.getValue() );
        final TimePeriod validTime2 = (TimePeriod) ad.getValidTime( timeSlices.get( 1 ) );
        final TimePosition begin2 = (TimePosition) validTime2.getBegin();
        assertEquals( "2009-01-01T00:00:00Z", begin2.getValue() );
        final TimePosition end2 = (TimePosition) validTime2.getEnd();
        assertEquals( UNKNOWN, end2.getIndeterminatePosition() );
        assertEquals( "", end2.getValue() );
    }

    @Test
    public void applyFilter() {
    }

    @Test
    public void getDynamicFilterEvaluateSchedules() {
    }

    @Test
    public void getDynamicFilterSnapshotTime() {
    }

    @Test
    public void getEvaluateSchedules() {
    }

    @Test
    public void getProjections() {
    }

    @Test
    public void getSnapshotTime() {
    }

    @Test
    public void getStaticFilter() {
    }

    @Test
    public void isTimeSlicePropertyTrue() {
        final PropertyType pt = exampleFeature.getType().getPropertyDeclaration( TIME_SLICE );
        assertTrue( ad.isTimeSliceProperty( pt ) );
    }

    @Test
    public void isTimeSlicePropertyFalse() {
        final PropertyType pt = exampleFeature.getType().getPropertyDeclaration( GML_IDENTIFIER );
        assertFalse( ad.isTimeSliceProperty( pt ) );
    }

    private static <T> List<T> toList( final Iterator<T> iterator ) {
        final List<T> list = new ArrayList<T>();
        while ( iterator.hasNext() ) {
            list.add( iterator.next() );
        }
        return list;
    }

    private static <T> List<T> toList( final Iterable<T> iterable ) {
        return toList( iterable.iterator() );
    }
}
