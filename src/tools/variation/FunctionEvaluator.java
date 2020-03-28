package tools.variation;

import emulator.SuperCC;
import game.Position;
import util.ByteList;

import java.util.ArrayList;

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
                checkArgCount(function, 0);
                return previousMove();
            case "nextmove":
                checkArgCount(function, 0);
                return nextMove();
            case "getmove":
                checkArgCount(function, 1);
                Object index = function.arguments.get(0).evaluate(interpreter);
                checkIfNumber(function, index);
                return getMoveAt(((Double)index).intValue());
            case "getoppositemove":
                checkArgCount(function, 1);
                Object move = function.arguments.get(0).evaluate(interpreter);
                checkIfMove(function, move);
                return getOppositeMove((Move)move);
            case "movesexecuted":
                checkArgCount(function, 0);
                return movesExecuted();
            case "movecount":
                checkArgCount(function, 1);
                move = function.arguments.get(0).evaluate(interpreter);
                checkIfMove(function, move);
                return moveCount((Move)move);
            case "seqlength":
                checkArgCount(function, 0);
                return seqLength();
            case "getchipsleft":
                checkArgCount(function, 0);
                return (double)emulator.getLevel().getChipsLeft();
            case "getredkeycount":
                checkArgCount(function, 0);
                return (double)emulator.getLevel().getKeys()[1];
            case "getyellowkeycount":
                checkArgCount(function, 0);
                return (double)emulator.getLevel().getKeys()[3];
            case "getgreenkeycount":
                checkArgCount(function, 0);
                return (double)emulator.getLevel().getKeys()[2];
            case "getbluekeycount":
                checkArgCount(function, 0);
                return (double)emulator.getLevel().getKeys()[0];
            case "hasflippers":
                checkArgCount(function, 0);
                return emulator.getLevel().getBoots()[0] != 0;
            case "hasfireboots":
                checkArgCount(function, 0);
                return emulator.getLevel().getBoots()[1] != 0;
            case "hassuctionboots":
                checkArgCount(function, 0);
                return emulator.getLevel().getBoots()[3] != 0;
            case "hasiceskates":
                checkArgCount(function, 0);
                return emulator.getLevel().getBoots()[2] != 0;
            case "getforegroundtile":
                checkArgCount(function, 2);
                Object x = function.arguments.get(0).evaluate(interpreter);
                Object y = function.arguments.get(1).evaluate(interpreter);
                checkIfNumber(function, x, y);
                return emulator.getLevel().getLayerFG().get(new Position(((Double)x).intValue(), ((Double)y).intValue()));
            case "getbackgroundtile":
                checkArgCount(function, 2);
                x = function.arguments.get(0).evaluate(interpreter);
                y = function.arguments.get(1).evaluate(interpreter);
                checkIfNumber(function, x, y);
                return emulator.getLevel().getLayerBG().get(new Position(((Double)x).intValue(), ((Double)y).intValue()));
            case "getplayerx":
                checkArgCount(function, 0);
                return (double)emulator.getLevel().getChip().getPosition().getX();
            case "getplayery":
                checkArgCount(function, 0);
                return (double)emulator.getLevel().getChip().getPosition().getY();
            case "move":
                return move(function.arguments);
            case "distanceto":
                checkArgCount(function, 2);
                x = function.arguments.get(0).evaluate(interpreter);
                y = function.arguments.get(1).evaluate(interpreter);
                checkIfNumber(function, x, y);
                int playerX = emulator.getLevel().getChip().getPosition().getX();
                int playerY = emulator.getLevel().getChip().getPosition().getY();
                return Math.abs(playerX - ((Double)x).intValue()) + Math.abs(playerY - ((Double)y).intValue());
            case "gettimeleft":
                checkArgCount(function, 0);
                int time = emulator.getLevel().getTimer();
                if(time < 0) time += 10001;
                return (double)time / 10;
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
        ByteList moves = manager.getPermutation(atSequence)[atMove];
        return getMove(moves.get(moves.size() - 1));
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
        ByteList moves = manager.getPermutation(atSequence)[atMove];
        return getMove(moves.get(0));
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
            case 'w':
                return new Move("w");
            case SuperCC.WAIT:
                return new Move("h");
        }
        return null;
    }

    private Move getMoveAt(int index) {
        int sum = 0;
        int prevSum = 0;
        for(int i = 0; i < manager.getSequenceCount(); i++) {
            sum += manager.getPermutation(i).length;
            if(index < sum) {
                return getMove(manager.getPermutation(i)[index - prevSum].get(0));
            }
            prevSum = 0;
        }
        return null;
    }

    private Move getOppositeMove(Move move) {
        switch(move.move.charAt(0)) {
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
        byte moveByte = toByte(move.move.charAt(0));
        double count = 0;
        for(int i = 0; i < manager.getSequenceCount(); i++) {
            for(ByteList bl : manager.getPermutation(i)) {
                for(byte b : bl) {
                    if (b == moveByte) {
                        count++;
                    }
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
        for(Expr arg : arguments) {
            Move move = (Move)arg.evaluate(interpreter);
            String str = move.move;
            for(int i = 0; i < move.number; i++) {
                for(int j = 0; j < str.length(); j++) {
                    byte moveDir = toByte(str.charAt(j));
                    interpreter.doMove(moveDir);
                }
            }
        }
        return null;
    }

    private byte toByte(char c) {
        switch(c) {
            case 'u':
                return SuperCC.UP;
            case 'r':
                return SuperCC.RIGHT;
            case 'd':
                return SuperCC.DOWN;
            case 'l':
                return SuperCC.LEFT;
            case 'w':
                return 'w';
            default:
                return SuperCC.WAIT;
        }
    }

    private void checkArgCount(Expr.Function function, int required) {
        if(function.arguments.size() != required) {
            throw new Interpreter.RuntimeError(function.token,
                    "Expected " + required + " arguments, got " + function.arguments.size());
        }
    }

    private void checkIfMove(Expr.Function function, Object arg1) {
        if(arg1 instanceof Move) {
            return;
        }
        throw new Interpreter.RuntimeError(function.token, "Argument must be a move");
    }

    private void checkIfNumber(Expr.Function function, Object... args) {
        for(Object arg : args) {
            if(!(arg instanceof Double)) {
                throw new Interpreter.RuntimeError(function.token, "Argument must be a number");
            }
        }
    }
}
