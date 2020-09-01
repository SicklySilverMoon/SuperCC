package game;

import static game.CreatureID.DEAD;

/**
 * Creatures are (at minimum, subclasses can extend this as needed) encoded as follows:
 *
 *       0 0    | 0 0 0 0 | 0 0 0 0 0 | 0 0 0 0 0
 *    DIRECTION | MONSTER |    ROW    |    COL
 */
public abstract class Creature {
    protected Position position;
    protected CreatureID creatureType;
    protected Direction direction;
    protected Direction[] directions;
    protected boolean sliding;
    protected Direction nextMoveDirectionCheat = null;

    protected static Level level;
    protected static CreatureList monsterList;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setDirectionPriority(Direction[] directions) {
        //For the love of god this should only be used by Cheats or for setting Chip's directions
        this.directions = directions;
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

    protected abstract Direction applySlidingTile(Direction direction, Tile tile, RNG rng);

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

    /** Returns a boolean representing if the creature can enter the given tile in the given direction.
     *
     * @param direction The direction the creature is moving.
     * @param tile The tile the creature is attempting to enter.
     * @return A boolean representing if the creature can enter the provided tile.
     */
    public abstract boolean canEnter(Direction direction, Tile tile);

    /** Returns a boolean representing if the creature can leave the given tile, in the given direction,
     * at the given position.
     *
     * @param direction The direction the creature is moving.
     * @param tile The tile the creature is attempting to exit.
     * @param position The position of the creature and tile.
     * @return A boolean representing if the creature can leave the given tile and position.
     */
    public abstract boolean canLeave(Direction direction, Tile tile, Position position);

    /** Advances the creature one tick (uses the creature's internal state).
     * Currently used only by Lynx as MS Creatures are always downcasted and
     * have their specialized tick method used.
     *
     * @param direction the direction to move the creature in
     * @return A boolean representing if the creature successfully advanced or not.
     */
    public abstract boolean tick(Direction direction);

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

    public static void setLevel(Level newLevel) {
        level = newLevel;
    }

    public static void setMonsterList(CreatureList newList) {
        monsterList = newList;
    }

    public abstract Creature clone();

}
