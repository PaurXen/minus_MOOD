package map;

import java.util.EnumSet;
import java.util.List;

public final class LegacyMapAdapter {
    private LegacyMapAdapter() {
    }

    public static MapData fromLegacyWalls(List<Wall> walls, List<LineWall> lineWalls) {
        MapData mapData = new MapData();

        Sector defaultSector = new Sector(0);
        SideDef defaultSide = new SideDef(0, "legacy_wall");

        mapData.addSector(defaultSector);
        mapData.addSideDef(defaultSide);

        IdCounter ids = new IdCounter();

        if (walls != null) {
            for (Wall wall : walls) {
                addRectangleWall(mapData, ids, wall, defaultSector, defaultSide);
            }
        }

        if (lineWalls != null) {
            for (LineWall lineWall : lineWalls) {
                addLineWall(mapData, ids, lineWall, defaultSector, defaultSide);
            }
        }

        return mapData;
    }

    private static void addRectangleWall(
            MapData mapData,
            IdCounter ids,
            Wall wall,
            Sector defaultSector,
            SideDef defaultSide
    ) {
        Vertex topLeft = new Vertex(ids.nextVertexId(), wall.x, wall.y);
        Vertex topRight = new Vertex(ids.nextVertexId(), wall.x + wall.width, wall.y);
        Vertex bottomRight = new Vertex(ids.nextVertexId(), wall.x + wall.width, wall.y + wall.height);
        Vertex bottomLeft = new Vertex(ids.nextVertexId(), wall.x, wall.y + wall.height);

        mapData.addVertex(topLeft);
        mapData.addVertex(topRight);
        mapData.addVertex(bottomRight);
        mapData.addVertex(bottomLeft);

        addSolidLine(mapData, ids, topLeft, topRight, defaultSector, defaultSide, 0);
        addSolidLine(mapData, ids, topRight, bottomRight, defaultSector, defaultSide, 0);
        addSolidLine(mapData, ids, bottomRight, bottomLeft, defaultSector, defaultSide, 0);
        addSolidLine(mapData, ids, bottomLeft, topLeft, defaultSector, defaultSide, 0);
    }

    private static void addLineWall(
            MapData mapData,
            IdCounter ids,
            LineWall lineWall,
            Sector defaultSector,
            SideDef defaultSide
    ) {
        Vertex start = new Vertex(ids.nextVertexId(), lineWall.x1, lineWall.y1);
        Vertex end = new Vertex(ids.nextVertexId(), lineWall.x2, lineWall.y2);

        mapData.addVertex(start);
        mapData.addVertex(end);

        addSolidLine(
                mapData,
                ids,
                start,
                end,
                defaultSector,
                defaultSide,
                lineWall.thickness
        );
    }

    private static void addSolidLine(
            MapData mapData,
            IdCounter ids,
            Vertex start,
            Vertex end,
            Sector defaultSector,
            SideDef defaultSide,
            double collisionThickness
    ) {
        LineDef lineDef = new LineDef(
                ids.nextLineId(),
                start,
                end,
                defaultSector,
                null,
                defaultSide,
                null,
                EnumSet.of(
                        LineFlag.BLOCKS_MOVEMENT,
                        LineFlag.BLOCKS_RAY,
                        LineFlag.BLOCKS_PROJECTILE
                )
        );

        lineDef.setCollisionThickness(collisionThickness);

        mapData.addLineDef(lineDef);
    }

    private static final class IdCounter {
        private int vertexId = 0;
        private int lineId = 0;

        private int nextVertexId() {
            return vertexId++;
        }

        private int nextLineId() {
            return lineId++;
        }
    }
}
