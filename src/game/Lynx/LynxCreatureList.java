package game.Lynx;

import game.*;

import static game.CreatureID.*;

public class LynxCreatureList extends CreatureList {
    private int[] creatureLayer; //a faux 'layer', whenever a creature moves into a position increment the resulting position index in this array. decrement it when they leave
    private Creature[] animationLayer; //Another faux layer that holds references to DEAD creatures that currently have animations playing, a position's index matches to an index in this array

    private boolean secondCall; //false when first called in a tick and set to true then, used to determine which "part" of the monster list tick should be done

    @Override
    public void setCreatures(Creature[] creatures) {
        list = creatures;
        creatureLayer = new int[1024];

        for (int i=1; i < creatures.length; i++) { //start at 1 to skip over Chip who shouldn't be added
            Creature c = creatures[i];
            creatureLayer[c.getPosition().index]++;
        }
    }

    @Override
    public void initialise() {

    }

    @Override
    public void finalise() {

    }

    @Override
    public void tick() {
        Creature chip = level.getChip();
        if (!secondCall) {
            for (int i = list.length - 1; i >= 1; i--) {
                //todo: lol death animations are only solid for Chip but other monsters cut them short, meaning we gotta create some kind of solution to faciltate interactions, maybe a another array that holds references to animations on whatever tiles
                Creature creature = list[i];
                if (creature.getTimeTraveled() != 0 || creature.getCreatureType() == DEAD) continue;

                for (Direction dir : creature.getDirectionPriority(chip, level.getRNG())) {
                    Position newPosition = creature.getPosition().move(dir);
                    Tile newTile = level.getLayerFG().get(newPosition);

                    if (!creature.canEnter(dir, newTile) || getCreaturesAtPosition(newPosition) != 0) continue;
                    if (!newPosition.isValid()) continue;

                    creature.setDirection(dir);
                    break;
                }
            }

            for (int i = list.length - 1; i >= 1; i--) {
                //Actual movement should be done here
                Creature creature = list[i];
                if (creature.getCreatureType() == DEAD || creature.getCreatureType() == BLOCK) continue;

                Position oldPosition = creature.getPosition();
                creature.tick();
                if (!oldPosition.equals(creature.getPosition()) && !creature.getCreatureType().isChip()) {
                    --creatureLayer[oldPosition.index];
                    ++creatureLayer[creature.getPosition().index];
                }
            }
            secondCall = true;
        }
        else {
            for (int i = list.length - 1; i >= 0; i--) {
                Creature creature = list[i];
                if (level.getLayerFG().get(creature.getPosition()) == Tile.TELEPORT) {
                    //todo
                }
            }
            secondCall = false;
        }
    }

    @Override
    public int getCreaturesAtPosition(Position position) {
        return creatureLayer[position.index];
    }

    @Override
    public void addClone(Position position) {

    }

    public LynxCreatureList(LynxCreature[] creatures) {
        super(creatures);
        creatureLayer = new int[1024];

        for (int i=1; i < creatures.length; i++) { //start at 1 to skip over Chip who shouldn't be added
            Creature c = creatures[i];
            creatureLayer[c.getPosition().index]++;
        }
    }
}
