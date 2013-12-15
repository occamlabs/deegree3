//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.services.wfs.transaction;

import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.gml.GMLStreamReader;

/**
 * {@link FeatureInputStream} that is backed by a sequence of GML-encoded features (without delimiting).
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class UnenclosedGmlFeatureInputStream implements FeatureInputStream {

    private final GMLStreamReader gmlReader;

    /**
     * Creates a new {@link UnenclosedGmlFeatureInputStream} instance.
     * 
     * @param gmlReader
     *            cursor must point at the <code>START_ELEMENT</code> event of the first feature element, after full
     *            iteration points at the next event after the <code>END_ELEMENT</code> event of the final feature
     *            element
     */
    public UnenclosedGmlFeatureInputStream( GMLStreamReader gmlReader ) {
        this.gmlReader = gmlReader;
    }

    @Override
    public Iterator<Feature> iterator() {
        return new Iterator<Feature>() {

            @Override
            public boolean hasNext() {
                return gmlReader.getXMLReader().isStartElement();
            }

            @Override
            public Feature next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                try {
                    Feature f = gmlReader.readFeature();
                    nextElement( gmlReader.getXMLReader() );
                    return f;
                } catch ( Exception e ) {
                    String msg = "Error reading feature from stream: " + e.getMessage();
                    throw new IllegalArgumentException( msg, e );
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void close() {
        // nothing to do
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
