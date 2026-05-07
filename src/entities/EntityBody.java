package entities;

import collision.CollisionBody;
import math.Vec2;

public class EntityBody extends CollisionBody {
    public boolean solid = true;
    public boolean collidable = true;

    public EntityBody(double x, double y, double radius) {
        super(x, y, radius);
    }

    public EntityBody(double x, double y, double radius, double height, double stepHeight) {
        super(x, y, radius, height, stepHeight);
    }

    public EntityBody(Vec2 position, double radius) {
        super(position, radius);
    }

    public EntityBody(Vec2 position, double radius, double height, double stepHeight) {
        super(position, radius, height, stepHeight);
    }
}
