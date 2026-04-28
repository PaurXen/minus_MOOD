package entities;

public class Player {
    public double x;
    public double y;
    public double angle;

    public double moveSpeed = 180.0;
    public double rotationSpeed = 2.5;
    public double radius = 6.0;

    public Player(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
}
