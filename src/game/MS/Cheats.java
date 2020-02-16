package game.MS;

import game.*;
import game.button.*;

public class Cheats {
    
    private final MSLevel level;
    
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
        for (int i = 0; i < level.getBrownButtons().length; i++) {
            if (level.getBrownButtons()[i].getTargetPosition().equals(trapPosition)) {
                level.getOpenTraps().set(i, open);
            }
        }
    }
    public void pressBlueButton() {
        new BlueButton(null).press(level);
    }
    public void pressBlueButton(BlueButton button) {
        button.press(level);
    }
    public void pressButton(Button button) {
        if (button instanceof GreenButton) button.press(level);
        if (button instanceof RedButton) pressRedButton((RedButton) button);
        if (button instanceof BrownButton) pressBrownButton((BrownButton) button);
        if (button instanceof BlueButton) button.press(level);
    }
    public void pressButton(Position position) {
        Button button = level.getButton(position);
        if (button != null) pressButton(button);
    }
    
    // Monster related cheats
    
    public void setDirection(MSCreature creature, Direction direction) {
        level.popTile(creature.getPosition());
        if (creature.getCreatureType() == CreatureID.BLOB) creature.setNextMoveDirectionCheat(direction);
        creature.setDirection(direction);
        level.insertTile(creature.getPosition(), creature.toTile());
    }
    public void setPosition(MSCreature creature, Position position) {
        level.popTile(creature.getPosition());
        creature.getPosition().setIndex(position.getIndex());
        level.insertTile(creature.getPosition(), creature.toTile());
    }
    public void setSliding(MSCreature creature, boolean sliding) {
        creature.setSliding(sliding, level);
    }
    public void kill(Creature creature) {
        level.getMonsterList().initialise();
        creature.kill();
        level.getSlipList().remove(creature);
        level.getMonsterList().numDeadMonsters++;
        level.popTile(creature.getPosition());
        level.getMonsterList().finalise();
    }
    public void animateMonster(Position position) {
        level.getMonsterList().addClone(position);
        level.getMonsterList().finalise();
    }
    public void reviveChip() {
        level.getChip().setCreatureType(CreatureID.CHIP);
        level.getLayerFG().set(level.getChip().getPosition(), Tile.CHIP_DOWN);
    }
    public void moveChip(Position position) {
        level.popTile(level.getChip().getPosition());
        level.getChip().getPosition().setIndex(position.getIndex());
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

            GreenButton[] oldGreenButtons = level.getGreenButtons();
            GreenButton[] greenButtons = new GreenButton[oldGreenButtons.length + 1];
            System.arraycopy(oldGreenButtons, 0, greenButtons, 0, oldGreenButtons.length);

            greenButtons[greenButtons.length - 1] = button;

            level.setGreenButtons(greenButtons);
        }
        if (tile == Tile.BUTTON_BLUE) {
            BlueButton button = new BlueButton(position);

            BlueButton[] oldBlueButtons = level.getBlueButtons();
            BlueButton[] blueButtons = new BlueButton[oldBlueButtons.length + 1];
            System.arraycopy(oldBlueButtons, 0, blueButtons, 0, oldBlueButtons.length);

            blueButtons[blueButtons.length - 1] = button;

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
        level.rng.setCurrentValue(rng);
    }
    
    public Cheats(MSLevel level) {
        this.level = level;
    }
    
}
