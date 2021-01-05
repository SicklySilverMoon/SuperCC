package game.Lynx;

import game.*;
import game.Cheats;
import game.MS.SlipList;
import game.button.*;

import java.util.BitSet;

import static game.CreatureID.BLOCK;

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
    public int getTimer(){
        if (tickNumber == 0) return startTime;
        else return startTime - tickNumber*5;
    }
    @Override
    public void setTimer(int n) {
        startTime = n + tickNumber - 1;
    }

    @Override
    public int getTChipTime() {
        if (tickNumber == 0) return 99995;
        else return 99995 - tickNumber*5;
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
    public Direction getAndCycleRFFDirection() {
        Direction priorDirection = rffDirection;
        rffDirection = rffDirection.turn(Direction.RIGHT);
        return priorDirection;
    }

    @Override
    public Direction getRFFDirection() {
        return rffDirection;
    }

    @Override
    public void cycleRFFDirection() {
        getAndCycleRFFDirection();
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

        monsterList.initialise();
        monsterList.tick(); //select monster moves
        selectChipMove(directions);
        monsterList.tick(); //move monsters
        boolean result = moveChip(directions);
        monsterList.tick(); //teleport monsters
        monsterList.finalise();
        return (result && !chip.isSliding());
    }

    private void selectChipMove(Direction[] directions) {
        if (directions.length == 0)
            return;
        if (directions.length > 1) {
            assert directions.length == 2;
            for (int j = 0; j < directions.length; j++) {
                Direction dir = directions[j];
                if (chip.getDirection() == dir && j > 0) {
                    int k = 0;
                    directions[j] = directions[k];
                    directions[k] = dir;
                    break;
                }
            }
        }

        Position newPosition = chip.getPosition().move(directions[0]);
        Tile currentTile = layerFG.get(chip.getPosition());
        Tile newTile = layerFG.get(newPosition);
        boolean canMove = (chip.canEnter(directions[0], newTile) && chip.canLeave(directions[0], currentTile, chip.getPosition()));

        if (!canMove || monsterList.animationAt(newPosition) != null) {
            if (directions.length > 1) {
                Direction first = directions[0];
                directions[0] = directions[1];
                directions[1] = first;
            }
            newPosition = chip.getPosition().move(directions[0]);
        }
        if (monsterList.claimed(newPosition) && monsterList.creatureAt(newPosition).getCreatureType() != BLOCK) {
            chip.kill();
            monsterList.creatureAt(newPosition).kill();
        }
    }

    private boolean moveChip(Direction[] directions) {
        if (chipDeathCheck())
            return false;

        if (chip.getTimeTraveled() != 0 || chip.getAnimationTimer() != 0) {
            return chip.tick(null);
        }
        if (directions.length == 0 && !chip.isSliding())
            return false;

        Tile formerTile = layerFG.get(chip.getPosition().move(chip.getDirection().turn(Direction.TURN_AROUND)));
        if (boots[3] == 0) {
            if (formerTile.isFF() && lastMoveForced)
                canOverride = true;
            else if (!formerTile.isIce() || boots[2] != 0)
                canOverride = false;
        }

        Tile currentTile = layerFG.get(chip.getPosition());
        if (!chip.isSliding() || (currentTile.isFF() && directions.length != 0)) {
            chip.setDirection(directions[0]); //chip just ignores the rules about can move into tiles and such
        }

        if (currentTile.isFF() && boots[3] == 0) {
            Direction slideDirection = chip.getSlideDirection(chip.getDirection(), currentTile, null);
            if (canOverride && directions.length != 0) {
                if (chip.getDirection() == slideDirection.turn(Direction.TURN_AROUND)) {
                    lastMoveForced = false;
                    return false;
                }
                if (chip.getDirection() == slideDirection) {
                    directions = new Direction[]{slideDirection};
                    lastMoveForced = true;
                }
                else lastMoveForced = false;
            }
            else {
                directions = new Direction[]{slideDirection};
                chip.setDirection(slideDirection);
                lastMoveForced = true;
            }
        }
        else if (currentTile.isSliding() || currentTile == Tile.TRAP) {
            if (!(currentTile.isIce() && boots[2] != 0) && !(currentTile.isFF() && boots[3] != 0)) {
                if (!currentTile.isIce())
                    lastMoveForced = false;
                Direction slideDirection = chip.getSlideDirection(chip.getDirection(), currentTile, null);
                directions = new Direction[]{slideDirection};
                chip.setDirection(slideDirection);
            }
        }

        //block slap
        if (directions.length > 1) {
            pushBlock(chip.getPosition().move(directions[1]), directions[1]);
        }

        //blocks ahead of chip
        if (directions.length > 0)
            pushBlock(chip.getPosition().move(directions[0]), directions[0]);

        return chip.tick(directions[0]);
    }

    private void pushBlock(Position position, Direction direction) {
        if (layerFG.get(position) == Tile.CLONE_MACHINE)
            return;
        Creature block = monsterList.creatureAt(position);
        if (block != null && block.getCreatureType() == CreatureID.BLOCK && block.getTimeTraveled() == 0) {
            block.setDirection(direction);
            monsterList.tickCreature(block, direction);
        }
    }

    /**
     * @return true if Chip is killed as a result of this, false otherwise
     */
    private boolean chipDeathCheck() {
        if (monsterList.claimed(chip.getPosition())) {
            chip.kill();
            monsterList.creatureAt(chip.getPosition()).kill();
            return true;
        }
        return false;
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
        this.rffDirection = INITIAL_SLIDE;
        this.step = step;
        this.cheats = new Cheats(this);
        this.LEVELSET_LENGTH = levelsetLength;
        this.INITIAL_SLIDE = INITIAL_SLIDE;

        Creature.setLevel(this);
        Creature.setMonsterList(monsterList);
        this.monsterList.setLevel(this);
    }
}
