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
package org.deegree.protocol.wfs.te;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.SelectionClause;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.te.TransformationClause;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.AdHocQuery;

/**
 * {@link AdHocQuery} with special support for filtering and transforming time-varying features.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class DynamicFeatureQuery extends AdHocQuery {

    private final SelectionClause selectionClause;

    private final TransformationClause transformation;

    /**
     * Creates a new {@link DynamicFeatureQuery} instance.
     *
     * @param handle
     *            client-generated query identifier, may be <code>null</code>
     * @param typeNames
     *            requested feature types (with optional aliases), may be <code>null</code>
     * @param featureVersion
     *            version of the feature instances to be retrieved, may be <code>null</code>
     * @param srsName
     *            WFS-supported SRS that should be used for returned feature geometries, may be <code>null</code>
     * @param projectionClauses
     *            limits the properties of the features that should be retrieved, may be <code>null</code>
     * @param sortBy
     *            properties whose values should be used to order the result set may be <code>null</code>
     * @param selectionClause
     *            filter constraints, may be <code>null</code>
     * @param transformation
     *            transformation to be applied to the feature instances, may be <code>null</code> (no transformation)
     */
    public DynamicFeatureQuery( final String handle, final TypeName[] typeNames, final String featureVersion,
                                final ICRS srsName, final ProjectionClause[] projectionClauses,
                                final SortProperty[] sortBy, final SelectionClause selectionClause,
                                final TransformationClause transformation ) {
        super( handle, typeNames, featureVersion, srsName, projectionClauses, sortBy );
        this.selectionClause = selectionClause;
        this.transformation = transformation;
    }

    /**
     * Returns the filter constraint.
     *
     * @return the filter constraint, may be <code>null</code> (no filtering)
     */
    public SelectionClause getSelectionClause() {
        return selectionClause;
    }

    /**
     * Returns the transformation to be applied to the feature instances.
     *
     * @return transformation to be applied to the feature instances, may be <code>null</code> (no transformation)
     */
    public TransformationClause getTransformation() {
        return transformation;
    }
}
