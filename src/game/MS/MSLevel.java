package game.MS;

import emulator.SuperCC;
import game.*;
import game.button.*;

import java.util.BitSet;

import static game.Tile.*;

public class MSLevel extends MSSaveState implements Level {

    private static final int HALF_WAIT = 0, KEY = 1, CLICK_EARLY = 2, CLICK_LATE = 3;

    public final int INITIAL_MONSTER_LIST_SIZE = monsterList.size();
    public final Position INITIAL_MONSTER_POSITION =
            (monsterList.size() == 0) ? null : monsterList.get(0).getPosition(); //this is needed or else half the levels aren't playable due to a crash from having an empty monster list
    public final int INITIAL_CHIPS_AMOUNT = chipsLeft;
    private final int LEVELSET_LENGTH;
    private final Ruleset RULESET = Ruleset.MS;

    private int levelNumber, startTime;
    private final byte[] title, password, hint;
    private final Position[] toggleDoors, teleports;
    private GreenButton[] greenButtons;
    private RedButton[] redButtons;
    private BrownButton[] brownButtons;
    private BlueButton[] blueButtons;
    private int rngSeed;
    private Step step;
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
    public byte[] getTitle() {
        return title;
    }
    @Override
    public byte[] getPassword() {
        return password;
    }
    @Override
    public byte[] getHint() {
        return hint;
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
    public GreenButton[] getGreenButtons() {
        return greenButtons;
    }
    @Override
    public RedButton[] getRedButtons() {
        return redButtons;
    }
    @Override
    public BrownButton[] getBrownButtons() {
        return brownButtons;
    }
    @Override
    public BlueButton[] getBlueButtons() {
        return blueButtons;
    }
    @Override
    public void setGreenButtons(GreenButton[] greenButtons) {
        this.greenButtons = greenButtons;
    }
    @Override
    public void setRedButtons(RedButton[] redButtons) {
        this.redButtons = redButtons;
    }
    @Override
    public void setBrownButtons(BrownButton[] brownButtons) {
        this.brownButtons = brownButtons;
    }
    @Override
    public void setBlueButtons(BlueButton[] blueButtons) {
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
    public boolean hasCyclicRFF() {
        return false;
    }

    @Override
    public int ticksPerMove() {
        return 2;
    }

    @Override
    public Layer getLayerBG() {
        return layerBG;
    }
    @Override
    public Layer getLayerFG() {
        return layerFG;
    }
    /**
     *
     * @return The current value of the timer that is displayed on screen.
     * Returns a negative value on untimed levels.
     */
    @Override
    public int getTimer(){
        if (tickNumber == 0) return startTime;
        else return startTime - tickNumber + 1;                     // The first tick does not change the timer
    }
    /**
     *
     * @return The current value of the timer if we are using TChip timing
     * (where the time starts at 999.9).
     */
    @Override
    public int getTChipTime() {
        if (tickNumber == 0) return 9999;
        else return 9999 - tickNumber + 1;                     // The first tick does not change the timer
    }
    void setTimer(int n) {
        startTime = n + tickNumber - 1;
    }
    @Override
    public int getChipsLeft(){
        return chipsLeft;
    }
    void setChipsLeft(int chipsLeft){
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
    void setKeys(short[] keys){
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
    void setBoots(byte[] boots){
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
    public BitSet getOpenTraps(){
        return traps;
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
    public Direction getInitialSlide() {
        return Direction.UP; //unused in MS, return the "default" value
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
    
    public MSLevel(int levelNumber, byte[] title, byte[] password, byte[] hint, Position[] toggleDoors, Position[] teleports,
                   GreenButton[] greenButtons, RedButton[] redButtons,
                   BrownButton[] brownButtons, BlueButton[] blueButtons, BitSet traps,
                   Layer layerBG, Layer layerFG, CreatureList monsterList, SlipList slipList,
                   MSCreature chip, int time, int chips, RNG rng, int rngSeed, Step step, int levelsetLength){
        
        super(layerBG, layerFG, monsterList, slipList, chip,
              time, chips, new short[4], new byte[4], rng, NO_CLICK, traps);
        
        this.levelNumber = levelNumber;
        this.startTime = time;
        this.title = title;
        this.password = password;
        this.hint = hint;
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

        Creature.setLevel(this);
        Creature.setMonsterList(monsterList);
        this.slipList.setLevel(this);
        this.monsterList.setLevel(this);

        for (BrownButton b : getBrownButtons()) {  //On level start every single trap is actually open in MSCC, this implements that so creatures and blocks starting on traps can exit them at any point in the level
            if (getLayerFG().get(b.getTargetPosition()).isChip() || getLayerFG().get(b.getTargetPosition()) == BLOCK || getLayerFG().get(b.getTargetPosition()) == ICE_BLOCK) {
                b.press(this);
            }
        }
    }

    @Override
    public void popTile(Position position){
        layerFG.set(position, layerBG.get(position));
        layerBG.set(position, FLOOR);
    }
    @Override
    public void insertTile(Position position, Tile tile){
        Tile fgTile = layerFG.get(position);
        if (!(fgTile.equals(FLOOR) && !tile.isMonster())) layerBG.set(position, layerFG.get(position));
        layerFG.set(position, tile);
    }

    @Override
    public Button getButton(Position position, Class buttonType) {
        Button[] buttons;
        if (buttonType.equals(GreenButton.class)) buttons = greenButtons;
        else if (buttonType.equals(RedButton.class)) buttons = redButtons;
        else if (buttonType.equals(BrownButton.class)) buttons = brownButtons;
        else if (buttonType.equals(BlueButton.class)) buttons = blueButtons;
        else throw new RuntimeException("Invalid class");
        for (Button b : buttons) {
            if (b.getButtonPosition().equals(position)) return b;
        }
        return null;
    }

    @Override
    public Button getButton(Position position) {
        for (Button[] buttons : new Button[][] {greenButtons, redButtons, brownButtons, blueButtons}) {
            for (Button b : buttons) {
                if (b.getButtonPosition().equals(position)) return b;
            }
        }
        return null;
    }
    @Override
    public boolean isTrapOpen(Position position) {
        for (BrownButton b : brownButtons) {
            if (b.getTargetPosition().equals(position) && b.isOpen(this)) return true;
        }
        return false;
    }
    
    private int moveType(char c, boolean halfMove, boolean chipSliding){
        if (SuperCC.isClick(c) || c == WAIT){
            if (mouseGoal != NO_CLICK) {
                if (chipSliding) return CLICK_EARLY;
                if (halfMove) return CLICK_LATE;
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
    
    private void moveChip(Direction[] directions){
        Position oldPosition = chip.getPosition().clone();
        for (Direction direction : directions) {
            if (chip.isSliding()) {
                if (!layerBG.get(chip.getPosition()).isFF()) continue;
                if (direction == chip.getDirection()) continue;
            }
            chip.tick(new Direction[] {direction}, false);
            if (!chip.getPosition().equals(oldPosition)) break;
        }
    }

    private void finaliseTraps(){
        for (BrownButton b : brownButtons) {
            if (layerBG.get(b.getButtonPosition()) == BUTTON_BROWN){
                traps.set(b.getTrapIndex(), true);
            }
            else if (layerFG.get(b.getTargetPosition()) == TRAP){
                traps.set(b.getTrapIndex(), false);
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
//        if (!ResetStep && (getLayerBG().get(AutopsyPosition).isCreature())) { //Gotta love data resetting
//            ResetStep = true;
//            return false;
//        }
        /*else*/ return chip.isDead();
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
        boolean isHalfMove = (tickNumber & 0x1) != 0; //A faster version of tickNumber % 2 != 0;
        int moveType = moveType(c, isHalfMove, chip.isSliding());
        monsterList.initialise();

//        if (isHalfMove) voluntaryMoveAllowed = true; //This is not used in finding out if the emulator should tick twice, that is instead handled by the value of this function's return, this is only used for TSG moves, yeah its bad
        if (!voluntaryMoveAllowed && idleMoves > 0) voluntaryMoveAllowed = true;

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
        if (moveType == CLICK_EARLY) { //Todo: TSG Is now broken, please fix
            if (voluntaryMoveAllowed) {
                moveChip(chip.seek(new Position(mouseGoal)));
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
        else if (moveType == CLICK_LATE && !chip.isSliding()) {
            moveChip(chip.seek(new Position(mouseGoal)));
            idleMoves = 0;
            voluntaryMoveAllowed = true;
        }
        if (endTick()) return false;

        monsterList.finalise();
        finaliseTraps();
        if (moveType == KEY || chip.getPosition().getIndex() == mouseGoal) mouseGoal = NO_CLICK;

        return (moveType == KEY || moveType == CLICK_EARLY) && !isHalfMove && !chip.isSliding();
    }

    void ResetData(Position position){ //Actual reset code for data reset
        Position ChipPosition = getChip().getPosition(); //Gets Chip's Current position
        if (position.x == 8) { //X reset
            Position ChipXReset = new Position(0, ChipPosition.y); //prepares to set Chip's X position to 0
            getChip().setPosition(ChipXReset); //sets Chip's X position to 0
            layerBG.set(position, (Tile.fromOrdinal(ChipPosition.x))); //Doesn't need to be checked as co-cords are always within valid tile ranges
        }
        if (position.x == 10) { //Y reset
            Position ChipYReset = new Position(ChipPosition.x, 0); //prepares to set Chip's Y position to 0
            getChip().setPosition(ChipYReset); //sets Chip's Y position to 0
            layerBG.set(position, (Tile.fromOrdinal(ChipPosition.y))); //Doesn't need to be checked as co-cords are always within valid tile ranges
        }
        if (position.x == 12) { //Sliding state reset
            if (chip.isSliding()) { //Is chip sliding?
                chip.setSliding(chip.isSliding(), false); //Stop Chip from sliding
                layerBG.set(position, WALL); //Place a wall (1 for data)
            }
            else {layerBG.set(position, FLOOR);} //if Chip isn't sliding place a floor (0 for data)
        }
        if (position.x == 14 || position.x == 18 || position.x == 20) { //Current keystroke's buffer, & x- and y-directions of the keystroke reset (SuCC doesn't measure this so i'm treating it as a constant 0)
            layerBG.set(position, FLOOR);
        }
        if (position.x == 22) { //Autopsy report reset
            if (chip.getCreatureType() == CreatureID.DEAD) {
                chip.setCreatureType(CreatureID.CHIP); //If he's CHIP he's not DEAD
                layerBG.set(position, FIRE); //In almost all situations where this is activated chip is killed by a block (due to the nature of blocks cloning instantly its usually only what can be used) which will place fire, so here i skip that and just place fire
            }
            else layerBG.set(position, FLOOR);
        }
        if (position.x == 24) { //Sliding Direction (X) reset
            //if Chip is sliding horizontally immobilize him
            layerBG.set(position, FLOOR); //defaulting to floor for resets not coded yet
        }
        if (position.x == 26) { //Sliding Direction (Y) reset
            //if Chip is sliding vertically immobilize him
            layerBG.set(position, FLOOR); //defaulting to floor for resets not coded yet
        }
        if (position.x == 28) { //amount of monsters in the monster list before the player starts playing the level reset
            if (INITIAL_MONSTER_LIST_SIZE<112) {
                layerBG.set(position, (Tile.fromOrdinal(INITIAL_MONSTER_LIST_SIZE)));
            }
            else {
                layerBG.set(position, WALL); //Everything beyond value 111 is not supported by SuCC and acts as a wall anyways
            }
        }
        if (position.x == 30) { //coordinates (X) of the initial position of the first monster reset, grabs from final variables set up in the Level.java file
            layerBG.set(position, (Tile.fromOrdinal(INITIAL_MONSTER_POSITION.x))); //Doesn't need to be checked as co-cords are always within valid tile ranges
        }
        if (position.x == 31) { //coordinates (Y) of the initial position of the first monster reset
            layerBG.set(position, (Tile.fromOrdinal(INITIAL_MONSTER_POSITION.y))); //Doesn't need to be checked as co-cords are always within valid tile ranges
        }
        if (position.x == 0) { //Level Number reset
            int levelLowByte = getLevelNumber() & 0xFF;
            if (levelLowByte < 112) {
                layerBG.set(position, Tile.fromOrdinal(levelLowByte)); //Doesn't need to be checked as co-cords are always within valid tile ranges
            }
            else {
                layerBG.set(position, WALL); //Everything beyond value 111 is not supported by SuCC and acts as a wall anyways
            }
        }
        if (position.x == 1) { //Level Number reset
            int levelHighByte = (getLevelNumber() >> 8) & 0xFF; //(number >> 8) & 0xFF, (number & 0xFF), this splits into a high and low order byte for the first couple tiles, (number & 0xFF) is low order
            if (levelHighByte < 112) {
                layerBG.set(position, Tile.fromOrdinal(levelHighByte)); //Doesn't need to be checked as co-cords are always within valid tile ranges
            }
            else {
                layerBG.set(position, WALL); //Everything beyond value 111 is not supported by SuCC and acts as a wall anyways
            }
        }
        if (position.x == 2) { //Levelset length reset
            int levelsetLowByte = (LEVELSET_LENGTH-1) & 0xFF;
            if (levelsetLowByte < 112) {
                layerBG.set(position, Tile.fromOrdinal(levelsetLowByte)); //Doesn't need to be checked as co-cords are always within valid tile ranges
            }
            else {
                layerBG.set(position, WALL); //Everything beyond value 111 is not supported by SuCC and acts as a wall anyways
            }
        }
        if (position.x == 3) { //Levelset length reset
            int levelsetHighByte = ((LEVELSET_LENGTH-1) >> 8) & 0xFF; //(number >> 8) & 0xFF, (number & 0xFF), this splits into a high and low order byte for the first couple tiles, (number & 0xFF) is low order
            if (levelsetHighByte < 112) {
                layerBG.set(position, Tile.fromOrdinal(levelsetHighByte)); //Doesn't need to be checked as co-cords are always within valid tile ranges
            }
            else {
                layerBG.set(position, WALL); //Everything beyond value 111 is not supported by SuCC and acts as a wall anyways
            }
        }
        if (position.x == 4) { //Level time reset
            if (getStartTime() < 0) {
                layerBG.set(position, FLOOR); //timeless levels are given as negative values, MSCC puts them as 0 which is a floor
                return;
            }
            int timeMSCC = getStartTime() / 10;
            int timeLowByte = timeMSCC & 0xFF;
            if (timeLowByte < 112) {
                layerBG.set(position, Tile.fromOrdinal(timeLowByte)); //Doesn't need to be checked as co-cords are always within valid tile ranges
            }
            else {
                layerBG.set(position, WALL); //Everything beyond value 111 is not supported by SuCC and acts as a wall anyways
            }
        }
        if (position.x == 5) { //Level time reset
            if (getStartTime() < 0) {
                layerBG.set(position, FLOOR); //timeless levels are given as negative values, MSCC puts them as 0 which is a floor
                return;
            }
            int timeMSCC = getStartTime() / 10; //Named for the way MSCC stores time
            int timeHighByte = (timeMSCC >> 8) & 0xFF;
            if (timeHighByte < 112) {
                layerBG.set(position, Tile.fromOrdinal(timeHighByte)); //Doesn't need to be checked as co-cords are always within valid tile ranges
            }
            else {
                layerBG.set(position, WALL); //Everything beyond value 111 is not supported by SuCC and acts as a wall anyways
            }
        }
        if (position.x == 6) { //Chips reset
            int chipsLowByte = INITIAL_CHIPS_AMOUNT & 0xFF;
            if (chipsLowByte < 112) {
                layerBG.set(position, Tile.fromOrdinal(chipsLowByte)); //Doesn't need to be checked as co-cords are always within valid tile ranges
            }
            else {
                layerBG.set(position, WALL); //Everything beyond value 111 is not supported by SuCC and acts as a wall anyways
            }
        }
        if (position.x == 7) { //Chips reset
            int chipsHighByte = (INITIAL_CHIPS_AMOUNT >> 8) & 0xFF; //(number >> 8) & 0xFF, (number & 0xFF), this splits into a high and low order byte for the first couple tiles, (number & 0xFF) is low order
            if (chipsHighByte < 112) {
                layerBG.set(position, Tile.fromOrdinal(chipsHighByte)); //Doesn't need to be checked as co-cords are always within valid tile ranges
            }
            else {
                layerBG.set(position, WALL); //Everything beyond value 111 is not supported by SuCC and acts as a wall anyways
            }
        }
        if (position.x == 9 || position.x == 11 || position.x == 13 || position.x == 15 || position.x == 16 || position.x == 17 ||position.x == 23 || position.x == 25 || position.x == 27 || position.x == 29) {
            layerBG.set(position, FLOOR); //defaulting to floor for resets either not coded or that always have a 0 value
        }
    }
}
