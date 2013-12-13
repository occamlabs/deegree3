/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.gml.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.GMLStreamReader;

/**
 * {@link FeatureInputStream} that extracts {@link Feature} elements from an XML/GML stream.
 * <p>
 * This class does not actually take the container structure into account, but detects (non-collection) feature elements
 * by their qualified name (as defined by the {@link AppSchema} attached to the {@link GMLStreamReader}).
 * </p>
 * <p>
 * It can be used for many types of documents:
 * <ul>
 * <li>Single GML feature document with no additional container</li>
 * <li>GML feature collection documents</li>
 * <li>Application-schema defined feature collection documents</li>
 * <li>WFS 2.0.0 feature collection documents (which are technically not GML feature collection documents)</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class RobustGmlFeatureInputStream implements FeatureInputStream {

    private final GMLStreamReader gmlStream;

    private final XMLStreamReader xmlStream;

    /**
     * Creates a new {@link RobustGmlFeatureInputStream} instance.
     * 
     * @param gmlStream
     *            initialized GML stream reader, must not be <code>null</code>
     * @throws XMLStreamException
     */
    public RobustGmlFeatureInputStream( GMLStreamReader gmlStream ) throws XMLStreamException {
        this.gmlStream = gmlStream;
        this.xmlStream = gmlStream.getXMLReader();
        forwardStreamToNextFeatureStartElement();
    }

    @Override
    public Iterator<Feature> iterator() {
        return new Iterator<Feature>() {

            @Override
            public boolean hasNext() {
                return isOnFeatureStartElement();
            }

            @Override
            public Feature next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                Feature feature = null;
                try {
                    feature = gmlStream.readFeature();
                } catch ( Exception e ) {
                    String msg = "Error reading GML feature from stream: " + e.getMessage();
                    throw new IllegalArgumentException( msg, e );
                }
                try {
                    forwardStreamToNextFeatureStartElement();
                } catch ( XMLStreamException e ) {
                    String msg = "Error forwarding GML stream to next feature: " + e.getMessage();
                    throw new IllegalArgumentException( msg, e );
                }
                return feature;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private boolean forwardStreamToNextFeatureStartElement()
                            throws XMLStreamException {
        while ( xmlStream.getEventType() != XMLStreamReader.END_DOCUMENT ) {
            if ( isOnFeatureStartElement() ) {
                return true;
            }
            xmlStream.next();
        }
        return false;
    }

    private boolean isOnFeatureStartElement() {
        if ( xmlStream.isStartElement() ) {
            QName elName = xmlStream.getName();
            FeatureType ft = gmlStream.getAppSchema().getFeatureType( elName );
            if ( ft != null && !( ft instanceof FeatureCollectionType ) && !ft.isAbstract() ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        try {
            gmlStream.close();
        } catch ( XMLStreamException e ) {
            throw new RuntimeException( e.getMessage() );
        }
    }

    @Override
    public FeatureCollection toCollection() {
        return Features.toCollection( this );
    }

    @Override
    public int count() {
        int i = 0;
        for ( @SuppressWarnings("unused")
        Feature f : this ) {
            i++;
        }
        close();
        return i;
    }

}
