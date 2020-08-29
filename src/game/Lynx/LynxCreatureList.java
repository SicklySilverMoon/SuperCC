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
        animationLayer = new Creature[1024];

        for (Creature c : creatures) {
            if (c.getCreatureType() == CHIP)
                continue;
            if (c.getCreatureType() != DEAD)
                creatureLayer[c.getPosition().index]++;
            else if (c.getAnimationTimer() != 0)
                animationLayer[c.getPosition().index] = c;
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
                if (animationLayer[newPosition.index] != null) {
                    Creature anim = animationLayer[newPosition.index];
                    anim.kill(); //killing an animation stops the animation
                    updateLayer(anim, anim.getPosition(), anim.getCreatureType());
                }

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

            CreatureID oldCreatureType = creature.getCreatureType();
            Position oldPosition = creature.getPosition();
            creature.tick(direction);
            updateLayer(creature, oldPosition, oldCreatureType);
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
        if (!position.isValid())
            return -1;
        return creatureLayer[position.index];
    }

    @Override
    public void addClone(Position position) {

    }

    private void updateLayer(Creature creature, Position oldPosition, CreatureID oldCreatureType) {
        if (!oldPosition.equals(creature.getPosition()) && !creature.getCreatureType().isChip()) {
            --creatureLayer[oldPosition.index];
            ++creatureLayer[creature.getPosition().index];
            return;
        }
        if (creature.getCreatureType() == DEAD) {
            if (creature.getCreatureType() != oldCreatureType)
                --creatureLayer[oldPosition.index];
            if (creature.getAnimationTimer() != 0)
                animationLayer[creature.getPosition().index] = creature;
            else animationLayer[creature.getPosition().index] = null;
        }
    }

    private void moveChip() {
        if (chipDeathCheck())
            return;

        Creature chip = level.getChip();
        Direction[] directions = chip.getDirectionPriority(chip, null);
        if (chip.getTimeTraveled() != 0) {
            chip.tick(null);
            return;
        }
        if (directions.length == 0) return;
        //todo: sliding bullshit lol

        Position newPosition = chip.getPosition().move(directions[0]);
        Creature anim = animationLayer[newPosition.index];
        if (!chip.canEnter(directions[0], level.getLayerFG().get(newPosition))
                || (anim != null && anim.getAnimationTimer() != 0)) {
            //todo: rewrite based on the fact that the diag inputs are always a specific direction pattern, which needs to be adjusted based on Chip's current direction
            if (directions.length > 1) {
                Direction first = directions[0];
                directions[0] = directions[1];
                directions[1] = first;
            }
            else {
                chip.setDirection(directions[0]);
                return;
            }
        }

        chip.setDirection(directions[0]); //chip just ignores the rules about can move into tiles and such

        //block slap
        if (directions.length > 1) {
            Position slapPosition = chip.getPosition().move(directions[1]);
            Creature toSlap = creatureAt(slapPosition);
            if (toSlap != null && toSlap.getCreatureType() == CreatureID.BLOCK && toSlap.getTimeTraveled() == 0) {
                CreatureID oldCreatureType = toSlap.getCreatureType();
                Position oldPosition = toSlap.getPosition();
                toSlap.setDirection(directions[1]);
                toSlap.tick(directions[1]);
                updateLayer(toSlap, oldPosition, oldCreatureType);
            }
        }

        //blocks ahead of chip
        Creature creatureAhead = creatureAt(chip.getPosition().move(directions[0]));
        if (creatureAhead != null && creatureAhead.getCreatureType() == CreatureID.BLOCK
                && creatureAhead.getTimeTraveled() == 0) {
            CreatureID oldCreatureType = creatureAhead.getCreatureType();
            Position oldPosition = creatureAhead.getPosition();
            creatureAhead.setDirection(directions[0]);
            creatureAhead.tick(directions[0]);
            updateLayer(creatureAhead, oldPosition, oldCreatureType);
        }
        chip.tick(directions[0]);
        chipDeathCheck();
    }

    /**
     * @return true if Chip is killed as a result of this, false otherwise
     */
    private boolean chipDeathCheck() {
        Creature chip = level.getChip();
        if (getCreaturesAtPosition(chip.getPosition()) != 0) {
            chip.kill();
            return true;
        }
        return false;
    }

    public LynxCreatureList(Creature[] creatures) {
        super(creatures);
        setCreatures(creatures);
    }
}
