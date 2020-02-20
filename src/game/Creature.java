package game;

public interface Creature {

    Direction getDirection();

    CreatureID getCreatureType();

    void setCreatureType(CreatureID creatureType);

    void kill();

    boolean isDead();

    Position getPosition();

    void turn(Direction turn);

    boolean isSliding();

    Tile toTile();

    int bits();

    void setSliding(boolean sliding);

    void setSliding(boolean sliding, Level genLevel);

}
