package tools.variation;

public class Move {
    public final String value;
    public final int number;
    public final String move;

    public Move(String value) {
        this.value = value;

        int numIndex = 0;
        while(value.charAt(numIndex) >= '0' && value.charAt(numIndex) <= '9') {
            numIndex++;
        }
        String num = value.substring(0, numIndex);
        this.number = num.equals("") ? 1 : Math.max(Integer.parseInt(num), 1);
        this.move = value.substring(numIndex);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Move) {
            return this.number == ((Move) obj).number && this.move.equals(((Move) obj).move);
        }
        return false;
    }
}
