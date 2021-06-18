package game.button;

import game.Level;
import game.Position;

public class BrownButton extends ConnectionButton {
    
    private final int trapIndex;
    
    @Override
    public void press(Level level) {
        level.setTrap(targetPosition, true);
    }
    
    public boolean isOpen(Level level) {
        return level.isTrapOpen(targetPosition);
    }
    
    public void release(Level level) {
        level.setTrap(targetPosition, false);
    }
    
    public int getTrapIndex() {
        return trapIndex;
    }
    
    public BrownButton(Position buttonPosition, Position trapPosition, int trapIndex) {
        super(buttonPosition, trapPosition);
        this.trapIndex = trapIndex;
    }
    
}
