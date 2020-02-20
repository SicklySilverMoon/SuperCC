package game.Lynx;

import game.*;

/**
 * Creatures are encoded as follows:
 *
 *       0 0    | 0 0 0 0 | 0 0 0 0 0 | 0 0 0 0 0
 *    DIRECTION | MONSTER |    ROW    |    COL
 */
public class LynxCreature implements Creature {

    private Position position;
    private CreatureID creatureType;
    private Direction direction;
    private boolean sliding;

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public CreatureID getCreatureType() {
        return creatureType;
    }

    @Override
    public void setCreatureType(CreatureID creatureType) {
        this.creatureType = creatureType;
    }

    @Override
    public void kill() {
        creatureType = CreatureID.DEAD;
    }

    @Override
    public boolean isDead() {
        return creatureType == CreatureID.DEAD;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void turn(Direction turn) {
        direction = direction.turn(turn);
    }

    @Override
    public boolean isSliding() {
        return sliding;
    }

    @Override
    public Tile toTile() {
        return null; //This might not even be necessary
    }

    @Override
    public int bits() {
        return direction.getBits() | creatureType.getBits() | position.getIndex();
    }

    @Override
    public void setSliding(boolean sliding) {
        this.sliding = sliding;
    }

    @Override
    public void setSliding(boolean sliding, Level genLevel) {
        //This might not even be necessary
    }

    public LynxCreature(Position position, Tile tile) {
        this.position = position;
    }
}
