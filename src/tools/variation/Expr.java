package tools.variation;

public abstract class Expr {
    interface Evaluator {
        Object evaluateBinary(Binary expr);
        Object evaluateLiteral(Literal expr);
        Object evaluateGroup(Group expr);
        Object evaluateLogical(Logical expr);
        Object evaluateUnary(Unary expr);
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
}
