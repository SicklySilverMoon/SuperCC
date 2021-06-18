package game;

import static game.CreatureID.DEAD;

public abstract class Creature {
    protected Level level;
    protected Position position;
    protected CreatureID creatureType;
    protected Direction direction;
    protected Direction tDirection = Direction.NONE;
    protected Direction fDirection = Direction.NONE;
    protected boolean sliding, teleportFlag;
    protected Direction nextMoveDirectionCheat = null;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getTDirection() {
        return tDirection;
    }

    public void setTDirection(Direction tDirection) {
        this.tDirection = tDirection;
    }

    public Direction getFDirection() {
        return fDirection;
    }

    public void setFDirection(Direction fDirection) {
        this.fDirection = fDirection;
    }

    public boolean getTeleportFlag() {
        return teleportFlag;
    }

    public void setTeleportFlag(boolean teleportFlag) {
        this.teleportFlag = teleportFlag;
    }

    public void setNextMoveDirectionCheat(Direction nextMoveDirectionCheat) {
        this.nextMoveDirectionCheat = nextMoveDirectionCheat;
    }

    public Direction getNextMoveDirectionCheat() {
        return nextMoveDirectionCheat;
    }

    public CreatureID getCreatureType() {
        return creatureType;
    }

    public abstract Direction[] getDirectionPriority(Creature chip, RNG rng);

    public abstract Direction getSlideDirection(Direction direction, Tile tile, RNG rng, boolean advanceRFF);

    /** Returns a direction besides NONE if the creature's move will be forced.
     * Side effects such as setting the creature's direction can occur.
     *
     * @param tile The tile the creature is currently on (may be ignored).
     * @return true if the next move is forced, false otherwise.
     */
    public abstract boolean getForcedMove(Tile tile);

    public void setCreatureType(CreatureID creatureType) {
        this.creatureType = creatureType;
    }

    public void kill() {
        creatureType = CreatureID.DEAD;
    }

    public boolean isDead() {
        return creatureType == CreatureID.DEAD;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    /** Turns the creature to face a specified direction.
     *
     * @param turn the direction the creature should turn.
     */
    public void turn(Direction turn) {
        direction = direction.turn(turn);
    }

    public abstract Tile toTile();

    @Override
    public String toString() {
        if (creatureType == DEAD) return "Dead monster at position " + position;
        return creatureType+" facing "+direction+" at position "+position;
    }

    /** Returns a number representing how long the creature has been traveling between tiles.
     * Of note is that this is only useful for lynx and will always be 0 in MS.
     *
     * @return An int between 0 and 7 (inclusive)
     * that represents how long the creature has been traveling between tiles
     */
    public abstract int getTimeTraveled();

    /** Returns a number representing how many ticks are left in an effect animation.
     *  Of note is that this is only useful for DEAD Lynx monsters as they are the only things that play
     *  animations.
     * @return An int between 0 and 12 (inclusive)
     */
    public abstract int getAnimationTimer();

    /** Returns a boolean representing if a creature can make a move with the given direction into the given position.
     * Checking both that it can leave its current position and enter the given one.
     * Side effects can and will occur, some based on the flags passed.
     *
     * @param direction The direction the creature should try to move in.
     * @param position The position the creature should try to enter.
     * @param clearAnims If any animations found should be stopped.
     * @param pushBlocks If any blocks found should be prepped to move.
     * @param pushBlocksNow If any blocks found should be prepped AND moved at this time.
     * @param releasing If the creature is releasing from a trap.
     * @return A boolean representing that the creature can make this move.
     */
    public abstract boolean canMakeMove(Direction direction, Position position, boolean clearAnims, boolean pushBlocks, boolean pushBlocksNow, boolean releasing);

//    /** Returns a boolean representing if the creature can enter the given tile in the given direction.
//     *
//     * @param direction The direction the creature is moving.
//     * @param tile The tile the creature is attempting to enter.
//     * @return A boolean representing if the creature can enter the provided tile.
//     */
//    public abstract boolean canEnter(Direction direction, Tile tile);
//
//    /** Returns a boolean representing if the creature can enter the given position in the given direction, side effects can and will occur.
//     *
//     * @param direction The direction the creature is moving.
//     * @param position The position the creature is attempting to enter.
//     * @param clearAnims If any animations found should be stopped.
//     * @param pushBlocks If any blocks found should be prepped to move.
//     * @param pushBlocksNow If any blocks found should be prepped AND moved at this time.
//     * @return A boolean representing if the creature can enter the provided position.
//     */
//    public abstract boolean canEnter(Direction direction, Position position, boolean clearAnims, boolean pushBlocks, boolean pushBlocksNow);
//
//    /** Returns a boolean representing if the creature can leave the given tile, in the given direction,
//     * at the given position.
//     *
//     * @param direction The direction the creature is moving.
//     * @param position The position of the creature and tile.
//     * @return A boolean representing if the creature can leave the given tile and position.
//     */
//    public abstract boolean canLeave(Direction direction, Position position);

    /** Returns a boolean representing if the given creature can override a force floor move.
     * Really only useful for Chip and even then only in Lynx.
     *
     * @return A boolean representing if the creature can override a force floor at this moment.
     */
    public abstract boolean canOverride();

    /** Advances a creature 1 tick according to their internal state.
     *
     * @param releasing if the creature is releasing from a trap.
     * @return If the move was successful or not.
     */
    public abstract boolean tick(boolean releasing);

    /** Returns an int representing a creature.
     *
     * @return An int with the bits arranged according to the creature bit encoding.
     */
    public abstract int bits();

    /**
     * @return A boolean representing if the creature is sliding.
     */
    public boolean isSliding() {
        return sliding;
    }

    public void setSliding(boolean sliding) {
        this.sliding = sliding;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public abstract Creature clone();
}
