package entities;

public class Player extends Entity {
    public double moveSpeed = 180.0;
    public double rotationSpeed = 2.5;

    public Player(double x, double y, double angle) {
        this(x, y, angle, 6.0);
    }

    public Player(double x, double y, double angle, double radius) {
        super(
                x,
                y,
                angle,
                new EntityBody(x, y, radius)
        );
    }

    public double getRadius() {
        return body.radius;
    }

    public void setRadius(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Player radius cannot be negative.");
        }

        body.radius = radius;
    }

    public double getHeight() {
        return body.height;
    }

    public void setHeight(double height) {
        if (height < 0) {
            throw new IllegalArgumentException("Player height cannot be negative.");
        }

        body.height = height;
    }

    public double getStepHeight() {
        return body.stepHeight;
    }

    public void setStepHeight(double stepHeight) {
        if (stepHeight < 0) {
            throw new IllegalArgumentException("Player step height cannot be negative.");
        }

        body.stepHeight = stepHeight;
    }
}