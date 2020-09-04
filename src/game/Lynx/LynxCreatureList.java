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
        /*  EVEN+0 1234____9ABC____
            EVEN+1 123____89AB____0
            EVEN+2 12____789A____F0
            EVEN+3 1____6789____EF0
            ODD+0  ____5678____DEF0
            ODD+1  ___4567____CDEF_
            ODD+2  __3456____BCDE__
            ODD+3  _2345____ABCD___  */
        teethStep = ((level.getTickNumber()-1 + level.getStep().ordinal()) & 4) == 0;
        System.out.printf("%d + %d = %d : %b\n", level.getTickNumber(), level.getStep().ordinal(), level.getTickNumber() + level.getStep().ordinal(), teethStep);

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
            if (creature.getTimeTraveled() != 0 || creature.isDead() ||  creature.getCreatureType().isChip()
                    || level.getLayerFG().get(creature.getPosition()) == Tile.CLONE_MACHINE)
                continue;

            for (Direction dir : creature.getDirectionPriority(chip, level.getRNG())) {
                if (dir == null)
                    continue;
                Position newPosition = creature.getPosition().move(dir);
                Tile currentTile = level.getLayerFG().get(creature.getPosition());
                Tile newTile = level.getLayerFG().get(newPosition);
                boolean canMove = (creature.canEnter(dir, newTile) && creature.canLeave(dir, currentTile, creature.getPosition()));

                if (!canMove || getCreaturesAtPosition(newPosition) != 0)
                    continue;
                if (!newPosition.isValid())
                    continue;
                Creature anim = getAnimationAtPosition(newPosition);
                if (anim != null) {
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
            if (creature.getCreatureType().isChip() || level.getLayerFG().get(creature.getPosition()) == Tile.CLONE_MACHINE
                || (creature.getCreatureType() == TEETH && !teethStep && creature.getTimeTraveled() == 0))
                continue;
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
            return 0;
        return creatureLayer[position.index];
    }

    private Creature getAnimationAtPosition(Position position) {
        if (!position.isValid())
            return null;
        return animationLayer[position.index];
    }

    @Override
    public void addClone(Position position) {
        if (level.getLayerFG().get(position) != Tile.CLONE_MACHINE)
            return;

        Creature template = null;
        Tile newTile = null;
        Position newPosition = null;
        Direction direction = null;

        for (Creature c: list) {
            if (c.getPosition().equals(position)) {
                template = c;
                direction = template.getDirection();
                newPosition = template.getPosition().move(direction);
                newTile = level.getLayerFG().get(template.getPosition().move(direction));
            }
        }
        if (template == null || newTile == null || newPosition == null)
            return;

        Creature clone;
        if (!template.canEnter(direction, newTile) || getCreaturesAtPosition(newPosition) != 0)
            return;

        clone = template.clone();
        boolean replacedDead = false;
        for (int i=0; i < list.length; i++) {
            Creature c = list[i];
            if (c.isDead() && c.getAnimationTimer() == 0) {
                list[i] = clone;
                replacedDead = true;
                break;
            }
        }
        if (!replacedDead) {
            newClones.add(clone);
        }

        clone.tick(direction);
        updateLayer(clone, template.getPosition(), clone.getCreatureType());
    }

    private void updateLayer(Creature creature, Position oldPosition, CreatureID oldCreatureType) {
        if (!oldPosition.equals(creature.getPosition()) && !creature.getCreatureType().isChip()) {
            --creatureLayer[oldPosition.index];
            ++creatureLayer[creature.getPosition().index];
            return;
        }
        if (creature.isDead()) {
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
        if (chip.getTimeTraveled() != 0 || chip.getAnimationTimer() != 0) {
            chip.tick(null);
            return;
        }
        if (directions.length == 0) return;
        //todo: sliding bullshit lol

        if (directions.length > 1) {
            assert directions.length == 2;
            for (int i = 0; i < directions.length; i++) {
                Direction dir = directions[i];
                if (chip.getDirection() == dir) {
                    int j = (i == 0 ? 1 : 0);
                    directions[i] = directions[j];
                    directions[j] = dir;
                }
            }
        }

        Position newPosition = chip.getPosition().move(directions[0]);
        Creature anim = getAnimationAtPosition(newPosition);
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
            pushBlock(chip.getPosition().move(directions[1]), directions[1]);
        }

        //blocks ahead of chip
        pushBlock(chip.getPosition().move(directions[0]), directions[0]);

        chip.tick(directions[0]);
        chipDeathCheck();
    }

    private void pushBlock(Position position, Direction direction) {
        Creature block = creatureAt(position);
        if (block != null && block.getCreatureType() == CreatureID.BLOCK && block.getTimeTraveled() == 0) {
            CreatureID oldCreatureType = block.getCreatureType();
            Position oldPosition = block.getPosition();
            block.setDirection(direction);
            block.tick(direction);
            updateLayer(block, oldPosition, oldCreatureType);
        }
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
