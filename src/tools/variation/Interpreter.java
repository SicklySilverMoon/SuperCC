package tools.variation;

import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.Level;
import util.ByteList;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.HashMap;

public class Interpreter implements Expr.Evaluator, Stmt.Executor {
    private final SuperCC emulator;
    private final ArrayList<Stmt> statements;
    private final JTextPane console;
    private final HashMap<String, Object> variables;
    private Level level;
    private final byte[] startingState;
    public int atSequence = 0;
    public int atMove = 0;
    public boolean inSequence = false;
    private VariationManager manager;
    private int[] sequenceIndex;
    private FunctionEvaluator evaluator;
    public ByteList moveList;
    private int amount = 1;
    private int found = 0;

    public Interpreter(SuperCC emulator, ArrayList<Stmt> statements, HashMap<String, Object> variables, JTextPane console) {
        this.emulator = emulator;
        this.statements = statements;
        this.variables = variables;
        this.console = console;
        this.level = emulator.getLevel();
        this.startingState = this.level.save();
        this.manager = new VariationManager(statements, variables, this.level, this);
        this.sequenceIndex = manager.sequenceIndex;
        this.evaluator = new FunctionEvaluator(emulator, this, this.manager);
        this.moveList = new ByteList();
    }

    public void interpret() {
        console.setText("");
        int fromStatement = 0;
        int count = 0;
        do {
            count++;
            level.load(manager.saveStates[atSequence]);
            moveList = manager.moveLists[atSequence];
            try {
                for (int i = fromStatement; i < statements.size(); i++) {
                    Stmt stmt = statements.get(i);
                    stmt.execute(this);
                }
            }
            catch(TerminateException te) { }
            catch(ReturnException re) {
                if(found >= amount) {
                    break;
                }
            }
            atSequence = manager.nextPermutation();
            if(atSequence == -1) {
                break;
            }
            fromStatement = sequenceIndex[atSequence];
        }while(!manager.finished);
        System.out.println("Number of variations tested: " + count);
        level.load(startingState);
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
        StyledDocument doc = console.getStyledDocument();
        Object obj = stmt.expr.evaluate(this);
        String str = (obj == null) ? "null\n" : obj.toString() + '\n';
        try {
            doc.insertString(doc.getLength(), str, null);
        } catch (BadLocationException e) {}
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
            emulator.tick(move, TickFlags.LIGHT);
            moveList.add(move);
            if(move != SuperCC.WAIT) {
                moveList.add(SuperCC.WAIT);
            }
            checkMove();
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
        Solution solution = new Solution(moveList, level.getRngSeed(), level.getStep());
        System.out.println(solution.toJSON().toJSONString());
        found++;
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
                if(left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
            case MINUS:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left - (double)right;
                }
            case STAR:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left * (double)right;
                }
            case SLASH:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left / (double)right;
                }
            case MODULO:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left % (double)right;
                }
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            case LESS:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left < (double)right;
                }
            case LESS_EQUAL:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left <= (double)right;
                }
            case GREATER:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left > (double)right;
                }
            case GREATER_EQUAL:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left >= (double)right;
                }
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
            case AND:
            case AND_AND:
                if(!isTruthy(left)) {
                    return left;
                }
            default:
                return expr.right.evaluate(this);
        }
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
        Object value = getValue(expr.var.lexeme);
        Object exprValue = expr.value.evaluate(this);
        switch(expr.operator.type) {
            case EQUAL:
                variables.put(expr.var.lexeme, exprValue);
                return exprValue;
            case PLUS_EQUAL:
                if(value instanceof Double && exprValue instanceof Double) {
                    double newValue = (double)value + (double)exprValue;
                    variables.put(expr.var.lexeme, newValue);
                    return newValue;
                }
            case MINUS_EQUAL:
                if(value instanceof Double && exprValue instanceof Double) {
                    double newValue = (double)value - (double)exprValue;
                    variables.put(expr.var.lexeme, newValue);
                    return newValue;
                }
            case STAR_EQUAL:
                if(value instanceof Double && exprValue instanceof Double) {
                    double newValue = (double)value * (double)exprValue;
                    variables.put(expr.var.lexeme, newValue);
                    return newValue;
                }
            case SLASH_EQUAL:
                if(value instanceof Double && exprValue instanceof Double) {
                    double newValue = (double)value / (double)exprValue;
                    variables.put(expr.var.lexeme, newValue);
                    return newValue;
                }
            case MODULO_EQUAL:
                if(value instanceof Double && exprValue instanceof Double) {
                    double newValue = (double)value % (double)exprValue;
                    variables.put(expr.var.lexeme, newValue);
                    return newValue;
                }
        }
        return null;
    }

    @Override
    public Object evaluateFunction(Expr.Function expr) {
        return evaluator.evaluate(expr);
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

    private double getValue(String variable) {
        try {
            double value = (double)variables.get(variable);
            return value;
        } catch(NullPointerException e) {
            return 0;
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
}
