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

import static java.util.Collections.synchronizedList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

/**
 * Pool for GDAL <code>dataset</code> objects that maintains a unique instance per thread.
 * <p>
 * Due to restrictions in GDAL's JNI wrapper, standard pooling doesn't work here. One has to use a unique instance
 * <i>per thread</i>. If a GDAL <code>dataset</code> is used by different threads (subsequently, not in parallel),
 * access violations occur that crash the VM.
 * </p>
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.5
 */
public class GdalDatasetPerThreadPool {

    private static GdalDatasetPerThreadPool instance;

    private final ThreadLocal<Map<File, Dataset>> fileToGdalDataset = new ThreadLocal<Map<File, Dataset>>() {
        @Override
        public Map<File, Dataset> initialValue() {
            return new HashMap<File, Dataset>();
        }
    };

    private final List<Dataset> openDatasets = synchronizedList( new ArrayList<Dataset>() );

    public static synchronized GdalDatasetPerThreadPool getInstance() {
        if ( instance == null ) {
            instance = new GdalDatasetPerThreadPool();
        }
        return instance;
    }

    private GdalDatasetPerThreadPool() {
        // avoid instantiation
    }

    public Dataset getDataset( File file )
                            throws IOException {
        File canonicalFile = file.getCanonicalFile();
        Map<File, Dataset> fileToGdalDataset = this.fileToGdalDataset.get();
        Dataset gdalDataset = fileToGdalDataset.get( canonicalFile );
        if ( gdalDataset == null ) {
            gdalDataset = gdal.OpenShared( canonicalFile.getPath() );
            openDatasets.add( gdalDataset );
            fileToGdalDataset.put( canonicalFile, gdalDataset );
        }
        return gdalDataset;
    }

    public void closeDatasets() {
        for ( Dataset dataset : openDatasets ) {
            dataset.delete();
        }
    }

}
