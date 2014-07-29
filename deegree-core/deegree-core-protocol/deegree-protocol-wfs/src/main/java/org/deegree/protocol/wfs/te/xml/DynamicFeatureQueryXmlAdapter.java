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

import static org.deegree.protocol.wfs.te.xml.TemporalityExtension100.WFS_TE_10_NS;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.Filter;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.te.TransformationClause;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.xml.QueryXMLAdapter;

/**
 * AXIOM-based parser for <code>wfs-te:DynamicFeatureQuery</code> elements (OGC 12-027r3).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class DynamicFeatureQueryXmlAdapter extends QueryXMLAdapter {

    private static final QName SNAPSHOT_GENERATION = new QName( WFS_TE_10_NS, "SnapshotGeneration" );

    /**
     * Parses the given <code>wfs-te:DynamicFeatureQuery</code> element.
     *
     * @param el
     *            <code>fes-te:DynamicFeatureQuery</code> element, must not be <code>null</code>
     * @return parsed {@link DynamicFeatureQuery}, never <code>null</code>
     */
    public DynamicFeatureQuery parse( final OMElement queryEl )
                            throws OWSException {
        // <xsd:attribute name="handle" type="xsd:string"/>
        final String handle = parseHandle( queryEl );
        // <xsd:attribute name="aliases" type="fes:AliasesType"/>
        final String[] aliases = parseAliases200( queryEl );
        // <xsd:attribute name="typeNames" type="fes:TypeNamesListType" use="required"/>
        final TypeName[] typeNames = parseTypeNames200( queryEl, aliases );
        // <xsd:attribute name="srsName" type="xsd:anyURI"/>
        final ICRS crs = parseSrsName( queryEl );
        // <xsd:attribute name="featureVersion" type="xsd:string"/>
        final String featureVersion = parseFeatureVersion( queryEl );
        // <xsd:element ref="fes:AbstractProjectionClause" minOccurs="0" maxOccurs="unbounded"/>
        final List<ProjectionClause> projectionClauses = parseProjectionClauses200( queryEl );
        // <xsd:element ref="fes:AbstractSelectionClause" minOccurs="0"/>
        final Filter filter = parseFilter200IfPresent( queryEl );
        // <xsd:element ref="fes:AbstractSortingClause" minOccurs="0"/>
        final List<SortProperty> sortProps = parseSortBy200( queryEl );
        final ProjectionClause[] projection = projectionClauses.toArray( new ProjectionClause[projectionClauses.size()] );
        final SortProperty[] sortPropsArray = sortProps.toArray( new SortProperty[sortProps.size()] );
        // <element minOccurs="0" name="transformation" type="wfste:AbstractTransformationClausePropertyType"/>
        final TransformationClause transformation = parseAbstractTransformationClauseIfPresent( queryEl );
        return new DynamicFeatureQuery( handle, typeNames, featureVersion, crs, projection, sortPropsArray, filter,
                                        transformation );
    }

    private TransformationClause parseAbstractTransformationClauseIfPresent( final OMElement queryEl ) {
        final OMElement snapshotGenerationEl = queryEl.getFirstChildWithName( SNAPSHOT_GENERATION );
        if ( snapshotGenerationEl == null ) {
            return null;
        }
        return new SnapshotGenerationXmlAdapter().parse( snapshotGenerationEl );
    }
}
