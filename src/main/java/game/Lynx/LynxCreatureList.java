package game.Lynx;

import game.*;
import game.button.BrownButton;

import static game.CreatureID.*;

public class LynxCreatureList extends CreatureList {
    private boolean[] creatureLayer;
    private int phase;

    @Override
    public void setCreatures(Creature[] creatures) {
        list = creatures;
        creatureLayer = new boolean[1024];

        for (Creature c : creatures) {
            if (c.getCreatureType() == CHIP)
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
        chipToCr = null;
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
                        creature.tick(false);
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
                boolean canMove = false;
                for (Direction dir : moves) {
                    if (dir == Direction.NONE)
                        break;

                    if (dir == Direction.WALKER_TURN) {
                        int turns = level.getRNG().pseudoRandom4();
                        dir = creature.getDirection();
                        while (turns-- != 0)
                            dir = dir.turn(Direction.RIGHT);
                    }
                    else if (dir == Direction.BLOB_TURN) {
                        dir = new Direction[] {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT}[level.getRNG().random4()];
                    }

                    Position crPos = creature.getPosition();
                    Position newPos = crPos.move(dir);
                    creature.setTDirection(dir);
                    if (creature.canMakeMove(dir, newPos, true, false, false, false)) {
                        canMove = true;
                        break;
                    }
                }
                if (!canMove && creature.getCreatureType() == TEETH)
                    creature.setTDirection(moves[0]); //emulation of TW's pdir as teeth were the only thing that used it
            }
            phase = 1;
            return;
        }

        if (phase == 1) {
            for (int i = list.length - 1; i >= 0; i--) {
                //Actual movement should be done here
                Creature creature = list[i];
                if (creature.getCreatureType() == CHIP || level.getLayerFG().get(creature.getPosition()) == Tile.CLONE_MACHINE
                        || creature.getCreatureType() == DEAD)
                    continue;

                creature.tick(false);

                creature.setFDirection(Direction.NONE);
                creature.setTDirection(Direction.NONE);
                if (creature.getTimeTraveled() == 0 && level.getLayerFG().get(creature.getPosition()) == Tile.BUTTON_BROWN) {
                    BrownButton b = level.getBrownButtons().get(creature.getPosition());
                    if (b != null)
                        springTrappedCreature(b.getTargetPosition());
                }
            }
            phase = 2;
            return;
        }

        if (phase == 2) {
            for (int i = list.length - 1; i >= 0; i--) {
                Creature creature = list[i];
                if (level.getLayerFG().get(creature.getPosition()) == Tile.TELEPORT) {
                    if (creature.getTimeTraveled() != 0 || creature.isDead())
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
    public boolean[] getClaimedArray() {
        return creatureLayer;
    }

    @Override
    public void setClaimedArray(boolean[] claimedArray) {
        this.creatureLayer = claimedArray;
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
        for (int i = 1; i < list.length; i++) {
            Creature c = list[i];
            if (c.isDead() && c.getAnimationTimer() == 0) {
                clone = creature.clone();
                list[i] = clone;
                break;
            }
        }
        if (clone == null && list.length + newClones.size() >= 2048) { //MAX_CREATURES in TW
            creature.tick(true);
            return;
        }
        boolean newClone = false;
        if (clone == null) {
            clone = creature.clone();
            newClones.add(clone);
            newClone = true;
        }

        if (!creature.tick(true)) {
            if (newClone)
                newClones.remove(clone);
            clone.kill();
            clone.kill(); //kill creature and anim
        }
    }

    @Override
    public void springTrappedCreature(Position position) {
        if (position == null || !position.isValid() || level.getLayerFG().get(position) != Tile.TRAP)
            return;
        Creature creature = creatureAt(position, true);
        if (creature == null || creature.getDirection() == Direction.NONE)
            return;
        creature.tick(true);
    }

    @Override
    public void addCreature(Creature creature) {
        if (list.length + newClones.size() >= 2048)
            return;
        newClones.add(creature);
        if (!creature.isDead())
            adjustClaim(creature.getPosition(), true);
    }

    private void teleportCreature(Creature creature) {
        Position[] teleports = level.getTeleports();
        int teleportIndex = -1;
        for (int i = 0; i < level.getTeleports().length; i++) {
            if (teleports[i].equals(creature.getPosition())) {
                teleportIndex = i;
                break;
            }
        }
        if (teleportIndex == -1) //not found
            return;
        int origIndex = teleportIndex; //use then dec

        while (true) {
            --teleportIndex;
            if (teleportIndex == -1)
                teleportIndex = teleports.length - 1;

            Position telePos = teleports[teleportIndex];
            if (creature.getCreatureType() != CHIP)
                adjustClaim(creature.getPosition(), false);

            creature.setPosition(telePos);
            if (!claimed(telePos) && creature.canMakeMove(creature.getDirection(), telePos.move(creature.getDirection()),
                    false, false, false, false)) {
                break;
            }
            if (teleportIndex == origIndex) {
                if (creature.getCreatureType() == CHIP) //TW sets chipstuck() to true here but its a death sentence
                    creature.kill();
                else
                    adjustClaim(telePos, true);
                return;
            }
            //note: TW has something about else if (ismarkedteleport(pos)) around this part, find out why maybe
        }

        if (creature.getCreatureType() != CHIP)
            adjustClaim(creature.getPosition(), true);
        creature.setTeleportFlag(true);
        return;
    }

    public LynxCreatureList(Creature[] creatures) {
        super(creatures);
        setCreatures(creatures);
    }
}
