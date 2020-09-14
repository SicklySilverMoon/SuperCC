package game.Lynx;

import game.*;

import static game.CreatureID.*;

public class LynxCreatureList extends CreatureList {
    private int[] creatureLayer; //a faux 'layer', whenever a creature moves into a position increment the resulting position index in this array. decrement it when they leave
    private Creature[] animationLayer; //Another faux layer that holds references to DEAD creatures that currently have animations playing, a position's index matches to an index in this array

    @Override
    public void setCreatures(Creature[] creatures, Layer layerFG, Layer layerBG) {
        list = creatures;
        creatureLayer = new int[1024];
        animationLayer = new Creature[1024];

        for (Creature c : creatures) {
            if (c.getCreatureType() == CHIP || layerFG.get(c.getPosition()) == Tile.CLONE_MACHINE)
                continue;
            if (c.getCreatureType() != DEAD)
                creatureLayer[c.getPosition().index]++;
            else if (c.getAnimationTimer() != 0)
                animationLayer[c.getPosition().index] = c;
        }
    }

    @Override
    public void initialise() {
        teethStep = ((level.getTickNumber()-1 + level.getStep().ordinal()) & 4) == 0;

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
            if (creature.getTimeTraveled() != 0 || creature.isDead() || level.getLayerFG().get(creature.getPosition()) == Tile.CLONE_MACHINE)
                continue;

            if (creature.getCreatureType() == CHIP) {
                selectChipMove();
                continue;
            }

            for (Direction dir : creature.getDirectionPriority(chip, level.getRNG())) {
                if (dir == null)
                    continue;
                Position newPosition = creature.getPosition().move(dir);
                Tile currentTile = level.getLayerFG().get(creature.getPosition());
                Tile newTile = level.getLayerFG().get(newPosition);
                boolean canMove = (creature.canEnter(dir, newTile) && creature.canLeave(dir, currentTile, creature.getPosition()));

                if (!canMove || numCreaturesAt(newPosition) != 0)
                    continue;
                if (!newPosition.isValid())
                    continue;
                Creature anim = animationAt(newPosition);
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
    public int numCreaturesAt(Position position) {
        if (!position.isValid())
            return 0;
        return creatureLayer[position.index];
    }

    @Override
    public Creature animationAt(Position position) {
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
        if (!template.canEnter(direction, newTile) || numCreaturesAt(newPosition) != 0)
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

    @Override
    public void springTrappedCreature(Position position) {
        if (level.getLayerFG().get(position) != Tile.TRAP || numCreaturesAt(position) == 0
        || !level.isTrapOpen(position))
            return;

        Creature trapped = creatureAt(position);
        CreatureID trappedType = trapped.getCreatureType();
        trapped.tick(trapped.getDirection());
        updateLayer(trapped, position, trappedType);
    }

    private void updateLayer(Creature creature, Position oldPosition, CreatureID oldCreatureType) {
        if (!oldPosition.equals(creature.getPosition()) && !creature.getCreatureType().isChip()) {
            if (level.getLayerFG().get(oldPosition) != Tile.CLONE_MACHINE)
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

    private void selectChipMove() {
        Creature chip = level.getChip();
        Direction[] directions = chip.getDirectionPriority(chip, null);

        if (directions.length == 0)
            return;
        if (directions.length > 1) {
            assert directions.length == 2;
            for (int j = 0; j < directions.length; j++) {
                Direction dir = directions[j];
                if (chip.getDirection() == dir && j > 0) {
                    int k = 0;
                    directions[j] = directions[k];
                    directions[k] = dir;
                    chip.setDirectionPriority(directions);
                    break;
                }
            }
        }

        Position newPosition = chip.getPosition().move(directions[0]);
        Tile currentTile = level.getLayerFG().get(chip.getPosition());
        Tile newTile = level.getLayerFG().get(newPosition);
        boolean canMove = (chip.canEnter(directions[0], newTile) && chip.canLeave(directions[0], currentTile, chip.getPosition()));

        if (!canMove || animationAt(newPosition) != null) {
            if (directions.length > 1) {
                Direction first = directions[0];
                directions[0] = directions[1];
                directions[1] = first;
                chip.setDirectionPriority(directions);
            }
            newPosition = chip.getPosition().move(directions[0]);
        }
        if (numCreaturesAt(newPosition) != 0 && creatureAt(newPosition).getCreatureType() != BLOCK) {
            chip.kill();
            creatureAt(newPosition).kill();
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
        if (directions.length == 0)
            return;
        //todo: Force floor override bullshit lol

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
        if (level.getLayerFG().get(position) == Tile.CLONE_MACHINE)
            return;
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
        if (numCreaturesAt(chip.getPosition()) != 0) {
            chip.kill();
            creatureAt(chip.getPosition()).kill();
            return true;
        }
        return false;
    }

    public LynxCreatureList(Creature[] creatures, Layer layerFG, Layer layerBG) {
        super(creatures);
        setCreatures(creatures, layerFG, layerBG);
    }
}
