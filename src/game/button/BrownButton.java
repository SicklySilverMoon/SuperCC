package game.button;

import game.Level;
import game.Position;

public class BrownButton extends Button {
    
    private final int trapIndex;
    
    @Override
    public void press(Level level) {
        level.getOpenTraps().set(trapIndex);
    }
    
    public BrownButton(Position buttonPosition, int trapIndex) {
        super(buttonPosition);
        this.trapIndex = trapIndex;
    }
    
}
