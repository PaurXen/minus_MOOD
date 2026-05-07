package entities;

import math.Vec2;

public class Entity {
    private static long nextId = 1;

    public final long id;

    public double angle;
    public boolean active = true;

    protected EntityBody body;

    public Entity(double x, double y, double angle, EntityBody body) {
        this(new Vec2(x, y), angle, body);
    }

    public Entity(Vec2 position, double angle, EntityBody body) {
        if (body == null) {
            throw new IllegalArgumentException("Entity body cannot be null.");
        }

        this.id = nextId++;
        this.angle = angle;
        this.body = body;

        this.body.setPosition(position);
    }

    public double getX() {
        return body.getX();
    }

    public double getY() {
        return body.getY();
    }

    public Vec2 getPosition() {
        return body.getPosition();
    }

    public void setPosition(double x, double y) {
        setPosition(new Vec2(x, y));
    }

    public void setPosition(Vec2 newPosition) {
        body.setPosition(newPosition);
    }

    public void moveBy(double dx, double dy) {
        body.setPosition(new Vec2(
                body.position.x + dx,
                body.position.y + dy
        ));
    }

    public EntityBody getBody() {
        return body;
    }

    public void setBody(EntityBody body) {
        if (body == null) {
            throw new IllegalArgumentException("Entity body cannot be null.");
        }

        Vec2 oldPosition = this.body.position;
        this.body = body;
        this.body.setPosition(oldPosition);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }
}
