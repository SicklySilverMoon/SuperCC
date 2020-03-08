package game.Lynx;

import game.*;

import static game.CreatureID.*;
import static game.CreatureID.TANK_MOVING;
import static game.Tile.BLOCK_RIGHT;
import static game.Tile.BLOCK_UP;

public class LynxCreature implements Creature {

    private Position position;
    private CreatureID creatureType;
    private Direction direction;
    private boolean sliding;
    private int timeTraveled;

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

    @Override
    public String toString() {
        if (creatureType == DEAD) return "Dead monster at position " + position;
        return creatureType+" facing "+direction+" at position "+position;
    }

    /** Returns a number representing how long the creature has been traveling between tiles.
     * Of note is that for all creatures except blobs this value increases by 2 for every tick completed.
     *
     * @return An int between 0 and 7 (inclusive)
     * that represents how many quarter moves the creature has been traveling between tiles
     */
    public int getTimeTraveled() {
        return timeTraveled;
    }

    boolean tick(Level level, boolean slidingMove) {
        return false;
    }

    public LynxCreature(Position position, Tile tile) {
        this.position = position;

        if (BLOCK_UP.ordinal() <= tile.ordinal() && tile.ordinal() <= BLOCK_RIGHT.ordinal()){
            direction = Direction.fromOrdinal((tile.ordinal() + 2) % 4);
            creatureType = BLOCK;
        }
        else{
            direction = Direction.fromOrdinal(tile.ordinal() % 4);
            if (tile == Tile.BLOCK) creatureType = BLOCK;
            else {
                creatureType = CreatureID.fromOrdinal((tile.ordinal() - 0x40) >>> 2);
            }
        }
        if (creatureType == TANK_STATIONARY) creatureType = TANK_MOVING;
    }

    public LynxCreature(int bitMonster) {
        direction = Direction.fromOrdinal(bitMonster >>> 14);
        creatureType = CreatureID.fromOrdinal((bitMonster >>> 10) & 0b1111);
        if (creatureType == CHIP_SLIDING) sliding = true; //TODO: Chip sliding probably doesn't need to exist for lynx
        position = new Position(bitMonster & 0b00_0000_1111111111);
    }
}
