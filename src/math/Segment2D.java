package math;

public class Segment2D {
    public final Vec2 start;
    public final Vec2 end;

    public Segment2D(Vec2 start, Vec2 end) {
        this.start = start;
        this.end = end;
    }

    public Segment2D(double x1, double y1, double x2, double y2) {
        this.start = new Vec2(x1, y1);
        this.end = new Vec2(x2, y2);
    }

    public Vec2 direction() {
        return end.subtract(start);
    }

    public double length() {
        return start.distanceTo(end);
    }

    public double lengthSquared() {
        return start.distanceSquaredTo(end);
    }

    public Vec2 closestPointTo(Vec2 point) {
        Vec2 segment = end.subtract(start);
        Vec2 startToPoint = point.subtract(start);

        double segmentLengthSquared = segment.lengthSquared();

        if (segmentLengthSquared == 0) {
            return start;
        }

        double t = startToPoint.dot(segment) / segmentLengthSquared;
        t = GeometryMath.clamp(t, 0, 1);

        return start.add(segment.multiply(t));
    }

    public double distanceToPoint(Vec2 point) {
        return point.distanceTo(closestPointTo(point));
    }

    public double distanceSquaredToPoint(Vec2 point) {
        return point.distanceSquaredTo(closestPointTo(point));
    }

    public Vec2 normalLeft() {
        return direction().normalized().perpendicularLeft();
    }

    public Vec2 normalRight() {
        return direction().normalized().perpendicularRight();
    }

    public Bounds2D getBounds() {
        return new Bounds2D(
                Math.min(start.x, end.x),
                Math.min(start.y, end.y),
                Math.max(start.x, end.x),
                Math.max(start.y, end.y)
        );
    }
}