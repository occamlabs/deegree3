import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.gdal.gdal.gdal;

public class AccessTileStore {

    public static void main( String[] args )
                            throws IOException {
        gdal.AllRegister();
        Workspace ws = new DefaultWorkspace( new File( "/home/schneider/.deegree/tilestore-test" ) );
        ws.startup();
        ws.initAll();

        // TileStore geotiffTs = ws.getResource( TileStoreProvider.class, "geotiff" );
        // TileDataSet dataSet = geotiffTs.getTileDataSet( "geotiff" );
        // long before = System.currentTimeMillis();
        // writeTilesToDisk( dataSet );
        // long elapsed = System.currentTimeMillis() - before;
        // System.out.println("took " + elapsed + " [ms]");
        // // System.out.println( dataSet.getTileMatrixSet().getTileMatrices().size() );
        // // Tile tile = dataSet.getTileDataLevels().get( 0 ).getTile( 0, 1 );
        // // BufferedImage im = tile.getAsImage();
        // // ImageIO.write( im, "png", new File( "/tmp/tile_geotiff.png" ) );
        // //
//        TileStore ecwTs = ws.getResource( TileStoreProvider.class, "Ortho10_2012" );
//        TileDataSet dataSet = ecwTs.getTileDataSet( "Ortho10_2012" );
//        writeTilesToDisk( dataSet );
    }

    private static void writeTilesToDisk( TileDataSet dataSet )
                            throws IOException {
        int levelIdx = 0;
        for ( TileDataLevel level : dataSet.getTileDataLevels() ) {
            System.out.println( level.getMetadata().getNumTilesX() );
            if ( levelIdx == 8) {
                for ( int x = 0; x < level.getMetadata().getNumTilesX(); x++ ) {
                    for ( int y = 0; y < level.getMetadata().getNumTilesY(); y++ ) {
                        Tile tile = level.getTile( x, y );
                        if ( tile != null ) {
                            BufferedImage im = tile.getAsImage();
                            if ( im != null ) {
                                ImageIO.write( im, "png", new File( "/tmp/gdal/tile_" + levelIdx + "_" + x + "_" + y
                                                                    + ".png" ) );
                            }
                        }
                    }
                }
            }
            levelIdx++;
        }
    }

}
