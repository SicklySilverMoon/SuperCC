package tools.variation;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.Level;
import tools.VariationTesting;
import util.ByteList;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Interpreter implements Expr.Evaluator, Stmt.Executor {
    private final SuperCC emulator;
    private final ArrayList<Stmt> statements;
    private final JTextPane console;
    private final HashMap<String, Object> variables;
    private Level level;
    private final byte[] startingState;
    private final ByteList startingMoveList;
    public int atSequence = 0;
    public int atMove = 0;
    public boolean inSequence = false;
    public VariationManager manager;
    private int[] sequenceIndex;
    private FunctionEvaluator evaluator;
    public ByteList moveList;
    private int amount = 1;
    public ArrayList<Solution> solutions = new ArrayList<>();
    public long count = 0;
    private boolean hadError = false;
    private VariationTesting vt;

    public Interpreter(SuperCC emulator, VariationTesting vt, ArrayList<Stmt> statements,
                       HashMap<String, Object> variables, JTextPane console) {
        this.emulator = emulator;
        this.statements = statements;
        this.variables = variables;
        this.console = console;
        this.level = emulator.getLevel();
        this.startingState = this.level.save();
        this.manager = new VariationManager(emulator, statements, variables, this.level, this);
        this.startingMoveList = this.manager.moveLists[0].clone();
        this.sequenceIndex = manager.sequenceIndex;
        this.evaluator = new FunctionEvaluator(emulator, this, this.manager);
        this.vt = vt;
    }

    public void interpret() {
        console.setText("");
        displayPermutationCount();
        if(manager.getSequenceCount() == 0) {
            hadError = true;
            print("Script must contain at least 1 sequence\n", new Color(255, 68, 68));
            return;
        }
        int fromStatement = 0;
        count = 0;
        do {
            count++;
            try {
                level.load(manager.saveStates[atSequence]);
                moveList = manager.moveLists[atSequence];
                for (int i = fromStatement; i < statements.size(); i++) {
                    Stmt stmt = statements.get(i);
                    stmt.execute(this);
                }
            }
            catch(TerminateException te) {
                atSequence = manager.nextPermutation();
                if(atSequence == -1) {
                    break;
                }
                fromStatement = sequenceIndex[atSequence];
                continue;
            }
            catch(ReturnException re) {
                if(solutions.size() >= amount) {
                    break;
                }
            }
            catch(RuntimeError err) {
                String str = "[Line " + err.token.line + "] " + err.getMessage() + " near " + err.token.lexeme + "\n";
                print(str, new Color(255, 68, 68));
                hadError = true;
                break;
            }
            catch(Exception e) {
                String str = "Unknown runtime error.\n  " + e.toString();
                print(str, new Color(255, 68, 68));
                hadError = true;
                e.printStackTrace();
                break;
            }
            atSequence = manager.nextPermutation();
            if(atSequence == -1) {
                break;
            }
            fromStatement = sequenceIndex[atSequence];
        }while(!manager.finished && !vt.killFlag);
        if(vt.killFlag) {
            print("Search stopped!\n", new Color(255, 68, 68));
        }
        if(!hadError) {
            String str = "Successfully tested " + String.format("%,d", count) + " variations.\nFound " +
                    String.format("%,d", solutions.size()) + " solutions.";
            print(str, new Color(0, 153, 0));
        }
        level.load(startingState);
        emulator.getSavestates().restart();
        for(byte move : startingMoveList) {
            emulator.tick(move, TickFlags.PRELOADING);
        }
        emulator.repaint(true);
    }

    @Override
    public void executeExpression(Stmt.Expression stmt) {
        stmt.expr.evaluate(this);
    }

    @Override
    public void executeBlock(Stmt.Block stmt) {
        for(Stmt statement : stmt.statements) {
            statement.execute(this);
        }
    }

    @Override
    public void executeIf(Stmt.If stmt) {
        if(isTruthy(stmt.condition.evaluate(this))) {
            stmt.thenBranch.execute(this);
        }
        else if(stmt.elseBranch != null) {
            stmt.elseBranch.execute(this);
        }
    }

    @Override
    public void executeFor(Stmt.For stmt) {
        if(stmt.init != null) {
            stmt.init.execute(this);
        }
        while(isTruthy(stmt.condition.evaluate(this))) {
            try {
                stmt.body.execute(this);
            } catch(BreakException b) {
                break;
            }
            if(stmt.post != null) {
                stmt.post.execute(this);
            }
        }
    }

    @Override
    public void executePrint(Stmt.Print stmt) {
        Object obj = stmt.expr.evaluate(this);
        String str = (obj == null) ? "null\n" : obj.toString() + '\n';
        print(str, Color.white);
    }

    @Override
    public void executeEmpty(Stmt.Empty stmt) {

    }

    @Override
    public void executeBreak(Stmt.Break stmt) {
        throw new BreakException();
    }

    @Override
    public void executeSequence(Stmt.Sequence stmt) {
        inSequence = true;
        manager.setVariables(atSequence);
        byte[] permutation = manager.getPermutation(atSequence);
        if(stmt.start != null) {
            stmt.start.execute(this);
        }
        for(atMove = 0; atMove < permutation.length;) {
            byte move = permutation[atMove];
            if(stmt.beforeMove != null) {
                stmt.beforeMove.execute(this);
            }
            if(move == 'w') {
                emulator.tick(SuperCC.WAIT, TickFlags.LIGHT);
                moveList.add(SuperCC.WAIT);
                checkMove();
                emulator.tick(SuperCC.WAIT, TickFlags.LIGHT);
                moveList.add(SuperCC.WAIT);
                checkMove();
            }
            else {
                emulator.tick(move, TickFlags.LIGHT);
                moveList.add(move);
                checkMove();
            }
            atMove++;
            if(stmt.afterMove != null) {
                stmt.afterMove.execute(this);
            }
        }
        atSequence++;
        inSequence = false;
    }

    @Override
    public void executeReturn(Stmt.Return stmt) {
        Solution initialSolution = new Solution(emulator.getSavestates().getMoveList(), level.getRngSeed(), level.getStep());

        emulator.getSavestates().restart();
        level.load(emulator.getSavestates().getSavestate());
        for(byte move : moveList) {
            emulator.tick(move, TickFlags.PRELOADING);
        }
        solutions.add(new Solution(emulator.getSavestates().getMoveList(), level.getRngSeed(), level.getStep()));

        throw new ReturnException();
    }

    @Override
    public void executeTerminate(Stmt.Terminate stmt) {
        int index = 0;
        if(stmt.index != null) {
            index = ((Double) stmt.index.evaluate(this)).intValue();
        }
        else {
            int atSeq = atSequence;
            int atMo = atMove;
            if(!inSequence) {
                if(atSeq > 0) {
                    atMo = 0;
                }
            }
            for(int i = 0; i < atSeq; i++) {
                index += manager.getPermutation(i).length;
            }
            index += atMo - 1;
        }
        manager.terminate(index);
        throw new TerminateException();
    }

    @Override
    public void executeContinue(Stmt.Continue stmt) {
        throw new TerminateException();
    }

    @Override
    public void executeAll(Stmt.All stmt) {
        if(stmt.amount != null) {
            this.amount = ((Double) stmt.amount.evaluate(this)).intValue();
        }
    }

    @Override
    public Object evaluateBinary(Expr.Binary expr) {
        Object left = expr.left.evaluate(this);
        Object right = expr.right.evaluate(this);

        switch(expr.operator.type) {
            case PLUS:
                checkIfNumber(expr.operator, left, right);
                return (double)left + (double)right;
            case MINUS:
                checkIfNumber(expr.operator, left, right);
                    return (double)left - (double)right;
            case STAR:
                checkIfNumber(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkIfNumber(expr.operator, left, right);
                return (double)left / (double)right;
            case MODULO:
                checkIfNumber(expr.operator, left, right);
                return (double)left % (double)right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            case LESS:
                checkIfNumber(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkIfNumber(expr.operator, left, right);
                return (double)left <= (double)right;
            case GREATER:
                checkIfNumber(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkIfNumber(expr.operator, left, right);
                return (double)left >= (double)right;
        }
        return null;
    }

    @Override
    public Object evaluateLiteral(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object evaluateGroup(Expr.Group expr) {
        return expr.expr.evaluate(this);
    }

    @Override
    public Object evaluateLogical(Expr.Logical expr) {
        Object left = expr.left.evaluate(this);

        switch(expr.operator.type) {
            case OR:
            case OR_OR:
                if(isTruthy(left)) {
                    return left;
                }
                break;
            case AND:
            case AND_AND:
                if(!isTruthy(left)) {
                    return left;
                }
                break;
            default:
                throw new RuntimeError(expr.operator, "Unknown logical expression");
        }
        return expr.right.evaluate(this);
    }

    @Override
    public Object evaluateUnary(Expr.Unary expr) {
        Object right = expr.right.evaluate(this);

        switch(expr.operator.type) {
            case BANG:
            case NOT:
                return !isTruthy(right);
            case MINUS:
                return -(double)right;
        }
        return null;
    }

    @Override
    public Object evaluateVariable(Expr.Variable expr) {
        return variables.get(expr.var.lexeme);
    }

    @Override
    public Object evaluateAssign(Expr.Assign expr) {
        Object value = getValue(expr.var);
        Object exprValue = expr.value.evaluate(this);
        double newValue;
        switch(expr.operator.type) {
            case EQUAL:
                variables.put(expr.var.lexeme, exprValue);
                return exprValue;
            case PLUS_EQUAL:
                checkIfNumber(expr.operator, exprValue);
                newValue = (double)value + (double)exprValue;
                variables.put(expr.var.lexeme, newValue);
                return newValue;
            case MINUS_EQUAL:
                checkIfNumber(expr.operator, exprValue);
                newValue = (double)value - (double)exprValue;
                variables.put(expr.var.lexeme, newValue);
                return newValue;
            case STAR_EQUAL:
                checkIfNumber(expr.operator, exprValue);
                newValue = (double)value * (double)exprValue;
                variables.put(expr.var.lexeme, newValue);
                return newValue;
            case SLASH_EQUAL:
                checkIfNumber(expr.operator, exprValue);
                newValue = (double)value / (double)exprValue;
                variables.put(expr.var.lexeme, newValue);
                return newValue;
            case MODULO_EQUAL:
                checkIfNumber(expr.operator, exprValue);
                newValue = (double)value % (double)exprValue;
                variables.put(expr.var.lexeme, newValue);
                return newValue;
        }
        return null;
    }

    @Override
    public Object evaluateFunction(Expr.Function expr) {
        return evaluator.evaluate(expr);
    }

    private void checkIfNumber(Token operator, Object left) {
        if(left instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkIfNumber(Token operator, Object left, Object right) {
        if(left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number");
    }

    public void checkMove() {
        if(level.isCompleted()) {
            executeReturn(null);
        }
        if(level.getChip().isDead()) {
            executeTerminate(new Stmt.Terminate(null));
            throw new TerminateException();
        }
    }

    private Object getValue(Token token) {
        try {
            return variables.get(token.value);
        } catch(NullPointerException e) {
            throw new RuntimeError(token, "Undefined variable");
        }
    }

    private boolean isTruthy(Object value) {
        if(value == null) {
            return false;
        }
        if(value instanceof Boolean) {
            return (boolean)value;
        }
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if(left == null && right == null) {
            return true;
        }
        if(left == null) {
            return false;
        }
        return left.equals(right);
    }

    private ArrayList<Stmt.Sequence> getSequences(ArrayList<Stmt> statements) {
        ArrayList<Stmt.Sequence> sequences = new ArrayList<>();
        for(Stmt stmt : statements) {
            if(stmt instanceof Stmt.Sequence) {
                sequences.add((Stmt.Sequence)stmt);
            }
        }
        return sequences;
    }

    private class BreakException extends RuntimeException {
        BreakException() {
            super(null, null, false, false);
        }
    }

    private class TerminateException extends RuntimeException {
        TerminateException() {
            super(null, null, false, false);
        }
    }

    private class ReturnException extends RuntimeException {
        ReturnException() { super(null, null, false, false); }
    }

    public static class RuntimeError extends RuntimeException {
        final Token token;

        RuntimeError(Token token, String message) {
            super(message);
            this.token = token;
        }
    }

    public void print(String message, Color color) {
        StyledDocument doc = console.getStyledDocument();
        Style style = console.addStyle("style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), message, style);
        } catch (BadLocationException e) {}
    }

    public void displayPermutationCount() {
        double total = manager.getPermutationCount();
        String type = (total > Permutation.LIMIT) ? "more than " : "up to ";
        String str = "Upper bound: " + type + String.format("%,.0f", total) + " variations\n";
        print(str, Color.white);
    }
}
