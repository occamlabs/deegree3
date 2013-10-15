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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.deegree.commons.gdal.GdalDataset;
import org.deegree.commons.gdal.GdalSettings;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;

/**
 * {@link Tile} backed by a {@link GdalDataset}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class GdalTile implements Tile {

    private final String fileName;

    private final Envelope tileEnvelope;

    private final int pixelsX;

    private final int pixelsY;

    private final String imageFormat;

    private final GdalSettings gdalSettings;

    /**
     * Creates a new {@link GdalTile} instance.
     * 
     * @param fileName
     *            name of GDAL-supported file, must not be <code>null</code>
     * @param tileEnvelope
     *            bounding box of the tile, must not be <code>null</code>
     * @param pixelsX
     *            width of the tile in pixels
     * @param pixelsY
     *            height of the tile in pixels
     * @param imageFormat
     * @param gdalSettings
     */
    GdalTile( String fileName, Envelope tileEnvelope, int pixelsX, int pixelsY, String imageFormat,
              GdalSettings gdalSettings ) {
        this.fileName = fileName;
        this.tileEnvelope = tileEnvelope;
        this.pixelsX = pixelsX;
        this.pixelsY = pixelsY;
        this.imageFormat = imageFormat;
        this.gdalSettings = gdalSettings;
    }

    @Override
    public BufferedImage getAsImage()
                            throws TileIOException {
        try {
            return gdalSettings.extractRegion( new File( fileName ), tileEnvelope, pixelsX, pixelsY, true );
        } catch ( Exception e ) {
            throw new TileIOException( e.getMessage(), e );
        }
    }

    @Override
    public InputStream getAsStream()
                            throws TileIOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            String formatName;
            if ( imageFormat.startsWith( "image/" ) ) {
                formatName = imageFormat.substring( 6 );
            } else {
                formatName = imageFormat;
            }
            BufferedImage img = gdalSettings.extractRegion( new File( fileName ), tileEnvelope, pixelsX, pixelsY, false );
            ImageIO.write( img, formatName, bos );
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new TileIOException( "Error retrieving image: " + e.getMessage(), e );
        }
        return new ByteArrayInputStream( bos.toByteArray() );
    }

    @Override
    public Envelope getEnvelope() {
        return tileEnvelope;
    }

    @Override
    public FeatureCollection getFeatures( int i, int j, int limit )
                            throws UnsupportedOperationException {
        throw new UnsupportedOperationException( "Feature retrieval is not supported by the GDALTileStore." );
    }

}
