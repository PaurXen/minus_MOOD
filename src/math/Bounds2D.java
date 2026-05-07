package math;

public class Bounds2D {
    public final double minX;
    public final double minY;
    public final double maxX;
    public final double maxY;

    public Bounds2D(double minX, double minY, double maxX, double maxY) {
        if (maxX < minX) {
            throw new IllegalArgumentException("maxX cannot be smaller than minX.");
        }

        if (maxY < minY) {
            throw new IllegalArgumentException("maxY cannot be smaller than minY.");
        }

        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public static Bounds2D fromPositionAndSize(double x, double y, double width, double height) {
        return new Bounds2D(x, y, x + width, y + height);
    }

    public double width() {
        return maxX - minX;
    }

    public double height() {
        return maxY - minY;
    }

    public Vec2 center() {
        return new Vec2(
                (minX + maxX) / 2.0,
                (minY + maxY) / 2.0
        );
    }

    public boolean contains(Vec2 point) {
        return point.x >= minX
                && point.x <= maxX
                && point.y >= minY
                && point.y <= maxY;
    }

    public boolean intersects(Bounds2D other) {
        return this.maxX >= other.minX
                && this.minX <= other.maxX
                && this.maxY >= other.minY
                && this.minY <= other.maxY;
    }

    public Bounds2D expanded(double amount) {
        return new Bounds2D(
                minX - amount,
                minY - amount,
                maxX + amount,
                maxY + amount
        );
    }

    public Vec2 closestPointTo(Vec2 point) {
        return new Vec2(
                GeometryMath.clamp(point.x, minX, maxX),
                GeometryMath.clamp(point.y, minY, maxY)
        );
    }
}