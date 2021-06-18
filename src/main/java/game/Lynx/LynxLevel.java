package game.Lynx;

import game.*;
import game.Cheats;
import game.MS.SlipList;
import game.button.*;

import java.util.BitSet;

import static game.Direction.*;

public class LynxLevel extends LynxSavestate implements Level {

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
    private final Direction INITIAL_SLIDE;
    private final Ruleset RULESET = Ruleset.LYNX;
    private boolean levelWon, turnTanks;

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
    public boolean supportsSliplist() {
        return false;
    }

    @Override
    public boolean hasCyclicRFF() {
        return true;
    }

    @Override
    public boolean chipInMonsterList() {
        return true;
    }

    @Override
    public int ticksPerMove() {
        return RULESET.ticksPerMove;
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
    public boolean isUntimed() {
        return startTime < 0;
    }

    @Override
    public int getTimer(){
        if (tickNumber == 0) return startTime;
        else return startTime - tickNumber*5 + 5; //first tick does not change timer
    }
    @Override
    public void setTimer(int n) {
        startTime = n + tickNumber - 1;
    }

    @Override
    public int getTChipTime() {
        if (tickNumber == 0) return 99995;
        else return 99995 - tickNumber*5 + 5; //first tick does not change timer
    }

    @Override
    public int getChipsLeft() {
        return chipsLeft;
    }

    @Override
    public void setChipsLeft(int chipsLeft) {
        this.chipsLeft = chipsLeft;
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
    public void setKeys(short[] keys) {
        this.keys = keys;
    }

    @Override
    public byte[] getBoots() {
        return boots;
    }

    @Override
    public void setBoots(byte[] boots){
        this.boots = boots;
    }

    @Override
    public CreatureList getMonsterList() {
        return monsterList;
    }

    @Override
    public SlipList getSlipList() {
        throw new UnsupportedOperationException("Sliplist does not exist under Lynx");
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
        return cheats;
    }

    @Override
    public RNG getRNG() {
        return rng;
    }

    @Override
    public Button getButton(Position position, Class<? extends Button> buttonType) {
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
            if (b.getTargetPosition().equals(position) && b.isOpen(this))
                return true;
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
    public Direction getInitialRFFDirection() {
        return INITIAL_SLIDE;
    }

    @Override
    public Direction getRFFDirection(boolean advance) {
        if (advance)
            rffDirection = rffDirection.turn(Direction.RIGHT);
        return rffDirection;
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
        tickNumber++;
        setLevelWon(false); //Each tick sets the level won state to false so that even when rewinding unless you stepped into the exit the level is not won
        turnTanks = false;

        monsterList.initialise();
        monsterList.tick(); //select monster moves
        Tile chipTileOld = layerFG.get(chip.getPosition());
        if (chip.getTimeTraveled() == 0)
            selectChipMove(directions[0]);
        Direction chipTDir = chip.getTDirection();
        monsterList.tick(); //move monsters
        boolean result = moveChip();
        Tile chipTileNew = layerFG.get(chip.getPosition());
        monsterList.tick(); //teleport monsters
        monsterList.finalise();

        if (turnTanks) {
            for (Creature m : monsterList) {
                Tile floor = layerFG.get(m.getPosition());
                if (m.getCreatureType() == CreatureID.TANK_MOVING && m.getTimeTraveled() == 0
                        && floor != Tile.CLONE_MACHINE && !floor.isIce())
                    m.turn(TURN_AROUND);
            }
        }

        if (layerFG.get(chip.getPosition()) == Tile.EXIT && chip.getTimeTraveled() == 0
                && chip.getAnimationTimer() == 0) {
            //todo: move this into the "entered tile" logic area as currently starting on the exit wins lol
            chip.kill();
            chip.kill(); //destroys the animation as well
            layerFG.set(chip.getPosition(), Tile.EXITED_CHIP);
            setLevelWon(true);
            return false;
        }
        return (result && !chip.isSliding() && !(chipTileOld.isSliding() || chipTileNew.isSliding()
                || chipTileOld == Tile.TRAP || chipTileNew == Tile.TRAP));
        //traps perform forced moves but aren't counted as sliding tiles as it would fuck some logic up
    }

    private void selectChipMove(Direction direction) {
        chip.setTDirection(NONE);
        chip.setFDirection(NONE);

        Position chipPos = chip.getPosition();
        if (chip.getForcedMove(layerFG.get(chipPos))) {
            return;
        }

        if (direction == NONE) {
            return;
        }

        if (direction.isDiagonal()) {
            Direction chipDir = chip.getDirection();
            if (direction.isComponent(chipDir)) {
                boolean canMoveMain = chip.canMakeMove(chipDir, chipPos.move(chipDir), false, true, false, false);
                Direction other = direction.decompose()[0] == chipDir ? direction.decompose()[1] : direction.decompose()[0];
                boolean canMoveOther = chip.canMakeMove(other, chipPos.move(other), false, true, false, false);
                if (!canMoveMain && canMoveOther) {
                    chip.setTDirection(other);
                    return;
                }
                chip.setTDirection(chipDir);
            }
            else {
                Direction vert = direction.decompose()[0];
                Direction horz = direction.decompose()[1]; //horz dir is always second in decompose
                if (chip.canMakeMove(horz, chipPos.move(horz), false, true, false, false)) {
                    chip.setTDirection(horz);
                }
                else {
                    chip.setTDirection(vert);
                }
            }
            return;
        }

        chip.canMakeMove(direction, chipPos.move(direction), false, true, false, false); //for side effects
        chip.setTDirection(direction);
    }

    private boolean moveChip() {
        boolean result = chip.tick(false) && chip.getTDirection() != Direction.NONE;
        chip.setTDirection(NONE); //mirror TW clearing the dirs at the end of the monster loop
        chip.setFDirection(NONE);

        //todo: this is an exact copy of the same code in the creature list because we handle chip outside the creature list, look into why we do that
        if (chip.getTimeTraveled() == 0 && layerFG.get(chip.getPosition()) == Tile.BUTTON_BROWN) {
            for (BrownButton b : brownButtons) {
                if (b.getButtonPosition().equals(chip.getPosition())) {
                    monsterList.springTrappedCreature(b.getTargetPosition());
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void insertTile(Position position, Tile tile) {
        layerFG.set(position, tile);
    }

    @Override
    public void popTile(Position position) {
        layerFG.set(position, Tile.FLOOR);
    }

    @Override
    public boolean shouldDrawCreatureNumber(Creature creature) {
        return !(creature.isDead() && creature.getAnimationTimer() == 0)
                && layerFG.get(creature.getPosition()) != Tile.CLONE_MACHINE;
    }

    @Override
    public void turnTanks() {
        turnTanks ^= true;
    }

    public LynxLevel(int levelNumber, byte[] title, byte[] password, byte[] hint, Position[] toggleDoors, Position[] teleports,
                   GreenButton[] greenButtons, RedButton[] redButtons,
                   BrownButton[] brownButtons, BlueButton[] blueButtons, BitSet traps,
                   Layer layerFG, CreatureList monsterList,
                   Creature chip, int time, int chips, RNG rng, int rngSeed, Step step, int levelsetLength, Direction INITIAL_SLIDE){

        super(layerFG, monsterList, chip,
                chips, new short[4], new byte[4], rng, traps);

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
        this.rffDirection = INITIAL_SLIDE.turn(TURN_LEFT); //Yup, easier to rotate left here than have every other section rotate right
        this.step = step;
        this.cheats = new Cheats(this);
        this.LEVELSET_LENGTH = levelsetLength;
        this.INITIAL_SLIDE = INITIAL_SLIDE;

        for (Creature c : monsterList)
            c.setLevel(this);
        this.monsterList.setLevel(this);
    }
}