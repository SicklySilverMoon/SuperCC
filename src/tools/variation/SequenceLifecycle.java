package tools.variation;

import java.util.Objects;

public class SequenceLifecycle {
    public Stmt start;
    public Stmt beforeMove;
    public Stmt afterMove;
    public Stmt beforeStep;
    public Stmt afterStep;
    public Stmt end;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceLifecycle lifecycle = (SequenceLifecycle) o;
        return Objects.equals(start, lifecycle.start) &&
                Objects.equals(beforeMove, lifecycle.beforeMove) &&
                Objects.equals(afterMove, lifecycle.afterMove) &&
                Objects.equals(beforeStep, lifecycle.beforeStep) &&
                Objects.equals(afterStep, lifecycle.afterStep) &&
                Objects.equals(end, lifecycle.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, beforeMove, afterMove, beforeStep, afterStep, end);
    }
}
