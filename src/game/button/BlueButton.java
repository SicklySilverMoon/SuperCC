package game.button;

import game.Creature;
import game.Level;
import game.Position;

import static game.CreatureID.TANK_MOVING;
import static game.Direction.TURN_RIGHT;

public class BlueButton extends Button {
    
    @Override
    public void press(Level level) {
        for (Creature m : level.getMonsterList()) {
            if (m.getCreatureType().isTank() && !m.isSliding()){
                m.setCreatureType(TANK_MOVING);
                m.turn(TURN_RIGHT);
                level.getLayerFG().set(m.getPosition(), m.toTile());
                m.turn(TURN_RIGHT);
            }
        }
    }
    
    public BlueButton(Position buttonPosition) {
        super(buttonPosition);
    }
    
}
