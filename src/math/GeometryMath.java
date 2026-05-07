package math;

public final class GeometryMath {
    private GeometryMath() {
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double distancePointToSegment(
            double px,
            double py,
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        return distancePointToSegment(
                new Vec2(px, py),
                new Segment2D(x1, y1, x2, y2)
        );
    }

    public static double distancePointToSegment(Vec2 point, Segment2D segment) {
        return segment.distanceToPoint(point);
    }

    public static double distanceSquaredPointToSegment(Vec2 point, Segment2D segment) {
        return segment.distanceSquaredToPoint(point);
    }

    public static Vec2 closestPointOnSegment(Vec2 point, Segment2D segment) {
        return segment.closestPointTo(point);
    }

    public static boolean circleIntersectsSegment(
            double circleX,
            double circleY,
            double radius,
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        return circleIntersectsSegment(
                new Vec2(circleX, circleY),
                radius,
                new Segment2D(x1, y1, x2, y2)
        );
    }

    public static boolean circleIntersectsSegment(Vec2 center, double radius, Segment2D segment) {
        double distanceSquared = distanceSquaredPointToSegment(center, segment);

        return distanceSquared < radius * radius;
    }

    public static boolean circleIntersectsRectangle(
            double circleX,
            double circleY,
            double radius,
            double rectX,
            double rectY,
            double rectWidth,
            double rectHeight
    ) {
        Bounds2D bounds = Bounds2D.fromPositionAndSize(
                rectX,
                rectY,
                rectWidth,
                rectHeight
        );

        return circleIntersectsBounds(
                new Vec2(circleX, circleY),
                radius,
                bounds
        );
    }

    public static boolean circleIntersectsBounds(Vec2 center, double radius, Bounds2D bounds) {
        Vec2 closestPoint = bounds.closestPointTo(center);

        double distanceSquared = center.distanceSquaredTo(closestPoint);

        return distanceSquared < radius * radius;
    }

    public static boolean pointInsideCircle(Vec2 point, Vec2 center, double radius) {
        return point.distanceSquaredTo(center) < radius * radius;
    }

    public static double dot(double ax, double ay, double bx, double by) {
        return ax * bx + ay * by;
    }
}