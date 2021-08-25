package game;

public enum Ruleset { //For the love of god do not use this outside of solution/JSONs,
    // and only use level.getTicksPerMove unless its in Solution
    CURRENT(0),
    MS(2),
    LYNX(4);

    public final int ticksPerMove;

    public static final Ruleset[] PLAYABLES = new Ruleset[] {MS, LYNX};

    Ruleset(int ticksPerMove) {
        this.ticksPerMove = ticksPerMove;
    }

    public Ruleset swap() {
        if (this == MS) return LYNX;
        if (this == LYNX) return MS;
        return CURRENT;
    }

    public String prettyPrint() {
        return switch (this) {
            case MS -> "MS";
            case LYNX -> "Lynx";
            default -> null;
        };
    }
}
