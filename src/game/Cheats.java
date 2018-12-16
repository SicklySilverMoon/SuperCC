package game;

import game.button.*;

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
    public void open(Position trapPosition) {
        for (int i = 0; i < level.getBrownButtons().length; i++) {
            if (level.getBrownButtons()[i].getTargetPosition().equals(trapPosition)) {
                level.getOpenTraps().set(i, true);
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
    
    // Monster related cheats
    
    public void setDirection(Creature creature, Direction direction) {
        creature.setDirection(direction);
    }
    public void setPosition(Creature creature, Position position) {
        creature.getPosition().setIndex(position.getIndex());
    }
    public void setSliding(Creature creature, boolean sliding) {
        creature.setSliding(sliding, level);
    }
    public void kill(Creature creature) {
        level.getMonsterList().initialise();
        creature.kill();
        level.getMonsterList().finalise();
    }
    public void reviveChip() {
        level.getChip().setCreatureType(CreatureID.CHIP);
    }
    public void moveChip(Position position) {
        level.popTile(level.getChip().getPosition());
        level.getChip().getPosition().setIndex(position.getIndex());
        level.insertTile(position, Tile.CHIP_DOWN);
    }
    
    // Layer related cheats
    
    public void setLayerBG(Position position, Tile tile) {
        level.getLayerBG().set(position, tile);
    }
    public void setLayerFG(Position position, Tile tile) {
        level.getLayerBG().set(position, tile);
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
    
    public Cheats(Level level) {
        this.level = level;
    }
    
}
