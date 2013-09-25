package org.deegree.tile.persistence.merge;

import java.util.ArrayList;
import java.util.List;

import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

public class MergingTileDataLevel implements TileDataLevel {

    private final List<TileDataLevel> tileDataLevels;

    private final TileMatrix tileMatrix;

    public MergingTileDataLevel( List<TileDataLevel> tileDataLevels, TileMatrix tileMatrix ) {
        this.tileDataLevels = tileDataLevels;
        this.tileMatrix = tileMatrix;
    }

    @Override
    public TileMatrix getMetadata() {
        return tileMatrix;
    }

    @Override
    public Tile getTile( long x, long y ) {
        List<Tile> tiles = new ArrayList<Tile>( tileDataLevels.size() );
        for ( TileDataLevel tileDataLevel : tileDataLevels ) {
            Tile tile = tileDataLevel.getTile( x, y );
            if ( tile != null ) {
                tiles.add( tile );
            }
        }
        if ( tiles.isEmpty() ) {
            return null;
        }
        if ( tiles.size() == 1 ) {
            return tiles.get( 0 );
        }
        return new MergingTile( tiles );
    }
}
