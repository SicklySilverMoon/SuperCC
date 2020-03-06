package game;

/**
 * Creatures are encoded as follows:
 *
 *       0 0    | 0 0 0 0 | 0 0 0 0 0 | 0 0 0 0 0
 *    DIRECTION | MONSTER |    ROW    |    COL
 */
public interface Creature {

    Direction getDirection();

    CreatureID getCreatureType();

    void setCreatureType(CreatureID creatureType);

    void kill();

    boolean isDead();

    Position getPosition();

    /** Turns the creature to face a specified direction.
     *
     * @param turn the direction the creature should turn.
     */
    void turn(Direction turn);

    /**
     * @return A boolean representing if the creature is sliding.
     */
    boolean isSliding();

    Tile toTile();

    /** Returns an int representing a creature.
     *
     * @return An int with the bits arranged according to the creature bit encoding.
     */
    int bits();

    void setSliding(boolean sliding);

    void setSliding(boolean sliding, Level genLevel);

}
