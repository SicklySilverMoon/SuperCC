package game;

import game.MS.*;
import game.button.*;

import java.util.BitSet;

public interface Level extends Savestate {
    int getLevelNumber();

    int getStartTime();

    byte[] getTitle();

    byte[] getPassword();

    byte[] getHint();

    Position[] getToggleDoors();

    Position[] getTeleports();

    GreenButton[] getGreenButtons();

    RedButton[] getRedButtons();

    BrownButton[] getBrownButtons();

    BlueButton[] getBlueButtons();

    void setGreenButtons(GreenButton[] greenButtons);

    void setRedButtons(RedButton[] redButtons);

    void setBrownButtons(BrownButton[] brownButtons);

    void setBlueButtons(BlueButton[] blueButtons);

    int getRngSeed();

    Step getStep();

    boolean supportsLayerBG();

    boolean supportsClick();

    boolean supportsSliplist();

    boolean hasCyclicRFF();

    boolean chipInMonsterList();

    int ticksPerMove();

    Layer getLayerBG();

    Layer getLayerFG();

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

    BitSet getOpenTraps();

    int getLevelsetLength();

    Cheats getCheats();

    RNG getRNG();

    Button getButton(Position position, Class buttonType);

    Button getButton(Position position);

    boolean isTrapOpen(Position position);

    int getTickNumber();

    Ruleset getRuleset();

    Direction getInitialRFFDirection();

    Direction getAndCycleRFFDirection();

    Direction getRFFDirection();

    void cycleRFFDirection();

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
     * @return true if the next move should be made automatically without input
     */
    boolean tick(char c, Direction[] directions);

    void insertTile(Position position, Tile tile);

    void popTile(Position position);

    /**
     * @param creature the creature in question.
     * @return a boolean representing if the monsterlist number of this creature should be drawn.
     */
    boolean shouldDrawCreatureNumber(Creature creature);
}
