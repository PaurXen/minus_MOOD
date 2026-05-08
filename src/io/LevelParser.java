package io;

import map.LegacyMapAdapter;
import map.Level;
import map.LevelMetadata;
import map.LineDef;
import map.LineFlag;
import map.LineWall;
import map.MapData;
import map.Sector;
import map.SideDef;
import map.SpawnPoint;
import map.SpawnType;
import map.Vertex;
import map.Wall;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

public final class LevelParser {
    private LevelParser() {
    }

    public static Level parse(
            Properties properties,
            int expectedLevelFormatVersion,
            String sourceName
    ) {
        if (properties == null) {
            throw new IllegalArgumentException("Level properties cannot be null.");
        }

        int actualFormatVersion = getInt(properties, "level_format_version", 1);

        if (actualFormatVersion != expectedLevelFormatVersion) {
            throw new RuntimeException(
                    "Unsupported level format version: " + actualFormatVersion
                            + ". Expected: " + expectedLevelFormatVersion
                            + ". Source: " + sourceName
            );
        }

        if (actualFormatVersion == 1) {
            return parseLegacyV1(properties, actualFormatVersion, sourceName);
        }

        if (actualFormatVersion == 2) {
            return parseNodeMapV2(properties, actualFormatVersion, sourceName);
        }

        throw new RuntimeException(
                "Unsupported level format version: " + actualFormatVersion
                        + ". Source: " + sourceName
        );
    }

    private static Level parseLegacyV1(
            Properties properties,
            int actualFormatVersion,
            String sourceName
    ) {
        LevelMetadata metadata = parseMetadata(properties, actualFormatVersion);

        List<Wall> legacyWalls = parseLegacyRectWalls(properties, sourceName);
        List<LineWall> legacyLineWalls = parseLegacyLineWalls(properties, sourceName);

        MapData mapData = LegacyMapAdapter.fromLegacyWalls(legacyWalls, legacyLineWalls);

        SpawnPoint playerSpawn = parseLegacyPlayerSpawn(properties, mapData);

        Level level = new Level(metadata, mapData, playerSpawn);

        applyPlayerSettings(properties, level);

        return MapCompiler.compile(level);
    }

    private static Level parseNodeMapV2(
            Properties properties,
            int actualFormatVersion,
            String sourceName
    ) {
        LevelMetadata metadata = parseMetadata(properties, actualFormatVersion);

        MapData mapData = new MapData();

        parseSectors(properties, mapData);
        parseSideDefs(properties, mapData);
        parseVertices(properties, mapData);
        parseLineDefs(properties, mapData, sourceName);

        SpawnPoint playerSpawn = parsePlayerSpawnV2(properties, mapData);
        mapData.addSpawnPoint(playerSpawn);

        Level level = new Level(metadata, mapData, playerSpawn);

        applyPlayerSettings(properties, level);

        return MapCompiler.compile(level);
    }

    private static LevelMetadata parseMetadata(Properties properties, int actualFormatVersion) {
        return new LevelMetadata(
                actualFormatVersion,
                properties.getProperty("level_id", "unknown"),
                properties.getProperty("name", "Unnamed Level"),
                properties.getProperty("author", "Unknown"),
                properties.getProperty("version", "0.0.0"),
                properties.getProperty("description", "")
        );
    }

    private static SpawnPoint parseLegacyPlayerSpawn(Properties properties, MapData mapData) {
        double playerX = getDouble(properties, "player_x", 120);
        double playerY = getDouble(properties, "player_y", 120);
        double playerAngle = getDouble(properties, "player_angle", 0);

        Sector sector = getDefaultSector(mapData);

        SpawnPoint spawnPoint = new SpawnPoint(
                0,
                SpawnType.PLAYER,
                playerX,
                playerY,
                playerAngle,
                sector
        );

        mapData.addSpawnPoint(spawnPoint);

        return spawnPoint;
    }

    private static SpawnPoint parsePlayerSpawnV2(Properties properties, MapData mapData) {
        String value = properties.getProperty("player_spawn");

        if (value == null) {
            double playerX = getDouble(properties, "player_x", 120);
            double playerY = getDouble(properties, "player_y", 120);
            double playerAngle = getDouble(properties, "player_angle", 0);

            return new SpawnPoint(
                    0,
                    SpawnType.PLAYER,
                    playerX,
                    playerY,
                    playerAngle,
                    getDefaultSector(mapData)
            );
        }

        String[] parts = splitCsv(value, 4, "player_spawn", "x,y,angle,sector_id");

        double x = parseDouble(parts[0], "player_spawn", "x");
        double y = parseDouble(parts[1], "player_spawn", "y");
        double angle = parseDouble(parts[2], "player_spawn", "angle");
        int sectorId = parseInt(parts[3], "player_spawn", "sector_id");

        Sector sector = mapData.findSectorById(sectorId);

        if (sector == null) {
            throw new RuntimeException(
                    "player_spawn references missing sector id: " + sectorId
            );
        }

        return new SpawnPoint(
                0,
                SpawnType.PLAYER,
                x,
                y,
                angle,
                sector
        );
    }

