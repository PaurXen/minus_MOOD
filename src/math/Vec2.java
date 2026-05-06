package math;

public class Vec2 {
    public final double x;
    public final double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(this.x + other.x, this.y + other.y);
    }

    public Vec2 subtract(Vec2 other) {
        return new Vec2(this.x - other.x, this.y - other.y);
    }

    public Vec2 multiply(double scalar) {
        return new Vec2(this.x * scalar, this.y * scalar);
    }

    public Vec2 divide(double scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Cannot divide Vec2 by zero.");
        }

        return new Vec2(this.x / scalar, this.y / scalar);
    }

    public double dot(Vec2 other) {
        return this.x * other.x + this.y * other.y;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public double distanceTo(Vec2 other) {
        return Math.sqrt(distanceSquaredTo(other));
    }

    public double distanceSquaredTo(Vec2 other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;

        return dx * dx + dy * dy;
    }

    public Vec2 normalized() {
        double length = length();

        if (length == 0) {
            return new Vec2(0, 0);
        }

        return divide(length);
    }

    public Vec2 perpendicularRight() {
        return new Vec2(y, -x);
    }

    public Vec2 perpendicularLeft() {
        return new Vec2(-y, x);
    }

    @Override
    public String toString() {
        return "Vec2(" + x + ", " + y + ")";
    }
}