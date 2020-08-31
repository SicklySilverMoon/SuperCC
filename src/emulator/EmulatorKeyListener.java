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
        UP_LEFT(SuperCC.UP_LEFT),
        DOWN_LEFT(SuperCC.DOWN_LEFT),
        DOWN_RIGHT(SuperCC.DOWN_RIGHT),
        UP_RIGHT(SuperCC.UP_RIGHT),
        REWIND,
        FORWARD;
        private char directionChar;
        private int keyCode;

        public int getKeyCode() {
            return keyCode;
        }
        
        Key(char directionChar) {
            this.directionChar = directionChar;
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
                if (e.isControlDown() && KeyEvent.VK_0 <= keyCode && keyCode <= KeyEvent.VK_9) {
                    boolean recording = emulator.getSavestates().checkpointRecorder(keyCode-KeyEvent.VK_0);
                    if (recording) emulator.showAction("Started Checkpoint Record");
                    else emulator.showAction("Finished Checkpoint Record");
                }
                if (e.getKeyCode() == KeyEvent.VK_SLASH) { //Hardcoded value for the '/' key, should switch this to a proper keybind
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
                if (e.isControlDown() && KeyEvent.VK_0 <= keyCode && keyCode <= KeyEvent.VK_9) {
                    //Time to code in loading the checkpoint moves
                    int size = emulator.getSavestates().getCheckpoint(keyCode-KeyEvent.VK_0).size();
                    for (int i = 0; i < size; i++) {
                        char c = emulator.getSavestates().getCheckpoint(keyCode-KeyEvent.VK_0).get(i);
                        switch (c){
                            case 'U': c = SuperCC.UP; break;
                            case 'L': c = SuperCC.LEFT; break;
                            case 'D': c = SuperCC.DOWN; break;
                            case 'R': c = SuperCC.RIGHT; break;
                            case '-': c = SuperCC.WAIT; break;
                        }
                        emulator.tick(c, TickFlags.GAME_PLAY);
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
                case UP_LEFT:
                case DOWN_LEFT:
                case DOWN_RIGHT:
                case UP_RIGHT:
                case HALF_WAIT:
                    if (!emulator.getLevel().getChip().isDead() && !SuperCC.areToolsRunning())
                        emulator.tick(k.directionChar, TickFlags.GAME_PLAY);
                    break;
                case FULL_WAIT:
                    if (!emulator.getLevel().getChip().isDead() && !SuperCC.areToolsRunning())
                        emulator.tick(k.directionChar, TickFlags.GAME_PLAY);
                    if (!emulator.getLevel().getChip().isDead() && !SuperCC.areToolsRunning())
                        emulator.tick(k.directionChar, TickFlags.GAME_PLAY);
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
            keyMap.put(controls[i], allKeys[i]);
        }
    }
    
}
