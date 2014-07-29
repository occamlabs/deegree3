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
import org.deegree.filter.SelectionClause;

/**
 * {@link SelectionClause} for evaluating {@link Filter} constraints at a given point in time or a time period.
 * <p>
 * Defined in OGC Web Feature Service (WFS) Temporality Extension discussion paper (OGC 12-027r3).
 * </p>
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class DynamicFeatureFilter implements TransformationClause {

    private final Boolean evaluateSchedules;

    private final Filter featureFilter;

    // gml:AbstractTimeGeometricPrimitive
    private final Object timeIndicator;

    /**
     * Creates a new {@link DynamicFeatureFilter} instance.
     *
     * @param evaluateSchedules
     *            <code>true</code> if the special semantics of properties with schedules shall be respected during
     *            evaluation, can be <code>null</code> (=<code>false</code>)
     * @param featureFilter
     *            filter on feature level, must not be <code>null</code>
     * @param timeIndicator
     *            time instant or time period when the {@link #featureFilter} is to be applied, can be <code>null</code>
     */
    public DynamicFeatureFilter( final Boolean evaluateSchedules, final Filter featureFilter, final Object timeIndicator ) {
        this.evaluateSchedules = evaluateSchedules;
        this.featureFilter = featureFilter;
        this.timeIndicator = timeIndicator;
    }

    /**
     * Returns whether the special semantics of properties with schedules shall be respected during evaluation.
     *
     * @return <code>true</code> if the special semantics of properties with schedules shall be respected during
     *         evaluation, can be <code>null</code> (=<code>false</code>)
     */
    public Boolean getEvaluateSchedules() {
        return evaluateSchedules;
    }

    /**
     * Returns the filter on feature level.
     *
     * @return filter on feature level, must not be <code>null</code>
     */
    public Filter getFeatureFilter() {
        return featureFilter;
    }

    /**
     * Returns the point in time or the time period when the filter is to be applied.
     *
     * @return point in time or the time period when the filter is to be applied, can be <code>null</code>
     */
    public Object getTimeIndicator() {
        return timeIndicator;
    }
}
