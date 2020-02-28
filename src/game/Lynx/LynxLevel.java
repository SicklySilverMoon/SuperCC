package game.Lynx;

import game.*;
import game.MS.Cheats;
import game.MS.MSSlipList;
import game.button.*;

import java.util.BitSet;

public class LynxLevel extends LynxSaveState implements Level {

    private static final int HALF_WAIT = 0, KEY = 1;
    private final int LEVELSET_LENGTH;

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
    public int getRngSeed() {
        return rngSeed;
    }

    @Override
    public Step getStep() {
        return step;
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
    public int getTimer(){
        if (tickNumber == 0) return startTime;
        else return startTime - tickNumber;
    }

    @Override
    public int getTChipTime() {
        if (tickNumber == 0) return 9999;
        else return 9999 - tickNumber;
    }

    @Override
    public int getChipsLeft() {
        return chipsLeft;
    }

    @Override
    public Creature getChip() {
        return chip;
    }

    @Override
    public short[] getKeys() {
        return keys;
    }

    @Override
    public byte[] getBoots() {
        return boots;
    }

    @Override
    public CreatureList getMonsterList() {
        return monsterList;
    }

    @Override
    public SlipList getSlipList() {
        return new MSSlipList(); //TODO: look into getting rid of this altogether
    }

    @Override
    public BitSet getOpenTraps() {
        return traps;
    }

    @Override
    public int getLevelsetLength() {
        return LEVELSET_LENGTH;
    }

    @Override
    public Cheats getCheats() {
        return null;
    }

    @Override
    public RNG getRNG() {
        return rng;
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
    public int getTickNumber() {
        return tickNumber;
    }

    @Override
    public void setClick(int position) {
        //Having set click in he interface means i don't have to add checks for MS mode into the gui areas, something i really do not want to do
    }

    @Override
    public void setLevelWon(boolean won) {
        this.levelWon = won;
    }

    @Override
    public boolean isCompleted() {
        return levelWon;
    }

    @Override
    public boolean tick(byte b, Direction[] directions) {
        setLevelWon(false);//Each tick sets the level won state to false so that even when rewinding unless you stepped into the exit the level is not won

        moveChip(directions);

        monsterList.tick();

        return false;
    }

    @Override
    public void insertTile(Position position, Tile tile) {

    }

    @Override
    public void popTile(Position position) {

    }

    private void moveChip(Direction[] directions) {
        Position oldPosition = chip.getPosition().clone();
        for (Direction direction : directions) {
            if (chip.isSliding()) {
                if (!layerBG.get(chip.getPosition()).isFF()) continue;
                if (direction == chip.getDirection()) continue;
            }
            chip.tick(this, false);
            if (!chip.getPosition().equals(oldPosition)) break;
        }
    }

    public LynxLevel(int levelNumber, byte[] title, byte[] password, byte[] hint, Position[] toggleDoors, Position[] teleports,
                   GreenButton[] greenButtons, RedButton[] redButtons,
                   BrownButton[] brownButtons, BlueButton[] blueButtons, BitSet traps,
                   Layer layerBG, Layer layerFG, CreatureList monsterList,
                   LynxCreature chip, int time, int chips, RNG rng, int rngSeed, Step step, int levelsetLength){

        super(layerBG, layerFG, monsterList, chip,
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
        this.LEVELSET_LENGTH = levelsetLength;

        this.monsterList.setLevel(this);
    }
}
