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

import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.te.TemporalityExtension100.WFS_TE_10_NS;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.te.xml.DynamicFeatureQueryXmlAdapter;

/**
 * AXIOM-based parser for substitutions of <code>fes:AbstractQueryExpression</code>.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class Wfs200QueryXmlAdapter {

    private static final QName WFS_200_AD_HOC_QUERY = new QName( WFS_200_NS, "Query" );

    private static final QName WFS_200_STORED_QUERY = new QName( WFS_200_NS, "StoredQuery" );

    private static final QName WFS_TE_10_DYNAMIC_FEATURE_QUERY = new QName( WFS_TE_10_NS, "DynamicFeatureQuery" );

    private static final Set<QName> KNOWN_SUBSTITUTIONS = new HashSet<QName>();

    static {
        KNOWN_SUBSTITUTIONS.add( WFS_200_AD_HOC_QUERY );
        KNOWN_SUBSTITUTIONS.add( WFS_200_STORED_QUERY );
        KNOWN_SUBSTITUTIONS.add( WFS_TE_10_DYNAMIC_FEATURE_QUERY );
    }

    /**
     * Parses the given <code>fes:AbstractQueryExpression</code> element.
     *
     * @param el
     *            <code>fes:AbstractQueryExpression</code> element, must not be <code>null</code>
     * @return parsed {@link Query}, never <code>null</code>
     * @throws OWSException
     */
    public Query parse( final OMElement el ) throws OWSException {
        final QName elName = new QName( el.getNamespaceURI(), el.getLocalName() );
        if ( WFS_200_AD_HOC_QUERY.equals( elName ) ) {
            return new QueryXMLAdapter().parseAdHocQuery200( el );
        } else if ( WFS_200_STORED_QUERY.equals( elName ) ) {
            return new QueryXMLAdapter().parseStoredQuery200( el );
        } else if ( WFS_TE_10_DYNAMIC_FEATURE_QUERY.equals( elName ) ) {
            return new DynamicFeatureQueryXmlAdapter().parse( el );
        }
        final String msg = "Element '" + elName + "' is not a known substitution for 'fes:AbstractQueryExpression'.";
        throw new XMLParsingException( new XMLAdapter( el ), el, msg );
    }

    /**
     * Returns whether the given element is in the substitution group for <code>fes:AbstractQueryExpression</code> .
     *
     * @param elName
     *            element name, can be <code>null</code>
     * @return <code>true</code>, if the lement is a known substitution, <code>false</code> otherwise
     */
    public boolean isKnownSubstitution( final QName elName ) {
        return KNOWN_SUBSTITUTIONS.contains( elName );
    }
}
