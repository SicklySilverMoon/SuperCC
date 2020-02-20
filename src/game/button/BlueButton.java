package game.button;

import game.Creature;
import game.Level;
import game.Position;

import static game.CreatureID.TANK_MOVING;
import static game.Direction.TURN_AROUND;

public class BlueButton extends Button { //TODO: This is pretty MS-centric, please fix that
    
    @Override
    public void press(Level level) {
        for (Creature m : level.getMonsterList()) {
            if (m.getCreatureType().isTank() && !m.isSliding()){
                m.setCreatureType(TANK_MOVING);
                m.turn(TURN_AROUND);
                level.getLayerFG().set(m.getPosition(), m.toTile());
            }
        }
        for (Creature m : level.getMonsterList().getNewClones()) { //Ensures Frankenstein glitch works in all situations, prior to this it wouldn't flip tanks that had been cloned earlier that tick due to them not being on the monster list and instead being on the newClones list, this now flips those on the newClones list as well
            if (m.getCreatureType().isTank() && !m.isSliding()) {
                m.setCreatureType(TANK_MOVING);
                m.turn(TURN_AROUND);
            }
        }
    }
    
    public BlueButton(Position buttonPosition) {
        super(buttonPosition);
    }
    
}
