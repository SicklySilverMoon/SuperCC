package tools.variation;

import java.util.stream.StreamSupport;

public class Interpreter implements Expr.Evaluator {
    public Object evaluate(Expr expr) {
        return expr.evaluate(this);
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
}
