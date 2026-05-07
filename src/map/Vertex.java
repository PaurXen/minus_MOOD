package map;

import math.Vec2;

public class Vertex {
    public final int id;
    public final double x;
    public final double y;

    public Vertex(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public Vec2 getPosition() {
        return new Vec2(x, y);
    }

    @Override
    public String toString() {
        return "Vertex(" + id + ", " + x + ", " + y + ")";
    }
}
