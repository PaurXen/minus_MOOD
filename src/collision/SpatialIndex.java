package collision;

// TODO: TBC: Walls and LineWalls will be replaced by graph based collision system, but for now this is a simple implementation to get things working.
import map.LineWall;
import map.Wall;
//import math.Vec2;
//import math.Segment2D;
//import math.GeometryMath;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class SpatialIndex {
    private final List<Wall> walls = new ArrayList<>();
    private final List<LineWall> lineWalls = new ArrayList<>();

    public void clear() {
        walls.clear();
        lineWalls.clear();
    }

    public void addWall(Wall wall) {
        if (wall != null) {
            walls.add(wall);
        }
    }

    public void addLineWall(LineWall lineWall) {
        if (lineWall != null) {
            lineWalls.add(lineWall);
        }
    }

    public void addWalls(List<Wall> walls) {
        if (walls == null) {
            return;
        }
        for (Wall wall : walls) {
            addWall(wall);
        }
    }

    public void addLineWalls(List<LineWall> lineWalls) {
        if (lineWalls == null) {
            return;
        }
        for (LineWall lineWall : lineWalls) {
            addLineWall(lineWall);
        }
    }

    public List<Wall> getNearbyWalls(double x, double y ,double radius) {
        return Collections.unmodifiableList(walls);
    }

    public List<LineWall> getNearbyLineWalls(double x, double y, double radius) {
        return Collections.unmodifiableList(lineWalls);
    }


}
