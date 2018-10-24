package graphics;

import java.awt.*;

import static graphics.MainWindow.TILE_SIZE;

public class Position {
    
    private int position;
    private int x;
    private int y;
    
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
    
    public Position(int x, int y){
        this.x = x;
        this.y = y;
        position = (y << 5) | x;
    }
    
    public Position(int position){
        setPosition(position);
    }
    
}
