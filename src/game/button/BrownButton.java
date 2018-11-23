package game.button;

import game.Level;
import game.Position;

public class BrownButton extends ConnectionButton {
    
    private final int trapIndex;
    
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
    
    public BrownButton(Position buttonPosition, Position trapPosition, int trapIndex) {
        super(buttonPosition, trapPosition);
        this.trapIndex = trapIndex;
    }
    
}
