package game.button;

import game.Level;
import game.Position;

import static game.Tile.TOGGLE_CLOSED;
import static game.Tile.TOGGLE_OPEN;

public class GreenButton extends Button{
    
    @Override
    public void press(Level level) {
        for (int i : level.toggleDoors) {
            if      (level.getLayerFG().get(i) == TOGGLE_OPEN) level.getLayerFG().set(i, TOGGLE_CLOSED);
            else if (level.getLayerFG().get(i) == TOGGLE_CLOSED) level.getLayerFG().set(i, TOGGLE_OPEN);
            else if (level.getLayerBG().get(i) == TOGGLE_OPEN) level.getLayerBG().set(i, TOGGLE_CLOSED);
            else if (level.getLayerBG().get(i) == TOGGLE_CLOSED) level.getLayerBG().set(i, TOGGLE_OPEN);
        }
    }
    
    public GreenButton(Position buttonPosition) {
        super(buttonPosition);
    }

}
