package tools.variation;

import java.util.ArrayList;
import java.util.Objects;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Binary binary = (Binary) o;
            return Objects.equals(left, binary.left) &&
                    Objects.equals(operator, binary.operator) &&
                    Objects.equals(right, binary.right);
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, operator, right);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Literal literal = (Literal) o;
            return Objects.equals(value, literal.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Group group = (Group) o;
            return Objects.equals(expr, group.expr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Logical logical = (Logical) o;
            return Objects.equals(left, logical.left) &&
                    Objects.equals(operator, logical.operator) &&
                    Objects.equals(right, logical.right);
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, operator, right);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Unary unary = (Unary) o;
            return Objects.equals(operator, unary.operator) &&
                    Objects.equals(right, unary.right);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operator, right);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Variable variable = (Variable) o;
            return Objects.equals(var, variable.var);
        }

        @Override
        public int hashCode() {
            return Objects.hash(var);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Assign assign = (Assign) o;
            return Objects.equals(var, assign.var) &&
                    Objects.equals(operator, assign.operator) &&
                    Objects.equals(value, assign.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(var, operator, value);
        }
    }

    static public class Function extends Expr {
        public final String name;
        public final ArrayList<Expr> arguments;
        public final Token token;

        public Function(String name, ArrayList<Expr> arguments, Token token) {
            this.name = name;
            this.arguments = arguments;
            this.token = token;
        }

        @Override
        public Object evaluate(Evaluator evaluator) {
            return evaluator.evaluateFunction(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Function function = (Function) o;
            return Objects.equals(name, function.name) &&
                    Objects.equals(arguments, function.arguments) &&
                    Objects.equals(token, function.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, arguments, token);
        }
    }
}
