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
package org.deegree.feature.persistence.sql.blob;

import org.deegree.commons.jdbc.TableName;
import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;

/**
 * Encapsulates the BLOB mapping parameters of a {@link MappedAppSchema}.
 * 
 * @see MappedAppSchema
 * @see FeatureTypeMapping
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.2
 */
public class BlobMapping {

    private final TableName gmlObjectsTable;

    private final TableName gmlIdentifiersTable;

    private final ICRS storageCrs;

    private final BlobCodec codec;

    /**
     * Creates a new {@link BlobMapping} instance.
     * 
     * @param gmlObjectsTable
     *            the name of the table that stores the BLOBs, must not be <code>null</code>
     * @param storageCrs
     *            crs used for storing geometries / envelopes, must not be <code>null</code>
     * @param codec
     *            the decoder / encoder used for the BLOBs, must not be <code>null</code>
     * @param gmlIdentifiersTable
     *            the name of the table that stores the GML identifiers for efficient filtering, can be
     *            <code>null</code> (not used)
     */
    public BlobMapping( String gmlObjectsTable, ICRS storageCrs, BlobCodec codec, String gmlIdentifiersTable ) {
        this.gmlObjectsTable = new TableName( gmlObjectsTable );
        this.storageCrs = storageCrs;
        this.codec = codec;
        if ( gmlIdentifiersTable != null ) {
            this.gmlIdentifiersTable = new TableName( gmlIdentifiersTable );
        } else {
            this.gmlIdentifiersTable = null;
        }
    }

    /**
     * Returns the table that stores the BLOBs.
     * 
     * @return the table that stores the BLOBs, never <code>null</code>
     */
    public TableName getTable() {
        return gmlObjectsTable;
    }

    /**
     * Returns the table that stores the GML identifiers.
     * 
     * @return table that stores the GML identifiers for efficient filtering, can be <code>null</code> (not used)
     */
    public TableName getGmlIdentifiersTable() {
        return gmlIdentifiersTable;
    }

    /**
     * Returns the {@link CRS} used for storing the geometries / envelopes.
     * 
     * @return the crs, never <code>null</code>
     */
    public ICRS getCRS() {
        return storageCrs;
    }

    /**
     * Returns the {@link BlobCodec} for encoding and decoding features / geometries.
     * 
     * @return the codec, never <code>null</code>
     */
    public BlobCodec getCodec() {
        return codec;
    }

    /**
     * Returns the name of the column that stores the gml ids.
     * 
     * @return the name of the column, never <code>null</code>
     */
    public String getGMLIdColumn() {
        return "gml_id";
    }

    /**
     * 
     * @return
     */
    public String getDataColumn() {
        return "binary_object";
    }

    /**
     * 
     * @return
     */
    public String getBBoxColumn() {
        return "gml_bounded_by";
    }

    /**
     * 
     * @return
     */
    public String getTypeColumn() {
        return "ft_type";
    }

    public String getInternalIdColumn() {
        return "id";
    }
}
