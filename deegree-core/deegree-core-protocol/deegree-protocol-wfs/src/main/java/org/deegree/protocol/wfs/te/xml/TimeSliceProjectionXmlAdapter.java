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
import org.deegree.filter.Filter;
import org.deegree.filter.te.TimeSliceProjection;
import org.deegree.protocol.wfs.query.xml.QueryXMLAdapter;
import org.deegree.time.primitive.TimeGeometricPrimitive;

/**
 * AXIOM-based parser for <code>wfs-te:TimeSliceProjection</code> elements (OGC 12-027r3).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class TimeSliceProjectionXmlAdapter extends QueryXMLAdapter {

    private static final QName RELEVANT_TIME = new QName( WFS_TE_10_NS, "relevantTime" );

    private static final QName TIME_SLICE_FILTER = new QName( WFS_TE_10_NS, "timeSliceFilter" );

    private static final QName INCLUDE_CANCELED = new QName( "includeCanceled" );

    private static final QName INCLUDE_CORRECTED = new QName( "includeCorrected" );

    /**
     * Parses the given <code>wfs-te:TimeSliceProjection</code> element.
     *
     * @param timeSliceProjectionEl
     *            <code>wfs-te:TimeSliceProjection</code> element, must not be <code>null</code>
     * @return parsed {@link TimeSliceProjection}, never <code>null</code>
     */
    public TimeSliceProjection parse( final OMElement timeSliceProjectionEl ) {
        // xsd: <element minOccurs="0" name="relevantTime">
        final TimeGeometricPrimitive relevantTime = parseRelevantTimeIfPresent( timeSliceProjectionEl );
        // xsd: <element minOccurs="0" name="timeSliceFilter">
        final Filter filter = parseTimeSliceFilterIfPresent( timeSliceProjectionEl );
        // xsd: <attribute default="false" name="includeCanceled" type="boolean"/>
        final Boolean includeCanceled = getAttributeAsBoolean( timeSliceProjectionEl, INCLUDE_CANCELED, null );
        // xsd: <attribute default="false" name="includeCorrected" type="boolean"/>
        final Boolean includeCorrected = getAttributeAsBoolean( timeSliceProjectionEl, INCLUDE_CORRECTED, null );
        return new TimeSliceProjection( relevantTime, filter, includeCanceled, includeCorrected );
    }

    private TimeGeometricPrimitive parseRelevantTimeIfPresent( final OMElement timeSliceProjectionEl ) {
        final OMElement relevantTimeEl = timeSliceProjectionEl.getFirstChildWithName( RELEVANT_TIME );
        if ( relevantTimeEl == null ) {
            return null;
        }
        final OMElement childEl = getRequiredChildElement( relevantTimeEl );
        return new GmlAbstractTimeGeometricXmlAdapter().parse( childEl );
    }

    private Filter parseTimeSliceFilterIfPresent( final OMElement timeSliceProjectionEl ) {
        final OMElement timeSliceFilterEl = timeSliceProjectionEl.getFirstChildWithName( TIME_SLICE_FILTER );
        if ( timeSliceFilterEl == null ) {
            return null;
        }
        final OMElement childEl = getRequiredChildElement( timeSliceFilterEl );
        return parseFilter200( childEl );
    }
}
