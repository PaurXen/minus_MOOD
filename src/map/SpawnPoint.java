package map;

import math.Vec2;

public class SpawnPoint {
    public final int id;
    public final SpawnType type;

    public Vec2 position;
    public double angle;

    public Sector sector;

    public SpawnPoint(int id, SpawnType type, double x, double y, double angle) {
        this(id, type, new Vec2(x, y), angle, null);
    }

    public SpawnPoint(int id, SpawnType type, Vec2 position, double angle, Sector sector) {
        if (type == null) {
            throw new IllegalArgumentException("SpawnPoint type cannot be null.");
        }

        if (position == null) {
            throw new IllegalArgumentException("SpawnPoint position cannot be null.");
        }

        this.id = id;
        this.type = type;
        this.position = position;
        this.angle = angle;
        this.sector = sector;
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }
}
