package graphics;

import java.awt.*;

import static graphics.MainWindow.TILE_SIZE;

public class Position {
    
    private int position;
    private int x;
    private int y;
    
    private static final int[] OFFSETS = new int[] {
        10, 13, 7, 16, 4, 19, 1, 11, 14, 8, 17, 5, 20, 2, 9, 12, 6, 15, 3, 18, 0, 10
    };
    
    int getX(){
        return x;
    }
    int getY(){
        return y;
    }
    int getPosition(){
        return position;
    }
    void setPosition(int position){
        this.position = position;
        x = position & 0b11111;
        y = position >>> 5;
    }
    
    int getGraphicX(){
        return x*TILE_SIZE + TILE_SIZE/2;
    }
    int getGraphicY(){
        return y*TILE_SIZE + TILE_SIZE/2;
    }
    
    int getGraphicX(int offset){
        if (offset >= OFFSETS.length) offset = OFFSETS.length - 1;
        return x*TILE_SIZE + OFFSETS[offset];
    }
    int getGraphicY(int offset){
        if (offset >= OFFSETS.length) offset = OFFSETS.length - 1;
        return y*TILE_SIZE + OFFSETS[offset];
    }
    
    public Position(int x, int y){
        this.x = x;
        this.y = y;
        position = (y << 5) | x;
    }
    
    public Position(int position){
        setPosition(position);
    }
    
}
