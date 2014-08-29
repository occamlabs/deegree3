//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.gml.schema;

import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_ELEMENT;
import static org.apache.xerces.xs.XSConstants.ELEMENT_DECLARATION;
import static org.apache.xerces.xs.XSConstants.MODEL_GROUP;
import static org.apache.xerces.xs.XSConstants.WILDCARD;
import static org.apache.xerces.xs.XSModelGroup.COMPOSITOR_ALL;
import static org.apache.xerces.xs.XSModelGroup.COMPOSITOR_CHOICE;
import static org.apache.xerces.xs.XSModelGroup.COMPOSITOR_SEQUENCE;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.ISOAP10GMDNS;
import static org.deegree.commons.xml.CommonNamespaces.ISO_2005_GCO_NS;
import static org.deegree.commons.xml.CommonNamespaces.ISO_2005_GSR_NS;
import static org.deegree.commons.xml.CommonNamespaces.ISO_2005_GSS_NS;
import static org.deegree.commons.xml.CommonNamespaces.ISO_2005_GTS_NS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;
import static org.deegree.feature.types.property.ValueRepresentation.INLINE;
import static org.deegree.feature.types.property.ValueRepresentation.REMOTE;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.schema.XMLSchemaInfoSet;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.ObjectPropertyType;
import org.deegree.feature.types.property.ValueRepresentation;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;

