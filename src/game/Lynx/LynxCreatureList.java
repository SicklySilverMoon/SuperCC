package game.Lynx;

import game.*;

import static game.CreatureID.DEAD;

public class LynxCreatureList extends CreatureList {
    private int[] creatureLayer; //a faux 'layer', whenever a creature moves into a position increment the resulting position index in this array. decrement it when they leave

    @Override
    public void initialise() {

    }

    @Override
    public void finalise() {

    }

    @Override
    public void tick() {
        for (int i = list.length - 1; i >= 0; i--) {
            //todo: the god damn trap sliding thing
            Creature creature = list[i];
            if (creature.getTimeTraveled() != 0 || creature.getCreatureType() == DEAD) continue;

            for (Direction dir : creature.getDirectionPriority(level.getChip(), level.getRNG())) {
                Tile newTile = level.getLayerFG().get(creature.getPosition().move(dir));
                if (!creature.canEnter(dir, newTile) || creaturesAtPosition(creature.getPosition()) != 0) continue;

                creature.setDirection(dir);
                break;
            }
        }

        for (int i = list.length - 1; i >= 0; i--) {
            //Actual movement should be done here
            Creature creature = list[i];
            if (creature.getCreatureType() == DEAD) continue;
            creature.tick();
        }
    }

    /** Returns the number of creatures currently occupying the specified position
     *
     * @param position The position to check (must be a valid position between 0 and 31 on x and y).
     * @return Number of creatures located within the given position.
     */
    private int creaturesAtPosition(Position position) {
        return creatureLayer[position.index];
    }

    @Override
    public void addClone(Position position) {

    }

    public LynxCreatureList(LynxCreature[] creatures) {
        super(creatures);
        creatureLayer = new int[1024];

        for (Creature c : creatures) {
            creatureLayer[c.getPosition().index]++;
        }
    }
}
