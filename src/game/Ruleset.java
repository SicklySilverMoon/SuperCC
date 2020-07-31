package game;

public enum Ruleset { //For the love of god do not use this outside of solution/JSONs
    CURRENT,
    MS,
    LYNX;

    public Ruleset swap() {
        if (this == MS) return LYNX;
        if (this == LYNX) return MS;
        return CURRENT;
    }
}
