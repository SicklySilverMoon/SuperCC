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
        UP_LEFT(SuperCC.UP_LEFT),
        DOWN_LEFT(SuperCC.DOWN_LEFT),
        DOWN_RIGHT(SuperCC.DOWN_RIGHT),
        UP_RIGHT(SuperCC.UP_RIGHT),
        HALF_WAIT(SuperCC.WAIT),
        FULL_WAIT(SuperCC.WAIT),
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
    private final HashMap<Integer, Key> keyMap = new HashMap<>();
    
    public void setKeyCode(Key key, int keyCode) {
        keyMap.remove(key.keyCode);
        key.keyCode = keyCode;
        keyMap.put(keyCode, key);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        SavestateManager savestates = emulator.getSavestates();
        if (savestates == null)
            return;
        int keyCode = e.getKeyCode();
        Key k = keyMap.getOrDefault(e.getKeyCode(), null); //Checks if the pressed key is one of the 'action' keys
        if (k == null) {
            if (e.getKeyCode() != KeyEvent.VK_SHIFT && e.isShiftDown()) {
                if (e.isControlDown() && KeyEvent.VK_0 <= keyCode && keyCode <= KeyEvent.VK_9) {
                    try {
                        boolean recording = savestates.macroRecorder(keyCode - KeyEvent.VK_0);
                        if (recording)
                            emulator.showAction("Started macro record");
                        else
                            emulator.showAction("Finished macro record");
                    }
                    catch (IllegalArgumentException e1) {
                        emulator.throwError("The macro start position was after the end.");
                    }
                }
                else {
                    if (!e.isControlDown()) { //Just so you can't accidentally save a state into these
                        savestates.addSavestate(keyCode);
                        emulator.showAction("State " + KeyEvent.getKeyText(e.getKeyCode()) + " saved");
                    }
                }
            }
            else {
                if (e.isControlDown() && KeyEvent.VK_0 <= keyCode && keyCode <= KeyEvent.VK_9) {
                    savestates.playMacro(keyCode-KeyEvent.VK_0);
                    emulator.getMainWindow().repaint(false);
                    emulator.showAction("Macro " + KeyEvent.getKeyText(e.getKeyCode()) + " loaded");
                }
                else if (savestates.load(keyCode, emulator.getLevel())) {
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
                    for (int i=0; i < emulator.getLevel().ticksPerMove(); i++) {
                        if (!emulator.getLevel().getChip().isDead() && !SuperCC.areToolsRunning())
                            emulator.tick(k.directionChar, TickFlags.GAME_PLAY);
                    }
                    break;
                case REWIND:
                    savestates.rewind();
                    emulator.getLevel().load(emulator.getSavestates().getSavestate());
                    emulator.showAction("Rewind");
                    emulator.getMainWindow().repaint(false);
                    break;
                case FORWARD:
                    savestates.replay();
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
