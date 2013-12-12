//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.feature.persistence.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.tom.sql.SQLValueMangler;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.BBoxTracker;
import org.deegree.feature.persistence.FeatureInspector;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.sql.insert.InsertRowManager;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.ResourceId;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.UpdateAction;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreTransaction} implementation for {@link SQLFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SQLFeatureStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( SQLFeatureStoreTransaction.class );

    private final SQLFeatureStore fs;

    private final MappedAppSchema schema;

    private final BlobMapping blobMapping;

    private final Connection conn;

    private final BBoxTracker bboxTracker;

    private final SqlFeatureStoreInsertHandler insertHandler;

    /**
     * Creates a new {@link SQLFeatureStoreTransaction} instance.
     * 
     * @param store
     *            corresponding feature store instance, must not be <code>null</code>
     * @param conn
     *            JDBC connection associated with the transaction, must not be <code>null</code> and have
     *            <code>autocommit</code> set to <code>false</code>
     * @param schema
     *            application schema with mapping information, must not be <code>null</code>
     * @param inspectors
     *            feature inspectors, must not be <code>null</code>
     */
    SQLFeatureStoreTransaction( SQLFeatureStore store, Connection conn, MappedAppSchema schema,
                                List<FeatureInspector> inspectors ) {
        this.fs = store;
        this.conn = conn;
        this.schema = schema;
        blobMapping = schema.getBlobMapping();
        this.bboxTracker = new BBoxTracker();
        insertHandler = new SqlFeatureStoreInsertHandler( fs, conn, inspectors, bboxTracker );
    }

    @Override
    public void commit()
                            throws FeatureStoreException {

        LOG.debug( "Committing transaction." );
        try {
            conn.commit();
            updateBBoxCache();
        } catch ( Throwable t ) {
            LOG.debug( t.getMessage(), t );
            throw new FeatureStoreException( "Unable to commit SQL transaction: " + t.getMessage() );
        } finally {
            try {
                conn.close();
            } catch ( SQLException e ) {
                LOG.error( "Error closing connection/removing it from the pool." );
            }
        }
    }

    private void updateBBoxCache()
                            throws FeatureStoreException {

        // beware of concurrent transactions
        synchronized ( fs ) {

            Set<QName> recalcFTs = bboxTracker.getRecalcFeatureTypes();
            Map<QName, Envelope> ftNamesToIncreaseBBoxes = bboxTracker.getIncreaseBBoxes();

            // handle bbox increases
            for ( Entry<QName, Envelope> ftNameToIncreaseBBox : ftNamesToIncreaseBBoxes.entrySet() ) {
                QName ftName = ftNameToIncreaseBBox.getKey();
                Envelope bbox = null;
                if ( fs.getBBoxCache().contains( ftName ) ) {
                    bbox = ftNameToIncreaseBBox.getValue();
                }
                if ( bbox != null ) {
                    Envelope oldBbox = fs.getBBoxCache().get( ftName );
                    if ( oldBbox != null ) {
                        bbox = oldBbox.merge( bbox );
                    }
                    fs.getBBoxCache().set( ftName, bbox );
                }
            }

            // TODO configuration switch for bbox recalculation strategy
            if ( !recalcFTs.isEmpty() ) {
                LOG.debug( "Full recalculation of feature type envelopes required. Delete 'bbox_cache.properties' if you need minimal envelopes." );
            }

            try {
                fs.getBBoxCache().persist();
            } catch ( Throwable t ) {
                LOG.error( "Unable to persist bbox cache: " + t.getMessage() );
            }
        }
    }

    @Override
    public void rollback()
                            throws FeatureStoreException {
        LOG.debug( "Performing rollback of transaction." );
        try {
            conn.rollback();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( "Unable to rollback SQL transaction: " + e.getMessage() );
        } finally {
            try {
                conn.close();
            } catch ( SQLException e ) {
                LOG.error( "Error closing connection/removing it from the pool." );
            }
        }
    }

    @Override
    public FeatureStore getStore() {
        return fs;
    }

    /**
     * Returns the underlying JDBC connection. Can be used for performing other operations in the same transaction
     * context.
     * 
     * @return the underlying JDBC connection, never <code>null</code>
     */
    public Connection getConnection() {
        return conn;
    }

    @Override
    public int performDelete( QName ftName, OperatorFilter filter, Lock lock )
                            throws FeatureStoreException {
        // TODO implement this more efficiently
        return performDelete( getIdFilter( ftName, filter ), lock );
    }

    @Override
    public int performDelete( IdFilter filter, Lock lock )
                            throws FeatureStoreException {
        int deleted = 0;
        if ( blobMapping != null ) {
            deleted = performDeleteBlob( filter, lock );
        } else {
            deleted = performDeleteRelational( filter, lock );
        }

        // TODO improve this
        for ( FeatureType ft : schema.getFeatureTypes( null, false, false ) ) {
            bboxTracker.delete( ft.getName() );
        }

        return deleted;
    }

    private int performDeleteBlob( IdFilter filter, Lock lock )
                            throws FeatureStoreException {
        int deleted = 0;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( "DELETE FROM " + blobMapping.getTable() + " WHERE "
                                    + blobMapping.getGMLIdColumn() + "=?" );
            for ( ResourceId id : filter.getSelectedIds() ) {
                stmt.setString( 1, id.getRid() );
                stmt.addBatch();
                if ( fs.getCache() != null ) {
                    fs.getCache().remove( id.getRid() );
                }
            }
            int[] deletes = stmt.executeBatch();
            for ( int noDeleted : deletes ) {
                deleted += noDeleted;
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            JDBCUtils.close( stmt );
        }
        LOG.debug( "Deleted " + deleted + " features." );
        return deleted;
    }

    private int performDeleteRelational( IdFilter filter, Lock lock )
                            throws FeatureStoreException {

        int deleted = 0;
        for ( ResourceId id : filter.getSelectedIds() ) {
            LOG.debug( "Analyzing id: " + id.getRid() );
            IdAnalysis analysis = null;
            try {
                analysis = schema.analyzeId( id.getRid() );
                LOG.debug( "Analysis: " + analysis );
                if ( !schema.getKeyDependencies().getDeleteCascadingByDB() ) {
                    LOG.debug( "Deleting joined rows manually." );
                    deleteJoinedRows( analysis );
                } else {
                    LOG.debug( "Depending on database to delete joined rows automatically." );
                }
                deleted += deleteFeatureRow( analysis );
            } catch ( IllegalArgumentException e ) {
                throw new FeatureStoreException( "Unable to determine feature type for id '" + id + "'." );
            }
        }
        return deleted;
    }

    private int deleteFeatureRow( IdAnalysis analysis )
                            throws FeatureStoreException {
        int deleted = 0;
        FeatureTypeMapping ftMapping = schema.getFtMapping( analysis.getFeatureType().getName() );
        FIDMapping fidMapping = ftMapping.getFidMapping();
        PreparedStatement stmt = null;
        try {
            StringBuilder sql = new StringBuilder( "DELETE FROM " + ftMapping.getFtTable() + " WHERE " );
            sql.append( fidMapping.getColumns().get( 0 ).first );
            sql.append( "=?" );
            for ( int i = 1; i < fidMapping.getColumns().size(); i++ ) {
                sql.append( " AND " );
                sql.append( fidMapping.getColumns().get( i ) );
                sql.append( "=?" );
            }
            stmt = conn.prepareStatement( sql.toString() );

            int i = 1;
            for ( String fidKernel : analysis.getIdKernels() ) {
                PrimitiveType pt = new PrimitiveType( fidMapping.getColumns().get( i - 1 ).second );
                PrimitiveValue value = new PrimitiveValue( fidKernel, pt );
                Object sqlValue = SQLValueMangler.internalToSQL( value );
                stmt.setObject( i++, sqlValue );
            }
            LOG.debug( "Executing: " + stmt );
            deleted += stmt.executeUpdate();
        } catch ( Throwable e ) {
            LOG.error( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            JDBCUtils.close( stmt );
        }
        return deleted;
    }

    /**
     * Deletes the joined rows for the specified feature id.
     * <p>
     * Deletes all joined rows and transitive join rows, but stops at joins to subfeature tables.
     * </p>
     * 
     * @param fid
     *            feature id, must not be <code>null</code>
     * @throws FeatureStoreException
     */
    private void deleteJoinedRows( IdAnalysis fid )
                            throws FeatureStoreException {

        Map<SQLIdentifier, Object> keyColsToValues = new HashMap<SQLIdentifier, Object>();

        FeatureTypeMapping ftMapping = schema.getFtMapping( fid.getFeatureType().getName() );

        // add values for feature id columns
        int i = 0;
        for ( Pair<SQLIdentifier, BaseType> fidColumns : ftMapping.getFidMapping().getColumns() ) {
            PrimitiveType pt = new PrimitiveType( fidColumns.second );
            PrimitiveValue value = new PrimitiveValue( fid.getIdKernels()[i], pt );
            Object sqlValue = SQLValueMangler.internalToSQL( value );
            keyColsToValues.put( fidColumns.first, sqlValue );
            i++;
        }

        // traverse mapping particles
        for ( Mapping particle : ftMapping.getMappings() ) {
            deleteJoinedRows( particle, keyColsToValues );
        }
    }

    private void deleteJoinedRows( Mapping particle, Map<SQLIdentifier, Object> keyColToValue )
                            throws FeatureStoreException {

        // TODO: After FeatureTypeJoin is introduced, rework this case (may allow joins)
        if ( particle instanceof FeatureMapping ) {
            return;
        }

        // determine and delete joined rows
        if ( particle.getJoinedTable() != null && !particle.getJoinedTable().isEmpty() ) {
            TableJoin tableJoin = particle.getJoinedTable().get( 0 );

            PreparedStatement stmt = null;
            try {
                StringBuilder sql = new StringBuilder( "SELECT " );
                boolean first = true;
                for ( SQLIdentifier selectColumn : tableJoin.getToColumns() ) {
                    if ( !first ) {
                        sql.append( ',' );
                    } else {
                        first = false;
                    }
                    sql.append( "X2." );
                    sql.append( selectColumn );
                }
                sql.append( " FROM " );
                sql.append( tableJoin.getFromTable() );
                sql.append( " X1," );
                sql.append( tableJoin.getToTable() );
                sql.append( " X2" );
                sql.append( " WHERE" );

                first = true;
                int i = 0;
                for ( SQLIdentifier fromColumn : tableJoin.getFromColumns() ) {
                    SQLIdentifier toColumn = tableJoin.getToColumns().get( i++ );
                    if ( !first ) {
                        sql.append( ',' );
                    } else {
                        first = false;
                    }
                    sql.append( " X1." );
                    sql.append( fromColumn );
                    sql.append( "=" );
                    sql.append( "X2." );
                    sql.append( toColumn );
                    first = false;
                }

                for ( Entry<SQLIdentifier, Object> joinKey : keyColToValue.entrySet() ) {
                    sql.append( " AND X1." );
                    sql.append( joinKey.getKey() );
                    sql.append( "=?" );
                    first = false;
                }

                stmt = conn.prepareStatement( sql.toString() );

                i = 1;
                for ( Entry<SQLIdentifier, Object> joinKey : keyColToValue.entrySet() ) {
                    stmt.setObject( i++, joinKey.getValue() );
                }
                LOG.debug( "Executing SELECT (following join): " + stmt );
                ResultSet rs = stmt.executeQuery();
                while ( rs.next() ) {
                    Map<SQLIdentifier, Object> joinKeyToValue = new HashMap<SQLIdentifier, Object>();
                    i = 1;
                    for ( SQLIdentifier toColumn : tableJoin.getToColumns() ) {
                        joinKeyToValue.put( toColumn, rs.getObject( i++ ) );
                    }
                    deleteJoinedRows( particle, tableJoin, joinKeyToValue );
                }
            } catch ( SQLException e ) {
                LOG.error( e.getMessage(), e );
                throw new FeatureStoreException( e.getMessage(), e );
            } finally {
                JDBCUtils.close( stmt );
            }
        } else {
            // process compound particle structure
            if ( particle instanceof CompoundMapping ) {
                CompoundMapping cm = (CompoundMapping) particle;
                for ( Mapping child : cm.getParticles() ) {
                    deleteJoinedRows( child, keyColToValue );
                }
            }
        }
    }

    private void deleteJoinedRows( Mapping particle, TableJoin tableJoin, Map<SQLIdentifier, Object> joinKeyColToValue )
                            throws FeatureStoreException {

        TableName joinTable = tableJoin.getToTable();

        if ( particle instanceof CompoundMapping ) {
            CompoundMapping cm = (CompoundMapping) particle;
            for ( Mapping child : cm.getParticles() ) {
                deleteJoinedRows( child, joinKeyColToValue );
            }
        }

        // DELETE join rows
        PreparedStatement stmt = null;
        try {
            StringBuilder sql = new StringBuilder( "DELETE FROM " + joinTable + " WHERE" );

            boolean first = true;
            for ( Entry<SQLIdentifier, Object> joinKey : joinKeyColToValue.entrySet() ) {
                if ( !first ) {
                    sql.append( " AND" );
                }
                sql.append( ' ' );
                sql.append( joinKey.getKey() );
                sql.append( "=?" );
                first = false;
            }

            stmt = conn.prepareStatement( sql.toString() );

            int i = 1;
            for ( Entry<SQLIdentifier, Object> joinKey : joinKeyColToValue.entrySet() ) {
                stmt.setObject( i++, joinKey.getValue() );
            }
            LOG.debug( "Executing DELETE (joined rows): " + stmt );
            stmt.executeUpdate();
        } catch ( SQLException e ) {
            LOG.error( e.getMessage(), e );
            throw new FeatureStoreException( e.getMessage(), e );
        } finally {
            JDBCUtils.close( stmt );
        }
    }

    @Override
    public List<String> performInsert( FeatureInputStream features, IDGenMode mode )
                            throws FeatureStoreException {
        return insertHandler.performInsert( features, mode );
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {
        return insertHandler.performInsert( fc, mode );
    }

    @Override
    public List<String> performUpdate( QName ftName, List<ParsedPropertyReplacement> replacementProps, Filter filter,
                                       Lock lock )
                                                               throws FeatureStoreException {
        LOG.debug( "Updating feature type '" + ftName + "', filter: " + filter + ", replacement properties: "
                                + replacementProps.size() );
        // TODO implement update more efficiently
        IdFilter idFilter = null;
        try {
            if ( filter instanceof IdFilter ) {
                idFilter = (IdFilter) filter;
            } else {
                idFilter = getIdFilter( ftName, (OperatorFilter) filter );
            }
        } catch ( Exception e ) {
            LOG.debug( e.getMessage(), e );
        }
        bboxTracker.update( ftName );
        return performUpdate( ftName, replacementProps, idFilter );
    }

    private List<String> performUpdate( QName ftName, List<ParsedPropertyReplacement> replacementProps, IdFilter filter )
                            throws FeatureStoreException {
        List<String> updated = null;
        if ( blobMapping != null ) {
            throw new FeatureStoreException( "Updates in SQLFeatureStore (BLOB mode) are currently not implemented." );
        } else {
            try {
                updated = performUpdateRelational( ftName, replacementProps, filter );
                if ( fs.getCache() != null ) {
                    for ( ResourceId id : filter.getSelectedIds() ) {
                        fs.getCache().remove( id.getRid() );
                    }
                }
            } catch ( Exception e ) {
                LOG.debug( e.getMessage(), e );
                throw new FeatureStoreException( e.getMessage(), e );
            }
        }
        return updated;
    }

    private List<String> performUpdateRelational( QName ftName, List<ParsedPropertyReplacement> replacementProps,
                                                  IdFilter filter )
                                                                          throws FeatureStoreException, FilterEvaluationException {

        FeatureTypeMapping ftMapping = schema.getFtMapping( ftName );
        FIDMapping fidMapping = ftMapping.getFidMapping();

        int updated = 0;
        PreparedStatement stmt = null;
        try {
            String sql = createRelationalUpdateStatement( ftMapping, fidMapping, replacementProps,
                                                          filter.getSelectedIds() );

            if ( sql != null ) {
                LOG.debug( "Update: " + sql );
                stmt = conn.prepareStatement( sql.toString() );
                setRelationalUpdateValues( replacementProps, ftMapping, stmt, filter, fidMapping );
                int[] updates = stmt.executeBatch();
                for ( int noUpdated : updates ) {
                    updated += noUpdated;
                }
            }
        } catch ( SQLException e ) {
            JDBCUtils.log( e, LOG );
            throw new FeatureStoreException( JDBCUtils.getMessage( e ), e );
        } finally {
            JDBCUtils.close( stmt );
        }
        LOG.debug( "Updated {} features.", updated );
        return new ArrayList<String>( filter.getMatchingIds() );
    }

    private void setRelationalUpdateValues( List<ParsedPropertyReplacement> replacementProps,
                                            FeatureTypeMapping ftMapping, PreparedStatement stmt, IdFilter filter,
                                            FIDMapping fidMapping )
                                                                    throws SQLException {
        int i = 1;

        for ( ParsedPropertyReplacement replacement : replacementProps ) {
            Property replacementProp = replacement.getNewValue();
            QName propName = replacementProp.getType().getName();
            Mapping mapping = ftMapping.getMapping( propName );
            if ( mapping != null ) {
                if ( mapping.getJoinedTable() != null && !mapping.getJoinedTable().isEmpty() ) {
                    continue;
                }
                ParticleConverter<TypedObjectNode> converter = (ParticleConverter<TypedObjectNode>) fs.getConverter( mapping );
                if ( mapping instanceof PrimitiveMapping ) {
                    MappingExpression me = ( (PrimitiveMapping) mapping ).getMapping();
                    if ( !( me instanceof DBField ) ) {
                        continue;
                    }
                    PrimitiveValue value = (PrimitiveValue) replacementProp.getValue();
                    converter.setParticle( stmt, value, i++ );
                } else if ( mapping instanceof GeometryMapping ) {
                    MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                    if ( !( me instanceof DBField ) ) {
                        continue;
                    }
                    Geometry value = (Geometry) replacementProp.getValue();
                    converter.setParticle( stmt, value, i++ );
                }
            }
        }

        for ( String id : filter.getMatchingIds() ) {
            IdAnalysis analysis = schema.analyzeId( id );
            int j = i;
            for ( String fidKernel : analysis.getIdKernels() ) {
                PrimitiveValue value = new PrimitiveValue( fidKernel, new PrimitiveType( fidMapping.getColumnType() ) );
                Object sqlValue = SQLValueMangler.internalToSQL( value );
                stmt.setObject( j++, sqlValue );
            }
            stmt.addBatch();
        }
    }

    private String createRelationalUpdateStatement( FeatureTypeMapping ftMapping, FIDMapping fidMapping,
                                                    List<ParsedPropertyReplacement> replacementProps,
                                                    List<ResourceId> list )
                                                                            throws FilterEvaluationException, FeatureStoreException, SQLException {
        StringBuffer sql = new StringBuffer( "UPDATE " );
        sql.append( ftMapping.getFtTable() );
        sql.append( " SET " );
        boolean first = true;
        for ( ParsedPropertyReplacement replacement : replacementProps ) {
            Property replacementProp = replacement.getNewValue();
            QName propName = replacementProp.getType().getName();
            Mapping mapping = ftMapping.getMapping( propName );
            if ( mapping != null ) {
                if ( mapping.getJoinedTable() != null && !mapping.getJoinedTable().isEmpty() ) {
                    addRelationallyMappedMultiProperty( replacement, mapping, ftMapping, list );
                    continue;
                }
                String column = null;
                ParticleConverter<TypedObjectNode> converter = (ParticleConverter<TypedObjectNode>) fs.getConverter( mapping );
                if ( mapping instanceof PrimitiveMapping ) {
                    MappingExpression me = ( (PrimitiveMapping) mapping ).getMapping();
                    if ( !( me instanceof DBField ) ) {
                        continue;
                    }
                    column = ( (DBField) me ).getColumn();
                    if ( !first ) {
                        sql.append( "," );
                    } else {
                        first = false;
                    }
                    sql.append( column );
                    sql.append( "=" );

                    // TODO communicate value for non-prepared statement converters
                    sql.append( converter.getSetSnippet( null ) );
                } else if ( mapping instanceof GeometryMapping ) {
                    MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                    if ( !( me instanceof DBField ) ) {
                        continue;
                    }
                    column = ( (DBField) me ).getColumn();
                    if ( !first ) {
                        sql.append( "," );
                    } else {
                        first = false;
                    }
                    sql.append( column );
                    sql.append( "=" );
                    // TODO communicate value for non-prepared statement converters
                    sql.append( converter.getSetSnippet( null ) );
                } else {
                    LOG.warn( "Updating of " + mapping.getClass() + " is currently not implemented. Omitting." );
                    continue;
                }
            } else {
                LOG.warn( "No mapping for update property '" + propName + "'. Omitting." );
            }
        }

        // only property changes in multi properties?
        if ( first ) {
            return null;
        }

        sql.append( " WHERE " );
        sql.append( fidMapping.getColumns().get( 0 ).first );
        sql.append( "=?" );
        for ( int i = 1; i < fidMapping.getColumns().size(); i++ ) {
            sql.append( " AND " );
            sql.append( fidMapping.getColumns().get( i ) );
            sql.append( "=?" );
        }
        return sql.toString();
    }

    private void addRelationallyMappedMultiProperty( ParsedPropertyReplacement replacement, Mapping mapping,
                                                     FeatureTypeMapping ftMapping, List<ResourceId> list )
                                                                             throws FilterEvaluationException, FeatureStoreException, SQLException {
        UpdateAction action = replacement.getUpdateAction();
        if ( action == null ) {
            action = UpdateAction.INSERT_AFTER;
        }
        switch ( action ) {
        case INSERT_BEFORE:
        case REMOVE:
        case REPLACE:
            LOG.warn( "Updating of multi properties is currently only supported for 'insertAfter' update action. Omitting." );
            break;
        case INSERT_AFTER:
            break;
        default:
            break;
        }
        InsertRowManager mgr = new InsertRowManager( fs, conn, null );
        List<Property> props = Collections.singletonList( replacement.getNewValue() );
        for ( ResourceId id : list ) {
            IdAnalysis analysis = schema.analyzeId( id.getRid() );
            FeatureType featureType = schema.getFeatureType( ftMapping.getFeatureType() );
            Feature f = featureType.newFeature( id.getRid(), props, null );
            mgr.updateFeature( f, ftMapping, analysis.getIdKernels(), mapping, replacement );
        }
    }

    private IdFilter getIdFilter( QName ftName, OperatorFilter filter )
                            throws FeatureStoreException {
        Set<String> ids = new HashSet<String>();
        Query query = new Query( ftName, filter, -1, -1, -1 );
        FeatureInputStream rs = null;
        try {
            rs = fs.query( query );
            for ( Feature feature : rs ) {
                ids.add( feature.getId() );
            }
        } catch ( FilterEvaluationException e ) {
            throw new FeatureStoreException( e );
        } finally {
            if ( rs != null ) {
                rs.close();
            }
        }
        return new IdFilter( ids );
    }

    @Override
    public String performReplace( Feature replacement, Filter filter, Lock lock, IDGenMode idGenMode )
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Replace is not supported yet." );
    }

}
