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

    public static class Sequence extends Stmt {
        public final MovePool movePool;
        public final Integer lowerLimit;
        public final Integer upperLimit;
        public final String lexicographic;
        public final Stmt start;
        public final Stmt beforeMove;
        public final Stmt afterMove;
        public final Permutation permutation;

        Sequence(MovePool movePool, Integer lowerLimit, Integer upperLimit, String lexicographic,
                 Stmt start, Stmt beforeMove, Stmt afterMove) {
            this.movePool = movePool;
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
            this.lexicographic = (lexicographic.equals("")) ? "urdlw" : lexicographic;
            this.start = start;
            this.beforeMove = beforeMove;
            this.afterMove = afterMove;
            this.permutation = new Permutation(movePool, lowerLimit, upperLimit, this.lexicographic);
        }

        @Override
        public void execute(Executor executor) {
            executor.executeSequence(this);
        }
    }

    public static class Return extends Stmt {
        @Override
        public void execute(Executor executor) {
            executor.executeReturn(this);
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
    }

    public static class Continue extends Stmt {
        @Override
        public void execute(Executor executor) {
            executor.executeContinue(this);
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
    }
}
