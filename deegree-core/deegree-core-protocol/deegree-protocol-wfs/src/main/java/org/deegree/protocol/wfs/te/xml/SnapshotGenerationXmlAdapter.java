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

import static org.deegree.protocol.wfs.te.TemporalityExtension100.WFS_TE_10_NS;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.filter.te.SnapshotGeneration;
import org.deegree.time.primitive.TimeGeometricPrimitive;

/**
 * AXIOM-based parser for <code>wfs-te:SnapshotGeneration</code> elements (OGC 12-027r3).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class SnapshotGenerationXmlAdapter extends XMLAdapter {

    private static final QName SNAPSHOT_TIME = new QName( WFS_TE_10_NS, "snapshotTime" );

    private static final QName EVALUATE_SCHEDULES = new QName( "evaluateSchedules" );

    /**
     * Parses the given <code>wfs-te:SnapshotGeneration</code> element.
     *
     * @param snapshotGenerationEl
     *            <code>wfs-te:SnapshotGeneration</code> element, must not be <code>null</code>
     * @return parsed {@link SnapshotGeneration}, never <code>null</code>
     */
    public SnapshotGeneration parse( final OMElement snapshotGenerationEl ) {
        // <element name="snapshotTime">
        final TimeGeometricPrimitive snapshotTime = parseRequiredSnapshotTime( snapshotGenerationEl );
        // <attribute default="false" name="evaluateSchedules" type="boolean"/>
        final Boolean evaluateSchedules = getAttributeAsBoolean( snapshotGenerationEl, EVALUATE_SCHEDULES, null );
        return new SnapshotGeneration( evaluateSchedules, snapshotTime );
    }

    private TimeGeometricPrimitive parseRequiredSnapshotTime( final OMElement timeSliceProjectionEl ) {
        final OMElement relevantTimeEl = getRequiredChildElement( timeSliceProjectionEl, SNAPSHOT_TIME );
        final OMElement childEl = getRequiredChildElement( relevantTimeEl );
        return new GmlAbstractTimeGeometricXmlAdapter().parse( childEl );
    }
}
