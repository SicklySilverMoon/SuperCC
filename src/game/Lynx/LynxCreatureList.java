package game.Lynx;

import game.*;

import static game.CreatureID.*;

public class LynxCreatureList extends CreatureList {
    private int[] creatureLayer; //a faux 'layer', whenever a creature moves into a position increment the resulting position index in this array. decrement it when they leave
    private Creature[] animationLayer; //Another faux layer that holds references to DEAD creatures that currently have animations playing, a position's index matches to an index in this array

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
        newClones.clear();
    }

    @Override
    public void finalise() {
        if (newClones.size() == 0) return;

        int length = list.length + newClones.size();
        Creature[] newList = new Creature[length];

        System.arraycopy(list, 0, newList, 0, list.length);
        for (int i=0; i < newClones.size(); i++) {
            newList[i + list.length] = newClones.get(i);
        }

        list = newList;
        newClones.clear();
    }

    @Override
    public void tick() {
        Creature chip = level.getChip();
        for (int i = list.length - 1; i >= 0; i--) {
            //todo: lol death animations are only solid for Chip but other monsters cut them short, meaning we gotta create some kind of solution to facilitate interactions, maybe a another array that holds references to animations on whatever tiles
            Creature creature = list[i];
            if (creature.getTimeTraveled() != 0 || creature.getCreatureType() == DEAD || creature.getCreatureType().isChip())
                continue;

            for (Direction dir : creature.getDirectionPriority(chip, level.getRNG())) {
                if (dir == null) continue;
                Position newPosition = creature.getPosition().move(dir);
                Tile currentTile = level.getLayerFG().get(creature.getPosition());
                Tile newTile = level.getLayerFG().get(newPosition);
                boolean canMove = (creature.canEnter(dir, newTile) && creature.canLeave(dir, currentTile, creature.getPosition()));

                if (!canMove || getCreaturesAtPosition(newPosition) != 0) continue;
                if (!newPosition.isValid()) continue;

                creature.setDirection(dir);
                break;
            }
        }

        for (int i = list.length - 1; i >= 0; i--) {
            //Actual movement should be done here
            Creature creature = list[i];
            Direction direction = null;
            if (creature.getCreatureType().isChip()) continue;
            if (creature.getCreatureType() != BLOCK && creature.getCreatureType() != CHIP_SWIMMING)
                direction = creature.getDirection();

            Position oldPosition = creature.getPosition();
            creature.tick(direction);
            updateLayer(creature, oldPosition);
        }

        moveChip();

        for (int i = list.length - 1; i >= 0; i--) {
            Creature creature = list[i];
            if (level.getLayerFG().get(creature.getPosition()) == Tile.TELEPORT) {
                //todo
            }
        }
    }

    @Override
    public int getCreaturesAtPosition(Position position) {
        return creatureLayer[position.index];
    }

    @Override
    public void addClone(Position position) {

    }

    private void updateLayer(Creature creature, Position oldPosition) {
        if (!oldPosition.equals(creature.getPosition()) && !creature.getCreatureType().isChip()) {
            --creatureLayer[oldPosition.index];
            ++creatureLayer[creature.getPosition().index];
        }
    }

    private void moveChip() {
        Creature chip = level.getChip();
        Direction[] directions = chip.getDirectionPriority(chip, null);
        if (chip.getTimeTraveled() != 0) {
            chip.tick(null);
            if (getCreaturesAtPosition(chip.getPosition()) != 0) chip.kill();
            return;
        }
        if (directions.length == 0) return;
        //todo: sliding bullshit lol
        chip.setDirection(directions[0]); //chip just ignores the rules about can move into tiles and such

        //block slap
        if (directions.length > 1) {
            Position slapPosition = chip.getPosition().move(directions[1]);
            Creature toSlap = creatureAt(slapPosition);
            if (toSlap != null && toSlap.getCreatureType() == CreatureID.BLOCK && toSlap.getTimeTraveled() == 0) {
                Position oldPosition = toSlap.getPosition();
                toSlap.setDirection(directions[1]);
                toSlap.tick(directions[1]);
                updateLayer(toSlap, oldPosition);
            }
        }

        //blocks ahead of chip
        Creature creatureAhead = creatureAt(chip.getPosition().move(directions[0]));
        if (creatureAhead != null && creatureAhead.getCreatureType() == CreatureID.BLOCK
                && creatureAhead.getTimeTraveled() == 0) {
            Position oldPosition = creatureAhead.getPosition();
            creatureAhead.setDirection(directions[0]);
            creatureAhead.tick(directions[0]);
            updateLayer(creatureAhead, oldPosition);
        }
        //todo: the bullshittery about diagonal moves and swapping to the secondary when blocked
        chip.tick(directions[0]);
        if (getCreaturesAtPosition(chip.getPosition()) != 0) chip.kill();
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
