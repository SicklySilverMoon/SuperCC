package tools.variation;

import emulator.SuperCC;
import emulator.TickFlags;
import game.Position;

import java.util.ArrayList;
import java.util.List;

public class FunctionEvaluator {
    private SuperCC emulator;
    private Interpreter interpreter;
    private VariationManager manager;

    public FunctionEvaluator(SuperCC emulator, Interpreter interpreter, VariationManager manager) {
        this.emulator = emulator;
        this.interpreter = interpreter;
        this.manager = manager;
    }

    public Object evaluate(Expr.Function function) {
        switch(function.name) {
            case "previousmove":
                return previousMove();
            case "nextmove":
                return nextMove();
            case "getmove":
                return getMoveAt(((Double)function.arguments.get(0).evaluate(interpreter)).intValue());
            case "getoppositemove":
                return getOppositeMove((Move)function.arguments.get(0).evaluate(interpreter));
            case "movesexecuted":
                return movesExecuted();
            case "movecount":
                return moveCount((Move)function.arguments.get(0).evaluate(interpreter));
            case "seqlength":
                return seqLength();
            case "getchipsleft":
                return (double)emulator.getLevel().getChipsLeft();
            case "getredkeycount":
                return (double)emulator.getLevel().getKeys()[1];
            case "getyellowkeycount":
                return (double)emulator.getLevel().getKeys()[3];
            case "getgreenkeycount":
                return (double)emulator.getLevel().getKeys()[2];
            case "getbluekeycount":
                return (double)emulator.getLevel().getKeys()[0];
            case "hasflippers":
                return emulator.getLevel().getBoots()[0] != 0;
            case "hasfireboots":
                return emulator.getLevel().getBoots()[1] != 0;
            case "hassuctionboots":
                return emulator.getLevel().getBoots()[3] != 0;
            case "hasiceskates":
                return emulator.getLevel().getBoots()[2] != 0;
            case "getforegroundtile":
                int x = ((Double)function.arguments.get(0).evaluate(interpreter)).intValue();
                int y = ((Double)function.arguments.get(1).evaluate(interpreter)).intValue();
                return emulator.getLevel().getLayerFG().get(new Position(x, y));
            case "getbackgroundtile":
                x = ((Double)function.arguments.get(0).evaluate(interpreter)).intValue();
                y = ((Double)function.arguments.get(1).evaluate(interpreter)).intValue();
                return emulator.getLevel().getLayerBG().get(new Position(x, y));
            case "getplayerx":
                return (double)emulator.getLevel().getChip().getPosition().getX();
            case "getplayery":
                return (double)emulator.getLevel().getChip().getPosition().getY();
            case "move":
                return move(function.arguments);
        }
        return null;
    }

    private Move previousMove() {
        int atSequence = interpreter.atSequence;
        int atMove = interpreter.atMove - 1;
        if(!interpreter.inSequence) {
            if(atSequence == 0) {
                return null;
            }
            atSequence--;
        }
        else {
            if(atMove < 0) {
                atSequence--;
                if(atSequence < 0) {
                    return null;
                }
                atMove = manager.getPermutation(atSequence).length - 1;
            }
        }
        return getMove(manager.getPermutation(atSequence)[atMove]);
    }

    private Move nextMove() {
        int atSequence = interpreter.atSequence;
        int atMove = interpreter.atMove;
        if(!interpreter.inSequence && atSequence > 0) {
            if(atSequence > manager.getSequenceCount()) {
                return null;
            }
            atMove = 0;
        }
        else {
            if(atMove >= manager.getPermutation(atSequence).length) {
                if(atSequence < manager.getSequenceCount() - 1) {
                    atSequence++;
                    atMove = 0;
                }
                else {
                    return null;
                }
            }
        }
        return getMove(manager.getPermutation(atSequence)[atMove]);
    }

    private Move getMove(byte move) {
        switch(move) {
            case SuperCC.UP:
                return new Move("u");
            case SuperCC.RIGHT:
                return new Move("r");
            case SuperCC.DOWN:
                return new Move("d");
            case SuperCC.LEFT:
                return new Move("l");
            case SuperCC.WAIT:
                return new Move("w");
        }
        return null;
    }

    private Move getMoveAt(int index) {
        int sum = 0;
        int prevSum = 0;
        for(int i = 0; i < manager.getSequenceCount(); i++) {
            sum += manager.getPermutation(i).length;
            if(index < sum) {
                return getMove(manager.getPermutation(i)[index - prevSum]);
            }
            prevSum = 0;
        }
        return null;
    }

    private Move getOppositeMove(Move move) {
        switch(move.move) {
            case 'u':
                return new Move("d");
            case 'r':
                return new Move("l");
            case 'l':
                return new Move("r");
            case 'd':
                return new Move("u");
        }
        return move;
    }

    private double movesExecuted() {
        int atSequence = interpreter.atSequence;
        int atMove = interpreter.atMove;
        int sum = 0;
        for(int i = 0; i < atSequence; i++) {
            sum += manager.getPermutation(i).length;
        }
        if(atSequence > 0) {
            if(atMove >= manager.getPermutation(atSequence - 1).length) {
                atMove = 0;
            }
        }
        return (double)sum + atMove;
    }

    private double moveCount(Move move) {
        byte moveByte = toByte(move);
        double count = 0;
        for(int i = 0; i < manager.getSequenceCount(); i++) {
            for(byte b : manager.getPermutation(i)) {
                if(b == moveByte) {
                    count++;
                }
            }
        }
        return count;
    }

    private double seqLength() {
        double length = 0;
        for(int i = 0; i < manager.getSequenceCount(); i++) {
            length += manager.getPermutation(i).length;
        }
        return length;
    }

    private Object move(ArrayList<Expr> arguments) {
        int mult = 1;
        for(Expr arg : arguments) {
            Move move = (Move)arg.evaluate(interpreter);
            byte moveDir = toByte(move);
            if(move.move == 'w') {
                mult = 2;
            }
            for(int i = 0; i < move.number * mult; i++) {
                emulator.tick(moveDir, TickFlags.LIGHT);
                interpreter.moveList.add(moveDir);
                if(moveDir != SuperCC.WAIT) {
                    interpreter.moveList.add(SuperCC.WAIT);
                }
                interpreter.checkMove();
            }
        }
        return null;
    }

    private byte toByte(Move move) {
        switch(move.move) {
            case 'u':
                return SuperCC.UP;
            case 'r':
                return SuperCC.RIGHT;
            case 'd':
                return SuperCC.DOWN;
            case 'l':
                return SuperCC.LEFT;
            default:
                return SuperCC.WAIT;
        }
    }
}
