package graphics;

import game.Position;

import java.awt.event.MouseEvent;

public class GameGraphicPosition extends Position {
    
    private final int tileSize;
    
    private static final int[] PATH_DISPLAY_OFFSETS = new int[] {
        10, 13, 7, 16, 4, 19, 1, 11, 14, 8, 17, 5, 20, 2, 9, 12, 6, 15, 3, 18, 0, 10
    };
    
    public int getGraphicXCentered(){
        return x * tileSize + tileSize / 2;
    }
    public int getGraphicYCentered(){
        return y * tileSize + tileSize / 2;
    }
    
    public int getGraphicX(int offsetIndex){
        if (offsetIndex >= PATH_DISPLAY_OFFSETS.length) offsetIndex = PATH_DISPLAY_OFFSETS.length - 1;
        return x * tileSize + PATH_DISPLAY_OFFSETS[offsetIndex];
    }
    public int getGraphicY(int offsetIndex){
        if (offsetIndex >= PATH_DISPLAY_OFFSETS.length) offsetIndex = PATH_DISPLAY_OFFSETS.length - 1;
        return y * tileSize + PATH_DISPLAY_OFFSETS[offsetIndex];
    }
    
    public GameGraphicPosition(Position p, int tileSize) {
        super(p.getX(), p.getY(), p.getIndex());
        this.tileSize = tileSize;
    }
    
    public GameGraphicPosition(MouseEvent e, int tileSize){
        super(e.getX() / tileSize, e.getY() / tileSize);
        this.tileSize = tileSize;
    }
    
}
