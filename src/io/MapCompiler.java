package io;

import map.Level;
import map.LineDef;
import map.MapData;
import map.Sector;
import map.SideDef;
import map.SpawnPoint;
import map.Vertex;

import java.util.HashSet;
import java.util.Set;

public final class MapCompiler {
    private MapCompiler() {
    }

    public static Level compile(Level level) {
        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null.");
        }

        if (level.metadata == null) {
            throw new IllegalArgumentException("Level metadata cannot be null.");
        }

        if (level.mapData == null) {
            throw new IllegalArgumentException("Level MapData cannot be null.");
        }

        validateMapData(level.mapData);
        validatePlayerSpawn(level);
        validatePlayerSettings(level);

        return level;
    }

    private static void validateMapData(MapData mapData) {
        validateUniqueVertexIds(mapData);
        validateUniqueLineIds(mapData);
        validateUniqueSectorIds(mapData);
        validateUniqueSideIds(mapData);
        validateLineReferences(mapData);
    }

    private static void validatePlayerSpawn(Level level) {
        SpawnPoint playerSpawn = level.playerSpawn;

        if (playerSpawn == null) {
            throw new IllegalArgumentException("Level must have a player spawn.");
        }

        if (playerSpawn.sector == null && !level.mapData.getSectors().isEmpty()) {
            throw new IllegalArgumentException("Player spawn must reference a sector.");
        }
    }

    private static void validatePlayerSettings(Level level) {
        if (level.playerRadius < 0) {
            throw new IllegalArgumentException("Player radius cannot be negative.");
        }

        if (level.playerHeight < 0) {
            throw new IllegalArgumentException("Player height cannot be negative.");
        }

        if (level.playerStepHeight < 0) {
            throw new IllegalArgumentException("Player step height cannot be negative.");
        }

        if (level.playerMoveSpeed < 0) {
            throw new IllegalArgumentException("Player move speed cannot be negative.");
        }

        if (level.playerRotationSpeed < 0) {
            throw new IllegalArgumentException("Player rotation speed cannot be negative.");
        }
    }

    private static void validateUniqueVertexIds(MapData mapData) {
        Set<Integer> ids = new HashSet<>();

        for (Vertex vertex : mapData.getVertices()) {
            if (!ids.add(vertex.id)) {
                throw new IllegalArgumentException("Duplicate vertex id: " + vertex.id);
            }
        }
    }

    private static void validateUniqueLineIds(MapData mapData) {
        Set<Integer> ids = new HashSet<>();

        for (LineDef lineDef : mapData.getLineDefs()) {
            if (!ids.add(lineDef.id)) {
                throw new IllegalArgumentException("Duplicate LineDef id: " + lineDef.id);
            }
        }
    }

    private static void validateUniqueSectorIds(MapData mapData) {
        Set<Integer> ids = new HashSet<>();

        for (Sector sector : mapData.getSectors()) {
            if (!ids.add(sector.id)) {
                throw new IllegalArgumentException("Duplicate sector id: " + sector.id);
            }
        }
    }

    private static void validateUniqueSideIds(MapData mapData) {
        Set<Integer> ids = new HashSet<>();

        for (SideDef sideDef : mapData.getSideDefs()) {
            if (!ids.add(sideDef.id)) {
                throw new IllegalArgumentException("Duplicate SideDef id: " + sideDef.id);
            }
        }
    }

    private static void validateLineReferences(MapData mapData) {
        for (LineDef lineDef : mapData.getLineDefs()) {
            if (lineDef.start == null) {
                throw new IllegalArgumentException("LineDef " + lineDef.id + " has null start vertex.");
            }

            if (lineDef.end == null) {
                throw new IllegalArgumentException("LineDef " + lineDef.id + " has null end vertex.");
            }

            if (lineDef.start == lineDef.end) {
                throw new IllegalArgumentException("LineDef " + lineDef.id + " uses the same start and end vertex.");
            }

            if (lineDef.length() == 0) {
                throw new IllegalArgumentException("LineDef " + lineDef.id + " has zero length.");
            }

            if (lineDef.frontSector == null) {
                throw new IllegalArgumentException("LineDef " + lineDef.id + " has null front sector.");
            }

            if (lineDef.frontSide == null) {
                throw new IllegalArgumentException("LineDef " + lineDef.id + " has null front SideDef.");
            }
        }
    }
}