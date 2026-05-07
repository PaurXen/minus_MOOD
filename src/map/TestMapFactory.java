package map;

import java.util.EnumSet;

public final class TestMapFactory {
    private TestMapFactory() {
    }

    public static MapData createTestMap() {
        MapData mapData = new MapData();

        Sector mainSector = new Sector(
                0,
                0,
                128,
                "floor_test",
                "ceiling_test",
                1.0
        );

        SideDef wallSide = new SideDef(0, "test_wall");
        SideDef innerWallSide = new SideDef(1, "test_inner_wall");

        mapData.addSector(mainSector);
        mapData.addSideDef(wallSide);
        mapData.addSideDef(innerWallSide);

        /*
         * Outer room.
         *
         * 60,60 ----------------------------- 840,60
         *   |                                  |
         *   |                                  |
         * 60,540 ---------------------------- 840,540
         */

        Vertex v0 = vertex(mapData, 0, 60, 60);
        Vertex v1 = vertex(mapData, 1, 840, 60);
        Vertex v2 = vertex(mapData, 2, 840, 540);
        Vertex v3 = vertex(mapData, 3, 60, 540);

        solidLine(mapData, 0, v0, v1, mainSector, wallSide, 0);
        solidLine(mapData, 1, v1, v2, mainSector, wallSide, 0);
        solidLine(mapData, 2, v2, v3, mainSector, wallSide, 0);
        solidLine(mapData, 3, v3, v0, mainSector, wallSide, 0);

        /*
         * Inner rectangular obstacle.
         */

        Vertex v4 = vertex(mapData, 4, 300, 180);
        Vertex v5 = vertex(mapData, 5, 500, 180);
        Vertex v6 = vertex(mapData, 6, 700, 320);
        Vertex v7 = vertex(mapData, 7, 300, 320);

        solidLine(mapData, 4, v4, v5, mainSector, innerWallSide, 0);
        solidLine(mapData, 5, v5, v6, mainSector, innerWallSide, 0);
        solidLine(mapData, 6, v6, v7, mainSector, innerWallSide, 0);
        solidLine(mapData, 7, v7, v4, mainSector, innerWallSide, 0);

        /*
         * Angled wall for testing non-axis-aligned collision and raycasting.
         */

        Vertex v8 = vertex(mapData, 8, 580, 390);
        Vertex v9 = vertex(mapData, 9, 760, 500);

        solidLine(mapData, 8, v8, v9, mainSector, innerWallSide, 16);

        /*
         * Player spawn reference for future use.
         * GameWorld still currently uses the loaded level player,
         * but raycasting/map systems can already see this spawn.
         */

        mapData.addSpawnPoint(new SpawnPoint(
                0,
                SpawnType.PLAYER,
                120.,
                120.,
                0.,
                mainSector
        ));

        return mapData;
    }

    private static Vertex vertex(MapData mapData, int id, double x, double y) {
        Vertex vertex = new Vertex(id, x, y);
        mapData.addVertex(vertex);
        return vertex;
    }

    private static void solidLine(
            MapData mapData,
            int id,
            Vertex start,
            Vertex end,
            Sector sector,
            SideDef side,
            double collisionThickness
    ) {
        LineDef lineDef = new LineDef(
                id,
                start,
                end,
                sector,
                null,
                side,
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
}