package game.Lynx;

import game.*;

import java.util.HashMap;
import java.util.Map;

import static game.CreatureID.*;

public class LynxCreatureList extends CreatureList {
    private boolean[] creatureLayer;
    private Map<Creature, Direction> forcedDirs = new HashMap<>();
    private int phase;

    @Override
    public void setCreatures(Creature[] creatures, Layer layerFG, Layer layerBG) {
        list = creatures;
        creatureLayer = new boolean[1024];

        for (Creature c : creatures) {
            if (c.getCreatureType() == CHIP || layerFG.get(c.getPosition()) == Tile.CLONE_MACHINE)
                continue;
            if (c.getCreatureType() != DEAD)
                creatureLayer[c.getPosition().index] = true;
        }

        for (Creature c : list)
            c.setLevel(level);
    }

    @Override
    public void initialise() {
        teethStep = ((level.getTickNumber()-1 + level.getStep().ordinal()) & 4) == 0;

        newClones.clear();
        forcedDirs.clear();
    }

    @Override
    public void finalise() {
        if (newClones.size() == 0)
            return;

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
                if (creature.getTimeTraveled() != 0 || creature.getCreatureType() == CHIP || level.getLayerFG().get(creature.getPosition()) == Tile.CLONE_MACHINE)
                    continue;

                if (creature.isDead()) {
                    if (creature.getAnimationTimer() != 0)
                        creature.tick(Direction.NONE);
                    continue;
                }

                Tile currentTile = level.getLayerFG().get(creature.getPosition());
                if (getForcedMove(creature, currentTile))
                    continue;

                Direction[] moves = creature.getDirectionPriority(chip, level.getRNG());
                for (int j=0; j < moves.length; j++) {
                    Direction dir = moves[j];
                    if (dir == null)
                        continue;
                    Position newPosition = creature.getPosition().move(dir);
                    boolean canMove = (creature.canEnter(dir, newPosition, false, true) && creature.canLeave(dir, creature.getPosition()));

                    if (!canMove) {
                        if (j == moves.length - 1) {
                            if (dir != Direction.NONE)
                                creature.setDirection(dir);
                            break;
                        }
                        continue;
                    }

                    if (dir != Direction.NONE)
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
                if (creature.getCreatureType().isChip() || level.getLayerFG().get(creature.getPosition()) == Tile.CLONE_MACHINE
                        || creature.getCreatureType() == DEAD
                        || (creature.getCreatureType() == TEETH && !teethStep && creature.getTimeTraveled() == 0))
                    continue;

                Direction direction = null;
                if (creature.getCreatureType() == BLOCK || creature.getCreatureType() == CHIP_SWIMMING)
                    direction = Direction.NONE; //todo: see a corrosponding todo in creature.tick
                if (forcedDirs.containsKey(creature)) //why spent 4 bits on saving fdir to a creature when you could just do this
                    direction = forcedDirs.get(creature);
                creature.tick(direction);
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
    public void adjustClaim(Position position, boolean claim) {
        if (!position.isValid())
            return;
        creatureLayer[position.index] = claim;
    }

    @Override
    public Creature animationAt(Position position) {
        if (!position.isValid())
            return null;
        for (Creature creature : list)
            if (creature.getPosition().equals(position) && creature.getCreatureType() == DEAD && creature.getAnimationTimer() != 0)
                return creature;

        return null;
    }

    @Override
    public void addClone(Position position) {
        if (level.getLayerFG().get(position) != Tile.CLONE_MACHINE)
            return;

        Creature template = null;
        Position newPosition = null;
        Direction direction = null;

        for (Creature c: list) {
            if (c.getPosition().equals(position)) {
                template = c;
                direction = template.getDirection();
                newPosition = template.getPosition().move(direction);
            }
        }
        if (template == null || newPosition == null)
            return;

        Creature clone;
        if (!template.canEnter(direction, newPosition, false, true))
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
    }

    @Override
    public void springTrappedCreature(Position position) {
        if (level.getLayerFG().get(position) != Tile.TRAP || !level.isTrapOpen(position))
            return;
        Creature trapped = creatureAt(position, true);
        if (trapped == null)
            return;
        CreatureID trappedType = trapped.getCreatureType();
        trapped.tick(trapped.getDirection());
    }

    private boolean getForcedMove(Creature creature, Tile tile) {
        if (tile.isSliding()) { //replication of TW's getforcedmove(), todo: check how TW uses and sets the teleport flag
            Direction slideDir = creature.getSlideDirection(creature.getDirection(), tile, null, true);
            if (tile.isIce()) {
                if (creature.getDirection() == Direction.NONE)
                    return false;
                if (creature.getCreatureType() == CHIP && level.getBoots()[2] != 0)
                    return false;
                forcedDirs.put(creature, slideDir);
                creature.setDirection(slideDir);
                return true;
            }
            else if (tile.isFF()) {
                forcedDirs.put(creature, slideDir);
                creature.setDirection(slideDir);
                return !creature.canOverride();
            }
            else if (tile == Tile.TELEPORT) { //please find how the CS_TELEPORTED flag works in TW
                forcedDirs.put(creature, slideDir);
                creature.setDirection(slideDir);
                return true;
            }
        }
        return false;
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
