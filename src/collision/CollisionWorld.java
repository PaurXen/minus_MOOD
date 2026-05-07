package collision;

import map.LegacyMapAdapter;
import map.LineDef;
import map.LineWall;
import map.MapData;
import map.Wall;
import math.GeometryMath;
import math.Segment2D;
import math.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollisionWorld {
    private MapData mapData;

    // Temporary legacy fields.
    // These exist only so the current Wall/LineWall level format keeps working.
    private List<Wall> walls;
    private List<LineWall> lineWalls;

    public CollisionWorld() {
        this(new MapData());
    }

    public CollisionWorld(MapData mapData) {
        setMapData(mapData);
        this.walls = Collections.emptyList();
        this.lineWalls = Collections.emptyList();
    }

    public CollisionWorld(List<Wall> walls, List<LineWall> lineWalls) {
        setLegacyGeometry(walls, lineWalls);
    }

    public void setMapData(MapData mapData) {
        if (mapData == null) {
            this.mapData = new MapData();
        } else {
            this.mapData = mapData;
        }
    }

    public MapData getMapData() {
        return mapData;
    }

    public void setLegacyGeometry(List<Wall> walls, List<LineWall> lineWalls) {
        setWalls(walls);
        setLineWalls(lineWalls);
        rebuildMapDataFromLegacyGeometry();
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
        return Collections.unmodifiableList(walls);
    }

    public List<LineWall> getLineWalls() {
        return Collections.unmodifiableList(lineWalls);
    }

    private void rebuildMapDataFromLegacyGeometry() {
        this.mapData = LegacyMapAdapter.fromLegacyWalls(walls, lineWalls);
    }

    public CollisionResult move(CollisionBody body, double dx, double dy) {
        return move(body, new Vec2(dx, dy));
    }

    public CollisionResult move(CollisionBody body, Vec2 movement) {
        Vec2 startPosition = body.position;
        Vec2 requestedPosition = startPosition.add(movement);

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

        return CollisionResult.blocked(
                startPosition,
                requestedPosition,
                finalPosition,
                blockedX,
                blockedY
        );
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
        return collidesWithLegacyWalls(position, radius)
                || collidesWithLineDefs(position, radius);
    }

    private boolean collidesWithLegacyWalls(Vec2 position, double radius) {
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

    private boolean collidesWithLineDefs(Vec2 position, double radius) {
        for (LineDef lineDef : mapData.getCollisionLines()) {
            if (!lineDef.blocksMovement()) {
                continue;
            }

            Segment2D segment = lineDef.getSegment();
            double collisionRadius = radius + lineDef.getCollisionThickness() / 2.0;

            if (GeometryMath.circleIntersectsSegment(position, collisionRadius, segment)) {
                return true;
            }
        }

        return false;
    }
}
