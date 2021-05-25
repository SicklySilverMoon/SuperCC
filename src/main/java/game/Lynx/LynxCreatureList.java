package game.Lynx;

import game.*;
import game.button.BrownButton;

import java.util.HashMap;
import java.util.Map;

import static game.CreatureID.*;

public class LynxCreatureList extends CreatureList {
    private boolean[] creatureLayer;
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
                creature.setFDirection(Direction.NONE);
                creature.setTDirection(Direction.NONE);

                if (creature.getTimeTraveled() != 0 || creature.getCreatureType() == CHIP)
                    continue;

                if (creature.isDead()) {
                    if (creature.getAnimationTimer() != 0)
                        creature.tick();
                    continue;
                }

                Tile currentTile = level.getLayerFG().get(creature.getPosition());
                if (creature.getForcedMove(currentTile))
                    continue;

                if (creature.getCreatureType() == BLOCK)
                    continue;

                if (currentTile == Tile.CLONE_MACHINE || currentTile == Tile.TRAP) {
                    creature.setTDirection(creature.getDirection());
                    continue;
                }

                Direction[] moves = creature.getDirectionPriority(chip, level.getRNG());
                for (Direction dir : moves) {
                    if (dir == Direction.NONE)
                        break;

                    Position crPos = creature.getPosition();
                    Position newPos = crPos.move(dir);
                    creature.setTDirection(dir);
                    boolean canMove = creature.canEnter(dir, newPos, false, true) && creature.canLeave(dir, crPos);
                    if (canMove)
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
                if (creature.getCreatureType() == CHIP || level.getLayerFG().get(creature.getPosition()) == Tile.CLONE_MACHINE
                        || creature.getCreatureType() == DEAD
                        || (creature.getCreatureType() == TEETH && !teethStep && creature.getTimeTraveled() == 0))
                    continue;

                creature.tick();

                creature.setFDirection(Direction.NONE);
                creature.setTDirection(Direction.NONE);
                if (creature.getTimeTraveled() == 0 && level.getLayerFG().get(creature.getPosition()) == Tile.BUTTON_BROWN) {
                    for (BrownButton b : level.getBrownButtons()) {
                        if (b.getButtonPosition().equals(creature.getPosition())) {
                            springTrappedCreature(b.getTargetPosition());
                            break;
                        }
                    }
                }
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
        if (!position.isValid() || level.getLayerFG().get(position) != Tile.CLONE_MACHINE)
            return;
        Creature creature = creatureAt(position, true);
        if (creature == null)
            return;

        //equivalent to TW's newcreature
        Creature clone = null;
        for (int i = 0; i < list.length; i++) {
            Creature c = list[i];
            if (c.isDead() && c.getAnimationTimer() == 0) {
                clone = creature.clone();
                list[i] = clone;
            }
        }
        if (list.length + newClones.size() >= 2048) { //MAX_CREATURES in TW
            creature.tick();
            return;
        }
        if (clone == null) {
            clone = creature.clone();
            newClones.add(clone);
        }

        clone.setTDirection(clone.getDirection());
        clone.tick(); //todo: might want to check how TW uses the releasing flag
    }

    @Override
    public void springTrappedCreature(Position position) {
        if (!position.isValid() || level.getLayerFG().get(position) != Tile.TRAP)
            return;
        Creature creature = creatureAt(position, true);
        if (creature == null)
            return;
        creature.setTDirection(creature.getDirection());
        creature.tick(); //todo: releasing flag again
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
