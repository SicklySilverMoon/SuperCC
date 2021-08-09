package game.MS;

import emulator.SuperCC;
import game.*;
import game.button.*;
import util.MultiHashMap;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static emulator.SuperCC.WAIT;
import static emulator.SuperCC.UP;
import static emulator.SuperCC.DOWN;
import static emulator.SuperCC.LEFT;
import static emulator.SuperCC.RIGHT;
import static game.CreatureID.TANK_MOVING;
import static game.Direction.TURN_AROUND;
import static game.Tile.*;

public class MSLevel extends MSSavestate implements Level {

    private static final int HALF_WAIT = 0, KEY = 1, CLICK_EARLY = 2, CLICK_LATE = 3;

    public final int INITIAL_MONSTER_LIST_SIZE = monsterList.size();
    public final Position INITIAL_MONSTER_POSITION =
            (monsterList.size() == 0) ? null : monsterList.get(0).getPosition(); //this is needed or else half the levels aren't playable due to a crash from having an empty monster list
    public final int INITIAL_CHIPS_AMOUNT = chipsLeft;
    private final int LEVELSET_LENGTH;
    private final Ruleset RULESET = Ruleset.MS;

    private int levelNumber, startTime;
    private final String title, password, hint, author;
    private final Position[] toggleDoors, teleports;
    private MultiHashMap<Position, GreenButton> greenButtons;
    private MultiHashMap<Position, RedButton> redButtons;
    private MultiHashMap<Position, BrownButton> brownButtons;
    private MultiHashMap<Position, BlueButton> blueButtons;
    private final int rngSeed;
    private final Step step;
    private boolean levelWon;
    
    private final Cheats cheats;
    
