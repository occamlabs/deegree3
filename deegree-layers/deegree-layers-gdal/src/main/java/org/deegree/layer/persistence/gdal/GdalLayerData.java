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
package org.deegree.layer.persistence.gdal;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.layer.LayerData;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.tile.GdalDataset;
import org.slf4j.Logger;

/**
 * {@link LayerData} implementation for layers that are drawn from GDAL datasets.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class GdalLayerData implements LayerData {

    private static final Logger LOG = getLogger( GdalLayerData.class );

    private final List<GdalDataset> datasets;

    private final Envelope bbox;

    private final int width;

    private final int height;

    GdalLayerData( List<GdalDataset> datasets, Envelope bbox, int width, int height ) {
        this.datasets = datasets;
        this.bbox = bbox;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render( RenderContext context ) {
        try {
            long before = System.currentTimeMillis();
            BufferedImage img = extractRegionFromGdalFiles();
            long elapsed = System.currentTimeMillis() - before;
            System.out.println("extract: " + elapsed + " [ms]");
            context.paintImage( img );
            elapsed = System.currentTimeMillis() - before;
            System.out.println("paint: " + elapsed + " [ms]");
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace:", e );
            LOG.error( "Unable to render raster: {}", e.getLocalizedMessage() );
        }
    }

    private BufferedImage extractRegionFromGdalFiles()
                            throws IOException {
        if ( datasets.size() == 1 ) {
            GdalDataset dataset = datasets.get( 0 );
            return dataset.extractRegion( bbox, width, height, false );
        }
        Graphics g = null;
        BufferedImage img = null;
        for ( GdalDataset dataset : datasets ) {
            if ( img == null ) {
                img = dataset.extractRegion( bbox, width, height, false );
                g = img.getGraphics();
                g.dispose();
            } else {
                BufferedImage img2 = dataset.extractRegion( bbox, width, height, false );
                g.drawImage( img2, 0, 0, null );
                g.dispose();
            }
        }
        return img;
    }

    @Override
    public FeatureCollection info() {
        throw new UnsupportedOperationException();
    }

}
