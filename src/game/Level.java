package game;

import game.button.*;

import java.util.BitSet;

import static game.Tile.*;

public class Level extends SaveState {
    
    private static final int HALF_WAIT = 0, KEY = 1, CLICK_EARLY = 2, CLICK_LATE = 3;
    public static final byte UP = 'u', LEFT = 'l', DOWN = 'd', RIGHT = 'r', WAIT = '-';
    
    private int levelNumber, startTime;
    private final byte[] title, password, hint;
    final Position[] toggleDoors, portals;
    private GreenButton[] greenButtons;
    private RedButton[] redButtons;
    private BrownButton[] brownButtons;
    private BlueButton[] blueButtons;
    private int rngSeed;
    private Step step;
    
    public final Cheats cheats;
    
    public int getLevelNumber() {
        return levelNumber;
    }
    public int getStartTime() {
        return startTime;
    }
    public byte[] getTitle() {
        return title;
    }
    public byte[] getPassword() {
        return password;
    }
    public byte[] getHint() {
        return hint;
    }
    public Position[] getToggleDoors() {
        return toggleDoors;
    }
    public Position[] getPortals() {
        return portals;
    }
    public GreenButton[] getGreenButtons() {
        return greenButtons;
    }
    public RedButton[] getRedButtons() {
        return redButtons;
    }
    public BrownButton[] getBrownButtons() {
        return brownButtons;
    }
    public BlueButton[] getBlueButtons() {
        return blueButtons;
    }
    public int getRngSeed(){
        return rngSeed;
    }
    public Step getStep(){
        return step;
    }
    
