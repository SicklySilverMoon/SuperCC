package game.button;

import game.Level;
import game.Position;

public class RedButton extends ConnectionButton {
    
    @Override
    public void press(Level level) {
        level.getMonsterList().addClone(targetPosition);
    }
    
    public RedButton(Position buttonPosition, Position clonerPosition) {
        super(buttonPosition, clonerPosition);
    }
    
}
