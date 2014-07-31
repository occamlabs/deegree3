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

import static org.deegree.protocol.wfs.te.TemporalityExtension100.FES_TE_10_NS;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.filter.Filter;
import org.deegree.filter.te.DynamicFeatureFilter;
import org.deegree.protocol.wfs.query.xml.QueryXMLAdapter;
import org.deegree.time.primitive.TimeGeometricPrimitive;

/**
 * AXIOM-based parser for <code>fes-te:DynamicFeatureFilter</code> elements (OGC 12-027r3).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.4
 */
public class DynamicFeatureFilterXmlAdapter extends QueryXMLAdapter {

    private static final QName EVALUATE_SCHEDULES = new QName( "evaluateSchedules" );

    private static final QName TIME_INDICATOR = new QName( FES_TE_10_NS, "timeIndicator" );

    private static final QName FEATURE_FILTER = new QName( FES_TE_10_NS, "featureFilter" );

    /**
     * Parses the given <code>fes-te:DynamicFeatureFilter</code> element.
     *
     * @param dynamicFeatureFilterEl
     *            <code>fes-te:DynamicFeatureFilter</code> element, must not be <code>null</code>
     * @return parsed {@link DynamicFeatureFilter}, never <code>null</code>
     */
    public DynamicFeatureFilter parse( final OMElement dynamicFeatureFilterEl ) {
        // <attribute default="false" name="evaluateSchedules" type="boolean"/>
        final Boolean evaluateSchedules = getAttributeAsBoolean( dynamicFeatureFilterEl, EVALUATE_SCHEDULES, null );
        // <element minOccurs="0" name="timeIndicator">
        final TimeGeometricPrimitive timeIndicator = parseTimeIndicatorIfPresent( dynamicFeatureFilterEl );
        // <element name="featureFilter">
        final Filter featureFilter = parseRequiredFeatureFilter( dynamicFeatureFilterEl );
        return new DynamicFeatureFilter( evaluateSchedules, featureFilter, timeIndicator );
    }

    private TimeGeometricPrimitive parseTimeIndicatorIfPresent( final OMElement dynamicFeatureFilterEl ) {
        final OMElement timeIndicatorEl = dynamicFeatureFilterEl.getFirstChildWithName( TIME_INDICATOR );
        if ( timeIndicatorEl == null ) {
            return null;
        }
        final OMElement childEl = getRequiredChildElement( timeIndicatorEl );
        return new GmlAbstractTimeGeometricXmlAdapter().parse( childEl );
    }

    private Filter parseRequiredFeatureFilter( final OMElement dynamicFeatureFilterEl ) {
        final OMElement featureFilterEl = getRequiredChildElement( dynamicFeatureFilterEl, FEATURE_FILTER );
        final OMElement childEl = getRequiredChildElement( featureFilterEl );
        return parseFilter200( childEl );
    }
}