/**
 * Provides access to the <i>object</i> element declarations of a GML schema (both application and GML core schema
 * objects).
 * <p>
 * An element declaration is an <i>object</i> element declaration, if it is in one or more of GML's object substitution
 * groups. In the latest version of GML (3.2.1), eight (?) classes of GML objects exist:
 * <ul>
 * <li>feature</li>
 * <li>geometry</li>
 * <li>value</li>
 * <li>topology</li>
 * <li>crs</li>
 * <li>time object</li>
 * <li>coverage</li>
 * <li>style</li>
 * <li>object?</li>
 * <li>gml?</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class GMLSchemaInfoSet extends XMLSchemaInfoSet {

    private static final Logger LOG = LoggerFactory.getLogger( GMLSchemaInfoSet.class );

    private static final String GML_PRE_32_NS = CommonNamespaces.GMLNS;

    private static final String GML_32_NS = CommonNamespaces.GML3_2_NS;

    private GMLVersion version;

    private XSElementDeclaration abstractObjectElementDecl;

    private XSElementDeclaration abstractGmlElementDecl;

    private XSElementDeclaration abstractFeatureElementDecl;

    private XSElementDeclaration abstractGeometryElementDecl;

    private XSElementDeclaration abstractValueElementDecl;

    private XSElementDeclaration abstractTopologyElementDecl;

    private XSElementDeclaration abstractCRSElementDecl;

    private XSElementDeclaration abstractTimeObjectElementDecl;

    private XSElementDeclaration abstractCoverageElementDecl;

    private XSElementDeclaration abstractStyleElementDecl;

    private XSElementDeclaration abstractTimeSliceElementDecl;

    private XSElementDeclaration abstractCurveSegmentElementDecl;

    private XSElementDeclaration abstractSurfacePatchElementDecl;

    private XSTypeDefinition abstractFeatureElementTypeDecl;

    private List<XSElementDeclaration> ftDecls;

    private List<XSElementDeclaration> fcDecls;

    private Set<QName> geomElementNames = new HashSet<QName>();

    private Set<QName> featureElementNames = new HashSet<QName>();

    private Set<QName> timeSliceElementNames = new HashSet<QName>();

    private SortedSet<String> appNamespaces;

    /**
     * Creates a new {@link GMLSchemaInfoSet} instance for the given GML version and using the specified schemas.
     *
     * @param version
     *            gml version of the schema files, can be null (auto-detect GML version)
     * @param schemaUrls
     *            URLs of the schema files to load, must not be <code>null</code>
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public GMLSchemaInfoSet( GMLVersion version, String... schemaUrls ) throws ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {
        super( schemaUrls );
        init( version );
    }

    /**
     * Creates a new {@link GMLSchemaInfoSet} instance for the given GML version and using the specified inputs.
     *
     * @param version
     *            gml version of the schema files, can be null (auto-detect GML version)
     * @param inputs
     *            input objects forthe schema files to load, must not be <code>null</code>
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public GMLSchemaInfoSet( GMLVersion gmlVersion, LSInput... inputs ) throws ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {
        super( inputs );
        init( gmlVersion );
    }

    private void init( GMLVersion version ) {
        if ( version == null ) {
            this.version = determineGMLVersion( this );
        } else {
            this.version = version;
        }
        switch ( this.version ) {
        case GML_2: {
            abstractFeatureElementDecl = getElementDecl( "_Feature", GML_PRE_32_NS );
            abstractGeometryElementDecl = getElementDecl( "_Geometry", GML_PRE_32_NS );
            abstractFeatureElementTypeDecl = getTypeDef( "AbstractFeatureType", GML_PRE_32_NS );
            break;
        }
        case GML_30:
        case GML_31: {
            abstractObjectElementDecl = getElementDecl( "_Object", GML_PRE_32_NS );
            abstractGmlElementDecl = getElementDecl( "_GML", GML_PRE_32_NS );
            abstractFeatureElementDecl = getElementDecl( "_Feature", GML_PRE_32_NS );
            abstractGeometryElementDecl = getElementDecl( "_Geometry", GML_PRE_32_NS );
            abstractValueElementDecl = getElementDecl( "_Value", GML_PRE_32_NS );
            abstractTopologyElementDecl = getElementDecl( "_Topology", GML_PRE_32_NS );
            abstractCRSElementDecl = getElementDecl( "_CRS", GML_PRE_32_NS );
            abstractTimeObjectElementDecl = getElementDecl( "_TimeObject", GML_PRE_32_NS );
            abstractCoverageElementDecl = getElementDecl( "_Coverage", GML_PRE_32_NS );
            abstractStyleElementDecl = getElementDecl( "_Style", GML_PRE_32_NS );
            abstractTimeSliceElementDecl = getElementDecl( "_TimeSlice", GML_PRE_32_NS );
            abstractCurveSegmentElementDecl = getElementDecl( "_CurveSegment", GML_PRE_32_NS );
            abstractSurfacePatchElementDecl = getElementDecl( "_SurfacePatch", GML_PRE_32_NS );
            abstractFeatureElementTypeDecl = getTypeDef( "AbstractFeatureType", GML_PRE_32_NS );
            break;
        }
        case GML_32: {
            abstractObjectElementDecl = getElementDecl( "AbstractObject", GML_32_NS );
            abstractGmlElementDecl = getElementDecl( "AbstractGML", GML_32_NS );
            abstractFeatureElementDecl = getElementDecl( "AbstractFeature", GML_32_NS );
            abstractGeometryElementDecl = getElementDecl( "AbstractGeometry", GML_32_NS );
            abstractValueElementDecl = getElementDecl( "AbstractValue", GML_32_NS );
            abstractTopologyElementDecl = getElementDecl( "AbstractTopology", GML_32_NS );
            abstractCRSElementDecl = getElementDecl( "AbstractCRS", GML_32_NS );
            abstractTimeObjectElementDecl = getElementDecl( "AbstractTimeObject", GML_32_NS );
            abstractCoverageElementDecl = getElementDecl( "AbstractCoverage", GML_32_NS );
            abstractStyleElementDecl = getElementDecl( "AbstractStyle", GML_32_NS );
            abstractTimeSliceElementDecl = getElementDecl( "AbstractTimeSlice", GML_32_NS );
            abstractCurveSegmentElementDecl = getElementDecl( "AbstractCurveSegment", GML_32_NS );
            abstractSurfacePatchElementDecl = getElementDecl( "AbstractSurfacePatch", GML_32_NS );
            abstractFeatureElementTypeDecl = getTypeDef( "AbstractFeatureType", GML_32_NS );
            break;
        }
        }

        this.ftDecls = getSubstitutions( abstractFeatureElementDecl, null, true, false );

        switch ( this.version ) {
        case GML_2:
        case GML_30:
        case GML_31: {
            // TODO do this the right way
            fcDecls = new ArrayList<XSElementDeclaration>();
            if ( getElementDecl( "_FeatureCollection", GML_PRE_32_NS ) != null ) {
                fcDecls.addAll( getSubstitutions( getElementDecl( "_FeatureCollection", GML_PRE_32_NS ), null, true,
                                                  false ) );
            }
            if ( getElementDecl( "FeatureCollection", GML_PRE_32_NS ) != null ) {
                fcDecls.addAll( getSubstitutions( getElementDecl( "FeatureCollection", GML_PRE_32_NS ), null, true,
                                                  false ) );
            }

            break;
        }
        case GML_32:
            List<XSElementDeclaration> featureDecls = getFeatureElementDeclarations( null, false );
            fcDecls = new ArrayList<XSElementDeclaration>();
            for ( XSElementDeclaration featureDecl : featureDecls ) {
                if ( isGML32FeatureCollection( featureDecl ) ) {
                    fcDecls.add( featureDecl );
                }
            }
            break;
        }

        for ( XSElementDeclaration elemDecl : ftDecls ) {
            QName name = new QName( elemDecl.getNamespace(), elemDecl.getName() );
            featureElementNames.add( name );
        }

        for ( XSElementDeclaration elemDecl : getGeometryElementDeclarations( null, false ) ) {
            QName name = new QName( elemDecl.getNamespace(), elemDecl.getName() );
            geomElementNames.add( name );
        }

        if ( abstractTimeSliceElementDecl != null ) {
            for ( XSElementDeclaration elemDecl : getTimeSliceElementDeclarations( null, false ) ) {
                QName name = new QName( elemDecl.getNamespace(), elemDecl.getName() );
                timeSliceElementNames.add( name );
            }
        }
    }

    /**
     * Determines the GML version of the given {@link XMLSchemaInfoSet} heuristically.
     *
     * @param xmlSchemaInfoSet
     *            XML schema, must not be <code>null</code>
     * @return gml version, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the GML version cannot be determined
     */
    public static GMLVersion determineGMLVersion( XMLSchemaInfoSet xmlSchemaInfoSet )
                            throws IllegalArgumentException {
        GMLVersion gmlVersion = GML_32;
        Set<String> namespaces = xmlSchemaInfoSet.getSchemaNamespaces();
        if ( namespaces.contains( GML_32_NS ) ) {
            LOG.debug( "Schema must be GML 3.2 (found GML 3.2 namespace)" );
        } else if ( !namespaces.contains( GMLNS ) ) {
            String msg = "Cannot interpret XML schema as GML schema. "
                         + "Neither GML core schema components in GML 3.2 namespace (" + GML3_2_NS + "), nor in "
                         + "pre-GML 3.2 namespace (" + GMLNS + ") are present.";
            throw new IllegalArgumentException( msg );
        } else {
            gmlVersion = GML_31;
            LOG.debug( "Automatic differentiation between GML 3.1, 3.0 and 2 is not implemented (same namespace URLs)." );
        }
        return gmlVersion;
    }

    /**
     * Returns the GML version used for the infoset.
     *
     * @return the GML version used for the infoset, never <code>null</code>
     */
    public GMLVersion getVersion() {
        return version;
    }

    /**
     * Returns whether the given namespace is a GML core namespace.
     *
     * @param ns
     *            namespace to check, may be <code>null</code>
     * @return true, if it is a GML core namespace, false otherwise
     */
    public static boolean isGMLNamespace( String ns ) {
        if ( GMLNS.equals( ns ) ) {
            return true;
        } else if ( GML3_2_NS.equals( ns ) ) {
            return true;
        } else if ( XSNS.equals( ns ) ) {
            return true;
        } else if ( XLNNS.equals( ns ) ) {
            return true;
        } else if ( ISOAP10GMDNS.equals( ns ) ) {
            return true;
        } else if ( ISO_2005_GSR_NS.equals( ns ) ) {
            return true;
        } else if ( ISO_2005_GSS_NS.equals( ns ) ) {
            return true;
        } else if ( ISO_2005_GTS_NS.equals( ns ) ) {
            return true;
        } else if ( ISO_2005_GCO_NS.equals( ns ) ) {
            return true;
        }
        return false;
    }

    /**
     * Returns all application namespaces that participate in this infoset.
     * <p>
     * This excludes all namespaces that are imported by the GML core schemas.
     * </p>
     *
     * @return all application namespaces, never <code>null</code>
     */
    public synchronized SortedSet<String> getAppNamespaces() {
        if ( appNamespaces == null ) {
            appNamespaces = new TreeSet<String>( getSchemaNamespaces() );
            appNamespaces.remove( version.getNamespace() );
            appNamespaces.remove( XMLNS );
            appNamespaces.remove( XLNNS );
            appNamespaces.remove( XSNS );
            appNamespaces.remove( "http://www.w3.org/XML/1998/namespace" );
            appNamespaces.remove( ISOAP10GMDNS );
            appNamespaces.remove( ISO_2005_GCO_NS );
            appNamespaces.remove( ISO_2005_GSR_NS );
            appNamespaces.remove( ISO_2005_GSS_NS );
            appNamespaces.remove( ISO_2005_GTS_NS );
        }
        return appNamespaces;
    }

    /**
     * Returns the element declaration of the abstract object element, i.e.
     * <code>{http://www.opengis.net/gml}_Object</code> (GML 3.0 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractObject</code> (GML 3.2).
     *
     * @return declaration object of the abstract object element, may be <code>null</code> (for GML 2)
     */
    public XSElementDeclaration getAbstractObjectElementDeclaration() {
        return abstractObjectElementDecl;
    }

    /**
     * Returns the element declaration of the abstract GML element, i.e. <code>{http://www.opengis.net/gml}_GML</code>
     * (GML 3.0 to 3.1) or <code>{http://www.opengis.net/gml/3.2}AbstractGML</code> (GML 3.2).
     *
     * @return declaration object of the abstract GML element, may be <code>null</code> (for GML 2)
     */
    public XSElementDeclaration getAbstractGMLElementDeclaration() {
        return abstractGmlElementDecl;
    }

    /**
     * Returns the element declaration of the abstract feature element, i.e.
     * <code>{http://www.opengis.net/gml}_Feature</code> (GML 2 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractFeature</code> (GML 3.2).
     *
     * @return declaration object of the abstract feature element
     */
    public XSElementDeclaration getAbstractFeatureElementDeclaration() {
        return abstractFeatureElementDecl;
    }

    /**
     * Returns the element declaration of the abstract geometry element, i.e.
     * <code>{http://www.opengis.net/gml}_Geometry</code> (GML 2 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractGeometry</code> (GML 3.2).
     *
     * @return declaration object of the abstract geometry element
     */
    public XSElementDeclaration getAbstractGeometryElementDeclaration() {
        return abstractGeometryElementDecl;
    }

    /**
     * Returns the element declaration of the abstract time slice element, i.e.
     * <code>{http://www.opengis.net/gml}_TimeSlice</code> (GML 2 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractTimeSlice</code> (GML 3.2).
     *
     * @return declaration object of the abstract time slice element
     */
    public XSElementDeclaration getAbstractTimeSliceElementDeclaration() {
        return abstractTimeSliceElementDecl;
    }

    /**
     * Returns the element declaration of the abstract curve segment element, i.e.
     * <code>{http://www.opengis.net/gml}_CurveSegment</code> (GML 3 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractCurveSegment</code> (GML 3.2).
     *
     * @return declaration object of the abstract curve segment element, may be <code>null</code> (for GML 2)
     */
    public XSElementDeclaration getAbstractCurveSegmentElementDeclaration() {
        return abstractCurveSegmentElementDecl;
    }

    /**
     * Returns the element declaration of the abstract surface patch element, i.e.
     * <code>{http://www.opengis.net/gml}_SurfacePatch</code> (GML 3 to 3.1) or
     * <code>{http://www.opengis.net/gml/3.2}AbstractSurfacePatch</code> (GML 3.2).
     *
     * @return element declaration object of the abstract geometry element, may be <code>null</code> (for GML 2)
     */
    public XSElementDeclaration getAbstractSurfacePatchElementDeclaration() {
        return abstractSurfacePatchElementDecl;
    }

    public List<XSElementDeclaration> getObjectElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractObjectElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getGmlElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractGmlElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getFeatureElementDeclarations( String namespace, boolean onlyConcrete ) {
        List<XSElementDeclaration> ftDecls = new ArrayList<XSElementDeclaration>();
        for ( XSElementDeclaration ftDecl : this.ftDecls ) {
            if ( !ftDecl.getAbstract() || !onlyConcrete ) {
                if ( namespace == null || ftDecl.getNamespace().equals( namespace ) ) {
                    ftDecls.add( ftDecl );
                }
            }
        }
        return ftDecls;
    }

    public List<XSTypeDefinition> getFeatureTypeDefinitions( String namespace, boolean onlyConcrete ) {
        return getSubtypes( abstractFeatureElementTypeDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getFeatureCollectionElementDeclarations( String namespace, boolean onlyConcrete ) {
        List<XSElementDeclaration> fcDecls = new ArrayList<XSElementDeclaration>();
        for ( XSElementDeclaration fcDecl : this.fcDecls ) {
            if ( !fcDecl.getAbstract() || !onlyConcrete ) {
                if ( namespace == null || fcDecl.getNamespace().equals( namespace ) ) {
                    fcDecls.add( fcDecl );
                }
            }
        }
        return fcDecls;
    }

    /**
     * Returns whether the given feature element declaration is a feature collection.
     * <p>
     * GML 3.2 does not have an abstract feature collection element anymore (to be precise: it's deprecated). Every
     * <code>gml:AbstractFeature</code> element that has a property whose content model extends
     * <code>gml:AbstractFeatureMemberType</code> is a feature collection. See OGC 07-061, section 6.5.
     * </p>
     *
     * @param featureDecl
     *            feature element declaration, must not be <code>null</code>
     * @return true, if the given element declaration is a feature collection, false otherwise
     */
    private boolean isGML32FeatureCollection( XSElementDeclaration featureDecl ) {
        XSComplexTypeDecl type = (XSComplexTypeDecl) featureDecl.getTypeDefinition();
        List<XSElementDeclaration> propDecls = getPropertyDecls( type );
        for ( XSElementDeclaration propDecl : propDecls ) {
            XSTypeDefinition propType = propDecl.getTypeDefinition();
            if ( propType.derivedFrom( GML_32_NS, "AbstractFeatureMemberType",
                                       (short) ( XSConstants.DERIVATION_RESTRICTION | XSConstants.DERIVATION_EXTENSION
                                                 | XSConstants.DERIVATION_UNION | XSConstants.DERIVATION_LIST ) ) ) {
                return true;
            }
            // handle deprecated FeatureCollection types as well (their properties are not based on
            // AbstractFeatureMemberType, but on FeaturePropertyType)
            if ( propType.derivedFrom( GML_32_NS, "FeaturePropertyType",
                                       (short) ( XSConstants.DERIVATION_RESTRICTION | XSConstants.DERIVATION_EXTENSION
                                                 | XSConstants.DERIVATION_UNION | XSConstants.DERIVATION_LIST ) ) ) {
                return true;
            }
        }
        return false;
    }

    private List<XSElementDeclaration> getPropertyDecls( XSComplexTypeDecl type ) {
        List<XSElementDeclaration> propDecls = new ArrayList<XSElementDeclaration>();
        getPropertyDecls( type.getParticle(), propDecls );
        return propDecls;
    }

    private void getPropertyDecls( XSParticle particle, List<XSElementDeclaration> propertyDecls ) {
        if ( particle != null ) {
            XSTerm term = particle.getTerm();
            if ( term instanceof XSElementDeclaration ) {
                propertyDecls.add( (XSElementDeclaration) term );
            } else if ( term instanceof XSModelGroup ) {
                XSObjectList particles = ( (XSModelGroup) term ).getParticles();
                for ( int i = 0; i < particles.getLength(); i++ ) {
                    getPropertyDecls( (XSParticle) particles.item( i ), propertyDecls );
                }
            } else {
                LOG.warn( "Unhandled term type: " + term.getClass() );
            }
        }
    }

    public List<XSElementDeclaration> getGeometryElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractGeometryElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getValueElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractValueElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getTopologyElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractTopologyElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getCRSElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractCRSElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getTimeObjectElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractTimeObjectElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getCoverageElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractCoverageElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getStyleElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractStyleElementDecl, namespace, true, onlyConcrete );
    }

    public List<XSElementDeclaration> getTimeSliceElementDeclarations( String namespace, boolean onlyConcrete ) {
        return getSubstitutions( abstractTimeSliceElementDecl, namespace, true, onlyConcrete );
    }

    public XSElementDeclaration getGeometryElement( QName elName ) {
        for ( XSElementDeclaration elementDecl : getGeometryElementDeclarations( null, false ) ) {
            if ( elementDecl.getNamespace().equals( elName.getNamespaceURI() )
                 && elementDecl.getName().equals( elName.getLocalPart() ) ) {
                return elementDecl;
            }
        }
        return null;
    }

    /**
     * Checks the given element declaration and returns a {@link ObjectPropertyType} if it defines a GML object property
     * or GML reference property.
     *
     * @param elDecl
     * @param ptName
     * @param minOccurs
     * @param maxOccurs
     * @param ptSubstitutions
     * @return corresponding {@link ObjectPropertyType} or <code>null</code> if it's not a GML object property
     */
    public ObjectPropertyType getGMLPropertyDecl( XSElementDeclaration elDecl, QName ptName, int minOccurs,
                                                  int maxOccurs, List<PropertyType> ptSubstitutions ) {
        if ( !( elDecl.getTypeDefinition() instanceof XSComplexTypeDefinition ) ) {
            return null;
        }
        XSComplexTypeDefinition typeDef = (XSComplexTypeDefinition) elDecl.getTypeDefinition();
        ObjectPropertyType pt = buildGeometryPropertyType( ptName, elDecl, typeDef, minOccurs, maxOccurs,
                                                           ptSubstitutions );
        if ( pt == null ) {
            pt = buildFeaturePropertyType( ptName, elDecl, typeDef, minOccurs, maxOccurs, ptSubstitutions );
        }
        if ( pt == null ) {
            if ( allowsXLink( (XSComplexTypeDefinition) elDecl.getTypeDefinition() ) ) {
                LOG.debug( "Identified generic object property declaration ({" + elDecl.getNamespace() + "}"
                           + elDecl.getName() + "), but handling is not implemented yet." );
                // // TODO actually determine allowed value representations
                // pt = new GenericObjectPropertyType( ptName, minOccurs, maxOccurs, elDecl.getAbstract(),
                // elDecl.getNillable(), ptSubstitutions, BOTH, typeDef );
            }
        }
        return pt;
    }

    private boolean allowsXLink( XSComplexTypeDefinition typeDef ) {
        XSObjectList xsObjectList = typeDef.getAttributeUses();
        for ( int i = 0; i < xsObjectList.getLength(); i++ ) {
            XSAttributeDeclaration attr = ( (XSAttributeUse) xsObjectList.item( i ) ).getAttrDeclaration();
            if ( "href".equals( attr.getName() ) && XLNNS.equals( attr.getNamespace() ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Analyzes the given complex type definition and returns a {@link FeaturePropertyType} if it defines a feature
     * property.
     *
     * @param elementDecl
     * @param typeDef
     * @param minOccurs
     * @param maxOccurs
     * @return corresponding {@link FeaturePropertyType} or null, if declaration does not define a feature property
     */
    private FeaturePropertyType buildFeaturePropertyType( QName ptName, XSElementDeclaration elementDecl,
                                                          XSComplexTypeDefinition typeDef, int minOccurs,
                                                          int maxOccurs, List<PropertyType> ptSubstitutions ) {

        LOG.trace( "Checking if element declaration '" + ptName + "' defines a feature property type." );
        FeaturePropertyType pt = null;

        XMLAdapter annotationXML = null;
        XSObjectList annotations = elementDecl.getAnnotations();
        if ( annotations.getLength() > 0 ) {
            XSAnnotation annotation = (XSAnnotation) annotations.item( 0 );
            String s = annotation.getAnnotationString();
            annotationXML = new XMLAdapter( new StringReader( s ) );
        }

        if ( annotationXML != null ) {
            pt = buildFeaturePropertyTypeGML32( ptName, elementDecl, typeDef, minOccurs, maxOccurs, ptSubstitutions,
                                                annotationXML );
            if ( pt != null ) {
                return pt;
            }
            pt = buildFeaturePropertyTypeXGml( ptName, elementDecl, typeDef, minOccurs, maxOccurs, ptSubstitutions,
                                               annotationXML );
            if ( pt != null ) {
                return pt;
            }
            pt = buildFeaturePropertyTypeAdv( ptName, elementDecl, typeDef, minOccurs, maxOccurs, ptSubstitutions,
                                              annotationXML );
            if ( pt != null ) {
                return pt;
            }
        }

        boolean allowsXLink = allowsXLink( typeDef );

        switch ( typeDef.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_EMPTY: {
            pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementDecl, ptSubstitutions, null,
                                          ValueRepresentation.REMOTE );
            return pt;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            LOG.trace( "CONTENTTYPE_ELEMENT" );
            XSParticle particle = typeDef.getParticle();
            XSTerm term = particle.getTerm();
            switch ( term.getType() ) {
            case XSConstants.MODEL_GROUP: {
                XSModelGroup modelGroup = (XSModelGroup) term;
                switch ( modelGroup.getCompositor() ) {
                case XSModelGroup.COMPOSITOR_ALL: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
                    break;
                }
                case XSModelGroup.COMPOSITOR_CHOICE:
                case XSModelGroup.COMPOSITOR_SEQUENCE: {
                    LOG.trace( "Found sequence / choice." );
                    XSObjectList sequence = modelGroup.getParticles();
                    if ( sequence.getLength() != 1 ) {
                        LOG.trace( "Length = '" + sequence.getLength() + "' -> cannot be a feature property." );
                        return null;
                    }
                    XSParticle particle2 = (XSParticle) sequence.item( 0 );
                    switch ( particle2.getTerm().getType() ) {
                    case XSConstants.ELEMENT_DECLARATION: {
                        XSElementDeclaration elementDecl2 = (XSElementDeclaration) particle2.getTerm();
                        QName elementName = new QName( elementDecl2.getNamespace(), elementDecl2.getName() );
                        if ( featureElementNames.contains( elementName ) ) {
                            LOG.trace( "Identified a feature property." );
                            pt = null;
                            if ( version.getNamespace().equals( elementName.getNamespaceURI() ) ) {
                                if ( allowsXLink ) {
                                    pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementDecl,
                                                                  ptSubstitutions, null, ValueRepresentation.BOTH );
                                } else {
                                    pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementDecl,
                                                                  ptSubstitutions, null, ValueRepresentation.INLINE );
                                }
                            } else {
                                if ( allowsXLink ) {
                                    pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementDecl,
                                                                  ptSubstitutions, elementName,
                                                                  ValueRepresentation.BOTH );
                                } else {
                                    pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementDecl,
                                                                  ptSubstitutions, null, ValueRepresentation.INLINE );
                                }
                            }
                            return pt;
                        }
                    }
                    case XSConstants.WILDCARD: {
                        LOG.debug( "Unhandled particle: WILDCARD" );
                        break;
                    }
                    case XSConstants.MODEL_GROUP: {
                        LOG.debug( "Unhandled particle: MODEL_GROUP" );
                        break;
                    }
                    }
                    break;
                }
                default: {
                    assert false;
                }
                }
                break;
            }
            case XSConstants.WILDCARD: {
                LOG.debug( "Unhandled particle: WILDCARD" );
                break;
            }
            case XSConstants.ELEMENT_DECLARATION: {
                LOG.debug( "Unhandled particle: ELEMENT_DECLARATION" );
                break;
            }
            default: {
                assert false;
            }
            }
            break;
        }
        default: {
            LOG.debug( "Unhandled content type in buildFeaturePropertyType(...) encountered." );
        }
        }
        return null;
    }

    private FeaturePropertyType buildFeaturePropertyTypeXGml( QName ptName, XSElementDeclaration elementDecl,
                                                              XSComplexTypeDefinition typeDef, int minOccurs,
                                                              int maxOccurs, List<PropertyType> ptSubstitutions,
                                                              XMLAdapter annotationXML ) {

        // handle schemas that use a source="urn:x-gml:targetElement" attribute
        // for defining the referenced feature type
        // inside the annotation element (e.g. CITE examples for WFS 1.1.0)
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
        QName refElement = annotationXML.getNodeAsQName( annotationXML.getRootElement(),
                                                         new XPath(
                                                                    "xs:appinfo[@source='urn:x-gml:targetElement']/text()",
                                                                    nsContext ), null );
        if ( refElement != null ) {
            LOG.debug( "Identified a feature property (urn:x-gml:targetElement)." );
            FeaturePropertyType pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementDecl,
                                                              ptSubstitutions, refElement, ValueRepresentation.BOTH );
            return pt;
        }
        return null;
    }

    private FeaturePropertyType buildFeaturePropertyTypeAdv( QName ptName, XSElementDeclaration elementDecl,
                                                             XSComplexTypeDefinition typeDef, int minOccurs,
                                                             int maxOccurs, List<PropertyType> ptSubstitutions,
                                                             XMLAdapter annotationXML ) {

        // handle adv schemas (referenced feature type inside annotation
        // element)
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
        nsContext.addNamespace( "adv", "http://www.adv-online.de/nas" );
        QName refElement = annotationXML.getNodeAsQName( annotationXML.getRootElement(),
                                                         new XPath( "xs:appinfo/adv:referenziertesElement/text()",
                                                                    nsContext ), null );
        if ( refElement != null ) {
            LOG.trace( "Identified a feature property (adv style)." );
            FeaturePropertyType pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementDecl,
                                                              ptSubstitutions, refElement, ValueRepresentation.BOTH );
            return pt;
        }
        return null;
    }

    private FeaturePropertyType buildFeaturePropertyTypeGML32( QName ptName, XSElementDeclaration elementDecl,
                                                               XSComplexTypeDefinition typeDef, int minOccurs,
                                                               int maxOccurs, List<PropertyType> ptSubstitutions,
                                                               XMLAdapter annotationXML ) {
        // handle GML 3.2 schemas (referenced feature type inside annotation element)
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
        nsContext.addNamespace( "gml", GML3_2_NS );
        QName refElement = annotationXML.getNodeAsQName( annotationXML.getRootElement(),
                                                         new XPath( "xs:appinfo/gml:targetElement/text()", nsContext ),
                                                         null );
        if ( refElement != null ) {
            LOG.trace( "Identified a feature property (GML 3.2 style)." );
            // TODO determine this properly
            ValueRepresentation vp = ValueRepresentation.REMOTE;
            FeaturePropertyType pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementDecl,
                                                              ptSubstitutions, refElement, vp );
            return pt;
        }
        return null;
    }

    /**
     * Analyzes the given complex type definition and returns a {@link GeometryPropertyType} if it defines a geometry
     * property.
     *
     * @param elementDecl
     * @param typeDef
     * @param minOccurs
     * @param maxOccurs
     * @return corresponding {@link GeometryPropertyType} or null, if declaration does not define a geometry property
     */
    private GeometryPropertyType buildGeometryPropertyType( QName ptName, XSElementDeclaration elementDecl,
                                                            XSComplexTypeDefinition typeDef, int minOccurs,
                                                            int maxOccurs, List<PropertyType> ptSubstitutions ) {

        switch ( typeDef.getContentType() ) {
        case CONTENTTYPE_ELEMENT: {
            LOG.trace( "CONTENTTYPE_ELEMENT" );
            XSParticle particle = typeDef.getParticle();
            XSTerm term = particle.getTerm();
            switch ( term.getType() ) {
            case MODEL_GROUP: {
                XSModelGroup modelGroup = (XSModelGroup) term;
                switch ( modelGroup.getCompositor() ) {
                case COMPOSITOR_ALL: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
                    break;
                }
                case COMPOSITOR_CHOICE: {
                    LOG.trace( "Found choice." );
                    XSObjectList geomChoice = modelGroup.getParticles();
                    int length = geomChoice.getLength();
                    Set<GeometryType> allowedTypes = new HashSet<GeometryType>();
                    for ( int i = 0; i < length; ++i ) {
                        XSParticle geomChoiceParticle = (XSParticle) geomChoice.item( i );
                        XSTerm geomChoiceTerm = geomChoiceParticle.getTerm();
                        if ( geomChoiceTerm.getType() == ELEMENT_DECLARATION ) {
                            // other types are not supported
                            XSElementDeclaration geomChoiceElement = (XSElementDeclaration) geomChoiceTerm;
                            // min occurs check should be done, in regards to the xlinking.
                            int minOccurs3 = geomChoiceParticle.getMinOccurs();
                            int maxOccurs3 = geomChoiceParticle.getMaxOccursUnbounded() ? -1
                                                                                       : geomChoiceParticle.getMaxOccurs();
                            if ( minOccurs3 != 1 || maxOccurs3 != 1 ) {
                                LOG.debug( "Only single geometries are currently supported, ignoring in choice (property '"
                                           + ptName + "')." );
                                return null;
                            }
                            QName elementName = new QName( geomChoiceElement.getNamespace(),
                                                           geomChoiceElement.getName() );
                            if ( geomElementNames.contains( elementName ) ) {
                                LOG.trace( "Identified a geometry property." );
                                GeometryType geometryType = getGeometryType( elementName );
                                allowedTypes.add( geometryType );
                            } else {
                                LOG.debug( "Unknown geometry type '" + elementName + "'." );
                            }
                        } else if ( geomChoiceTerm.getType() == MODEL_GROUP ) {
                            LOG.warn( "Unhandled particle: MODEL_GROUP" );
                        } else if ( geomChoiceTerm.getType() == WILDCARD ) {
                            LOG.warn( "Unhandled particle: WILDCARD" );
                        } else {
                            LOG.warn( "Unexpected XSTerm type: " + geomChoiceTerm.getType() );
                        }
                    }
                    if ( !allowedTypes.isEmpty() ) {
                        return new GeometryPropertyType( ptName, minOccurs, maxOccurs, elementDecl, ptSubstitutions,
                                                         allowedTypes, CoordinateDimension.DIM_2_OR_3, BOTH );
                    }
                    break;
                }
                case COMPOSITOR_SEQUENCE: {
                    LOG.trace( "Found sequence." );
                    XSObjectList sequence = modelGroup.getParticles();
                    if ( sequence.getLength() != 1 ) {
                        LOG.trace( "Length = '" + sequence.getLength() + "' -> cannot be a geometry property." );
                        return null;
                    }
                    XSParticle particle2 = (XSParticle) sequence.item( 0 );
                    XSTerm geomTerm = particle2.getTerm();
                    switch ( geomTerm.getType() ) {
                    case ELEMENT_DECLARATION: {
                        XSElementDeclaration elementDecl2 = (XSElementDeclaration) geomTerm;
                        // min occurs check should be done, in regards to the xlinking.
                        int maxOccurs2 = particle2.getMaxOccursUnbounded() ? -1 : particle2.getMaxOccurs();
                        if ( maxOccurs2 > 1 ) {
                            LOG.debug( "Only single geometries are currently supported." );
                            return null;
                        }
                        QName elementName = new QName( elementDecl2.getNamespace(), elementDecl2.getName() );
                        if ( geomElementNames.contains( elementName ) ) {
                            LOG.trace( "Identified a geometry property." );
                            GeometryType geometryType = getGeometryType( elementName );
                            return new GeometryPropertyType( ptName, minOccurs, maxOccurs, elementDecl,
                                                             ptSubstitutions, geometryType,
                                                             CoordinateDimension.DIM_2_OR_3, BOTH );
                        }
                    }
                    case WILDCARD: {
                        LOG.debug( "Unhandled particle: WILDCARD" );
                        break;
                    }
                    case MODEL_GROUP: {
                        // more then one kind of geometries allowed
                        XSModelGroup geomModelGroup = (XSModelGroup) geomTerm;
                        switch ( geomModelGroup.getType() ) {
                        case COMPOSITOR_ALL: {
                            // all geometries?, lets make it a custom property
                            LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
                            break;
                        }
                        case COMPOSITOR_CHOICE: {
                            XSObjectList geomChoice = geomModelGroup.getParticles();
                            int length = geomChoice.getLength();
                            Set<GeometryType> allowedTypes = new HashSet<GeometryType>();
                            for ( int i = 0; i < length; ++i ) {
                                XSParticle geomChoiceParticle = (XSParticle) sequence.item( i );
                                XSTerm geomChoiceTerm = geomChoiceParticle.getTerm();
                                if ( geomChoiceTerm.getType() == ELEMENT_DECLARATION ) {
                                    // other types are not supported
                                    XSElementDeclaration geomChoiceElement = (XSElementDeclaration) geomChoiceTerm;
                                    // min occurs check should be done, in regards to the xlinking.
                                    int minOccurs3 = geomChoiceParticle.getMinOccurs();
                                    int maxOccurs3 = geomChoiceParticle.getMaxOccursUnbounded() ? -1
                                                                                               : particle2.getMaxOccurs();
                                    if ( maxOccurs3 > 1 ) {
                                        LOG.warn( "Only single geometries are currently supported, ignoring in choice." );
                                        // return null;
                                    }
                                    QName elementName = new QName( geomChoiceElement.getNamespace(),
                                                                   geomChoiceElement.getName() );
                                    if ( geomElementNames.contains( elementName ) ) {
                                        LOG.trace( "Identified a geometry property." );
                                        GeometryType geometryType = getGeometryType( elementName );
                                        allowedTypes.add( geometryType );
                                    } else {
                                        LOG.debug( "Unknown geometry type '" + elementName + "'." );
                                    }
                                } else {
                                    LOG.warn( "Unsupported type particle type." );
                                }
                            }
                            if ( !allowedTypes.isEmpty() ) {
                                return new GeometryPropertyType( ptName, minOccurs, maxOccurs, elementDecl,
                                                                 ptSubstitutions, allowedTypes,
                                                                 CoordinateDimension.DIM_2_OR_3, BOTH );
                            }
                            break;
                        }
                        case COMPOSITOR_SEQUENCE: {
                            // sequence of geometries?, lets make it a custom property
                            LOG.debug( "Unhandled model group: COMPOSITOR_SEQUENCE" );
                            break;
                        }
                        }
                        LOG.debug( "Unhandled particle: MODEL_GROUP" );
                        break;
                    }
                    }
                    break;
                }
                default: {
                    assert false;
                }
                }
                break;
            }
            case WILDCARD: {
                LOG.debug( "Unhandled particle: WILDCARD" );
                break;
            }
            case ELEMENT_DECLARATION: {
                LOG.debug( "Unhandled particle: ELEMENT_DECLARATION" );
                break;
            }
            default: {
                assert false;
            }
            }
            break;
        }
        }
        return null;
    }

    private GeometryType getGeometryType( QName gmlGeometryName ) {
        String localPart = gmlGeometryName.getLocalPart();
        GeometryType result = GeometryType.GEOMETRY;
        try {
            result = GeometryType.fromGMLTypeName( localPart );
        } catch ( Exception e ) {
            LOG.debug( "Unmappable geometry type: " + gmlGeometryName.toString()
                       + " (currently not supported by geometry model)" );
        }
        LOG.trace( "Mapping '" + gmlGeometryName + "' -> " + result );
        return result;
    }

    public GMLPropertySemantics getTimeSlicePropertySemantics( XSElementDeclaration elDecl ) {
        GMLPropertySemantics semantics = derivePropertySemantics( elDecl );
        if ( semantics == null ) {
            return null;
        }
        XSElementDeclaration valueEl = semantics.getValueElDecl();
        if ( valueEl != null ) {
            QName valueElName = new QName( valueEl.getNamespace(), valueEl.getName() );
            if ( timeSliceElementNames.contains( valueElName ) ) {
                return semantics;
            }
        }
        return null;
    }

    /**
     * Determines the generic GML property type interpretation for the given {@link XSElementDeclaration}.
     *
     * @param elDecl
     *            element declaration, must not be <code>null</code>
     * @return GML property type semantics, or <code>null</code> if the element declaration cannot be interpreted as a
     *         GML property type
     */
    public GMLPropertySemantics derivePropertySemantics( XSElementDeclaration elDecl ) {

        if ( !( elDecl.getTypeDefinition() instanceof XSComplexTypeDefinition ) ) {
            return null;
        }

        QName ptName = new QName( elDecl.getNamespace(), elDecl.getName() );
        LOG.trace( "Checking if element declaration '" + ptName + "' defines a complex-valued GML property type." );

        XSComplexTypeDefinition typeDef = (XSComplexTypeDefinition) elDecl.getTypeDefinition();
        boolean allowsXlink = allowsXLink( typeDef );

        XSElementDeclaration valueElementDeclFromAnnotations = determineAnnotationDefinedValueElement( elDecl );
        XSElementDeclaration valueElementDeclFromModelGroup = determineModelGroupDefinedValueElement( typeDef );

        if ( valueElementDeclFromModelGroup == null && valueElementDeclFromAnnotations != null ) {
            ValueRepresentation allowedRepresentation = REMOTE;
            return new GMLPropertySemantics( elDecl, valueElementDeclFromAnnotations, allowedRepresentation );
        }
        if ( valueElementDeclFromModelGroup != null && valueElementDeclFromAnnotations == null ) {
            ValueRepresentation allowedRepresentation = INLINE;
            if ( allowsXlink ) {
                allowedRepresentation = BOTH;
            }
            return new GMLPropertySemantics( elDecl, valueElementDeclFromModelGroup, allowedRepresentation );
        }
        if ( valueElementDeclFromModelGroup != null && valueElementDeclFromAnnotations != null ) {
            ValueRepresentation allowedRepresentation = INLINE;
            if ( allowsXlink ) {
                allowedRepresentation = BOTH;
            }
            return new GMLPropertySemantics( elDecl, valueElementDeclFromModelGroup, allowedRepresentation );
        }
        return null;
    }

    /**
     * Returns the properties of the given complex type, in order.
     *
     * @param type
     *            complex type definition, must not be <code>null</code>
     * @return element declarations of the properties, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the complex type definition does not follow the requirements to define a sequence of properties
     */
    public List<XSElementDeclaration> getProperties( final XSComplexTypeDefinition type ) {
        final List<XSElementDeclaration> childTerms = new ArrayList<XSElementDeclaration>();
        addChildElementDecls( type.getParticle(), childTerms );
        return childTerms;
    }

    private void addChildElementDecls( final XSParticle particle, final List<XSElementDeclaration> propDecls ) {
        if ( particle != null ) {
            final XSTerm term = particle.getTerm();
            if ( term instanceof XSElementDeclaration ) {
                propDecls.add( (XSElementDeclaration) term );
            } else if ( term instanceof XSModelGroup ) {
                final XSModelGroup modelGroup = (XSModelGroup) term;
                if ( modelGroup.getCompositor() == COMPOSITOR_SEQUENCE ) {
                    final XSObjectList particles = modelGroup.getParticles();
                    for ( int i = 0; i < particles.getLength(); i++ ) {
                        addChildElementDecls( (XSParticle) particles.item( i ), propDecls );
                    }
                } else {
                    final String msg = "Choice/All composition is not supported in a property list definition.";
                    throw new IllegalArgumentException( msg );
                }
            } else if ( term instanceof XSWildcard ) {
                throw new IllegalArgumentException( "Wildcards are not possible in a property list definition." );
            }
        }
    }

    private XSElementDeclaration determineAnnotationDefinedValueElement( XSElementDeclaration elDecl ) {
        XMLAdapter annotationXML = null;
        XSObjectList annotations = elDecl.getAnnotations();
        QName targetElement = null;
        if ( annotations.getLength() > 0 ) {
            XSAnnotation annotation = (XSAnnotation) annotations.item( 0 );
            String s = annotation.getAnnotationString();
            annotationXML = new XMLAdapter( new StringReader( s ) );
            targetElement = determineTargetElementGml32( annotationXML );
            if ( targetElement == null ) {
                targetElement = determineTargetElementXGml( annotationXML );
            }
            if ( targetElement == null ) {
                targetElement = determineTargetElementAdv( annotationXML );
            }
        }
        if ( targetElement != null ) {
            return getElementDecl( targetElement );
        }
        return null;
    }

    private QName determineTargetElementGml32( XMLAdapter annotationXML ) {
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
        nsContext.addNamespace( "gml", GML3_2_NS );
        QName refElement = annotationXML.getNodeAsQName( annotationXML.getRootElement(),
                                                         new XPath( "xs:appinfo/gml:targetElement/text()", nsContext ),
                                                         null );
        if ( refElement != null ) {
            LOG.trace( "Identified a target element annotation (GML 3.2 style)." );
        }
        return refElement;
    }

    private QName determineTargetElementXGml( XMLAdapter annotationXML ) {
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
        QName refElement = annotationXML.getNodeAsQName( annotationXML.getRootElement(),
                                                         new XPath(
                                                                    "xs:appinfo[@source='urn:x-gml:targetElement']/text()",
                                                                    nsContext ), null );
        if ( refElement != null ) {
            LOG.trace( "Identified a target element annotation (urn:x-gml style)." );
        }
        return refElement;
    }

    private QName determineTargetElementAdv( XMLAdapter annotationXML ) {
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "xs", CommonNamespaces.XSNS );
        nsContext.addNamespace( "adv", "http://www.adv-online.de/nas" );
        QName refElement = annotationXML.getNodeAsQName( annotationXML.getRootElement(),
                                                         new XPath( "xs:appinfo/adv:referenziertesElement/text()",
                                                                    nsContext ), null );
        if ( refElement != null ) {
            LOG.trace( "Identified a target element annotation (adv style)." );
        }
        return refElement;
    }

    private XSElementDeclaration determineModelGroupDefinedValueElement( XSComplexTypeDefinition typeDef ) {
        switch ( typeDef.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            LOG.trace( "CONTENTTYPE_ELEMENT" );
            XSParticle particle = typeDef.getParticle();
            XSTerm term = particle.getTerm();
            switch ( term.getType() ) {
            case XSConstants.MODEL_GROUP: {
                XSModelGroup modelGroup = (XSModelGroup) term;
                switch ( modelGroup.getCompositor() ) {
                case XSModelGroup.COMPOSITOR_ALL: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
                    break;
                }
                case XSModelGroup.COMPOSITOR_CHOICE:
                case XSModelGroup.COMPOSITOR_SEQUENCE: {
                    LOG.trace( "Found sequence / choice." );
                    XSObjectList sequence = modelGroup.getParticles();
                    if ( sequence.getLength() != 1 ) {
                        LOG.trace( "Length = '" + sequence.getLength() + "' -> cannot be a property declaration." );
                        return null;
                    }
                    XSParticle particle2 = (XSParticle) sequence.item( 0 );
                    switch ( particle2.getTerm().getType() ) {
                    case XSConstants.ELEMENT_DECLARATION: {
                        return (XSElementDeclaration) particle2.getTerm();
                    }
                    case XSConstants.WILDCARD: {
                        LOG.debug( "Unhandled particle: WILDCARD" );
                        break;
                    }
                    case XSConstants.MODEL_GROUP: {
                        LOG.debug( "Unhandled particle: MODEL_GROUP" );
                        break;
                    }
                    }
                    break;
                }
                default: {
                    assert false;
                }
                }
                break;
            }
            case XSConstants.WILDCARD: {
                LOG.debug( "Unhandled particle: WILDCARD" );
                break;
            }
            case XSConstants.ELEMENT_DECLARATION: {
                LOG.debug( "Unhandled particle: ELEMENT_DECLARATION" );
                break;
            }
            default: {
                assert false;
            }
            }
            break;
        }
        default: {
            LOG.debug( "Unhandled content type in determineModelGroupDefinedValueElement(...) encountered." );
        }
        }
        return null;
    }

}
