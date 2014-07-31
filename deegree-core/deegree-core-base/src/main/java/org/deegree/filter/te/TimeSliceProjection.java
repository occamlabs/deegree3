/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.filter.te;

import org.deegree.filter.Filter;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.time.primitive.TimeGeometricPrimitive;

/**
 * {@link ProjectionClause} for limiting the time slices of a feature.
 * <p>
 * Defined in OGC Web Feature Service (WFS) Temporality Extension discussion paper (OGC 12-027r3).
 * </p>
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class TimeSliceProjection implements ProjectionClause {

    private final TimeGeometricPrimitive relevantTime;

    private final Filter timeSliceFilter;

    private final Boolean includeCanceled;

    private final Boolean includeCorrected;

    /**
     * Creates a new {@link TimeSliceProjection} instance.
     *
     * @param relevantTime
     *            time instant or time period to be considered, can be <code>null</code> (consider all time slices)
     * @param timeSliceFilter
     *            filter for restricting the time slices, can be <code>null</code> (no filter)
     * @param includeCanceled
     *            <code>true</code>, if cancelled time slices shall be included, can be <code>null</code> (=
     *            <code>false</code>)
     * @param includeCorrected
     *            <code>true</code>, if corrected time slices shall be included, can be <code>null</code> (=
     *            <code>false</code>)
     */
    public TimeSliceProjection( final TimeGeometricPrimitive relevantTime, final Filter timeSliceFilter,
                                final Boolean includeCanceled, final Boolean includeCorrected ) {
        this.relevantTime = relevantTime;
        this.timeSliceFilter = timeSliceFilter;
        this.includeCanceled = includeCanceled;
        this.includeCorrected = includeCorrected;
    }

    /**
     * Returns the time instant or time period to be considered.
     *
     * @return time instant or time period to be considered, can be <code>null</code> (consider all time slices)
     */
    public TimeGeometricPrimitive getRelevantTime() {
        return relevantTime;
    }

    /**
     * Returns the filter for restricting the time slices.
     *
     * @return filter for restricting the time slices, can be <code>null</code> (no filter)
     */
    public Filter getTimeSliceFilter() {
        return timeSliceFilter;
    }

    /**
     * Returns whether cancelled time slices shall be included.
     *
     * @return <code>true</code>, if cancelled time slices shall be included, can be <code>null</code> (=
     *         <code>false</code>)
     */
    public Boolean getIncludeCanceled() {
        return includeCanceled;
    }

    /**
     * Returns whether corrected time slices shall be included.
     *
     * @return <code>true</code>, if corrected time slices shall be included, can be <code>null</code> (=
     *         <code>false</code>)
     */
    public Boolean getIncludeCorrected() {
        return includeCorrected;
    }
}
