package game.button;

import game.Level;
import game.Position;

public abstract class Button {
    
    private final Position buttonLocation;
    
    public Position getButtonPosition() {
        return buttonLocation;
    }
    
    public abstract void press(Level level);
    
    public Button(Position buttonLocation) {
        this.buttonLocation = buttonLocation;
    }
    
}
