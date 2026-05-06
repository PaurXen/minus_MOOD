package collision;

// TODO: TBC: Walls and LineWalls will be replaced by graph based collision system, but for now this is a simple implementation to get things working.
import map.LineWall;
import  map.Wall;
import math.GeometryMath;
import math.Segment2D;
import math.Vec2;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class CollisionWorld {
    private List<Wall> walls;
    private List<LineWall> lineWalls;

    public CollisionWorld() {
        this.walls = new ArrayList<>();
        this.lineWalls = new ArrayList<>();
    }

    public CollisionWorld(List<Wall> walls, List<LineWall> lineWalls) {
        setWalls(walls);
        setLineWalls(lineWalls);
    }

    public void setWalls(List<Wall> walls) {
        if (walls == null) {
            this.walls = Collections.emptyList();
        } else {
            this.walls = new ArrayList<>(walls);
        }
    }

    public void setLineWalls(List<LineWall> lineWalls) {
        if (lineWalls == null) {
            this.lineWalls = Collections.emptyList();
        } else {
            this.lineWalls = new ArrayList<>(lineWalls);
        }
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public List<LineWall> getLineWalls() {
        return lineWalls;
    }

    public CollisionResult move(CollisionBody body, double dx, double dy) {
        return move(body, new Vec2(dx, dy));
    }

    public CollisionResult move (CollisionBody body, Vec2 movment) {
        Vec2 startPosition = body.position;
        Vec2 requestedPosition = startPosition.add(movment);

        double finalX = startPosition.x;
        double finalY = startPosition.y;

        boolean blockedX = false;
        boolean blockedY = false;

        if (!isPositionBlocked(requestedPosition.x, startPosition.y, body.radius)) {
            finalX = requestedPosition.x;
        } else {
            blockedX = true;
        }

        if (!isPositionBlocked(finalX, requestedPosition.y, body.radius)) {
            finalY = requestedPosition.y;
        } else {
            blockedY = true;
        }

        Vec2 finalPosition = new Vec2(finalX, finalY);

        if (!blockedX && !blockedY) {
            return CollisionResult.noCollision(startPosition, requestedPosition);
        }
        return CollisionResult.blocked(startPosition, requestedPosition, finalPosition, blockedX, blockedY);
    }

    public CollisionResult moveAndApply(CollisionBody body, double dx, double dy) {
        return moveAndApply(body, new Vec2(dx, dy));
    }

    public CollisionResult moveAndApply(CollisionBody body, Vec2 movement) {
        CollisionResult result = move(body, movement);
        body.setPosition(result.finalPosition);
        return result;
    }

    public boolean collides(CollisionBody body) {
        return isPositionBlocked(body.position, body.radius);
    }

    public boolean isPositionBlocked(double x, double y, double radius) {
        return isPositionBlocked(new Vec2(x, y), radius);
    }

    public boolean isPositionBlocked(Vec2 position, double radius) {
        return collidesWithWalls(position, radius)
                || collidesWithLineWalls(position, radius);
    }

    private boolean collidesWithWalls(Vec2 position, double radius) {
        for (Wall wall : walls) {
            if (GeometryMath.circleIntersectsRectangle(
                    position.x,
                    position.y,
                    radius,
                    wall.x,
                    wall.y,
                    wall.width,
                    wall.height
            )) {
                return true;

            }
        }

        return false;
    }

    private boolean collidesWithLineWalls(Vec2 position, double radius) {
        for (LineWall wall : lineWalls) {
            Segment2D segment = new Segment2D(
                    wall.x1,
                    wall.y1,
                    wall.x2,
                    wall.y2
            );

            double collisionRadius = radius + wall.thickness / 2.0;

            if (GeometryMath.circleIntersectsSegment(
                    position,
                    collisionRadius,
                    segment
            )) {
                return true;
            }
        }

        return false;
    }
}
