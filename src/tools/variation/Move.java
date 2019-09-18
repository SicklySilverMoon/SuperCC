package tools.variation;

public class Move {
    public final String value;
    public final int number;
    public final char move;

    public Move(String value) {
        this.value = value;
        String num = value.substring(0, value.length() - 1);
        this.number = num.equals("") ? 1 : Integer.parseInt(num);
        this.move = value.charAt(value.length() - 1);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Move) {
            return this.move == ((Move) obj).move;
        }
        return false;
    }
}
