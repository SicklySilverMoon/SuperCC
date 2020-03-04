package tools.variation;

import java.util.Objects;

public class BoundLimit {
    public Integer lower = null;
    public Integer upper = null;

    public BoundLimit() {

    }

    public BoundLimit(Integer limit) {
        this.lower = limit;
        this.upper = limit;
    }

    public BoundLimit(Integer lower, Integer upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public void setBounds(MovePoolContainer movePools) {
        int size = movePools.optional.size + movePools.forced.size;
        if(lower == null && upper == null) {
            lower = size;
            upper = size;
        }
        else if(upper == null) {
            upper = lower;
        }
        if(upper < lower) {
            int temp = upper;
            upper = lower;
            lower = temp;
        }
        upper = Math.min(upper, size);
        lower = Math.min(lower, size);
        upper = Math.max(upper, movePools.forced.size);
        lower = Math.max(lower, movePools.forced.size);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundLimit that = (BoundLimit) o;
        return Objects.equals(lower, that.lower) &&
                Objects.equals(upper, that.upper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lower, upper);
    }
}
