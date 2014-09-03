/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
package org.deegree.protocol.wfs.te.xml;

import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.deegree.filter.Filter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.logical.And;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.te.DynamicFeatureFilter;
import org.deegree.filter.te.PropertyExclusion;
import org.deegree.filter.te.SnapshotGeneration;
import org.deegree.filter.te.TimeSliceProjection;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.xml.GetFeatureXMLAdapter;
import org.deegree.protocol.wfs.te.DynamicFeatureQuery;
import org.deegree.time.primitive.TimeInstant;
import org.junit.Test;

/**
 * Ensures the correct parsing of the query examples from OGC 12-027r3, Annex A.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class Te100AnnexAQueryParsingTest {

    @Test
    public void readExampleA1()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a01_query.xml" );
        assertNotNull( query.getSelectionClause() );
        final ProjectionClause[] projections = query.getProjectionClauses();
        assertEquals( 0, projections.length );
        final SnapshotGeneration snapshot = (SnapshotGeneration) query.getTransformation();
        assertNotNull( snapshot );
        assertNull( snapshot.getEvaluateSchedule() );
        final TimeInstant snapshotTime = (TimeInstant) snapshot.getSnapshotTime();
        assertEquals( "2011-07-12T09:11:01.857Z", snapshotTime.getPosition().getValue() );
    }

    @Test
    public void readExampleA2_1()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a02_1_query.xml" );
        final DynamicFeatureFilter dynamicFilter = (DynamicFeatureFilter) query.getSelectionClause();
        assertNotNull( dynamicFilter );
        final TimeInstant timeIndicator = (TimeInstant) dynamicFilter.getTimeIndicator();
        assertEquals( "2011-07-12T09:11:01.857Z", timeIndicator.getPosition().getValue() );
        final OperatorFilter featureFilter = (OperatorFilter) dynamicFilter.getFeatureFilter();
        assertNotNull( featureFilter );
        final And and = (And) featureFilter.getOperator();
        assertNotNull( and );
        final ProjectionClause[] projections = query.getProjectionClauses();
        assertEquals( 0, projections.length );
        final SnapshotGeneration snapshot = (SnapshotGeneration) query.getTransformation();
        assertNotNull( snapshot );
        assertNull( snapshot.getEvaluateSchedule() );
        final TimeInstant snapshotTime = (TimeInstant) snapshot.getSnapshotTime();
        assertEquals( "2011-07-12T09:11:01.857Z", snapshotTime.getPosition().getValue() );
    }

    @Test
    public void readExampleA2_2()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a02_2_query.xml" );
        final DynamicFeatureFilter dynamicFilter = (DynamicFeatureFilter) query.getSelectionClause();
        assertNotNull( dynamicFilter );
        assertNull( dynamicFilter.getTimeIndicator() );
        final OperatorFilter featureFilter = (OperatorFilter) dynamicFilter.getFeatureFilter();
        assertNotNull( featureFilter );
        final And and = (And) featureFilter.getOperator();
        assertNotNull( and );
        final ProjectionClause[] projections = query.getProjectionClauses();
        assertEquals( 0, projections.length );
        final SnapshotGeneration snapshot = (SnapshotGeneration) query.getTransformation();
        assertNotNull( snapshot );
        assertNull( snapshot.getEvaluateSchedule() );
        final TimeInstant snapshotTime = (TimeInstant) snapshot.getSnapshotTime();
        assertEquals( "2011-07-12T09:11:01.857Z", snapshotTime.getPosition().getValue() );
    }

    @Test
    public void readExampleA3()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a03_query.xml" );
        final Filter filter = (Filter) query.getSelectionClause();
        assertNotNull( filter );
        final ProjectionClause[] projections = query.getProjectionClauses();
        assertEquals( 1, projections.length );
        final TimeSliceProjection timeSlice = (TimeSliceProjection) projections[0];
        assertNotNull( timeSlice );
        assertNull( timeSlice.getIncludeCanceled() );
        assertNull( timeSlice.getIncludeCorrected() );
        final TimeInstant relevantTime = (TimeInstant) timeSlice.getRelevantTime();
        assertEquals( "2011-07-12T09:11:01.857Z", relevantTime.getPosition().getValue() );
        assertNotNull( timeSlice.getRelevantTime() );
        assertNull( timeSlice.getTimeSliceFilter() );
        final SnapshotGeneration snapshot = (SnapshotGeneration) query.getTransformation();
        assertNull( snapshot );
    }

    @Test
    public void readExampleA4()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a04_query.xml" );
        final Filter filter = (Filter) query.getSelectionClause();
        assertNotNull( filter );
        final ProjectionClause[] projections = query.getProjectionClauses();
        assertEquals( 1, projections.length );
        final TimeSliceProjection timeSlice = (TimeSliceProjection) projections[0];
        assertNotNull( timeSlice );
        assertNull( timeSlice.getIncludeCanceled() );
        assertNull( timeSlice.getIncludeCorrected() );
        final TimeInstant relevantTime = (TimeInstant) timeSlice.getRelevantTime();
        assertEquals( "2010-04-08T00:00:00.000Z", relevantTime.getPosition().getValue() );
        final Filter timeSliceFilter = timeSlice.getTimeSliceFilter();
        assertNotNull( timeSliceFilter );
        final SnapshotGeneration snapshot = (SnapshotGeneration) query.getTransformation();
        assertNull( snapshot );
    }

    // TODO implement temporal operators in filter
    @Test(expected = UnsupportedOperationException.class)
    public void readExampleA5()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a05_query.xml" );
    }

    // TODO implement temporal operators in filter
    @Test(expected = UnsupportedOperationException.class)
    public void readExampleA6()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a06_query.xml" );
    }

    @Test
    public void readExampleA7()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a07_query.xml" );
        final DynamicFeatureFilter filter = (DynamicFeatureFilter) query.getSelectionClause();
        assertNotNull( filter );
        final ProjectionClause[] projections = query.getProjectionClauses();
        assertEquals( 1, projections.length );
        final TimeSliceProjection timeSlice = (TimeSliceProjection) projections[0];
        assertNotNull( timeSlice );
        assertEquals( TRUE, timeSlice.getIncludeCanceled() );
        assertEquals( TRUE, timeSlice.getIncludeCorrected() );
        final TimeInstant relevantTime = (TimeInstant) timeSlice.getRelevantTime();
        assertNull( relevantTime );
    }

    @Test
    public void readExampleA8()
                            throws Exception {
        // standard WFS Query, not a DynamicFeatureQuery
    }

    @Test
    public void readExampleA9()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a09_query.xml" );
        final DynamicFeatureFilter filter = (DynamicFeatureFilter) query.getSelectionClause();
        assertNotNull( filter );
        final ProjectionClause[] projections = query.getProjectionClauses();
        assertEquals( 0, projections.length );
        final SnapshotGeneration snapshot = (SnapshotGeneration) query.getTransformation();
        assertEquals( TRUE, snapshot.getEvaluateSchedule() );
        final TimeInstant snapshotTime = (TimeInstant) snapshot.getSnapshotTime();
        assertEquals( "2011-07-01T08:00:00.000Z", snapshotTime.getPosition().getValue() );
    }

    @Test
    public void readExampleA10()
                            throws Exception {
        final DynamicFeatureQuery query = parseExample( "example_a10_query.xml" );
        final Filter filter = (Filter) query.getSelectionClause();
        assertNotNull( filter );
        final ProjectionClause[] projections = query.getProjectionClauses();
        assertEquals( 2, projections.length );
        final PropertyExclusion firstExclusion = (PropertyExclusion) projections[0];
        assertEquals( "{http://www.aixm.aero/schema/5.1}featureMetadata", "" + firstExclusion.getPropertyName() );
        final PropertyExclusion secondExclusion = (PropertyExclusion) projections[1];
        assertEquals( "{http://www.aixm.aero/schema/5.1}timeSliceMetadata", "" + secondExclusion.getPropertyName() );
        final SnapshotGeneration snapshot = (SnapshotGeneration) query.getTransformation();
        assertNull( snapshot.getEvaluateSchedule() );
        final TimeInstant snapshotTime = (TimeInstant) snapshot.getSnapshotTime();
        assertEquals( "2011-07-12T09:11:01.857Z", snapshotTime.getPosition().getValue() );
    }

    private DynamicFeatureQuery parseExample( final String resourceName )
                            throws Exception {
        final GetFeatureXMLAdapter parser = new GetFeatureXMLAdapter();
        parser.load( Te100AnnexAQueryParsingTest.class.getResource( "annexa/" + resourceName ) );
        final GetFeature request = parser.parse();
        return (DynamicFeatureQuery) request.getQueries().get( 0 );
    }
}
