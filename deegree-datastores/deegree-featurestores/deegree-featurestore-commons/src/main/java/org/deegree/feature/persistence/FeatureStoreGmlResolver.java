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
package org.deegree.feature.persistence;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.filter.MatchAction.ANY;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GMLReferenceResolver} that uses queries to a {@link FeatureStore} for resolving common types of object
 * references.
 * <p>
 * Supported types of URIs:
 * <ul>
 * <li>Bare xpointer: #${id}: Mapped to a feature id filter</li>
 * <li>URN uuids: urn:uuid:${id} Mapped to a filter on the <code>gml:identifier</code> property</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @version 3.4
 */
public class FeatureStoreGmlResolver implements GMLReferenceResolver {

    private static Logger LOG = LoggerFactory.getLogger( FeatureStoreGmlResolver.class );

    private final FeatureStore fs;

    private final QName ABSTRACT_FEATURE_NAME = new QName( GML3_2_NS, "AbstractFeature" );

    private final QName GML_IDENTIFIER_NAME = new QName( GML3_2_NS, "identifier" );

    /**
     * Creates a new {@link FeatureStoreGmlResolver} instance.
     * 
     * @param fs
     *            feature store to be used for retrieving local features, must not be <code>null</code>
     */
    public FeatureStoreGmlResolver( final FeatureStore fs ) {
        this.fs = fs;
    }

    @Override
    public GMLObject getObject( final String uri, final String baseURL ) {
        LOG.debug( "Retrieving object for uri '" + uri + "'." );
        if ( uri.startsWith( "#" ) ) {
            LOG.debug( "Bare xpointer (local id)" );
            return getObjectByResourceId( uri.substring( 1 ) );
        } else if ( uri.startsWith( "urn:uuid:" ) ) {
            LOG.debug( "urn:uuid" );
            return getObjectByGmlIdentifier( uri.substring( 9 ), "urn:uuid:" );
        }
        throw new ReferenceResolvingException( "Unable to resolve reference '" + uri + "'." );
    }

    private GMLObject getObjectByResourceId( final String id ) {
        try {
            return fs.getObjectById( id );
        } catch ( FeatureStoreException e ) {
            throw new ReferenceResolvingException( e.getMessage(), e );
        }
    }

    private GMLObject getObjectByGmlIdentifier( final String id, final String codeSpace ) {
        final Filter filter = getGmlIdentifierFilter( id );
        final TypeName[] typeNames = new TypeName[] { new TypeName( ABSTRACT_FEATURE_NAME, null ) };
        final Query query = new Query( typeNames, filter, null, null, null );
        FeatureInputStream features = null;
        try {
            features = fs.query( query );
            final Iterator<Feature> iterator = features.iterator();
            if ( iterator.hasNext() ) {
                return iterator.next();
            }
        } catch ( Exception e ) {
            throw new ReferenceResolvingException( e.getMessage(), e );
        } finally {
            if ( features != null ) {
                features.close();
            }
        }
        return null;
    }

    private Filter getGmlIdentifierFilter( final String id ) {
        final ValueReference gmlIdentifier = new ValueReference( GML_IDENTIFIER_NAME );
        final Operator operator = new PropertyIsEqualTo( gmlIdentifier, new Literal<PrimitiveValue>( id ), true, ANY );
        return new OperatorFilter( operator );
    }

}
