package emulator;

import game.Direction;
import game.Level;

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
        Key k = keyMap.getOrDefault(e.getKeyCode(), null);
        if (k == null) {
            if (e.isShiftDown()) {
                emulator.getSavestates().addSavestate(keyCode);
                emulator.showAction("State " + KeyEvent.getKeyText(e.getKeyCode()) + " saved");
            }
            else {
                if (emulator.getSavestates().load(keyCode, emulator.getLevel())) {
                    emulator.showAction("State " + KeyEvent.getKeyText(e.getKeyCode()) + " loaded");
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
                    if (!emulator.getLevel().getChip().isDead())
                        emulator.tick(k.directionByte, TickFlags.GAME_PLAY);
                    break;
                case FULL_WAIT:
                    if (!emulator.getLevel().getChip().isDead())
                        emulator.tick(k.directionByte, TickFlags.GAME_PLAY);
                    if (!emulator.getLevel().getChip().isDead())
                        emulator.tick(k.directionByte, TickFlags.GAME_PLAY);
                    break;
                case REWIND:
                    emulator.getSavestates().rewind();
                    emulator.getLevel().load(emulator.getSavestates().getSavestate());
                    emulator.showAction("Rewind");
                    emulator.getMainWindow().repaint(emulator.getLevel(), false);
                    break;
                case FORWARD:
                    emulator.getSavestates().replay();
                    emulator.getLevel().load(emulator.getSavestates().getSavestate());
                    emulator.showAction("Replay");
                    emulator.getMainWindow().repaint(emulator.getLevel(), false);
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
