package game;

import game.MS.MSCreatureList;
import game.button.*;

import java.util.HashMap;
import java.util.Map;

public class Cheats {
    
    private final Level level;
    
    // Button related cheats
    
    public void pressGreenButton() {
        new GreenButton(null).press(level);
    }
    public void pressGreenButton(GreenButton button) {
        button.press(level);
    }
    public void pressRedButton(RedButton button) {
        level.getMonsterList().initialise();
        button.press(level);
        level.getMonsterList().finalise();
    }
    public void clone(Position clonerPosition) {
        level.getMonsterList().initialise();
        level.getMonsterList().addClone(clonerPosition);
        level.getMonsterList().finalise();
    }
    public void pressBrownButton(BrownButton button) {
        button.press(level);
    }
    public void setTrap(Position trapPosition, boolean open) {
        level.setTrap(trapPosition, open);
    }
    public void pressBlueButton() {
        new BlueButton(null).press(level);
    }
    public void pressBlueButton(BlueButton button) {
        button.press(level);
    }
    public void pressButton(Button button) {
        if (button instanceof RedButton)
            pressRedButton((RedButton) button);
        else
            button.press(level);
    }
    public void pressButton(Position position) {
        Button button = level.getButton(position);
        if (button != null) pressButton(button);
    }
    
    // Monster related cheats
    
    public void setDirection(Creature creature, Direction direction) {
        if (level.supportsLayerBG())
            level.popTile(creature.getPosition());
        if (creature.getCreatureType() == CreatureID.BLOB) creature.setNextMoveDirectionCheat(direction);
        creature.setDirection(direction);
        if (level.supportsLayerBG())
            level.insertTile(creature.getPosition(), creature.toTile());
    }
    public void setPosition(Creature creature, Position position) {
        if (level.supportsLayerBG())
            level.popTile(creature.getPosition());
        creature.getPosition().setIndex(position.getIndex());
        if (level.supportsLayerBG())
            level.insertTile(creature.getPosition(), creature.toTile());
    }
    public void kill(Creature creature) {
        level.getMonsterList().initialise();
        creature.kill();
        if (level.supportsSliplist()) {
            level.getSlipList().remove(creature);
            ((MSCreatureList)(level.getMonsterList())).incrementDeadMonsters();
        }
        if (level.supportsLayerBG())
            level.popTile(creature.getPosition());
        level.getMonsterList().finalise();
    }
    public void animateMonster(Position position) {
        level.getMonsterList().addClone(position);
        level.getMonsterList().finalise();
    }
    public void reviveChip() {
        level.getChip().setCreatureType(CreatureID.CHIP);
        if (level.supportsLayerBG())
            level.getLayerFG().set(level.getChip().getPosition(), Tile.CHIP_DOWN);
    }
    public void moveChip(Position position) {
        if (level.supportsLayerBG())
            level.popTile(level.getChip().getPosition());
        level.getChip().getPosition().setIndex(position.getIndex());
        if (level.supportsLayerBG())
            level.insertTile(position, level.getChip().toTile());
    }
    
    // Layer related cheats
    
    public void setLayerBG(Position position, Tile tile) {
        level.getLayerBG().set(position, tile);
    }
    public void setLayerFG(Position position, Tile tile) {
        level.getLayerBG().set(position, tile);
    }
    public void popTile(Position position) {
        level.popTile(position);
    }
    public void insertTile(Position position, Tile tile) {
        level.insertTile(position, tile);

        if (tile == Tile.BUTTON_GREEN) { //All this just to avoid a null pointer when you use insert tile to add a blue or green button
            GreenButton button = new GreenButton(position);

            Map<Position, GreenButton> greenButtons = new HashMap<>(level.getGreenButtons());
            greenButtons.put(position ,button);

            level.setGreenButtons(greenButtons);
        }
        if (tile == Tile.BUTTON_BLUE) {
            BlueButton button = new BlueButton(position);

            Map<Position, BlueButton> blueButtons = new HashMap<>(level.getBlueButtons());
            blueButtons.put(position, button);

            level.setBlueButtons(blueButtons);
        }
    }
    
    // Level related cheats
    
    public void setTimer(int timer) {
        level.setTimer(timer);
    }
    public void setChipsLeft(int chipsLeft) {
        level.setChipsLeft(chipsLeft);
    }
    public void setKeys(short[] keys) {
        level.setKeys(keys);
    }
    public void setBoots(byte[] boots) {
        level.setBoots(boots);
    }
    public void setRng(int rng) {
        level.getRNG().setCurrentValue(rng);
    }
    
    public Cheats(Level level) {
        this.level = level;
    }
    
}
