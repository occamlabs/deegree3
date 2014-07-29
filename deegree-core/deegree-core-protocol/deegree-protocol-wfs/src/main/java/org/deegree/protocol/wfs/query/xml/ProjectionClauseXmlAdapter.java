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
package org.deegree.protocol.wfs.query.xml;

import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.protocol.wfs.te.xml.TemporalityExtension100.WFS_TE_10_NS;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.protocol.wfs.te.xml.PropertyExclusionXmlAdapter;
import org.deegree.protocol.wfs.te.xml.TimeSliceProjectionXmlAdapter;

/**
 * AXIOM-based parser for substitutions of <code>fes:AbstractProjectionClause</code>.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class ProjectionClauseXmlAdapter {

    private static final QName FES_20_PROPERTY_NAME = new QName( FES_20_NS, "PropertyName" );

    private static final QName WFS_TE_10_PROPERTY_EXCLUSION = new QName( WFS_TE_10_NS, "PropertyExclusion" );

    private static final QName WFS_TE_10_TIME_SLICE_PROJECTION = new QName( WFS_TE_10_NS, "TimeSliceProjection" );

    private static final Set<QName> KNOWN_SUBSTITUTIONS = new HashSet<QName>();

    static {
        KNOWN_SUBSTITUTIONS.add( FES_20_PROPERTY_NAME );
        KNOWN_SUBSTITUTIONS.add( WFS_TE_10_PROPERTY_EXCLUSION );
        KNOWN_SUBSTITUTIONS.add( WFS_TE_10_TIME_SLICE_PROJECTION );
    }

    /**
     * Parses the given <code>fes:AbstractProjectionClause</code> element.
     *
     * @param el
     *            <code>fes:AbstractProjectionClause</code> element, must not be <code>null</code>
     * @return parsed {@link ProjectionClause}, never <code>null</code>
     */
    public ProjectionClause parse( final OMElement el ) {
        final QName elName = new QName( el.getNamespaceURI(), el.getLocalName() );
        if ( FES_20_PROPERTY_NAME.equals( elName ) ) {
            return new QueryXMLAdapter().parsePropertyName200( el );
        } else if ( WFS_TE_10_PROPERTY_EXCLUSION.equals( elName ) ) {
            return new PropertyExclusionXmlAdapter().parse( el );
        } else if ( WFS_TE_10_TIME_SLICE_PROJECTION.equals( elName ) ) {
            return new TimeSliceProjectionXmlAdapter().parse( el );
        }
        final String msg = "Element '" + elName + "' is not a known substitution for 'fes:AbstractProjectionClause'.";
        throw new XMLParsingException( new XMLAdapter( el ), el, msg );
    }

    /**
     * Returns whether the given element is in the substitution group for <code>fes:AbstractProjectionClause</code> .
     *
     * @param elName
     *            element name, can be <code>null</code>
     * @return <code>true</code>, if the lement is a known substitution, <code>false</code> otherwise
     */
    public boolean isKnownSubstitution( final QName elName ) {
        return KNOWN_SUBSTITUTIONS.contains( elName );
    }
}
