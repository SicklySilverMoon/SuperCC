package game.Lynx;

import game.*;

import static game.CreatureID.*;

public class LynxCreatureList extends CreatureList {
    private boolean[] creatureLayer;
    private Creature[] animationLayer; //Another faux layer that holds references to DEAD creatures that currently have animations playing, a position's index matches to an index in this array
    private int phase;

    @Override
    public void setCreatures(Creature[] creatures, Layer layerFG, Layer layerBG) {
        list = creatures;
        creatureLayer = new boolean[1024];
        animationLayer = new Creature[1024];

        for (Creature c : creatures) {
            if (c.getCreatureType() == CHIP || layerFG.get(c.getPosition()) == Tile.CLONE_MACHINE)
                continue;
            if (c.getCreatureType() != DEAD)
                creatureLayer[c.getPosition().index] = true;
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
        if (phase == 0) {
            for (int i = list.length - 1; i >= 0; i--) {
                Creature creature = list[i];
                if (creature.getTimeTraveled() != 0 || creature.isDead() || level.getLayerFG().get(creature.getPosition()) == Tile.CLONE_MACHINE)
                    continue;

                if (creature.getCreatureType() == CHIP)
                    continue;

                Direction[] moves = creature.getDirectionPriority(chip, level.getRNG());
                for (int j=0; j < moves.length; j++) {
                    Direction dir = moves[j];
                    if (dir == null)
                        continue;
                    Position newPosition = creature.getPosition().move(dir);
                    Tile currentTile = level.getLayerFG().get(creature.getPosition());
                    Tile newTile = level.getLayerFG().get(newPosition);
                    boolean canMove = (creature.canEnter(dir, newTile) && creature.canLeave(dir, currentTile, creature.getPosition()));

                    if (!canMove || claimed(newPosition) || !newPosition.isValid()) {
                        if (j == moves.length - 1) {
                            creature.setDirection(dir);
                            break;
                        }
                        continue;
                    }
                    Creature anim = animationAt(newPosition);
                    if (anim != null) {
                        anim.kill(); //killing an animation stops the animation
                        updateLayer(anim, anim.getPosition(), anim.getCreatureType());
                    }

                    creature.setDirection(dir);
                    break;
                }
            }
            phase = 1;
            return;
        }

        if (phase == 1) {
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
            phase = 2;
            return;
        }

        if (phase == 2) {
            for (int i = list.length - 1; i >= 0; i--) {
                Creature creature = list[i];
                if (level.getLayerFG().get(creature.getPosition()) == Tile.TELEPORT) {
                    if (creature.getTimeTraveled() != 0 || creature.getAnimationTimer() != 0)
                        continue;
                    if (level.getLayerFG().get(creature.getPosition()) == Tile.TELEPORT)
                        teleportCreature(creature);
                }
            }
            phase = 0;
            return;
        }
    }

    @Override
    public boolean claimed(Position position) {
        if (!position.isValid())
            return false;
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
        if (!template.canEnter(direction, newTile) || claimed(newPosition))
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
        if (level.getLayerFG().get(position) != Tile.TRAP || !claimed(position)
        || !level.isTrapOpen(position))
            return;

        Creature trapped = creatureAt(position);
        CreatureID trappedType = trapped.getCreatureType();
        trapped.tick(trapped.getDirection());
        updateLayer(trapped, position, trappedType);
    }

    @Override
    public boolean tickCreature(Creature creature, Direction direction) {
        for (Creature c : list) {
            if (c == creature) {
                CreatureID oldCreatureType = creature.getCreatureType();
                Position oldPosition = creature.getPosition();
                boolean result = creature.tick(direction);
                updateLayer(creature, oldPosition, oldCreatureType);
                return result;
            }
        }
        return false;
    }

    private void updateLayer(Creature creature, Position oldPosition, CreatureID oldCreatureType) {
        if (!oldPosition.equals(creature.getPosition()) && !creature.getCreatureType().isChip()) {
            if (level.getLayerFG().get(oldPosition) != Tile.CLONE_MACHINE)
                creatureLayer[oldPosition.index] = false;
            creatureLayer[creature.getPosition().index] = true;
            return;
        }
        if (creature.isDead()) {
            if (creature.getCreatureType() != oldCreatureType)
                creatureLayer[oldPosition.index] = false;
            if (creature.getAnimationTimer() != 0)
                animationLayer[creature.getPosition().index] = creature;
            else animationLayer[creature.getPosition().index] = null;
        }
    }

    private void teleportCreature(Creature creature) {
        Position originalPosition = creature.getPosition();
        Position[] teleports = level.getTeleports();
        int index = 0;
        for (int i = 0; i < teleports.length; i++) {
            Position teleportPosition = teleports[i];
            if (teleportPosition.equals(originalPosition)) {
                index = i;
                break;
            }
        }

        for (;;) {
            --index;
            if (index < 0)
                index = teleports.length - 1;
            Position teleportPosition = teleports[index];
            if (creature.getCreatureType() != CHIP)
                creatureLayer[creature.getPosition().index] = false;
            creature.setPosition(teleportPosition);
            if (!claimed(teleportPosition) && creature.canEnter(creature.getDirection(), level.getLayerFG().get(teleportPosition)))
                break;
            if (teleportPosition.equals(originalPosition)) {
                if (creature.getCreatureType() != CHIP)
                    creatureLayer[originalPosition.index] = true;
                return;
            }
        }

        if (creature.getCreatureType() != CHIP)
            creatureLayer[creature.getPosition().index] = true;
    }

    public LynxCreatureList(Creature[] creatures, Layer layerFG, Layer layerBG) {
        super(creatures);
        setCreatures(creatures, layerFG, layerBG);
    }
}
