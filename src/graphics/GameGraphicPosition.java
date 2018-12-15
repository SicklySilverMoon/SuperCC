package graphics;

import game.Position;

import java.awt.event.MouseEvent;

import static graphics.Gui.TILE_SIZE;

public class GameGraphicPosition extends Position {
    
    private static final int[] PATH_DISPLAY_OFFSETS = new int[] {
        10, 13, 7, 16, 4, 19, 1, 11, 14, 8, 17, 5, 20, 2, 9, 12, 6, 15, 3, 18, 0, 10
    };
    
    public int getGraphicXCentered(){
        return x*TILE_SIZE + TILE_SIZE/2;
    }
    public int getGraphicYCentered(){
        return y*TILE_SIZE + TILE_SIZE/2;
    }
    
    public int getGraphicX(int offsetIndex){
        if (offsetIndex >= PATH_DISPLAY_OFFSETS.length) offsetIndex = PATH_DISPLAY_OFFSETS.length - 1;
        return x*TILE_SIZE + PATH_DISPLAY_OFFSETS[offsetIndex];
    }
    public int getGraphicY(int offsetIndex){
        if (offsetIndex >= PATH_DISPLAY_OFFSETS.length) offsetIndex = PATH_DISPLAY_OFFSETS.length - 1;
        return y*TILE_SIZE + PATH_DISPLAY_OFFSETS[offsetIndex];
    }
    
    public GameGraphicPosition(Position p) {
        super(p.getX(), p.getY(), p.getIndex());
    }
    
    public GameGraphicPosition(MouseEvent e){
        super(e.getX() / TILE_SIZE, e.getY() / TILE_SIZE);
    }
    
}
