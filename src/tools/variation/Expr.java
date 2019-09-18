package tools.variation;

import java.util.ArrayList;

public abstract class Expr {
    interface Evaluator {
        Object evaluateBinary(Binary expr);
        Object evaluateLiteral(Literal expr);
        Object evaluateGroup(Group expr);
        Object evaluateLogical(Logical expr);
        Object evaluateUnary(Unary expr);
        Object evaluateVariable(Variable expr);
        Object evaluateAssign(Assign expr);
        Object evaluateFunction(Function expr);
    }

    abstract public Object evaluate(Evaluator evaluator);

    static public class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public Object evaluate(Evaluator evaluator) {
            return evaluator.evaluateBinary(this);
        }
    }

    static public class Literal extends Expr {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public Object evaluate(Evaluator evaluator) {
            return evaluator.evaluateLiteral(this);
        }
    }

    static public class Group extends Expr {
        public final Expr expr;

        public Group(Expr expr) {
            this.expr = expr;
        }

        @Override
        public Object evaluate(Evaluator evaluator) {
            return evaluator.evaluateGroup(this);
        }
    }

    static public class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public Object evaluate(Evaluator evaluator) {
            return evaluator.evaluateLogical(this);
        }
    }

    static public class Unary extends Expr {
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public Object evaluate(Evaluator evaluator) {
            return evaluator.evaluateUnary(this);
        }
    }

    static public class Variable extends Expr {
        public final Token var;

        public Variable(Token var) {
            this.var = var;
        }

        @Override
        public Object evaluate(Evaluator evaluator) {
            return evaluator.evaluateVariable(this);
        }
    }

    static public class Assign extends Expr {
        public final Token var;
        public final Token operator;
        public final Expr value;

        public Assign(Token var, Token operator, Expr value) {
            this.var = var;
            this.operator = operator;
            this.value = value;
        }

        @Override
        public Object evaluate(Evaluator evaluator) {
            return evaluator.evaluateAssign(this);
        }
    }

    static public class Function extends Expr {
        public final String name;
        public final ArrayList<Expr> arguments;

        public Function(String name, ArrayList<Expr> arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        @Override
        public Object evaluate(Evaluator evaluator) {
            return evaluator.evaluateFunction(this);
        }
    }
}
