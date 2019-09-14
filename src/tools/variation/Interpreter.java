package tools.variation;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.StreamSupport;

public class Interpreter implements Expr.Evaluator, Stmt.Executor {
    private final JTextPane console;
    private HashMap<String, Object> variables;

    public Interpreter(HashMap<String, Object> variables, JTextPane console) {
        this.variables = variables;
        this.console = console;
    }

    public void interpret(ArrayList<Stmt> statements) {
        console.setText("");
        for(Stmt statement : statements) {
            statement.execute(this);
        }
    }

    public Object evaluate(Expr expr) {
        return expr.evaluate(this);
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
        Permutation p = new Permutation(stmt.movePool, stmt.lowerLimit, stmt.upperLimit, stmt.lexicographic);
        int count = 0;
        do {
            count++;
            p.nextPermutation();
        }while(!p.finished);

        StyledDocument doc = console.getStyledDocument();
        String str = "Number of permutations: " + count + "\n";
        try {
            doc.insertString(doc.getLength(), str, null);
        } catch (BadLocationException e) {}
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

    private class BreakException extends RuntimeException {
        BreakException() {
            super(null, null, false, false);
        }
    }
}
