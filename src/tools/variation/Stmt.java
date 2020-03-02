package tools.variation;

import java.util.ArrayList;
import java.util.Objects;

public abstract class Stmt {
    interface Executor {
        void executeExpression(Expression stmt);
        void executeBlock(Block stmt);
        void executeIf(If stmt);
        void executeFor(For stmt);
        void executePrint(Print stmt);
        void executeEmpty(Empty stmt);
        void executeBreak(Break stmt);
        void executeSequence(Sequence stmt);
        void executeReturn(Return stmt);
        void executeTerminate(Terminate stmt);
        void executeContinue(Continue stmt);
        void executeAll(All stmt);
    }

    abstract public void execute(Executor executor);

    public static class Expression extends Stmt {
        public final Expr expr;

        public Expression(Expr expr) {
            this.expr = expr;
        }

        @Override
        public void execute(Executor executor) {
            executor.executeExpression(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Expression that = (Expression) o;
            return Objects.equals(expr, that.expr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr);
        }
    }

    public static class Block extends Stmt {
        public final ArrayList<Stmt> statements;

        public Block(ArrayList<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public void execute(Executor executor) {
            executor.executeBlock(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Block block = (Block) o;
            return Objects.equals(statements, block.statements);
        }

        @Override
        public int hashCode() {
            return Objects.hash(statements);
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public void execute(Executor executor) {
            executor.executeIf(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            If anIf = (If) o;
            return Objects.equals(condition, anIf.condition) &&
                    Objects.equals(thenBranch, anIf.thenBranch) &&
                    Objects.equals(elseBranch, anIf.elseBranch);
        }

        @Override
        public int hashCode() {
            return Objects.hash(condition, thenBranch, elseBranch);
        }
    }

    public static class For extends Stmt {
        public final Stmt init;
        public final Expr condition;
        public final Stmt post;
        public final Stmt body;

        public For(Stmt init, Expr condition, Stmt post, Stmt body) {
            this.init = init;
            this.condition = condition;
            this.post = post;
            this.body = body;
        }

        @Override
        public void execute(Executor executor) {
            executor.executeFor(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            For aFor = (For) o;
            return Objects.equals(init, aFor.init) &&
                    Objects.equals(condition, aFor.condition) &&
                    Objects.equals(post, aFor.post) &&
                    Objects.equals(body, aFor.body);
        }

        @Override
        public int hashCode() {
            return Objects.hash(init, condition, post, body);
        }
    }

    public static class Print extends Stmt {
        public final Expr expr;

        public Print(Expr expr) {
            this.expr = expr;
        }

        @Override
        public void execute(Executor executor) {
            executor.executePrint(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Print print = (Print) o;
            return Objects.equals(expr, print.expr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr);
        }
    }

    public static class Empty extends Stmt {
        @Override
        public void execute(Executor executor) {
            executor.executeEmpty(this);
        }

        @Override
        public boolean equals(Object o) {
            return getClass() == o.getClass();
        }
    }

    public static class Break extends Stmt {
        @Override
        public void execute(Executor executor) {
            executor.executeBreak(this);
        }

        @Override
        public boolean equals(Object o) {
            return getClass() == o.getClass();
        }
    }

    public static class Sequence extends Stmt {
        public final MovePool movePoolOptional;
        public final MovePool movePoolForced;
        public final Integer lowerLimit;
        public final Integer upperLimit;
        public final String lexicographic;
        public final Stmt start;
        public final Stmt beforeMove;
        public final Stmt afterMove;
        public final Stmt beforeStep;
        public final Stmt afterStep;
        public final Stmt end;
        public final Permutation permutation;

        Sequence(MovePool movePoolOptional, MovePool movePoolForced, Integer lowerLimit, Integer upperLimit, String lexicographic,
                 Stmt start, Stmt beforeMove, Stmt afterMove, Stmt beforeStep, Stmt afterStep, Stmt end) {
            this.movePoolOptional = movePoolOptional;
            this.movePoolForced = movePoolForced;
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
            this.lexicographic = (lexicographic.equals("")) ? "urdlwh" : lexicographic;
            this.start = start;
            this.beforeMove = beforeMove;
            this.afterMove = afterMove;
            this.beforeStep = beforeStep;
            this.afterStep = afterStep;
            this.end = end;
            this.permutation = new Permutation(movePoolOptional, movePoolForced, lowerLimit, upperLimit, this.lexicographic);
        }

        @Override
        public void execute(Executor executor) {
            executor.executeSequence(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sequence sequence = (Sequence) o;
            return movePoolOptional.equals(sequence.movePoolOptional) &&
                    movePoolForced.equals(sequence.movePoolForced) &&
                    Objects.equals(lowerLimit, sequence.lowerLimit) &&
                    Objects.equals(upperLimit, sequence.upperLimit) &&
                    lexicographic.equals(sequence.lexicographic) &&
                    Objects.equals(start, sequence.start) &&
                    Objects.equals(beforeMove, sequence.beforeMove) &&
                    Objects.equals(afterMove, sequence.afterMove) &&
                    Objects.equals(beforeStep, sequence.beforeStep) &&
                    Objects.equals(afterStep, sequence.afterStep) &&
                    Objects.equals(end, sequence.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(movePoolOptional, movePoolForced, lowerLimit, upperLimit, lexicographic, start, beforeMove, afterMove, beforeStep, afterStep, end, permutation);
        }
    }

    public static class Return extends Stmt {
        @Override
        public void execute(Executor executor) {
            executor.executeReturn(this);
        }

        @Override
        public boolean equals(Object o) {
            return getClass() == o.getClass();
        }
    }

    public static class Terminate extends Stmt {
        public final Expr index;

        Terminate(Expr index) {
            this.index = index;
        }

        @Override
        public void execute(Executor executor) {
            executor.executeTerminate(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Terminate terminate = (Terminate) o;
            return Objects.equals(index, terminate.index);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index);
        }
    }

    public static class Continue extends Stmt {
        @Override
        public void execute(Executor executor) {
            executor.executeContinue(this);
        }

        @Override
        public boolean equals(Object o) {
            return getClass() == o.getClass();
        }
    }

    public static class All extends Stmt {
        public final Expr amount;

        All(Expr amount) {
            this.amount = amount;
        }

        @Override
        public void execute(Executor executor) {
            executor.executeAll(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            All all = (All) o;
            return Objects.equals(amount, all.amount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(amount);
        }
    }
}
