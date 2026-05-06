package collision;

import math.Vec2;

public class CollisionBody {
    public Vec2 position;

    public double radius;

    // TODO: TBA: height and stepHeight are not used yet, but they will be useful for handling stairs and slopes in the future.
    public double height;

    // TODO: TBA: stepHeight is the maximum height that the player can step up onto without jumping. This will be useful for handling stairs and slopes in the future.
    public double stepHeight;

    public CollisionBody(double x, double y, double radius) {
        this(new Vec2(x, y), radius, 0., 0.);
    }

    public CollisionBody(double x, double y, double radius, double height, double stepHeight) {
        this(new Vec2(x, y), radius, height, stepHeight);
    }

    public CollisionBody(Vec2 position, double radius) {
        this(position, radius, 0., 0.);
    }

    public CollisionBody(Vec2 position, double radius, double height, double stepHeight) {
        if (radius < 0) {
            throw new IllegalArgumentException("Collision body radius cannot be negative.");
        }

        // TODO: TO BE RECONSIDERED: Is negative height really invalid? It could be used for representing holes or pits in the ground. For now, we will disallow it to keep things simple.
        if (height < 0) {
            throw new IllegalArgumentException("Collision body height cannot be negative.");
        }
        // TODO: TO BE RECONSIDERED: Is negative step height really invalid? It could be used for representing drops or ledges that the player can fall off of. For now, we will disallow it to keep things simple.
        if (stepHeight < 0) {
            throw new IllegalArgumentException("Collision body step height cannot be negative.");
        }

        this.position = position;
        this.radius = radius;
        this.height = height;
        this.stepHeight = stepHeight;
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public  void setPosition(Vec2 newPosition) {
        this.position = newPosition;
    }
}
