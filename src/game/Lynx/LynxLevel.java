package game.Lynx;

import game.*;
import game.MS.Cheats;
import game.button.BlueButton;
import game.button.BrownButton;
import game.button.GreenButton;
import game.button.RedButton;

import java.util.BitSet;

public class LynxLevel extends LynxSaveState implements Level {
    @Override
    public int getLevelNumber() {
        return 0;
    }

    @Override
    public int getStartTime() {
        return 0;
    }

    @Override
    public byte[] getTitle() {
        return new byte[0];
    }

    @Override
    public byte[] getPassword() {
        return new byte[0];
    }

    @Override
    public byte[] getHint() {
        return new byte[0];
    }

    @Override
    public Position[] getToggleDoors() {
        return new Position[0];
    }

    @Override
    public Position[] getPortals() {
        return new Position[0];
    }

    @Override
    public GreenButton[] getGreenButtons() {
        return new GreenButton[0];
    }

    @Override
    public RedButton[] getRedButtons() {
        return new RedButton[0];
    }

    @Override
    public BrownButton[] getBrownButtons() {
        return new BrownButton[0];
    }

    @Override
    public BlueButton[] getBlueButtons() {
        return new BlueButton[0];
    }

    @Override
    public void setGreenButtons(GreenButton[] greenButtons) {

    }

    @Override
    public void setRedButtons(RedButton[] redButtons) {

    }

    @Override
    public void setBrownButtons(BrownButton[] brownButtons) {

    }

    @Override
    public void setBlueButtons(BlueButton[] blueButtons) {

    }

    @Override
    public int getRngSeed() {
        return 0;
    }

    @Override
    public Step getStep() {
        return null;
    }

    @Override
    public Layer getLayerBG() {
        return null;
    }

    @Override
    public Layer getLayerFG() {
        return null;
    }

    @Override
    public int getTimer() {
        return 0;
    }

    @Override
    public int getTChipTime() {
        return 0;
    }

    @Override
    public int getChipsLeft() {
        return 0;
    }

    @Override
    public Creature getChip() {
        return null;
    }

    @Override
    public short[] getKeys() {
        return new short[0];
    }

    @Override
    public byte[] getBoots() {
        return new byte[0];
    }

    @Override
    public CreatureList getMonsterList() {
        return null;
    }

    @Override
    public SlipList getSlipList() {
        return null;
    }

    @Override
    public BitSet getOpenTraps() {
        return null;
    }

    @Override
    public int getLevelsetLength() {
        return 0;
    }

    @Override
    public Cheats getCheats() {
        return null;
    }

    @Override
    public RNG getRNG() {
        return null;
    }

    @Override
    public void setClick(int position) {

    }

    @Override
    public void setLevelWon(boolean won) {

    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public boolean tick(byte b, Direction[] directions) {
        return false;
    }
}
