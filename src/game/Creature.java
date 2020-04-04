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
    protected static Level level;
    protected boolean sliding;

    public Direction getDirection() {
        return direction;
    }

    public CreatureID getCreatureType() {
        return creatureType;
    }

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

}
