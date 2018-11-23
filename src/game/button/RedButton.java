package game.button;

import game.Level;
import game.Position;

public class RedButton extends Button{
    
    private final Position clonerPosition;
    
    @Override
    public void press(Level level) {
        level.getMonsterList().addClone(clonerPosition);
    }
    
    public RedButton(Position buttonPosition, Position clonerPosition) {
        super(buttonPosition);
        this.clonerPosition = clonerPosition;
    }
    
}
