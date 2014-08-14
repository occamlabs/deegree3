package org.deegree.services.wfs.te;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.junit.BeforeClass;
import org.junit.Test;

public class DeegreeDynamicFeatureQueryStrategyTest {

    private static final QName TIME_SLICE = QName.valueOf( "{http://www.aixm.aero/schema/5.1}timeSlice" );

    private static final QName GML_IDENTIFIER = QName.valueOf( "{http://www.opengis.net/gml/3.2}identifier" );

    private final DeegreeDynamicFeatureQueryStrategy strategy = new DeegreeDynamicFeatureQueryStrategy();

    private static Feature exampleFeature;

    @BeforeClass
    public static void setup()
                            throws Exception {
        final FeatureCollection fc = new Aixm51ExampleDatasetLoader().load();
        exampleFeature = fc.iterator().next();
    }

    @Test
    public void addTimeSlice() {
        final FeatureType airspaceFt = exampleFeature.getType();
        final List<Property> props = emptyList();
        final Feature airspace = airspaceFt.newFeature( "NEW", props, null );
        assertEquals( 0, toList( strategy.getTimeSlices( airspace ) ).size() );
        final ElementNode timeSlice = toList( strategy.getTimeSlices( exampleFeature ) ).get( 0 );
        strategy.addTimeSlice( airspace, timeSlice );
        assertEquals( 1, toList( strategy.getTimeSlices( airspace ) ).size() );
    }

    @Test
    public void anyInteracts() {
        // TODO
    }

    @Test
    public void copyWithoutTimeSlices() {
        final Feature copyWithoutTimeSlices = strategy.copyWithoutTimeSlices( exampleFeature );
        assertEquals( 4, exampleFeature.getProperties().size() );
        assertEquals( 2, copyWithoutTimeSlices.getProperties().size() );
    }

    @Test
    public void getName() {
        final Property prop = exampleFeature.getProperties().get( 0 );
        final QName name = strategy.getName( prop );
        assertEquals( GML_IDENTIFIER, name );
    }

    @Test
    public void getTimeSlices() {
        assertEquals( 4, exampleFeature.getProperties().size() );
    }

    @Test
    public void laxDuring() {
    }

    @Test
    public void getCorrectionNumber() {
    }

    @Test
    public void getInterpretation() {
    }

    @Test
    public void getNonSpecialProperties() {
    }

    @Test
    public void getSequenceNumber() {
        final List<ElementNode> timeSlices = toList( strategy.getTimeSlices( exampleFeature ) );
        assertEquals( new Integer( 1 ), strategy.getSequenceNumber( timeSlices.get( 0 ) ) );
        assertEquals( new Integer( 2 ), strategy.getSequenceNumber( timeSlices.get( 1 ) ) );
    }

    @Test
    public void getValidTime() {
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
    public void isTimeSliceProperty() {
        final PropertyType pt = exampleFeature.getType().getPropertyDeclaration( TIME_SLICE );
        assertTrue( strategy.isTimeSliceProperty( pt ) );
    }

    @Test
    public void isTimeSlicePropertyNot() {
        final PropertyType pt = exampleFeature.getType().getPropertyDeclaration( GML_IDENTIFIER );
        assertFalse( strategy.isTimeSliceProperty( pt ) );
    }

    private <T> List<T> toList( final Iterable<T> iterable ) {
        final List<T> list = new ArrayList<T>();
        for ( final T entry : iterable ) {
            list.add( entry );
        }
        return list;
    }
}