    private static void applyPlayerSettings(Properties properties, Level level) {
        level.playerMoveSpeed = getDouble(properties, "player_speed", 180);
        level.playerRotationSpeed = getDouble(properties, "player_rotation_speed", 2.5);
        level.playerRadius = getDouble(properties, "player_radius", 6);
        level.playerHeight = getDouble(properties, "player_height", 48);
        level.playerStepHeight = getDouble(properties, "player_step_height", 12);
    }

    private static void parseSectors(Properties properties, MapData mapData) {
        int sectorCount = getInt(properties, "sector_count", 0);

        for (int i = 0; i < sectorCount; i++) {
            String key = "sector_" + i;
            String value = requireProperty(properties, key);

            String[] parts = splitCsv(
                    value,
                    5,
                    key,
                    "floor_height,ceiling_height,floor_material,ceiling_material,light_level"
            );

            double floorHeight = parseDouble(parts[0], key, "floor_height");
            double ceilingHeight = parseDouble(parts[1], key, "ceiling_height");
            String floorMaterial = parts[2];
            String ceilingMaterial = parts[3];
            double lightLevel = parseDouble(parts[4], key, "light_level");

            mapData.addSector(new Sector(
                    i,
                    floorHeight,
                    ceilingHeight,
                    floorMaterial,
                    ceilingMaterial,
                    lightLevel
            ));
        }
    }

    private static void parseSideDefs(Properties properties, MapData mapData) {
        int sideDefCount = getInt(properties, "sidedef_count", 0);

        for (int i = 0; i < sideDefCount; i++) {
            String key = "sidedef_" + i;
            String value = requireProperty(properties, key);

            String[] parts = splitCsv(
                    value,
                    6,
                    key,
                    "upper_material,middle_material,lower_material,x_offset,y_offset,transparent"
            );

            String upperMaterial = parts[0];
            String middleMaterial = parts[1];
            String lowerMaterial = parts[2];
            double xOffset = parseDouble(parts[3], key, "x_offset");
            double yOffset = parseDouble(parts[4], key, "y_offset");
            boolean transparent = parseBoolean(parts[5], key, "transparent");

            mapData.addSideDef(new SideDef(
                    i,
                    upperMaterial,
                    middleMaterial,
                    lowerMaterial,
                    xOffset,
                    yOffset,
                    transparent
            ));
        }
    }

    private static void parseVertices(Properties properties, MapData mapData) {
        int vertexCount = getInt(properties, "vertex_count", 0);

        for (int i = 0; i < vertexCount; i++) {
            String key = "vertex_" + i;
            String value = requireProperty(properties, key);

            String[] parts = splitCsv(value, 2, key, "x,y");

            double x = parseDouble(parts[0], key, "x");
            double y = parseDouble(parts[1], key, "y");

            mapData.addVertex(new Vertex(i, x, y));
        }
    }

    private static void parseLineDefs(
            Properties properties,
            MapData mapData,
            String sourceName
    ) {
        int lineDefCount = getInt(properties, "linedef_count", 0);

        for (int i = 0; i < lineDefCount; i++) {
            String key = "linedef_" + i;
            String value = requireProperty(properties, key);

            String[] parts = splitCsv(
                    value,
                    8,
                    key,
                    "start_vertex,end_vertex,front_sector,back_sector,front_sidedef,back_sidedef,flags,collision_thickness"
            );

            int startVertexId = parseInt(parts[0], key, "start_vertex");
            int endVertexId = parseInt(parts[1], key, "end_vertex");
            int frontSectorId = parseInt(parts[2], key, "front_sector");
            int backSectorId = parseInt(parts[3], key, "back_sector");
            int frontSideDefId = parseInt(parts[4], key, "front_sidedef");
            int backSideDefId = parseInt(parts[5], key, "back_sidedef");

            EnumSet<LineFlag> flags = parseLineFlags(parts[6], key);
            double collisionThickness = parseDouble(parts[7], key, "collision_thickness");

            Vertex start = requireVertex(mapData, startVertexId, key);
            Vertex end = requireVertex(mapData, endVertexId, key);

            Sector frontSector = requireSector(mapData, frontSectorId, key);
            Sector backSector = findOptionalSector(mapData, backSectorId, key);

            SideDef frontSide = requireSideDef(mapData, frontSideDefId, key);
            SideDef backSide = findOptionalSideDef(mapData, backSideDefId, key);

            LineDef lineDef = new LineDef(
                    i,
                    start,
                    end,
                    frontSector,
                    backSector,
                    frontSide,
                    backSide,
                    flags
            );

            lineDef.setCollisionThickness(collisionThickness);

            mapData.addLineDef(lineDef);
        }
    }

