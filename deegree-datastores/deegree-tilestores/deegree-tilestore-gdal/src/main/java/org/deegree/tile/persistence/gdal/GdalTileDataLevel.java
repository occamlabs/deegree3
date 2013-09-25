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

import org.apache.commons.pool.impl.GenericObjectPool;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TileDataLevel} backed by an overview of a GDAL <code>Dataset</code>.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.5
 */
class GdalTileDataLevel implements TileDataLevel {

    private static Logger LOG = LoggerFactory.getLogger( GdalTileDataLevel.class );

    private final TileMatrix metadata;

    private final GeometryFactory fac = new GeometryFactory();

    private final GenericObjectPool<Dataset> gdalDatasetPool;

    private final int xMin;

    private final int yMin;

    private final int yMax;

    private final int xMax;

    private Envelope datasetEnvelope;

    private double unitsPerPixelX;

    private double unitsPerPixelY;

    /**
     * Creates a new {@link GdalTileDataLevel} instance.
     * 
     * @param matrix
     *            tile matrix, must not be <code>null</code>
     * @param file
     *            raster file, must not be <code>null</code>
     * @param xMin
     * @param yMin
     * @param tilesX
     * @param tilesY
     * @throws Exception
     */
    GdalTileDataLevel( TileMatrix matrix, File file, int xMin, int yMin, int xMax, int yMax ) throws Exception {
        this.metadata = matrix;
        GdalDatasetFactory fac = new GdalDatasetFactory( file );
        gdalDatasetPool = new GenericObjectPool<Dataset>( fac );
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        Dataset dataset = gdalDatasetPool.borrowObject();
        try {
            Band firstBand = dataset.GetRasterBand( 1 );
            datasetEnvelope = getEnvelope( dataset );
            double width = datasetEnvelope.getSpan0();
            double height = datasetEnvelope.getSpan1();
            unitsPerPixelX = width / (double) firstBand.getXSize();
            unitsPerPixelY = height / (double) firstBand.getYSize();
            LOG.info( "GDAL info" );
            LOG.info( "- pixels (x): " + firstBand.getXSize() );
            LOG.info( "- pixels (y): " + firstBand.getYSize() );
            LOG.info( "- units per pixel (x): " + unitsPerPixelX );
            LOG.info( "- units per pixel (y): " + unitsPerPixelY );
            LOG.info( "- units per pixel (by matrix): " + matrix.getResolution() );
        } finally {
            gdalDatasetPool.returnObject( dataset );
        }
    }

    private static Envelope getEnvelope( Dataset dataset ) {
        double[] geoTransform = dataset.GetGeoTransform();
        int rasterXSize = dataset.getRasterXSize();
        int rasterYSize = dataset.getRasterYSize();
        double pixelResX = geoTransform[1];
        double pixelResY = geoTransform[5];
        double minX = geoTransform[0];
        double maxX = minX + pixelResX * rasterXSize;
        double minY = geoTransform[3];
        double maxY = minY + pixelResY * rasterYSize;
        if ( minX > maxX ) {
            double tmp = maxX;
            maxX = minX;
            minX = tmp;
        }
        if ( minY > maxY ) {
            double tmp = maxY;
            maxY = minY;
            minY = tmp;
        }
        Point min = new DefaultPoint( null, null, null, new double[] { minX, minY } );
        Point max = new DefaultPoint( null, null, null, new double[] { maxX, maxY } );
        return new DefaultEnvelope( min, max );
    }

    @Override
    public TileMatrix getMetadata() {
        return metadata;
    }

    @Override
    public Tile getTile( long x, long y ) {
        // System.out.println(metadata.getIdentifier());
        // System.out.println(metadata.getNumTilesX());
        if ( !isWithinLimits(x,y) ) {
            return null;
        }
        double tileWidth = metadata.getTileWidth();
        double tileHeight = metadata.getTileHeight();
        Envelope matrixEnvelope = metadata.getSpatialMetadata().getEnvelope();
        double minX = tileWidth * x + matrixEnvelope.getMin().get0();
        double maxX = minX + tileWidth;
        double maxY = matrixEnvelope.getMax().get1() - tileHeight * y;
        double minY = maxY - tileHeight;
        ICRS crs = metadata.getSpatialMetadata().getEnvelope().getCoordinateSystem();
        Point min = new DefaultPoint( null, crs, null, new double[] { minX, minY } );
        Point max = new DefaultPoint( null, crs, null, new double[] { maxX, maxY } );
        Envelope tileEnvelope = new DefaultEnvelope( null, crs, null, min, max );
        min = new DefaultPoint( null, crs, null, new double[] { minX, maxY } );
        max = new DefaultPoint( null, crs, null, new double[] { maxX, minY } );
        Envelope tileEnvelope2 = new DefaultEnvelope( null, crs, null, min, max );
        double relX = minX - datasetEnvelope.getMin().get0();
        double relY = datasetEnvelope.getMax().get1() - maxY;
        int datasetMinX = (int) ( relX / unitsPerPixelX );
        int datasetMinY = (int) ( relY / unitsPerPixelY );
        int datasetPixelsX = (int) ( tileWidth / unitsPerPixelX );
        int datasetPixelsY = (int) ( tileHeight / unitsPerPixelY );
        return new GdalTile( tileEnvelope, datasetEnvelope, (int) metadata.getTilePixelsX(),
                             (int) metadata.getTilePixelsY(), gdalDatasetPool, datasetMinX, datasetMinY,
                             datasetPixelsX, datasetPixelsY, x, y, metadata.getResolution(), unitsPerPixelX,
                             unitsPerPixelY, tileEnvelope2 );
    }

    private boolean isWithinLimits( long x, long y ) {
        return x >= xMin && x <= xMax && y >= yMin && y <= yMax; 
    }
}
