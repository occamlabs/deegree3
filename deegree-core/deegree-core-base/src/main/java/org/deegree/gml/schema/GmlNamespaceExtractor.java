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
package org.deegree.gml.schema;

import static org.apache.xerces.xs.XSConstants.SCOPE_GLOBAL;
import static org.apache.xerces.xs.XSWildcard.NSCONSTRAINT_LIST;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;

/**
 * Provides methods to determine the namespaces of attributes and elements that may occur in the contents of
 * {@link GMLObjectType}s.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GmlNamespaceExtractor {

    private final GMLSchemaInfoSet schema;

    public GmlNamespaceExtractor( AppSchema schema ) {
        this.schema = schema.getGMLSchema();
    }

    public Set<String> getNamespaces( FeatureType ft ) {
        if ( ft.getSchema() != null && ft.getSchema().getGMLSchema() != null ) {
            GMLSchemaInfoSet gmlSchema = ft.getSchema().getGMLSchema();
            XSElementDeclaration elDecl = gmlSchema.getElementDecl( ft.getName() );
            return getNamespaces( elDecl );
        }
        return null;
    }

    public Set<String> getNamespaces( XSElementDeclaration elDecl ) {
        Set<String> namespaces = new LinkedHashSet<String>();
        Set<QName> elDecls = new HashSet<QName>();
        addNamespacesWithSubstitutions( elDecl, namespaces, elDecls );
        return namespaces;
    }

    private void addNamespacesWithSubstitutions( XSElementDeclaration elDecl, Set<String> namespaces, Set<QName> elDecls ) {
        List<XSElementDeclaration> substitutions = schema.getSubstitutions( elDecl, null, true, true );
        for ( XSElementDeclaration substitution : substitutions ) {
            addNamespaces( substitution, namespaces, elDecls );
        }
    }

    private void addNamespaces( XSElementDeclaration elDecl, Set<String> namespaces, Set<QName> elDecls ) {

        QName elName = getName( elDecl );
        if ( elDecl.getScope() == SCOPE_GLOBAL && elDecls.contains( elName ) ) {
            return;
        }
        namespaces.add( elName.getNamespaceURI() );
        elDecls.add( elName );
        XSTypeDefinition typeDef = elDecl.getTypeDefinition();
        if ( typeDef instanceof XSComplexTypeDefinition ) {
            addNamespaces( (XSComplexTypeDefinition) typeDef, namespaces, elDecls );
        }
    }

    private void addNamespaces( XSComplexTypeDefinition typeDef, Set<String> namespaces, Set<QName> elDecls ) {
        XSObjectList attributeUses = typeDef.getAttributeUses();
        for ( int i = 0; i < attributeUses.getLength(); i++ ) {
            XSAttributeDeclaration attr = ((XSAttributeUse) attributeUses.get( i )).getAttrDeclaration();
            namespaces.add( attr.getNamespace() );
        }
        XSParticle particle = typeDef.getParticle();
        if ( particle != null ) {
            addNamespaces( particle.getTerm(), namespaces, elDecls );
        }
    }

    private void addNamespaces( XSTerm term, Set<String> namespaces, Set<QName> elDecls ) {
        if ( term instanceof XSElementDeclaration ) {
            addNamespacesWithSubstitutions( (XSElementDeclaration) term, namespaces, elDecls );
        } else if ( term instanceof XSModelGroup ) {
            XSModelGroup modelGroup = (XSModelGroup) term;
            XSObjectList particles = modelGroup.getParticles();
            for ( int i = 0; i < particles.getLength(); i++ ) {
                XSParticle particle = (XSParticle) particles.get( i );
                addNamespaces( particle.getTerm(), namespaces, elDecls );
            }
        } else if ( term instanceof XSWildcard ) {
            XSWildcard wildCard = (XSWildcard) term;
            if ( wildCard.getConstraintType() == NSCONSTRAINT_LIST ) {
                StringList nsConstraints = wildCard.getNsConstraintList();
                for ( int i = 0; i < nsConstraints.getLength(); i++ ) {
                    String ns = (String) nsConstraints.get( i );
                    namespaces.add( ns );
                }
            } else {
                System.out.println( "HUHU: wildcard" );   
            }
        } else {
            throw new IllegalArgumentException( "Unexpected XSTerm subtype: " + term );
        }
    }

    private QName getName( XSElementDeclaration elDecl ) {
        return new QName( elDecl.getNamespace(), elDecl.getName() );
    }
}
