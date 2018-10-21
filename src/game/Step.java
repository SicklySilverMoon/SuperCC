package game;

public enum Step{
    ODD,
    EVEN;

    @Override
    public String toString(){
        if (this == Step.ODD) return "Odd";
        return "Even";
    }
}
