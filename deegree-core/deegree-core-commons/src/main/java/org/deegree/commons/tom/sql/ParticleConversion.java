/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.commons.tom.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.deegree.commons.tom.TypedObjectNode;

/**
 * Helper class for delayed calls to {@link ParticleConverter#setParticle(PreparedStatement, TypedObjectNode, int)}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class ParticleConversion<T extends TypedObjectNode> {

    private final ParticleConverter<T> converter;

    private final T particle;

    public ParticleConversion( ParticleConverter<T> converter, T particle ) {
        this.converter = converter;
        this.particle = particle;
    }

    /**
     * Sets the SQL parameters in the <code>PreparedStatement</code>.
     * 
     * @param stmt
     *            prepared statement, must not be <code>null</code>
     * @param firstSqlParamIndex
     *            index of the first SQL parameter in the statement
     * @return number of SQL prepared statement arguments that the particle corresponds to
     * @throws SQLException
     */
    public int setSqlParameters( PreparedStatement stmt, int firstSqlParamIndex )
                            throws SQLException {
        return converter.setParticle( stmt, particle, firstSqlParamIndex );
    }
}
