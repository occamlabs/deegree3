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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.filter.te.PropertyExclusion;

/**
 * AXIOM-based parser for <code>wfs-te:PropertyExclusion</code> elements (OGC 12-027r3).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class PropertyExclusionXmlAdapter extends XMLAdapter {

    private static final QName PROPERTY_NAME = new QName( WFS_TE_10_NS, "propertyName" );

    /**
     * Parses the given <code>wfs-te:PropertyExclusion</code> element.
     *
     * @param propertyExclusionEl
     *            <code>wfs-te:PropertyExclusion</code> element, must not be <code>null</code>
     * @return parsed {@link PropertyExclusion}, never <code>null</code>
     */
    public PropertyExclusion parse( final OMElement propertyExclusionEl ) {
        // <element name="propertyName" type="QName"/>
        final OMElement propertyNameEl = getRequiredChildElement( propertyExclusionEl, PROPERTY_NAME );
        final QName propertyName = parseQName( propertyNameEl.getText(), propertyNameEl );
        return new PropertyExclusion( propertyName );
    }
}
