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

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.filter.projection.ProjectionClause;

/**
 * {@link ProjectionClause} that excludes a property from a feature or from the timeslices of a {@link Feature}.
 * <p>
 * Defined in OGC Web Feature Service (WFS) Temporality Extension discussion paper (OGC 12-027r3).
 * </p>
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class PropertyExclusion implements ProjectionClause {

    private final QName propertyName;

    /**
     * Creates a new {@link PropertyExclusion} instance.
     *
     * @param propertyName
     *            targeted property, must not be <code>null</code>
     */
    public PropertyExclusion( final QName propertyName ) {
        this.propertyName = propertyName;
    }

    /**
     * Returns the targeted property.
     *
     * @return targeted property, never <code>null</code>
     */
    public QName getPropertyName() {
        return propertyName;
    }
}