package io;

import map.Level;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class LevelLoader {
    private LevelLoader() {
    }

    public static Level loadLevel(String path, int expectedLevelFormatVersion) {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(path)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Could not load level: " + path, e);
        }

        return LevelParser.parse(
                properties,
                expectedLevelFormatVersion,
                path
        );
    }
}