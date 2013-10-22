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
package org.deegree.commons.gdal;

import java.io.File;

import org.apache.commons.pool.KeyedPoolableObjectFactory;

/**
 * Used by {@link GdalDatasetThreadPoolCache} to create new {@link GdalDataset} instances.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class GdalDatasetFactory implements KeyedPoolableObjectFactory<File, GdalDataset> {

    private final GdalDatasetPool pool;

    GdalDatasetFactory( GdalDatasetPool pool ) {
        this.pool = pool;
    }

    @Override
    public GdalDataset makeObject( File key )
                            throws Exception {
        return new GdalDataset( key, pool.getCrs( key ) );
    }

    @Override
    public void destroyObject( File key, GdalDataset obj )
                            throws Exception {
        obj.detach();
    }

    @Override
    public boolean validateObject( File key, GdalDataset obj ) {
        return true;
    }

    @Override
    public void activateObject( File key, GdalDataset obj )
                            throws Exception {
        // nothing to do        
    }

    @Override
    public void passivateObject( File key, GdalDataset obj )
                            throws Exception {
        // nothing to do
    }

}
