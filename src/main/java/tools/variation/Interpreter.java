package tools.variation;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.Level;
import tools.VariationTesting;
import util.CharList;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;

public class Interpreter implements Expr.Evaluator, Stmt.Executor {
    private Parser parser;
    private final SuperCC emulator;
    private final ArrayList<Stmt> statements;
    private final JTextPane console;
    private final HashMap<String, Object> variables;
    private Level level;
    public int atSequence = 0;
    public int atMove = 0;
    public boolean inSequence = false;
    public VariationManager manager;
    private int[] sequenceIndex;
    private FunctionEvaluator evaluator;
    public CharList moveList;
    private int amount = 1;
    public ArrayList<Solution> solutions = new ArrayList<>();
    private boolean hadError = false;
    private VariationTesting vt;
    private int fromStatement = 0;
    public long variationCount = 0;
    public double totalPermutationCount;
    public double totalValidPermutationCount;

    public Interpreter(SuperCC emulator, VariationTesting vt, JTextPane console, String code) {
        this.parser = new Parser(console);
        this.statements = parser.parseCode(code);
        this.variables = parser.getVariables(code);
        this.emulator = emulator;
        this.console = console;
        this.level = emulator.getLevel();
        this.manager = new VariationManager(emulator, statements, variables, this.level, this);
        this.sequenceIndex = manager.sequenceIndex;
        this.evaluator = new FunctionEvaluator(emulator, this, this.manager);
        this.vt = vt;
        this.totalPermutationCount = manager.getTotalPermutationCount();
        this.totalValidPermutationCount = manager.getTotalValidPermutationCount();
    }

    public void interpret() {
        if(parser.hadError) {
            return;
        }

        begin();
        long timeStart = System.currentTimeMillis();
        while(!vt.killFlag && !isFinished()) {
            variationCount++;
            try {
                level.load(manager.savestates[atSequence][manager.getStartingMove()]);
                moveList = manager.moveLists[atSequence][manager.getStartingMove()].clone();
                for (int i = fromStatement; i < statements.size(); i++) {
                    Stmt stmt = statements.get(i);
                    stmt.execute(this);
                }
            }
            catch(TerminateException te) {
                getNextPermutation();
                continue;
            }
            catch(ReturnException re) {
                if(solutions.size() >= amount) {
                    break;
                }
            }
            catch(RuntimeError err) {
                String str = "[Line " + err.token.line + " near '" + err.token.lexeme + "'] " + err.getMessage() + "\n";
                print(str, new Color(255, 68, 68));
                hadError = true;
            }
            catch(Exception e) {
                String str = "Unknown runtime error.\n  " + e.toString();
                print(str, new Color(255, 68, 68));
                hadError = true;
                e.printStackTrace();
            }
            getNextPermutation();
        }
        long timeEnd = System.currentTimeMillis();
        double totalTime = (double)(timeEnd - timeStart)/1000;
        long varsPerSecond = Math.round(variationCount/totalTime);
        finish(totalTime, varsPerSecond);
    }

    private void begin() {
        console.setText("");
        displayPermutationCount();
        if(!manager.validate()) {
            hadError = true;
        }
    }

    private void getNextPermutation() {
        atSequence = manager.nextPermutation();
        if(atSequence == -1) {
            return;
        }
        fromStatement = sequenceIndex[atSequence];
    }

    private boolean isFinished() {
        return hadError || atSequence == -1;
    }

    private void finish(double totalTime, long varsPerSecond) {
        if(vt.killFlag) {
            print("Search stopped!\n", new Color(255, 68, 68));
        }
        if(!hadError) {
            String str = "Successfully tested " + String.format("%,d", variationCount) + " variations.\n" +
                    "Found " + String.format("%,d", solutions.size()) + " solutions.\n" +
                    "Ran for " + totalTime + "s at " + String.format("%,d", varsPerSecond) + " vars/s.";
            print(str, new Color(0, 153, 0));
        }
        emulator.getSavestates().restart();
        level.load(emulator.getSavestates().getSavestate());
        if(manager.getSequenceCount() > 0) {
            if(manager.moveLists != null) {
                for (char move : manager.moveLists[0][0]) {
                    emulator.tick(move, TickFlags.PRELOADING);
                }
            }
        }
        if(vt.hasGui) {
            emulator.repaint(true);
        }
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
        else {
            stmt.elseBranch.execute(this);
        }
    }

    @Override
    public void executeFor(Stmt.For stmt) {
        stmt.init.execute(this);
        while(isTruthy(stmt.condition.evaluate(this))) {
            try {
                stmt.body.execute(this);
            } catch(BreakException b) {
                break;
            }
            stmt.post.execute(this);
        }
    }

