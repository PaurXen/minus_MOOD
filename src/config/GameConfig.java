package config;

import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GameConfig {
    public static GameSettings loadGameSettings(String path) {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(path)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Could not load game settings: " + path, e);
        }

        GameSettings settings = new GameSettings();

        settings.gameTitle = properties.getProperty("game_title", "Untitled Game");
        settings.gameVersion = properties.getProperty("game_version", "0.0.0");
        settings.gameBuild = properties.getProperty("game_build", "dev");

        settings.gameAuthor = properties.getProperty("game_author", "Unknown");
        settings.gameDescription = properties.getProperty("game_description", "");

        settings.configVersion = getInt(properties, "config_version", 1);
        settings.levelFormatVersion = getInt(properties, "level_format_version", 1);
        settings.controlsFormatVersion = getInt(properties, "controls_format_version", 1);

        settings.windowWidth = getInt(properties, "window_width", 900);
        settings.windowHeight = getInt(properties, "window_height", 600);
        settings.windowResizable = getBoolean(properties, "window_resizable", false);

        settings.defaultLevel = properties.getProperty("default_level", "levels/level01.properties");

        settings.debugMode = getBoolean(properties, "debug_mode", true);
        settings.showDebugText = getBoolean(properties, "show_debug_text", true);
        settings.showCollisionShapes = getBoolean(properties, "show_collision_shapes", true);

        settings.targetFPS = getInt(properties, "target_fps", 60);

        return settings;
    }

    public static KeyBindings loadControls(String path) {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(path)) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Could not load controls config. Using defaults.");
        }

        int controlsFormatVersion = getInt(properties, "controls_format_version", 1);

        if (controlsFormatVersion != 1) {
            throw new RuntimeException("Unsupported controls format version: " + controlsFormatVersion);
        }

        KeyBindings bindings = new KeyBindings();

        bindings.moveForward = keyCode(properties.getProperty("move_forward", "W"));
        bindings.moveBackward = keyCode(properties.getProperty("move_backward", "S"));
        bindings.strafeLeft = keyCode(properties.getProperty("strafe_left", "A"));
        bindings.strafeRight = keyCode(properties.getProperty("strafe_right", "D"));
        bindings.rotateLeft = keyCode(properties.getProperty("rotate_left", "LEFT"));
        bindings.rotateRight = keyCode(properties.getProperty("rotate_right", "RIGHT"));

        bindings.toggleDebug = keyCode(properties.getProperty("toggle_debug", "BACK_QUOTE"));

        return bindings;
    }

    private static int keyCode(String keyName) {
        keyName = keyName.trim().toUpperCase();

        return switch (keyName) {
            case "W" -> KeyEvent.VK_W;
            case "A" -> KeyEvent.VK_A;
            case "S" -> KeyEvent.VK_S;
            case "D" -> KeyEvent.VK_D;

            case "LEFT" -> KeyEvent.VK_LEFT;
            case "RIGHT" -> KeyEvent.VK_RIGHT;
            case "UP" -> KeyEvent.VK_UP;
            case "DOWN" -> KeyEvent.VK_DOWN;

            case "SPACE" -> KeyEvent.VK_SPACE;
            case "SHIFT" -> KeyEvent.VK_SHIFT;
            case "CONTROL", "CTRL" -> KeyEvent.VK_CONTROL;
            case "E" -> KeyEvent.VK_E;

            case "BACK_QUOTE", "`", "GRAVE" -> KeyEvent.VK_BACK_QUOTE;

            default -> {
                System.out.println("Unknown key: " + keyName + ". Using W as fallback.");
                yield KeyEvent.VK_W;
            }
        };
    }

    private static int getInt(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return Integer.parseInt(value.trim());
    }

    private static boolean getBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value.trim());
    }
}