    @Override
    public int getLevelNumber() {
        return levelNumber;
    }
    @Override
    public int getStartTime() {
        return startTime;
    }
    @Override
    public String getTitle() {
        return title;
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getHint() {
        return hint;
    }
    @Override
    public String getAuthor() {
        return author;
    }
    @Override
    public Position[] getToggleDoors() {
        return toggleDoors;
    }
    @Override
    public Position[] getTeleports() {
        return teleports;
    }
    @Override
    public MultiHashMap<Position, GreenButton> getGreenButtons() {
        return greenButtons;
    }
    @Override
    public MultiHashMap<Position, RedButton> getRedButtons() {
        return redButtons;
    }
    @Override
    public MultiHashMap<Position, BrownButton> getBrownButtons() {
        return brownButtons;
    }
    @Override
    public MultiHashMap<Position, BlueButton> getBlueButtons() {
        return blueButtons;
    }
    @Override
    public void setGreenButtons(MultiHashMap<Position, GreenButton> greenButtons) {
        this.greenButtons = greenButtons;
    }
    @Override
    public void setRedButtons(MultiHashMap<Position, RedButton> redButtons) {
        this.redButtons = redButtons;
    }
    @Override
    public void setBrownButtons(MultiHashMap<Position, BrownButton> brownButtons) {
        this.brownButtons = brownButtons;
    }
    @Override
    public void setBlueButtons(MultiHashMap<Position, BlueButton> blueButtons) {
        this.blueButtons = blueButtons;
    }
    @Override
    public int getRngSeed(){
        return rngSeed;
    }
    @Override
    public Step getStep(){
        return step;
    }

    @Override
    public boolean supportsLayerBG() {
        return true;
    }

    @Override
    public boolean supportsClick() {
        return true;
    }

    @Override
    public boolean supportsSliplist() {
        return true;
    }

    @Override
    public boolean supportsDiagonal() {
        return false;
    }

    @Override
    public boolean hasCyclicRFF() {
        return false;
    }

    @Override
    public boolean chipInMonsterList() {
        return false;
    }

    @Override
    public int ticksPerMove() {
        return RULESET.ticksPerMove;
    }

    @Override
    public Layer getLayerBG() {
        return layerBG;
    }
    @Override
    public Layer getLayerFG() {
        return layerFG;
    }

    @Override
    public boolean isUntimed() {
        return startTime < 0;
    }
    /**
     *
     * @return The current value of the timer that is displayed on screen.
     * Returns a negative value on untimed levels.
     */
    @Override
    public int getTimer(){
        if (tickNumber == 0) return startTime;
        else return startTime - tickNumber*10 + 10;                     // The first tick does not change the timer
    }
    @Override
    public void setTimer(int n) {
        startTime = n + tickNumber - 1;
    }
    /**
     *
     * @return The current value of the timer if we are using TChip timing
     * (where the time starts at 999.9).
     */
    @Override
    public int getTChipTime() {
        if (tickNumber == 0) return 99990;
        else return 99990 - tickNumber*10 + 10;                     // The first tick does not change the timer
    }
    @Override
    public int getChipsLeft(){
        return chipsLeft;
    }
    @Override
    public void setChipsLeft(int chipsLeft){
        this.chipsLeft = chipsLeft;
    }
    @Override
    public MSCreature getChip(){
        return chip;
    }
    /**
     * Returns chip's keys
     * <p>
     * Chip's keys are short array with 4 entries containing the number of
     * blue, red, green and yellow keys in that order.
     * </p>
     * @return chip's keys
     */
    @Override
    public short[] getKeys(){
        return keys;
    }
    /**
     * Set chip's keys
     * <p>
     * Chip's keys are short array with 4 entries containing the number of
     * blue, red, green and yellow keys in that order.
     * </p>
     */
    @Override
    public void setKeys(short[] keys){
        this.keys = keys;
    }
    /**
     * Returns chip's boots
     * <p>
     * Chip's boots are byte array with 4 entries containing the number of
     * flippers, fire boots, skates and suction boots in that order.
     * </p>
     * @return chip's boots
     */
    @Override
    public byte[] getBoots(){
        return boots;
    }
    /**
     * Sets chip's boots
     * <p>
     * Chip's boots are byte array with 4 entries containing the number of
     * flippers, fire boots, skates and suction boots in that order.
     * </p>
     */
    @Override
    public void setBoots(byte[] boots){
        this.boots = boots;
    }
    @Override
    public CreatureList getMonsterList(){
        return monsterList;
    }
    @Override
    public SlipList getSlipList(){
        return slipList;
    }
    @Override
    public void setTrap(Position trapPos, boolean open) {
        for (List<BrownButton> buttons : brownButtons.rawValues()) {
            for (BrownButton b : buttons) {
                if (b.getTargetPosition().equals(trapPos))
                    traps.set(b.getTrapIndex(), open);
            }
        }
    }
    @Override
    public int getLevelsetLength() {
        return LEVELSET_LENGTH;
    }
    @Override
    public Cheats getCheats() {
        return cheats;
    }
    @Override
    public RNG getRNG() {
        return rng;
    }
    @Override
    public int getTickNumber() {
        return tickNumber;
    }
    @Override
    public Ruleset getRuleset() {
        return RULESET;
    }
    @Override
    public Direction getInitialRFFDirection() {
        return Direction.UP; //unused in MS, return the "default" value
    }
    @Override
    public Direction getRFFDirection(boolean advance) {
        int rngValue = rng.getCurrentValue();
        Direction slideDir = Direction.fromOrdinal(rng.random4());
        if (!advance)
            rng.setCurrentValue(rngValue);
        return slideDir;
    }
    /**
     * @param position the last clicked position.
     */
    @Override
    public void setClick(int position){
        this.mouseGoal = position;
    }

    @Override
    public void setLevelWon(boolean won) {levelWon = won;}

    @Override
    public boolean isCompleted() {
        return levelWon;
    }

    @Override
    public void popTile(Position position){
        if (!position.isValid())
            return;
        layerFG.set(position, layerBG.get(position));
        layerBG.set(position, FLOOR);
    }
    @Override
    public void insertTile(Position position, Tile tile){
        if (!position.isValid())
            return;
        Tile fgTile = layerFG.get(position);
        if (!(fgTile.equals(FLOOR) && !tile.isMonster())) layerBG.set(position, layerFG.get(position));
        layerFG.set(position, tile);
    }

    @Override
    public Button getButton(Position position, Class<? extends Button> buttonType) {
        if (buttonType.equals(GreenButton.class))
            return greenButtons.get(position);
        else if (buttonType.equals(RedButton.class))
            return redButtons.get(position);
        else if (buttonType.equals(BrownButton.class))
            return brownButtons.get(position);
        else if (buttonType.equals(BlueButton.class))
            return blueButtons.get(position);
        else throw new RuntimeException("Invalid class");
    }

    @Override
    public Button getButton(Position position) {
        for (Map<Position, ? extends Button> buttons : List.of(greenButtons, redButtons, brownButtons, blueButtons)) {
            if (buttons.get(position) != null)
                return buttons.get(position);
        }
        return null;
    }
    @Override
    public boolean isTrapOpen(Position position) {
        for (List<BrownButton> buttons : brownButtons.rawValues()) {
            for (BrownButton b : buttons) {
                if (b.getTargetPosition().equals(position) && traps.get(b.getTrapIndex()))
                    return true;
            }
        }
        return false;
    }
    
    private int moveType(char c, boolean halfMove, boolean chipSliding){
        if (SuperCC.isClick(c) || c == WAIT){
            if (mouseGoal != NO_CLICK) {
                if (chipSliding)
                    return CLICK_EARLY;
                if (halfMove)
                    return CLICK_LATE;
            }
            else return HALF_WAIT;
        }
        else if (c == UP || c == LEFT || c == DOWN || c == RIGHT){
            return KEY;
        }
        return HALF_WAIT;
    }
    
    private void moveChipSliding(){
        Direction direction = chip.getDirection();
        Tile bgTile = layerBG.get(chip.getPosition());
        if (bgTile.isFF()) chip.tick(new Direction[] {direction}, true);
        else chip.tick(chip.getSlideDirectionPriority(bgTile, rng, true), true);
    }

    //returns true if both directions were attempted and failed
    private boolean moveChip(Direction[] directions){
        Position oldPosition = chip.getPosition().clone();
        int attemptedDirs = 0;
        for (Direction direction : directions) {
            if (chip.isSliding()) {
                if (!layerBG.get(chip.getPosition()).isFF())
                    continue;
                if (direction == chip.getDirection())
                    continue;
            }
            attemptedDirs++;
            chip.tick(new Direction[] {direction}, false);
            if (!chip.getPosition().equals(oldPosition))
                return false;
        }
        return attemptedDirs == directions.length;
    }

    private void finaliseTraps(){
        HashSet<Position> pressedButtons = new HashSet<>();
        for (List<BrownButton> buttons : brownButtons.rawValues()) {
            for (BrownButton b : buttons) {
                if (layerFG.get(b.getButtonPosition()) != BUTTON_BROWN && !pressedButtons.contains(b.getButtonPosition())) {
                    traps.set(b.getTrapIndex(), true);
                    pressedButtons.add(b.getButtonPosition());
                }
                else if (layerFG.get(b.getTargetPosition()) == TRAP) {
                    traps.set(b.getTrapIndex(), false);
                }
            }
        }
    }

    private void initialiseSlidingMonsters(){
        for (Creature m : monsterList) m.setSliding(false);
        for (Creature m : slipList) m.setSliding(true);
    }
    
    private boolean endTick() {
        if (layerBG.get(chip.getPosition()).equals(EXIT) && levelWon){
            layerFG.set(chip.getPosition(), EXITED_CHIP);
            chip.kill();
        }
        return chip.isDead();
    }

    public boolean shouldDrawCreatureNumber(Creature creature) {
        return true; //method is only useful for lynx really
    }

    public void turnTanks() {
        for (Creature m : monsterList) {
            if (m.getCreatureType().isTank() && !m.isSliding()) {
                m.setCreatureType(TANK_MOVING);
                m.turn(TURN_AROUND);
                    layerFG.set(m.getPosition(), m.toTile());
            }
        }
        for (Creature m : monsterList.getNewClones()) {
            if (m.getCreatureType().isTank() && !m.isSliding()) {
                m.setCreatureType(TANK_MOVING);
                m.turn(TURN_AROUND);
            }
        }
    }
    
    /**
     * Advances a tick (10th of a second).
     * <p>
     *     This method is not responsible for setting the click position, or
     *     for checking whether chip can move (in case chip moved the previous
     *     tick).
     * </p>
     * @param c The direction in which to move. If b is positive it should be
     *          one of UP, LEFT, DOWN, RIGHT and WAIT. If b is negative, it is
     *          interpreted as a mouse click. Note that the click itself is not
     *          set here - use {@link #setClick(int)} for that.
     * @param directions The directions in which chip should try to move
     * @return true if the next move should be made automatically without input
     */
    @Override
    public boolean tick(char c, Direction[] directions){

        setLevelWon(false); //Each tick sets the level won state to false so that even when rewinding unless you stepped into the exit the level is not won
        initialiseSlidingMonsters();
        boolean isHalfMove = (tickNumber & 0x1) != 0; //odd tick
        int moveType = moveType(c, isHalfMove, chip.isSliding());
        boolean failedDirections = false;
        monsterList.initialise();

        if (isHalfMove) voluntaryMoveAllowed = true; //This is not used in finding out if the emulator should tick twice, that is instead handled by the value of this function's return, this is only used for TSG moves, yeah its bad
//        if (!voluntaryMoveAllowed && idleMoves > 0) voluntaryMoveAllowed = true;

        if (isHalfMove && mouseGoal == NO_CLICK && moveType == HALF_WAIT) {
            idleMoves++;
            if (idleMoves > 2) {
                layerFG.set(chip.getPosition(), CHIP_DOWN);
            }
        }

        if (tickNumber > 0 && !isHalfMove) monsterList.tick();

        if (endTick()) return false;
        if (chip.isSliding()) moveChipSliding();
        if (endTick()) return false;
        if (moveType == CLICK_EARLY && voluntaryMoveAllowed) { //todo: it seems like it should be impossible to do a key move onto an FF and then TSG right after
//            System.out.println("tsg");
            Direction sought = chip.seek(new Position(mouseGoal))[0];
            /* CCLP4:34, the TWS Sharpeye provided has a discrepancy between TW and SuCC, the first click has an offset
            from Chip of (-2, 1), in TW this is below the gravel, in SuCC its below and one to the right, much like how
            TW-TSG can't load a lot of normal mouse solutions, we can't load a lot of TSG solutions */
            if (!chip.isSliding() || !sought.equals(chip.getDirection())) {
                failedDirections = moveChip(new Direction[]{sought});
                idleMoves = 0;
                voluntaryMoveAllowed = false;
            }
//            else if (isHalfMove) {
//                moveType = CLICK_LATE;
//            }
        }
        if (endTick()) return false;
        tickNumber++;
        slipList.tick();
        if (endTick()) return false;
        if (moveType == KEY) {
            moveChip(directions);
            idleMoves = 0;
            voluntaryMoveAllowed = false;
        }
        else if (moveType == CLICK_LATE && !chip.isSliding() && voluntaryMoveAllowed) {
//            System.out.println("normal");
            failedDirections = moveChip(chip.seek(new Position(mouseGoal)));
            idleMoves = 0;
//            voluntaryMoveAllowed = false; //this causes the TSG unit test instances to desync, something is wrong here
            if (failedDirections)
                voluntaryMoveAllowed = true; //this way causes the bottom part of SuCCTest L 75 to desync, but why???
        }
        if (endTick())
            return false;

        monsterList.finalise();
        finaliseTraps();
        if (moveType == KEY || chip.getPosition().getIndex() == mouseGoal)
            mouseGoal = NO_CLICK;
        else if ((moveType == CLICK_LATE || moveType == CLICK_EARLY) && failedDirections)
            mouseGoal = NO_CLICK;

        return (moveType == KEY || moveType == CLICK_EARLY) && !isHalfMove && !chip.isSliding();
    }

    void resetData(int xPosition) {
        Position position = new Position(xPosition, 0);
        byte lowByte, highByte;
        boolean lowOrder = xPosition % 2 == 0;
        int shiftBy = xPosition % 2 == 0 ? 0 : 8;
        switch (xPosition) {
            case 0, 1 -> {
                lowByte = (byte) levelNumber;
                highByte = (byte) (levelNumber >>> 8);
                if ((lowOrder ? lowByte : highByte) != 49)
                    layerBG.set(position, Tile.fromOrdinal((byte) (levelNumber >>> shiftBy)));
            }
            case 2, 3 -> {
                lowByte = (byte) LEVELSET_LENGTH;
                highByte = (byte) (LEVELSET_LENGTH >>> 8);
                if ((lowOrder ? lowByte : highByte) != 49)
                    layerBG.set(position, Tile.fromOrdinal((byte) (LEVELSET_LENGTH >>> shiftBy)));
            }
            case 4, 5 -> {
                int msccTime = startTime / 100;
                lowByte = (byte) msccTime;
                highByte = (byte) (msccTime >>> 8);
                if ((lowOrder ? lowByte : highByte) != 49)
                    layerBG.set(position, Tile.fromOrdinal((byte) (msccTime >>> shiftBy)));
            }
            case 6, 7 -> {
                lowByte = (byte) INITIAL_CHIPS_AMOUNT;
                highByte = (byte) (INITIAL_CHIPS_AMOUNT >>> 8);
                if ((lowOrder ? lowByte : highByte) != 49)
                    layerBG.set(position, Tile.fromOrdinal((byte) (INITIAL_CHIPS_AMOUNT >>> shiftBy)));
            }
            case 8, 9 -> {
                layerBG.set(position, Tile.fromOrdinal((byte) (chip.getPosition().x >>> shiftBy)));
                chip.setPosition(new Position(0, chip.getPosition().y));
            }
            case 10, 11 -> {
                layerBG.set(position, Tile.fromOrdinal((byte) (chip.getPosition().y >>> shiftBy)));
                chip.setPosition(new Position(chip.getPosition().x, 0));
            }
            case 12, 13 -> {
                int sliding = chip.isSliding() ? 1 : 0;
                layerBG.set(position, Tile.fromOrdinal((byte) (sliding >>> shiftBy)));
                chip.setSliding(false);
            }
            //buffered input, unsupported by SuCC
            //MSCC's level title visibility, unsupported by SuCC
            //keystroke directions, unsupported by SuCC
            case 14, 15, 16, 17, 18, 19, 20, 21 -> layerBG.set(position, FLOOR);
            case 22, 23 -> {
                int deathCause = 0;
                Tile chipFG = layerFG.get(chip.getPosition());
                Tile chipBG = layerBG.get(chip.getPosition());
                if (chipFG.isChip() || chipFG.isSwimmingChip()) {
                    if (chipFG.isChip() && chipBG == BOMB)
                        deathCause = 3;
                }
                else if (chipFG == DROWNED_CHIP)
                    deathCause = 1;
                else if (chipFG == BURNED_CHIP)
                    deathCause = 2;
                else if (chipFG == BLOCK || chipFG == ICE_BLOCK)
                    deathCause = 4;
                else if (chipFG.isMonster())
                    deathCause = 5;
                layerBG.set(position, Tile.fromOrdinal((byte) (deathCause >>> shiftBy)));
                chip.setCreatureType(CreatureID.CHIP);
            }
            case 24, 25 -> {
                if (!chip.isSliding()) {
                    layerBG.set(position, FLOOR);
                    return;
                }
                short slipDirectionX = 0;
                if (chip.getDirection() == Direction.RIGHT) {
                    slipDirectionX = 1;
                    chip.setSliding(false); //not accurate, must research more
                }
                else if (chip.getDirection() == Direction.LEFT) {
                    slipDirectionX = -1;
                    chip.setSliding(false);
                }
                layerBG.set(position, Tile.fromOrdinal((byte) (slipDirectionX >>> shiftBy)));
            }
            case 26, 27 -> {
                if (!chip.isSliding()) {
                    layerBG.set(position, FLOOR);
                    return;
                }
                short slipDirectionY = 0;
                if (chip.getDirection() == Direction.DOWN) {
                    slipDirectionY = 1;
                    chip.setSliding(false); //not accurate, must research more
                }
                else if (chip.getDirection() == Direction.UP) {
                    slipDirectionY = -1;
                    chip.setSliding(false);
                }
                layerBG.set(position, Tile.fromOrdinal((byte) (slipDirectionY >>> shiftBy)));
            }
            case 28, 29 -> {
                lowByte = (byte) INITIAL_MONSTER_LIST_SIZE;
                highByte = (byte) (INITIAL_MONSTER_LIST_SIZE >>> 8);
                if ((lowOrder ? lowByte : highByte) != 49)
                    layerBG.set(position, Tile.fromOrdinal((byte) (INITIAL_MONSTER_LIST_SIZE >>> shiftBy)));
            }
            case 30 -> layerBG.set(position, Tile.fromOrdinal((byte) (INITIAL_MONSTER_POSITION.x)));
            case 31 -> layerBG.set(position, Tile.fromOrdinal((byte) (INITIAL_MONSTER_POSITION.y)));
        }
    }

    public MSLevel(int levelNumber, String title, String password, String hint, String author,
                   Position[] toggleDoors, Position[] teleports,
                   MultiHashMap<Position, GreenButton> greenButtons, MultiHashMap<Position, RedButton> redButtons,
                   MultiHashMap<Position, BrownButton> brownButtons, MultiHashMap<Position, BlueButton> blueButtons,
                   BitSet traps, Layer layerBG, Layer layerFG, CreatureList monsterList, SlipList slipList,
                   MSCreature chip, int time, int chips, RNG rng, int rngSeed, Step step, int levelsetLength){

        super(layerBG, layerFG, monsterList, slipList, chip,
                chips, new short[4], new byte[4], rng, NO_CLICK, traps);

        this.levelNumber = levelNumber;
        this.startTime = time;
        this.title = title;
        this.password = password;
        this.hint = hint;
        this.author = author;
        this.toggleDoors = toggleDoors;
        this.teleports = teleports;
        this.greenButtons = greenButtons;
        this.redButtons = redButtons;
        this.brownButtons = brownButtons;
        this.blueButtons = blueButtons;
        this.rngSeed = rngSeed;
        this.step = step;
        this.cheats = new Cheats(this);
        this.LEVELSET_LENGTH = levelsetLength;

        for (Creature c : monsterList)
            c.setLevel(this);
        chip.setLevel(this);

        this.slipList.setLevel(this);
        this.monsterList.setLevel(this);

        for (List<BrownButton> buttons : getBrownButtons().rawValues()) {  //On level start every single trap is actually open in MSCC, this implements that so creatures and blocks starting on traps can exit them at any point in the level
            for (BrownButton b : buttons) {
                if (getLayerFG().get(b.getTargetPosition()).isChip() || getLayerFG().get(b.getTargetPosition()) == BLOCK || getLayerFG().get(b.getTargetPosition()) == ICE_BLOCK) {
                    b.press(this);
                }
            }
        }
    }
}
