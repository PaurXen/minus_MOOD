package io;

import entities.Player;
import map.LineWall;
import map.Wall;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LevelLoader {
    public static Level loadLevel(String path, int expectedLevelFormatVersion) {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(path)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Could not load level: " + path, e);
        }

        int actualFormatVersion = getInt(properties, "level_format_version", 1);

        if (actualFormatVersion != expectedLevelFormatVersion) {
            throw new RuntimeException(
                    "Unsupported level format version: " + actualFormatVersion
                            + ". Expected: " + expectedLevelFormatVersion
            );
        }

        Level level = new Level();

        level.levelFormatVersion = actualFormatVersion;
        level.id = properties.getProperty("level_id", "unknown");
        level.name = properties.getProperty("name", "Unnamed Level");
        level.author = properties.getProperty("author", "Unknown");
        level.version = properties.getProperty("version", "0.0.0");
        level.description = properties.getProperty("description", "");

        double playerX = getDouble(properties, "player_x", 120);
        double playerY = getDouble(properties, "player_y", 120);
        double playerAngle = getDouble(properties, "player_angle", 0);

        level.player = new Player(playerX, playerY, playerAngle);

        level.player.moveSpeed = getDouble(properties, "player_speed", 180);
        level.player.rotationSpeed = getDouble(properties, "player_rotation_speed", 2.5);
        level.player.radius = getDouble(properties, "player_radius", 6);

        int rectWallCount = getInt(properties, "rect_wall_count", 0);

        for (int i = 0; i < rectWallCount; i++) {
            String value = properties.getProperty("rect_wall_" + i);

            if (value == null) {
                throw new RuntimeException("Missing rect_wall_" + i + " in level: " + path);
            }

            String[] parts = value.split(",");

            if (parts.length != 4) {
                throw new RuntimeException("Invalid rect_wall_" + i + ". Expected: x,y,width,height");
            }

            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            double width = Double.parseDouble(parts[2].trim());
            double height = Double.parseDouble(parts[3].trim());

            level.walls.add(new Wall(x, y, width, height));
        }

        int lineWallCount = getInt(properties, "line_wall_count", 0);

        for (int i = 0; i < lineWallCount; i++) {
            String value = properties.getProperty("line_wall_" + i);

            if (value == null) {
                throw new RuntimeException("Missing line_wall_" + i + " in level: " + path);
            }

            String[] parts = value.split(",");

            if (parts.length != 5) {
                throw new RuntimeException("Invalid line_wall_" + i + ". Expected: x1,y1,x2,y2,thickness");
            }

            double x1 = Double.parseDouble(parts[0].trim());
            double y1 = Double.parseDouble(parts[1].trim());
            double x2 = Double.parseDouble(parts[2].trim());
            double y2 = Double.parseDouble(parts[3].trim());
            double thickness = Double.parseDouble(parts[4].trim());

            level.lineWalls.add(new LineWall(x1, y1, x2, y2, thickness));
        }

        return level;
    }

    private static double getDouble(Properties properties, String key, double defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return Double.parseDouble(value.trim());
    }

    private static int getInt(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return Integer.parseInt(value.trim());
    }
}
