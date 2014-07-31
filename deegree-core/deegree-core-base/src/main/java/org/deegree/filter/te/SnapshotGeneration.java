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

import org.deegree.time.primitive.TimeGeometricPrimitive;

/**
 * {@link TransformationClause} for generating SNAPSHOT timeslices.
 * <p>
 * Defined in OGC Web Feature Service (WFS) Temporality Extension discussion paper (OGC 12-027r3).
 * </p>
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class SnapshotGeneration implements TransformationClause {

    private final Boolean evaluateSchedule;

    private final TimeGeometricPrimitive snapshotTime;

    /**
     * Creates a new {@link TransformationClause} instance.
     *
     * @param evaluateSchedule
     *            <code>true</code>, if properties with schedule shall be evaluated, can be <code>null</code> (=
     *            <code>false</code>)
     * @param snapshotTime
     *            time instant or time period of the SNAPSHOT timeslices, must not be <code>null</code>
     */
    public SnapshotGeneration( final Boolean evaluateSchedule, final TimeGeometricPrimitive snapshotTime ) {
        this.snapshotTime = snapshotTime;
        this.evaluateSchedule = evaluateSchedule;
    }

    /**
     * Returns whether properties with schedule shall be evaluated.
     *
     * @return <code>true</code>, if properties with schedule shall be evaluated, can be <code>null</code> (=
     *         <code>false</code>)
     */
    public Boolean getEvaluateSchedule() {
        return evaluateSchedule;
    }

    /**
     * Returns the time instant or time period of the SNAPSHOT timeslices.
     *
     * @return time instant or time period of the SNAPSHOT timeslices, never <code>null</code>
     */
    public TimeGeometricPrimitive getSnapshotTime() {
        return snapshotTime;
    }
}