    public Layer getLayerBG() {
        return layerBG;
    }
    public Layer getLayerFG() {
        return layerFG;
    }
    /**
     *
     * @return The current value of the timer that is displayed on screen.
     * Returns a negative value on untimed levels.
     */
    public int getTimer(){
        if (tickNumber == 0) return startTime;
        else return startTime - tickNumber + 1;                     // The first tick does not change the timer
    }
    /**
     *
     * @return The current value of the timer if we are using TChip timing
     * (where the time starts at 999.9).
     */
    public int getTChipTime() {
        if (tickNumber == 0) return 9999;
        else return 9999 - tickNumber + 1;                     // The first tick does not change the timer
    }
    void setTimer(int n) {
        startTime = n + tickNumber - 1;
    }
    public int getChipsLeft(){
        return chipsLeft;
    }
    void setChipsLeft(int chipsLeft){
        this.chipsLeft = chipsLeft;
    }
    public Creature getChip(){
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
    public CreatureList getMonsterList(){
        return monsterList;
    }
    public SlipList getSlipList(){
        return slipList;
    }
    public BitSet getOpenTraps(){
        return traps;
    }
    /**
     * @param position the last clicked position.
     */
    public void setClick(int position){
        this.mouseClick = position;
    }
    
    public boolean isCompleted() {
        return layerFG.get(chip.getPosition()) == EXITED_CHIP;
    }
    
    public Level(int levelNumber, byte[] title, byte[] password, byte[] hint, Position[] toggleDoors, Position[] portals,
                 GreenButton[] greenButtons, RedButton[] redButtons,
                 BrownButton[] brownButtons, BlueButton[] blueButtons, BitSet traps,
                 Layer layerBG, Layer layerFG, CreatureList monsterList, SlipList slipList,
                 Creature chip, int time, int chips, RNG rng, int rngSeed, Step step){
        
        super(layerBG, layerFG, monsterList, slipList, chip,
              time, chips, new short[4], new byte[4], rng, NO_CLICK, traps);
        
        this.levelNumber = levelNumber;
        this.startTime = time;
        this.title = title;
        this.password = password;
        this.hint = hint;
        this.toggleDoors = toggleDoors;
        this.portals = portals;
        this.greenButtons = greenButtons;
        this.redButtons = redButtons;
        this.brownButtons = brownButtons;
        this.blueButtons = blueButtons;
        this.rngSeed = rngSeed;
        this.step = step;
        this.cheats = new Cheats(this);
        
        this.slipList.setLevel(this);
        this.monsterList.setLevel(this);
    }
    
    void popTile(Position position){
        layerFG.set(position, layerBG.get(position));
        layerBG.set(position, FLOOR);
    }
    void insertTile(Position position, Tile tile){
        Tile fgTile = layerFG.get(position);
        if (!(fgTile.equals(FLOOR) && !tile.isMonster())) layerBG.set(position, layerFG.get(position));
        layerFG.set(position, tile);
    }
    
    Button getButton(Position position, Class buttonType) {
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
    Button getButton(Position position) {
        for (Button[] buttons : new Button[][] {greenButtons, redButtons, brownButtons, blueButtons}) {
            for (Button b : buttons) {
                if (b.getButtonPosition().equals(position)) return b;
            }
        }
        return null;
    }
    boolean isTrapOpen(Position position) {
        for (BrownButton b : brownButtons) {
            if (b.getTargetPosition().equals(position) && b.isOpen(this)) return true;
        }
        return false;
    }
    
    private int moveType(byte b, boolean halfMove, boolean chipSliding){
        if (b <= 0 || b == WAIT){
            if (mouseClick != NO_CLICK) {
                if (chipSliding) return CLICK_EARLY;
                if (halfMove) return CLICK_LATE;
            }
            else return HALF_WAIT;
        }
        else if (b == UP || b == LEFT || b == DOWN || b == RIGHT){
            return KEY;
        }
        return HALF_WAIT;
    }
    
    private void moveChipSliding(){
        Direction direction = chip.getDirection();
        Tile bgTile = layerBG.get(chip.getPosition());
        if (bgTile.isFF()) chip.tick(new Direction[] {direction}, this, true);
        else chip.tick(chip.getSlideDirectionPriority(bgTile, rng, true), this, true);
    }
    
    private void moveChip(Direction[] directions){
        Position oldPosition = chip.getPosition().clone();
        for (Direction direction : directions) {
            if (chip.isSliding()) {
                if (!layerBG.get(chip.getPosition()).isFF()) continue;
                if (direction == chip.getDirection()) continue;
            }
            chip.tick(new Direction[] {direction}, this, false);
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
        if (layerBG.get(chip.getPosition()).equals(EXIT)){
            layerFG.set(chip.getPosition(), EXITED_CHIP);
            chip.kill();
        }
        return chip.isDead();
    }
    
    /**
     * Advances a tick (10th of a second).
     * <p>
     *     This method is not responsible for setting the click position, or
     *     for checking whether chip can move (in case chip moved the previous
     *     tick).
     * </p>
     * @param b The direction in which to move. If b is positive it should be
     *          one of UP, LEFT, DOWN, RIGHT and WAIT. If b is negative, it is
     *          interpreted as a mouse click. Note that the click itself is not
     *          set here - use {@link #setClick(int)} for that.
     * @param directions The directions in which chip should try to move
     * @return true if the next move should be made automatically without input
     */
    public boolean tick(byte b, Direction[] directions){
        
        initialiseSlidingMonsters();
        boolean isHalfMove = tickNumber % 2 != 0;
        int moveType = moveType(b, isHalfMove, chip.isSliding());
        monsterList.initialise();
    
        if (tickNumber > 0 && !isHalfMove) monsterList.tick();
        
        if (endTick()) return false;
        if (chip.isSliding()) moveChipSliding();
        if (endTick()) return false;
        tickNumber++;
        if (moveType == CLICK_EARLY) moveChip(chip.seek(new Position(mouseClick)));
        if (endTick()) return false;
        slipList.tick();
        if (endTick()) return false;
        if (moveType == KEY) moveChip(directions);
        else if (moveType == CLICK_LATE) moveChip(chip.seek(new Position(mouseClick)));
        if (endTick()) return false;

        monsterList.finalise();
        finaliseTraps();
        if (moveType == KEY || chip.getPosition().getIndex() == mouseClick) mouseClick = NO_CLICK;
    
        return moveType == KEY && !isHalfMove && !chip.isSliding();
    }
    
}
