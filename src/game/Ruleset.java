package game;

public enum Ruleset {
    MS("MS"),
    LYNX("Lynx");

    public final String name;

    Ruleset(String name) {
        this.name = name;
    }

    public static Ruleset fromName(String name) {
        for (Ruleset r : values()) {
            if (r.name.equals(name)) return r;
        }
        return null;
    }
}
