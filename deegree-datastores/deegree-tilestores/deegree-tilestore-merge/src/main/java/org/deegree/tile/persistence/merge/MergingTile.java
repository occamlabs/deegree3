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
package org.deegree.tile.persistence.merge;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;
import org.slf4j.Logger;

/**
 * {@link Tile} implementation used by {@link MergingTileStore}.
 * 
 * @author <a href="mailto:Reijer.Copier@idgis.nl">Reijer Copier</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class MergingTile implements Tile {

    private static final Logger LOG = getLogger( MergingTile.class );

    private final List<Tile> tiles;

    MergingTile( List<Tile> tiles ) {
        this.tiles = tiles;
    }

    @Override
    public BufferedImage getAsImage()
                            throws TileIOException {
        LOG.debug( "Merging tiles" );
        Iterator<Tile> itr = tiles.iterator();
        Tile firstTile = itr.next();
        BufferedImage img = firstTile.getAsImage();
        Graphics g = img.getGraphics();
        while ( itr.hasNext() ) {
            Tile nextTile = itr.next();
            BufferedImage nextImage = nextTile.getAsImage();
            g.drawImage( nextImage, 0, 0, null );
        }
        return img;
    }

    @Override
    public InputStream getAsStream()
                            throws TileIOException {
        LOG.debug( "Writing image" );
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            BufferedImage img = getAsImage();
            if ( img.getTransparency() != BufferedImage.OPAQUE ) {
                BufferedImage noTransparency = new BufferedImage( img.getWidth(), img.getHeight(), TYPE_3BYTE_BGR );
                Graphics g = noTransparency.getGraphics();
                g.drawImage( img, 0, 0, null );
                img = noTransparency;
            }
            ImageIO.write( img, "jpeg", output );
        } catch ( IOException e ) {
            throw new TileIOException( e );
        }
        LOG.debug( "Output size: " + output.size() );
        return new ByteArrayInputStream( output.toByteArray() );
    }

    @Override
    public Envelope getEnvelope() {
        return tiles.get( 0 ).getEnvelope();
    }

    @Override
    public FeatureCollection getFeatures( int i, int j, int limit )
                            throws UnsupportedOperationException {
        throw new UnsupportedOperationException( "MergingTile does not support getFeatures" );
    }
}
