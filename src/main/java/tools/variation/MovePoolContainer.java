package tools.variation;

import java.util.Objects;

public class MovePoolContainer {
    public MovePool optional = new MovePool();
    public MovePool forced = new MovePool();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovePoolContainer that = (MovePoolContainer) o;
        return optional.equals(that.optional) &&
                forced.equals(that.forced);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optional, forced);
    }
}
