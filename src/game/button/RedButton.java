package game.button;

import game.Level;
import game.Position;

public class RedButton extends ConnectionButton {
    
    @Override
    public void press(Level level) {
        level.getMonsterList().addClone(targetPosition); //Sends a clone signal to the addClone code, targetPosition is sent as (X Y)
    }
    
    public RedButton(Position buttonPosition, Position clonerPosition) {
        super(buttonPosition, clonerPosition);
    }
    
}
