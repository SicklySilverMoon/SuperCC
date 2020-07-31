package emulator;

public class TickFlags {
    
    public static final TickFlags GAME_PLAY = new TickFlags(true, true, true),
        REPLAY = new TickFlags(true, false, false),
        LIGHT = new TickFlags(false, false, true),
        PRELOADING = new TickFlags(false, true, true);
    
    public final boolean repaint;
    public final boolean save;
    public final boolean multiTick;
    
    public TickFlags(boolean repaint, boolean save, boolean multiTick) {
        this.repaint = repaint;
        this.save = save;
        this.multiTick = multiTick;
    }
    
}