    private static List<Wall> parseLegacyRectWalls(Properties properties, String sourceName) {
        List<Wall> walls = new ArrayList<>();

        int rectWallCount = getInt(properties, "rect_wall_count", 0);

        for (int i = 0; i < rectWallCount; i++) {
            String key = "rect_wall_" + i;
            String value = requireProperty(properties, key);

            String[] parts = splitCsv(value, 4, key, "x,y,width,height");

            double x = parseDouble(parts[0], key, "x");
            double y = parseDouble(parts[1], key, "y");
            double width = parseDouble(parts[2], key, "width");
            double height = parseDouble(parts[3], key, "height");

            walls.add(new Wall(x, y, width, height));
        }

        return walls;
    }

    private static List<LineWall> parseLegacyLineWalls(Properties properties, String sourceName) {
        List<LineWall> lineWalls = new ArrayList<>();

        int lineWallCount = getInt(properties, "line_wall_count", 0);

        for (int i = 0; i < lineWallCount; i++) {
            String key = "line_wall_" + i;
            String value = requireProperty(properties, key);

            String[] parts = splitCsv(value, 5, key, "x1,y1,x2,y2,thickness");

            double x1 = parseDouble(parts[0], key, "x1");
            double y1 = parseDouble(parts[1], key, "y1");
            double x2 = parseDouble(parts[2], key, "x2");
            double y2 = parseDouble(parts[3], key, "y2");
            double thickness = parseDouble(parts[4], key, "thickness");

            lineWalls.add(new LineWall(x1, y1, x2, y2, thickness));
        }

        return lineWalls;
    }

    private static EnumSet<LineFlag> parseLineFlags(String value, String key) {
        EnumSet<LineFlag> flags = EnumSet.noneOf(LineFlag.class);

        if (value == null) {
            return flags;
        }

        String trimmedValue = value.trim();

        if (trimmedValue.isEmpty() || trimmedValue.equalsIgnoreCase("NONE")) {
            return flags;
        }

        String[] parts = trimmedValue.split("\\|");

        for (String part : parts) {
            String flagName = part.trim();

            if (flagName.isEmpty()) {
                continue;
            }

            try {
                flags.add(LineFlag.valueOf(flagName));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(
                        "Invalid LineFlag in " + key + ": " + flagName,
                        e
                );
            }
        }

        return flags;
    }

    private static Vertex requireVertex(MapData mapData, int id, String ownerKey) {
        Vertex vertex = mapData.findVertexById(id);

        if (vertex == null) {
            throw new RuntimeException(ownerKey + " references missing vertex id: " + id);
        }

        return vertex;
    }

    private static Sector requireSector(MapData mapData, int id, String ownerKey) {
        Sector sector = mapData.findSectorById(id);

        if (sector == null) {
            throw new RuntimeException(ownerKey + " references missing sector id: " + id);
        }

        return sector;
    }

    private static Sector findOptionalSector(MapData mapData, int id, String ownerKey) {
        if (id < 0) {
            return null;
        }

        return requireSector(mapData, id, ownerKey);
    }

    private static SideDef requireSideDef(MapData mapData, int id, String ownerKey) {
        SideDef sideDef = mapData.findSideDefById(id);

        if (sideDef == null) {
            throw new RuntimeException(ownerKey + " references missing sidedef id: " + id);
        }

        return sideDef;
    }

    private static SideDef findOptionalSideDef(MapData mapData, int id, String ownerKey) {
        if (id < 0) {
            return null;
        }

        return requireSideDef(mapData, id, ownerKey);
    }

    private static Sector getDefaultSector(MapData mapData) {
        if (mapData != null && !mapData.getSectors().isEmpty()) {
            return mapData.getSectors().get(0);
        }

        return null;
    }

    private static String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);

        if (value == null) {
            throw new RuntimeException("Missing required property: " + key);
        }

        return value;
    }

    private static String[] splitCsv(
            String value,
            int expectedLength,
            String key,
            String expectedFormat
    ) {
        String[] parts = value.split(",");

        if (parts.length != expectedLength) {
            throw new RuntimeException(
                    "Invalid " + key + ". Expected: " + expectedFormat
                            + ". Got: " + value
            );
        }

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        return parts;
    }

    private static double parseDouble(String value, String key, String fieldName) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Invalid number in " + key + " for field " + fieldName + ": " + value,
                    e
            );
        }
    }

    private static int parseInt(String value, String key, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Invalid integer in " + key + " for field " + fieldName + ": " + value,
                    e
            );
        }
    }

    private static boolean parseBoolean(String value, String key, String fieldName) {
        String trimmed = value.trim();

        if (trimmed.equalsIgnoreCase("true")) {
            return true;
        }

        if (trimmed.equalsIgnoreCase("false")) {
            return false;
        }

        throw new RuntimeException(
                "Invalid boolean in " + key + " for field " + fieldName + ": " + value
        );
    }

    private static double getDouble(Properties properties, String key, double defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return parseDouble(value, key, key);
    }

    private static int getInt(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return parseInt(value, key, key);
    }
}