package game.button;

import game.Level;
import game.Position;

public class BrownButton extends Button {
    
    private final int trapIndex;
    private final Position trapPosition;
    
    @Override
    public void press(Level level) {
        level.getOpenTraps().set(trapIndex);
    }
    
    public void release(Level level) {
        level.getOpenTraps().set(trapIndex, false);
    }
    
    public int getTrapIndex() {
        return trapIndex;
    }
    
    public Position getTrapPosition() {
        return trapPosition;
    }
    
    public BrownButton(Position buttonPosition, Position trapPosition, int trapIndex) {
        super(buttonPosition);
        this.trapPosition = trapPosition;
        this.trapIndex = trapIndex;
    }
    
}
