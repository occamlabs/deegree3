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
package org.deegree.feature.persistence.sql;

import static org.deegree.commons.utils.JDBCUtils.closeQuietly;
import static org.deegree.feature.Features.findFeaturesAndGeometries;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.BBoxTracker;
import org.deegree.feature.persistence.FeatureInspector;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.insert.FeatureRow;
import org.deegree.feature.persistence.sql.insert.InsertRowManager;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometries;
import org.deegree.geometry.Geometry;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.deegree.gml.utils.GmlReferenceCollector;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.sqldialect.filter.DBField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link Feature} inserts for the {@link SqlFeatureStoreTransaction}.
 * 
 * @see SqlFeatureStoreTransaction
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class SqlFeatureStoreInsertHandler {

    private static final Logger LOG = LoggerFactory.getLogger( SqlFeatureStoreInsertHandler.class );

    private final SQLFeatureStore fs;

    private final Connection conn;

    private final List<FeatureInspector> inspectors;

    private final BBoxTracker bboxTracker;

    private final BlobMapping blobMapping;

    private ParticleConverter<Geometry> blobGeomConverter;

    private final QName GML_IDENTIFIER_NAME;

    SqlFeatureStoreInsertHandler( SQLFeatureStore fs, Connection conn, List<FeatureInspector> inspectors,
                                  BBoxTracker bboxTracker ) {
        this.fs = fs;
        this.conn = conn;
        this.inspectors = inspectors;
        this.bboxTracker = bboxTracker;
        blobMapping = fs.getSchema().getBlobMapping();
        if ( blobMapping != null ) {
            DBField bboxColumn = new DBField( blobMapping.getBBoxColumn() );
            GeometryStorageParams geometryParams = new GeometryStorageParams( blobMapping.getCRS(), null, DIM_2 );
            GeometryMapping blobGeomMapping = new GeometryMapping( null, true, bboxColumn, GeometryType.GEOMETRY,
                                                                   geometryParams, null );
            blobGeomConverter = fs.getGeometryConverter( blobGeomMapping );
        }
        GML_IDENTIFIER_NAME = getGmlIdentifierName( fs.getSchema().getGMLSchema() );
    }

    private QName getGmlIdentifierName( GMLSchemaInfoSet gmlSchema ) {
        String gmlNamespace = GML_32.getNamespace();
        if ( gmlSchema != null ) {
            gmlNamespace = gmlSchema.getVersion().getNamespace();
        }
        return new QName( gmlNamespace, "identifier" );
    }

    List<String> performInsert( FeatureInputStream features, IDGenMode mode )
                            throws FeatureStoreException {
        if ( mode != USE_EXISTING || blobMapping == null ) {
            FeatureCollection fc = features.toCollection();
            GmlReferenceCollector referenceCollector = new GmlReferenceCollector();
            for ( Feature feature : fc ) {
                referenceCollector.add( feature );
            }
            try {
                referenceCollector.resolveLocalRefs();
            } catch ( ReferenceResolvingException e ) {
                throw new FeatureStoreException( e.getMessage() );
            }
            return performInsert( fc, mode );
        }
        LOG.debug( "performInsert (streaming)" );
        return performInsertBlobUseExisting( features, blobMapping );
    }

    List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {

        LOG.debug( "performInsert (non-streaming)" );

        Set<Geometry> geometries = new LinkedHashSet<Geometry>();
        Set<Feature> features = new LinkedHashSet<Feature>();
        Set<String> fids = new LinkedHashSet<String>();
        Set<String> gids = new LinkedHashSet<String>();
        for ( Feature member : fc ) {
            findFeaturesAndGeometries( member, geometries, features, fids, gids );
        }

        LOG.debug( features.size() + " features / " + geometries.size() + " geometries" );

        for ( FeatureInspector inspector : inspectors ) {
            for ( Feature f : features ) {
                // TODO cope with inspectors that return a different instance
                inspector.inspect( f );
            }
        }

        long begin = System.currentTimeMillis();

        try {
            PreparedStatement blobInsertStmt = null;
            if ( blobMapping != null ) {
                switch ( mode ) {
                case GENERATE_NEW: {
                    // TODO don't change incoming features / geometries
                    for ( Feature feature : features ) {
                        String newFid = "FEATURE_" + generateNewId();
                        String oldFid = feature.getId();
                        if ( oldFid != null ) {
                            fids.remove( oldFid );
                        }
                        fids.add( newFid );
                        feature.setId( newFid );
                    }
                    for ( Geometry geometry : geometries ) {
                        String newGid = "GEOMETRY_" + generateNewId();
                        String oldGid = geometry.getId();
                        if ( oldGid != null ) {
                            gids.remove( oldGid );
                        }
                        gids.add( newGid );
                        geometry.setId( newGid );
                    }
                    break;
                }
                case REPLACE_DUPLICATE: {
                    throw new FeatureStoreException( "REPLACE_DUPLICATE is not available yet." );
                }
                case USE_EXISTING: {
                    // TODO don't change incoming features / geometries
                    for ( Feature feature : features ) {
                        if ( feature.getId() == null ) {
                            String newFid = "FEATURE_" + generateNewId();
                            feature.setId( newFid );
                            fids.add( newFid );
                        }
                    }

                    for ( Geometry geometry : geometries ) {
                        if ( geometry.getId() == null ) {
                            String newGid = "GEOMETRY_" + generateNewId();
                            geometry.setId( newGid );
                            gids.add( newGid );
                        }
                    }
                    break;
                }
                }
                StringBuilder sql = new StringBuilder( "INSERT INTO " );
                sql.append( blobMapping.getTable() );
                sql.append( " (" );
                sql.append( blobMapping.getGMLIdColumn() );
                sql.append( "," );
                sql.append( blobMapping.getTypeColumn() );
                sql.append( "," );
                sql.append( blobMapping.getDataColumn() );
                sql.append( "," );
                sql.append( blobMapping.getBBoxColumn() );
                sql.append( ") VALUES(?,?,?," );
                sql.append( blobGeomConverter.getSetSnippet( null ) );
                sql.append( ")" );
                LOG.debug( "Inserting: {}", sql );
                blobInsertStmt = conn.prepareStatement( sql.toString() );
                for ( Feature feature : features ) {
                    if ( blobInsertStmt != null ) {
                        insertGmlObject( blobInsertStmt, feature );
                    }
                    FeatureTypeMapping ftMapping = fs.getMapping( feature.getName() );
                    if ( ftMapping != null ) {
                        throw new UnsupportedOperationException();
                    }
                    ICRS storageSrs = blobMapping.getCRS();
                    bboxTracker.insert( feature, storageSrs );
                }
                if ( blobInsertStmt != null ) {
                    blobInsertStmt.close();
                }
            } else {
                // pure relational mode
                List<FeatureRow> idAssignments = new ArrayList<FeatureRow>();
                InsertRowManager insertManager = new InsertRowManager( fs, conn, mode );
                for ( Feature feature : features ) {
                    FeatureTypeMapping ftMapping = fs.getMapping( feature.getName() );
                    if ( ftMapping == null ) {
                        throw new FeatureStoreException( "Cannot insert feature of type '" + feature.getName()
                                                         + "'. No mapping defined and BLOB mode is off." );
                    }
                    idAssignments.add( insertManager.insertFeature( feature, ftMapping ) );
                    Pair<TableName, GeometryMapping> mapping = ftMapping.getDefaultGeometryMapping();
                    if ( mapping != null ) {
                        ICRS storageSrs = mapping.second.getCRS();
                        bboxTracker.insert( feature, storageSrs );
                    }
                }
                if ( insertManager.getDelayedRows() != 0 ) {
                    String msg = "After insertion, " + insertManager.getDelayedRows()
                                            + " delayed rows left uninserted. Probably a cyclic key constraint blocks insertion.";
                    throw new RuntimeException( msg );
                }
                // TODO why is this necessary?
                fids.clear();
                for ( FeatureRow assignment : idAssignments ) {
                    fids.add( assignment.getNewId() );
                }
            }
        } catch ( Throwable t ) {
            String msg = "Error inserting feature: " + t.getMessage();
            LOG.error( msg );
            LOG.trace( "Stack trace:", t );
            throw new FeatureStoreException( msg, t );
        }

        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Insertion of " + features.size() + " features: " + elapsed + " [ms]" );
        return new ArrayList<String>( fids );
    }

    private String generateNewId() {
        return UUID.randomUUID().toString();
    }

    private List<String> performInsertBlobUseExisting( FeatureInputStream features, BlobMapping blobMapping )
                            throws FeatureStoreException {
        boolean useGmlIdentifiersTable = blobMapping.getGmlIdentifiersTable() != null;
        PreparedStatement gmlObjectInsertStmt = null;
        PreparedStatement gmlIdentifierInsertStmt = null;
        List<String> fids = new ArrayList<String>();
        try {
            gmlObjectInsertStmt = getPreparedStatementInsertGmlObject( blobMapping );
            if ( useGmlIdentifiersTable ) {
                gmlIdentifierInsertStmt = getPreparedStatementInsertGmlIdentifier( blobMapping );
            }
            for ( Feature feature : features ) {
                for ( FeatureInspector inspector : inspectors ) {
                    feature = inspector.inspect( feature );
                }
                FeatureTypeMapping ftMapping = fs.getMapping( feature.getName() );
                if ( ftMapping != null ) {
                    String msg = "Hybrid mode insert not implemented yet.";
                    throw new UnsupportedOperationException( msg );
                }
                fids.add( feature.getId() );
                insertGmlObject( gmlObjectInsertStmt, feature );
                if ( useGmlIdentifiersTable ) {
                    insertGmlIdentifier( gmlIdentifierInsertStmt, feature );
                }
                ICRS storageSrs = blobMapping.getCRS();
                bboxTracker.insert( feature, storageSrs );
            }
        } catch ( SQLException e ) {
            String msg = "Error inserting feature (BLOB-mode): " + e.getMessage();
            throw new FeatureStoreException( msg );
        } finally {
            closeQuietly( gmlObjectInsertStmt );
            closeQuietly( gmlIdentifierInsertStmt );
        }
        return fids;
    }

    private PreparedStatement getPreparedStatementInsertGmlObject( BlobMapping blobMapping )
                            throws SQLException {
        StringBuilder sql = new StringBuilder( "INSERT INTO " );
        sql.append( blobMapping.getTable() );
        sql.append( " (" );
        sql.append( blobMapping.getGMLIdColumn() );
        sql.append( "," );
        sql.append( blobMapping.getTypeColumn() );
        sql.append( "," );
        sql.append( blobMapping.getDataColumn() );
        sql.append( "," );
        sql.append( blobMapping.getBBoxColumn() );
        sql.append( ") VALUES(?,?,?," );
        sql.append( blobGeomConverter.getSetSnippet( null ) );
        sql.append( ")" );
        return conn.prepareStatement( sql.toString() );
    }

    private void insertGmlObject( PreparedStatement stmt, Feature feature )
                            throws SQLException, FeatureStoreException {

        LOG.debug( "Inserting feature with id '" + feature.getId() + "' (BLOB)" );
        if ( fs.getSchema().getFeatureType( feature.getName() ) == null ) {
            throw new FeatureStoreException( "Cannot insert feature '" + feature.getName()
                                             + "': feature type is not served by this feature store." );
        }
        ICRS crs = blobMapping.getCRS();
        stmt.setString( 1, feature.getId() );
        stmt.setShort( 2, fs.getFtId( feature.getName() ) );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            BlobCodec codec = fs.getSchema().getBlobMapping().getCodec();
            codec.encode( feature, fs.getNamespaceContext(), bos, crs );
        } catch ( Exception e ) {
            String msg = "Error encoding feature for BLOB: " + e.getMessage();
            LOG.error( msg );
            LOG.trace( "Stack trace:", e );
            throw new SQLException( msg, e );
        }
        byte[] bytes = bos.toByteArray();
        stmt.setBytes( 3, bytes );
        LOG.debug( "Feature blob size: " + bytes.length );
        Geometry bboxGeom = null;
        try {
            Envelope bbox = feature.getEnvelope();
            if ( bbox != null ) {
                bboxGeom = Geometries.getAsGeometry( bbox );
            }
        } catch ( Exception e ) {
            LOG.warn( "Unable to determine bbox of feature with id '" + feature.getId() + "': " + e.getMessage() );
        }
        blobGeomConverter.setParticle( stmt, bboxGeom, 4 );
        stmt.execute();
    }

    private PreparedStatement getPreparedStatementInsertGmlIdentifier( BlobMapping blobMapping )
                            throws SQLException {
        StringBuilder sql = new StringBuilder( "INSERT INTO " );
        sql.append( blobMapping.getGmlIdentifiersTable() );
        sql.append( " (" );
        sql.append( "gml_id" );
        sql.append( "," );
        sql.append( "gml_identifier" );
        sql.append( "," );
        sql.append( "codespace" );
        sql.append( ") VALUES(?,?,?)" );
        return conn.prepareStatement( sql.toString() );
    }

    private void insertGmlIdentifier( PreparedStatement gmlIdentifierInsertStmt, Feature feature )
                            throws SQLException {
        List<Property> gmlIdentifierProps = feature.getProperties( GML_IDENTIFIER_NAME );
        String fid = feature.getId();
        for ( Property gmlIdentifierProp : gmlIdentifierProps ) {
            String gmlIdentifier = getIdentifier( gmlIdentifierProp );
            String codespace = null;
            PrimitiveValue codeSpaceValue = gmlIdentifierProp.getAttributes().get( new QName( "codeSpace" ) );
            if ( codeSpaceValue != null ) {
                codespace = codeSpaceValue.toString();
            }
            insertGmlIdentifier( gmlIdentifierInsertStmt, fid, gmlIdentifier, codespace );
        }
    }

    private String getIdentifier( Property gmlIdentifierProp ) {
        List<TypedObjectNode> children = gmlIdentifierProp.getChildren();
        for ( TypedObjectNode child : children ) {
            if ( child instanceof PrimitiveValue ) {
                return "" + child;
            }
        }
        return null;
    }

    private void insertGmlIdentifier( PreparedStatement gmlIdentifierInsertStmt, String fid, String gmlIdentifier,
                                      String codespace )
                                                              throws SQLException {
        gmlIdentifierInsertStmt.setString( 1, fid );
        gmlIdentifierInsertStmt.setString( 2, gmlIdentifier );
        gmlIdentifierInsertStmt.setString( 3, codespace );
        gmlIdentifierInsertStmt.execute();
    }
}
