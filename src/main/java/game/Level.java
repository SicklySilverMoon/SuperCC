package game;

import game.MS.*;
import game.button.*;
import util.MultiHashMap;

import java.util.BitSet;
import java.util.Map;

public interface Level extends Savestate {
    int MASK_TICK_MULTI     = 0b01;
    int MASK_DISCARD_INPUT  = 0b10;

    int getLevelNumber();

    int getStartTime();

    String getTitle();

    String getPassword();

    String getHint();

    String getAuthor();

    Position[] getToggleDoors();

    Position[] getTeleports();

    MultiHashMap<Position, GreenButton> getGreenButtons();

    MultiHashMap<Position, RedButton> getRedButtons();

    MultiHashMap<Position, BrownButton> getBrownButtons();

    MultiHashMap<Position, BlueButton> getBlueButtons();

    void setGreenButtons(MultiHashMap<Position, GreenButton> greenButtons);

    void setRedButtons(MultiHashMap<Position, RedButton> redButtons);

    void setBrownButtons(MultiHashMap<Position, BrownButton> brownButtons);

    void setBlueButtons(MultiHashMap<Position, BlueButton> blueButtons);

    int getRngSeed();

    Step getStep();

    boolean supportsLayerBG();

    boolean supportsClick();

    boolean supportsSliplist();

    boolean supportsDiagonal();

    boolean hasCyclicRFF();

    boolean chipInMonsterList();

    /**
     * Represents if a trap's button has to be held for the trap to be open.
     */
    boolean trapRequiresHeldButton();

    boolean creaturesAreTiles();

    boolean hasStillTanks();

    boolean swimmingChipIsCreature();

    boolean blocksInMonsterList();

    int ticksPerMove();

    Layer getLayerBG();

    Layer getLayerFG();

    boolean isUntimed();

    /**
     *
     * @return The current value of the timer that is displayed on screen.
     * Returns a negative value on untimed levels.
     */
    int getTimer();

    void setTimer(int n);

    /**
     *
     * @return The current value of the timer if we are using TChip timing
     * (where the time starts at 999.9).
     */
    int getTChipTime();

    int getChipsLeft();

    void setChipsLeft(int chipsLeft);

    Creature getChip();

    /**
     * Returns chip's keys
     * <p>
     * Chip's keys are short array with 4 entries containing the number of
     * blue, red, green and yellow keys in that order.
     * </p>
     * @return chip's keys
     */
    short[] getKeys();

    /**
     * Set chip's keys
     * <p>
     * Chip's keys are short array with 4 entries containing the number of
     * blue, red, green and yellow keys in that order.
     * </p>
     */
    void setKeys(short[] keys);

    /**
     * Returns chip's boots
     * <p>
     * Chip's boots are byte array with 4 entries containing the number of
     * flippers, fire boots, skates and suction boots in that order.
     * </p>
     * @return chip's boots
     */
    byte[] getBoots();

    void setBoots(byte[] boots);

    CreatureList getMonsterList();

    SlipList getSlipList();

    void setTrap(Position trapPos, boolean open);

    int getLevelsetLength();

    Cheats getCheats();

    RNG getRNG();

    Button getButton(Position position, Class<? extends Button> buttonType);

    Button getButton(Position position);

    boolean isTrapOpen(Position position);

    int getTickNumber();

    Ruleset getRuleset();

    Direction getInitialRFFDirection();

    Direction getRFFDirection(boolean advance);

    /**
     * @param position the last clicked position.
     */
    void setClick(int position);

    void setLevelWon(boolean won);

    boolean isCompleted();

    /**
     * Advances a tick (10th of a second).
     * <p>
     *     This method is not responsible for setting the click position, or
     *     for checking whether chip can move (in case chip moved the previous
     *     tick).
     * </p>
     * @param c The direction in which to move. If c is positive it should be
     *          one of UP, LEFT, DOWN, RIGHT and WAIT. If c is negative, it is
     *          interpreted as a mouse click. Note that the click itself is not
     *          set here - use {@link #setClick(int)} for that.
     * @param directions The directions in which chip should try to move
     * @return An int with bit 0 representing if the next move should be made automatically without input
     *         and bit 1 representing if the input given to this move should be discarded.
     */
    int tick(char c, Direction[] directions);

    void insertTile(Position position, Tile tile);

    void popTile(Position position);

    /**
     * @param creature the creature in question.
     * @return a boolean representing if the monsterlist number of this creature should be drawn.
     */
    boolean shouldDrawCreatureNumber(Creature creature);

    void turnTanks();

    Creature newCreature(Direction dir, CreatureID creatureType, Position position);
}
