package featurecat.lizzie.rules;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Coordinates {
    private final int x;
    private final int y;

    public static Coordinates of(int x, int y) {
        return new Coordinates(x, y);
    }

    public static Coordinates of(int[] coord) {
        return of(coord[0], coord[1]);
    }

    private Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int[] toArray() {
        return new int[]{x, y};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return new EqualsBuilder()
                .append(x, that.x)
                .append(y, that.y)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(x)
                .append(y)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("x", x)
                .append("y", y)
                .toString();
    }
}
