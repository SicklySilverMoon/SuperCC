package game;

import java.awt.event.MouseEvent;

import static game.Creature.*;
import static graphics.Gui.TILE_SIZE;
import static java.lang.Math.abs;

public class Position {
    
    private static final int  MOVE_DOWN  =  0b100000,
        MOVE_UP    = -0b100000,
        MOVE_RIGHT =  0b000001,
        MOVE_LEFT  = -0b000001;
    
    public static final byte UNCLICKABLE = 127;
    
    private int index;
    private int x;
    private int y;
    
    private static final int[] OFFSETS = new int[] {
        10, 13, 7, 16, 4, 19, 1, 11, 14, 8, 17, 5, 20, 2, 9, 12, 6, 15, 3, 18, 0, 10
    };
    
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getIndex(){
        return index;
    }
    public void setIndex(int index){
        this.index = index;
        x = index & 0b11111;
        y = index >>> 5;
    }
    
    private void moveUp(){
        y--;
        index += MOVE_UP;
    }
    private void moveLeft(){
        x--;
        index += MOVE_LEFT;
    }
    private void moveDown(){
        y++;
        index += MOVE_DOWN;
    }
    private void moveRight(){
        x++;
        index += MOVE_RIGHT;
    }
    public Position move(int direction){
        Position p = clone();
        switch (direction){
            case DIRECTION_UP:    p.moveUp(); break;
            case DIRECTION_LEFT:  p.moveLeft(); break;
            case DIRECTION_DOWN:  p.moveDown(); break;
            case DIRECTION_RIGHT: p.moveRight(); break;
        }
        return p;
    }
    
    public Position add(int x, int y){
        return new Position(this.x + x, this.y + y);
    }
    
    public int getGraphicX(){
        return x*TILE_SIZE + TILE_SIZE/2;
    }
    public int getGraphicY(){
        return y*TILE_SIZE + TILE_SIZE/2;
    }
    
    public int getGraphicX(int offset){
        if (offset >= OFFSETS.length) offset = OFFSETS.length - 1;
        return x*TILE_SIZE + OFFSETS[offset];
    }
    public int getGraphicY(int offset){
        if (offset >= OFFSETS.length) offset = OFFSETS.length - 1;
        return y*TILE_SIZE + OFFSETS[offset];
    }
    
    public static Position screenPosition(Position chipPosition){
        int screenX, screenY;
        int chipX = chipPosition.x;
        int chipY = chipPosition.y;
        
        if (chipX < 5) screenX = 0;
        else if (chipX >= 27) screenX = 23;
        else screenX = chipX - 4;
        
        if (chipY < 5) screenY = 0;
        else if (chipY >= 27) screenY = 23;
        else screenY = chipY - 4;
        
        return new Position(screenX, screenY);
    }
    
    public static Position clickPosition(Position screenPosition, byte clickByte){
        int n = -clickByte - 1;
        return new Position(screenPosition.x + n % 9, screenPosition.y + n / 9);
    }
    
    public byte clickByte(Position chipPosition){
        Position screen = screenPosition(chipPosition);
        if (y - screen.y < 9 && y - screen.y >= 0 &&
            x - screen.x < 9 && x - screen.x >= 0){
            return (byte) -(9 * (y - screen.y) + (x - screen.x) + 1);
        }
        return UNCLICKABLE;
    }
    
    public int[] seek(Position seekedPosition){
        int verticalDifference = y - seekedPosition.y;
        int horizontalDifference = x - seekedPosition.x;
        
        int verticalDirection = -1;
        if (verticalDifference > 0) verticalDirection = DIRECTION_UP;
        else if (verticalDifference < 0) verticalDirection = DIRECTION_DOWN;
        
        int horizontalDirection = -1;
        if (horizontalDifference > 0) horizontalDirection = DIRECTION_LEFT;
        else if (horizontalDifference < 0) horizontalDirection = DIRECTION_RIGHT;
        
        if (abs(verticalDifference) >= abs(horizontalDifference))
            return new int[] {verticalDirection, horizontalDirection};
        else return new int[] {horizontalDirection, verticalDirection};
    }
    
    public Position(MouseEvent e){
        this.x = e.getX() / TILE_SIZE;
        this.y = e.getY() / TILE_SIZE;
        index = (y << 5) | x;
    }
    
    public Position(int x, int y){
        this.x = x;
        this.y = y;
        index = (y << 5) | x;
    }
    
    public Position(int index){
        setIndex(index);
    }
    
    @Override
    public String toString() {
        return "("+x+", "+y+")";
    }
    
    @Override
    public Position clone(){
        return new Position(index);
    }
    
    public boolean equals(Position p){
        return index == p.index;
    }
    
}