    @Override
    public void executePrint(Stmt.Print stmt) {
        Object obj = stmt.expr.evaluate(this);
        String str = (obj == null) ? "null\n" : obj.toString() + '\n';
        if(str.endsWith(".0\n")) {
            str = str.substring(0, str.length() - 3) + '\n';
        }
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
        CharList[] permutation = manager.getPermutation(atSequence);
        int start = manager.getStartingMove(atSequence);
        if(start == 0) {
            manager.setVariables(atSequence, 0);
            stmt.lifecycle.start.execute(this);
        }
        for(atMove = start; atMove < permutation.length;) {
            if(atMove > 0 && atMove < permutation.length - 1) {
                manager.setVariables(atSequence, atMove);
            }
            stmt.lifecycle.beforeMove.execute(this);
            for(int i = 0; i < permutation[atMove].size(); i++) {
                stmt.lifecycle.beforeStep.execute(this);
                doMove(permutation[atMove].get(i));
                stmt.lifecycle.afterStep.execute(this);
            }
            atMove++;
            stmt.lifecycle.afterMove.execute(this);
        }
        stmt.lifecycle.end.execute(this);
        atSequence++;
        inSequence = false;
    }

    @Override
    public void executeReturn(Stmt.Return stmt) {
        emulator.getSavestates().restart();
        level.load(emulator.getSavestates().getSavestate());
        for(char move : moveList) {
            emulator.tick(move, TickFlags.PRELOADING);
        }
        solutions.add(new Solution(emulator.getSavestates().getMoveList(), level.getRngSeed(), level.getStep(), level.getRuleset(), level.getInitialRFFDirection()));

        throw new ReturnException();
    }

    @Override
    public void executeTerminate(Stmt.Terminate stmt) {
        if(!inSequence && manager.getPermutation(atSequence - 1).length == 0) {
            manager.terminateZero(atSequence - 1);
            throw new TerminateException();
        }

        int index;
        if(stmt.index != null) {
            index = ((Double) stmt.index.evaluate(this)).intValue();
        }
        else {
            index = inSequence ? atMove : -1;
            for(int i = 0; i < atSequence; i++) {
                index += manager.getPermutation(i).length;
            }
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
        Object value = variables.get(expr.var.value);
        if(value == null && expr.operator.type != TokenType.EQUAL) {
            throw new RuntimeError(expr.var, "Variable undefined");
        }
        Object exprValue = expr.value.evaluate(this);
        switch(expr.operator.type) {
            case EQUAL:
                variables.put(expr.var.lexeme, exprValue);
                return exprValue;
            case PLUS_EQUAL:
                return applyAssignment((a, b) -> a + b, value, exprValue, expr);
            case MINUS_EQUAL:
                return applyAssignment((a, b) -> a - b, value, exprValue, expr);
            case STAR_EQUAL:
                return applyAssignment((a, b) -> a * b, value, exprValue, expr);
            case SLASH_EQUAL:
                return applyAssignment((a, b) -> a / b, value, exprValue, expr);
            case MODULO_EQUAL:
                return applyAssignment((a, b) -> a % b, value, exprValue, expr);
        }
        return null;
    }

    @Override
    public Object evaluateFunction(Expr.Function expr) {
        return evaluator.evaluate(expr);
    }

    private void checkIfNumber(Token operator, Object left, Object right) {
        if(left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number");
    }

    public void doMove(char move) {
        if (move == 'w') {
            tick(SuperCC.WAIT);
            tick(SuperCC.WAIT);
        } else {
            tick(move);
        }
    }

    private void tick(char move) {
        emulator.tick(move, TickFlags.LIGHT);
        moveList.add(move);
        checkMove();
    }

    private void checkMove() {
        if(level.isCompleted()) {
            executeReturn(null);
        }
        if(level.getChip().isDead()) {
            executeTerminate(new Stmt.Terminate(null));
            throw new TerminateException();
        }
    }

    private double applyAssignment(DoubleBinaryOperator op, Object value, Object exprValue, Expr.Assign expr) {
        checkIfNumber(expr.operator, exprValue, value);
        double newValue = op.applyAsDouble((double)value, (double)exprValue);
        variables.put(expr.var.lexeme, newValue);
        return newValue;
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
        return Objects.equals(left, right);
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
        String type = (totalValidPermutationCount > Permutation.LIMIT) ? "more than " : "up to ";
        String str = "Upper bound: " + type + String.format("%,.0f", totalValidPermutationCount) + " variations\n";
        print(str, Color.white);
    }

    public double getPermutationIndex() {
        return manager.getPermutationIndex();
    }
}
