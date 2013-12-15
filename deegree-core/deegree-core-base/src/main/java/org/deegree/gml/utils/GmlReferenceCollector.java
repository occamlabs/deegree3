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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.tom.Reference;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReference;
import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;

/**
 * Collects {@link GMLObjects} and references for easy consistency checking and resolving of graphs of {@link GMLObject}
 * s.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class GmlReferenceCollector {

    private final Map<String, GMLObject> idToObject = new HashMap<String, GMLObject>();

    private final List<Reference<?>> refs = new ArrayList<Reference<?>>();

    private final List<Reference<?>> localRefs = new ArrayList<Reference<?>>();

    /**
     * Adds the given {@link GMLObject}, as well as it's sub-objects and references.
     * 
     * @param object
     *            object, must not be <code>null</code>
     */
    public void add( GMLObject object ) {
        GMLObjectVisitor visitor = new GMLObjectVisitor() {

            @Override
            public boolean visitReference( Reference<?> ref ) {
                refs.add( ref );
                if ( ref.getURI().startsWith( "#" ) ) {
                    localRefs.add( ref );
                }
                return false;
            }

            @Override
            public boolean visitGeometry( Geometry geom ) {
                if ( geom.getId() != null ) {
                    idToObject.put( geom.getId(), geom );
                }
                return true;
            }

            @Override
            public boolean visitFeature( Feature feature ) {
                if ( feature.getId() != null ) {
                    idToObject.put( feature.getId(), feature );
                }
                return true;
            }
        };
        GMLObjectWalker walker = new GMLObjectWalker( visitor );
        walker.traverse( object );
    }

    /**
     * Returns the {@link GMLObject} with the specified id.
     * 
     * @param id
     *            id of the object to be returned
     * @return the object, or <code>null</code> if it has not been added before
     */
    public GMLObject getObject( String id ) {
        return idToObject.get( id );
    }

    /**
     * Returns all {@link GMLObject} (but no {@link GMLReference} instances) that have been added.
     * 
     * @return all gml objects that have been added before, may be empty, but never <code>null</code>
     */
    public Map<String, GMLObject> getObjects() {
        return idToObject;
    }

    /**
     * Return all {@link GMLReference} instances that have been added.
     * 
     * @return all gml references that have been added before, may be empty, but never <code>null</code>
     */
    public List<Reference<?>> getReferences() {
        return refs;
    }

    /**
     * Resolves all local references that have been added before against the added objects.
     * 
     * @throws ReferenceResolvingException
     *             if a local reference cannot be resolved
     */
    public void resolveLocalRefs()
                            throws ReferenceResolvingException {

        for ( Reference<?> ref : localRefs ) {
            String id = ref.getURI().substring( 1 );
            if ( ref.getReferencedObject() == null ) {
                String msg = "Cannot resolve reference to object with id '" + id + "'. There is no such object.";
                throw new ReferenceResolvingException( msg );
            }
        }
    }

}
