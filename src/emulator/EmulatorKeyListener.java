package emulator;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class EmulatorKeyListener extends KeyAdapter {
    
    private final SuperCC emulator;
    
    public enum Key {
        UP(SuperCC.UP),
        LEFT(SuperCC.LEFT),
        DOWN(SuperCC.DOWN),
        RIGHT(SuperCC.RIGHT),
        HALF_WAIT(SuperCC.WAIT),
        FULL_WAIT(SuperCC.WAIT),
        REWIND,
        FORWARD;
        private byte directionByte;
        private int keyCode;

        public int getKeyCode() {
            return keyCode;
        }
        
        Key(byte directionByte) {
            this.directionByte = directionByte;
        }
        Key() {}
    }
    private HashMap<Integer, Key> keyMap = new HashMap<>();
    
    public void setKeyCode(Key key, int keyCode) {
        keyMap.remove(key.keyCode);
        key.keyCode = keyCode;
        keyMap.put(keyCode, key);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        Key k = keyMap.getOrDefault(e.getKeyCode(), null); //Checks if the pressed key is one of the 'action' keys
        if (k == null) {
            if (e.getKeyCode() != KeyEvent.VK_SHIFT && e.isShiftDown()) {
                if (e.isControlDown() && 48 <= keyCode && keyCode <= 57) { //Values for 0 through 9
                    boolean recording = emulator.getSavestates().checkpointRecorder(keyCode-48);
                    if (recording) emulator.showAction("Started Checkpoint Record");
                    else emulator.showAction("Finished Checkpoint Record");
                }
                if (e.getKeyCode() == 47) { //Hardcoded value for the '/' key, should switch this to a proper keybind
                    emulator.getSavestates().addUndesirableSavestate();
                    emulator.showAction("Undesirable State saved");
                }
                else {
                    if (!e.isControlDown()) { //Just so you can't accidentally save a state into these
                        emulator.getSavestates().addSavestate(keyCode);
                        emulator.showAction("State " + KeyEvent.getKeyText(e.getKeyCode()) + " saved");
                    }
                }
            }
            else {
                if (e.isControlDown() && 48 <= keyCode && keyCode <= 57) { //Values for 0 through 9
                    //Time to code in loading the checkpoint moves
                    int size = emulator.getSavestates().getCheckpoint(keyCode-48).size();
                    for (int i = 0; i < size; i++) {
                        byte b = emulator.getSavestates().getCheckpoint(keyCode-48).get(i);
                        switch (b){
                            case 85: b = SuperCC.UP; break;
                            case 76: b = SuperCC.LEFT; break;
                            case 68: b = SuperCC.DOWN; break;
                            case 82: b = SuperCC.RIGHT; break;
                            case 45: b = SuperCC.WAIT; break;
                        }
                        emulator.tick(b, TickFlags.GAME_PLAY);
                    }
                emulator.showAction("Checkpoint " + KeyEvent.getKeyText(e.getKeyCode()) + " loaded");
                }
                else if (emulator.getSavestates().load(keyCode, emulator.getLevel())) {
                    emulator.showAction("State " + KeyEvent.getKeyText(e.getKeyCode()) + " loaded");
                    emulator.getMainWindow().repaint(false);
                }
            }
        }
        else {
            switch (k) {
                case UP:
                case LEFT:
                case DOWN:
                case RIGHT:
                case HALF_WAIT:
                    if (!emulator.getLevel().getChip().isDead() && !SuperCC.areToolsRunning())
                        emulator.tick(k.directionByte, TickFlags.GAME_PLAY);
                    break;
                case FULL_WAIT:
                    if (!emulator.getLevel().getChip().isDead() && !SuperCC.areToolsRunning())
                        emulator.tick(k.directionByte, TickFlags.GAME_PLAY);
                    if (!emulator.getLevel().getChip().isDead() && !SuperCC.areToolsRunning())
                        emulator.tick(k.directionByte, TickFlags.GAME_PLAY);
                    break;
                case REWIND:
                    emulator.getSavestates().rewind();
                    emulator.getLevel().load(emulator.getSavestates().getSavestate());
                    emulator.showAction("Rewind");
                    emulator.getMainWindow().repaint(false);
                    break;
                case FORWARD:
                    emulator.getSavestates().replay();
                    emulator.getLevel().load(emulator.getSavestates().getSavestate());
                    emulator.showAction("Replay");
                    emulator.getMainWindow().repaint(false);
                    break;
            }
        }
        
    }
    
    public EmulatorKeyListener(SuperCC emulator) {
        this.emulator = emulator;
        int[] controls = emulator.getPaths().getControls();
        Key[] allKeys = Key.values();
        for (int i = 0; i < allKeys.length; i++) {
            allKeys[i].keyCode = controls[i];
            keyMap.put((int) controls[i], allKeys[i]);
        }
    }
    
}
