package emulator;

public enum TickFlags {
    
    GAME_PLAY(true, true, true),
    REPLAY(true, false, false),
    LIGHT(false, false, true),
    PRELOADING(false, true, true);
    
    public final boolean repaint;
    public final boolean save;
    public final boolean multiTick;
    
    TickFlags(boolean repaint, boolean save, boolean multiTick) {
        this.repaint = repaint;
        this.save = save;
        this.multiTick = multiTick;
    }
    
}
