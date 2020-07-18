package game.Lynx;

import game.*;
import game.MS.Cheats;
import game.MS.SlipList;
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
    private Direction initialSlide;
    private final Ruleset RULESET = Ruleset.LYNX;
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
    public boolean supportsLayerBG() {
        return false;
    }

    @Override
    public boolean supportsClick() {
        return false;
    }

    @Override
    public boolean hasCyclicRFF() {
        return true;
    }

    @Override
    public Layer getLayerBG() {
        throw new UnsupportedOperationException("Background Layer does not exist under Lynx");
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
        return new SlipList(); //TODO: look into getting rid of this altogether
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
        return initialSlide;
    }

    @Override
    public void setClick(int position) {
        throw new UnsupportedOperationException("Mouse clicks do not exist under Lynx");
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
    public boolean tick(char c, Direction[] directions) {
        setLevelWon(false);//Each tick sets the level won state to false so that even when rewinding unless you stepped into the exit the level is not won

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

    }

    public LynxLevel(int levelNumber, byte[] title, byte[] password, byte[] hint, Position[] toggleDoors, Position[] teleports,
                   GreenButton[] greenButtons, RedButton[] redButtons,
                   BrownButton[] brownButtons, BlueButton[] blueButtons, BitSet traps,
                   Layer layerFG, CreatureList monsterList,
                   LynxCreature chip, int time, int chips, RNG rng, int rngSeed, Step step, int levelsetLength, Direction initialSlide){

        super(layerFG, monsterList, chip,
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
        this.initialSlide = initialSlide;

        Creature.setLevel(this);
        Creature.setMonsterList(monsterList);
        this.monsterList.setLevel(this);
    }
}
