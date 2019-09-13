package tools.variation;

import java.util.ArrayList;

public abstract class Stmt {
    interface Executor {
        void executeExpression(Expression stmt);
        void executeBlock(Block stmt);
        void executeIf(If stmt);
        void executeFor(For stmt);
        void executePrint(Print stmt);
        void executeEmpty(Empty stmt);
        void executeBreak(Break stmt);
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
    }

    public static class Empty extends Stmt {
        @Override
        public void execute(Executor executor) {
            executor.executeEmpty(this);
        }
    }

    public static class Break extends Stmt {
        @Override
        public void execute(Executor executor) {
            executor.executeBreak(this);
        }
    }
}
