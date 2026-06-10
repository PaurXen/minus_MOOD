package map;

public class LevelExit {
    private final double x;
    private final double y;
    private final double radius;

    private boolean unlocked;

    public LevelExit(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.unlocked = false;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isInRange(double playerX, double playerY) {
        double dx = playerX - x;
        double dy = playerY - y;
        return dx * dx + dy * dy <= radius * radius;
    }
}