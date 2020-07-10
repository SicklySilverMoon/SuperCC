package game;

public enum Ruleset {
    CURRENT,
    MS,
    LYNX;

    public Ruleset swap() {
        if (this == MS) return LYNX;
        else return MS;
    }
}
