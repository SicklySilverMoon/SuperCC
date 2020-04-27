package game;

/**
 * Creatures are encoded as follows:
 *
 *       0 0    | 0 0 0 0 | 0 0 0 0 0 | 0 0 0 0 0
 *    DIRECTION | MONSTER |    ROW    |    COL
 */
public abstract class Creature {
    protected Position position;
    protected CreatureID creatureType;
    protected Direction direction;
    protected boolean sliding;

    protected static Level level;
    protected static CreatureList monsterList;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public CreatureID getCreatureType() {
        return creatureType;
    }

    public abstract Direction[] getDirectionPriority(Creature chip, RNG rng);

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

    /** Returns a number representing how long the creature has been traveling between tiles.
     * Of note is that this is only useful for lynx and will always be 0 in MS.
     *
     * @return An int between 0 and 7 (inclusive)
     * that represents how long the creature has been traveling between tiles
     */
    public abstract int getTimeTraveled();

    /** Returns a boolean representing if the creature can enter the given tile in the given direction.
     *
     * @param direction The direction the creature is moving.
     * @param tile The tile the creature is attempting to enter.
     * @return
     */
    public abstract boolean canEnter(Direction direction, Tile tile);

    /** Returns an int representing a creature.
     *
     * @return An int with the bits arranged according to the creature bit encoding.
     */
    public abstract int bits();

    /**
     * @return A boolean representing if the creature is sliding.
     */
    public abstract boolean isSliding();

    public void setSliding(boolean sliding) {
        this.sliding = sliding;
    }

    public static void setLevel(Level newLevel) {
        level = newLevel;
    }

    public static void setMonsterList(CreatureList newList) {
        monsterList = newList;
    }

}
