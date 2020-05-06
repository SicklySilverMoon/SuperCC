package game.button;

import game.Level;
import game.Position;

import static game.Tile.TOGGLE_CLOSED;
import static game.Tile.TOGGLE_OPEN;

public class GreenButton extends Button{
    
    @Override
    public void press(Level level) {
        for (Position p : level.getToggleDoors()) {
            if      (level.getLayerFG().get(p) == TOGGLE_OPEN) level.getLayerFG().set(p, TOGGLE_CLOSED);
            else if (level.getLayerFG().get(p) == TOGGLE_CLOSED) level.getLayerFG().set(p, TOGGLE_OPEN);
            else if (level.supportsLayerBG()) {
                if (level.getLayerBG().get(p) == TOGGLE_OPEN) level.getLayerBG().set(p, TOGGLE_CLOSED);
                else if (level.getLayerBG().get(p) == TOGGLE_CLOSED) level.getLayerBG().set(p, TOGGLE_OPEN);
            }
        }
    }
    
    public GreenButton(Position buttonPosition) {
        super(buttonPosition);
    }

}
