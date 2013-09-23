/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.gdal;

import java.io.File;

import org.apache.commons.pool.PoolableObjectFactory;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

/**
 * <code>PoolableObjectFactory</code> for GDAL <code>Dataset</code> objects.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.5
 */
class GdalDatasetFactory implements PoolableObjectFactory<Dataset> {

    private final File file;

    /**
     * Creates a new {@link GdalDatasetFactory}.
     * 
     * @param file
     *            raster file, must not be <code>null</code> and supported by GDAL
     */
    GdalDatasetFactory( File file ) {
        this.file = file;
    }

    @Override
    public void activateObject( Dataset o )
                            throws Exception {
        // nothing to do
    }

    @Override
    public void destroyObject( Dataset o )
                            throws Exception {
        o.delete();
    }

    @Override
    public Dataset makeObject()
                            throws Exception {
        return gdal.OpenShared( file.toString() );
    }

    @Override
    public void passivateObject( Dataset o )
                            throws Exception {
        // nothing to do
    }

    @Override
    public boolean validateObject( Dataset o ) {
        return true;
    }

}
