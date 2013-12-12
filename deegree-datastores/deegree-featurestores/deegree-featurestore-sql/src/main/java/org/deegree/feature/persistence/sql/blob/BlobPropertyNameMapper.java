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
package org.deegree.feature.persistence.sql.blob;

import static java.util.Collections.singletonList;
import static org.apache.xerces.impl.xpath.XPath.Axis.ATTRIBUTE;
import static org.apache.xerces.impl.xpath.XPath.Axis.CHILD;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.gml.GMLVersion.GML_32;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.Join;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.PropertyNameMapping;
import org.deegree.sqldialect.filter.TableAliasManager;
import org.deegree.sqldialect.filter.UnmappableException;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;

/**
 * {@link PropertyNameMapper} for the BLOB mode of the {@link SQLFeatureStore}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class BlobPropertyNameMapper implements PropertyNameMapper {

    private final SQLDialect dialect;

    private final SQLFeatureStore fs;

    private final BlobMapping blobMapping;

    private final QName gmlIdentifierName;

    /**
     * Creates a new {@link BlobPropertyNameMapper} instance.
     * 
     * @param fs
     * @param dialect
     * @param blobMapping
     */
    public BlobPropertyNameMapper( SQLFeatureStore fs, SQLDialect dialect, BlobMapping blobMapping ) {
        this.fs = fs;
        this.dialect = dialect;
        this.blobMapping = blobMapping;
        this.gmlIdentifierName = new QName( GML_32.getNamespace(), "identifier" );
    }

    @Override
    public PropertyNameMapping getMapping( ValueReference propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException {
        if ( blobMapping.getGmlIdentifiersTable() != null ) {
            Expr xpath = propName.getAsXPath();
            if ( xpath != null && xpath instanceof LocationPath ) {
                LocationPath locationPath = (LocationPath) xpath;
                List<?> steps = locationPath.getSteps();
                Object step = steps.remove( 0 );
                if ( isNameStep( step, gmlIdentifierName, CHILD, propName.getNsContext() ) ) {
                    if ( steps.isEmpty() ) {
                        return getGmlIdentifierMapping( aliasManager );
                    }
                    step = steps.remove( 0 );
                    if ( isNameStep( step, new QName( "codeSpace" ), ATTRIBUTE, propName.getNsContext() ) ) {
                        return getGmlIdentifierCodeSpaceMapping( aliasManager );
                    }
                    return getGmlIdentifierMapping( aliasManager );
                }
            }
        }
        throw new UnmappableException( "Cannot map '" + propName + "'." );
    }

    private boolean isNameStep( Object o, QName name, int axis, NamespaceBindings nsBindings ) {
        if ( !( o instanceof NameStep ) ) {
            return false;
        }
        NameStep step = (NameStep) o;
        if ( step.getAxis() != axis ) {
            return false;
        }
        return name.equals( getQName( step, nsBindings ) );
    }

    private QName getQName( NameStep step, NamespaceBindings nsBindings ) {
        String prefix = step.getPrefix();
        QName qName;
        if ( prefix.isEmpty() ) {
            qName = new QName( step.getLocalName() );
        } else {
            String ns = nsBindings.translateNamespacePrefixToUri( prefix );
            qName = new QName( ns, step.getLocalName(), prefix );
        }
        return qName;
    }

    @Override
    public PropertyNameMapping getSpatialMapping( ValueReference propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException {
        return getBoundedByMapping( aliasManager );
    }

    private PropertyNameMapping getBoundedByMapping( TableAliasManager aliasManager ) {
        GeometryStorageParams geometryParams = new GeometryStorageParams( blobMapping.getCRS(),
                                                                          dialect.getUndefinedSrid(), DIM_2 );
        GeometryMapping bboxMapping = new GeometryMapping( null, false, new DBField( blobMapping.getBBoxColumn() ),
                                                           GEOMETRY, geometryParams, null );
        return new PropertyNameMapping( fs.getGeometryConverter( bboxMapping ), null, blobMapping.getBBoxColumn(),
                                        aliasManager.getRootTableAlias() );
    }

    private PropertyNameMapping getGmlIdentifierMapping( TableAliasManager aliasManager ) {
        String column = "gml_identifier";
        ParticleConverter<?> converter = dialect.getPrimitiveConverter( column, new PrimitiveType( STRING ) );
        String tableAlias = aliasManager.generateNew();
        List<Join> joins = getJoinsToGmlIdentifierTable( aliasManager, tableAlias );
        return new PropertyNameMapping( converter, joins, column, tableAlias );
    }

    private PropertyNameMapping getGmlIdentifierCodeSpaceMapping( TableAliasManager aliasManager ) {
        String column = "codespace";
        ParticleConverter<?> converter = dialect.getPrimitiveConverter( column, new PrimitiveType( STRING ) );
        String tableAlias = aliasManager.generateNew();
        List<Join> joins = getJoinsToGmlIdentifierTable( aliasManager, tableAlias );
        return new PropertyNameMapping( converter, joins, column, tableAlias );
    }

    private List<Join> getJoinsToGmlIdentifierTable( TableAliasManager aliasManager, String tableAlias ) {
        Join join = new Join( blobMapping.getTable().getName(), aliasManager.getRootTableAlias(),
                              blobMapping.getGMLIdColumn(), "gml_identifiers", tableAlias, "gml_id" );
        return singletonList( join );
    }
}
