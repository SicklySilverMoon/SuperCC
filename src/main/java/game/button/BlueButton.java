package game.button;

import game.Creature;
import game.Level;
import game.Position;

import static game.CreatureID.TANK_MOVING;
import static game.Direction.TURN_AROUND;

public class BlueButton extends Button {
    
    @Override
    public void press(Level level) {
        level.turnTanks();
    }
    
    public BlueButton(Position buttonPosition) {
        super(buttonPosition);
    }
    
}
