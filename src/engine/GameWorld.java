package engine;

import collision.CollisionBody;
import collision.CollisionResult;
import collision.CollisionWorld;
import config.GameSettings;
import entities.Player;
import input.InputHandler;
import io.Level;
import io.LevelLoader;
import map.LineDef;
import map.LineWall;
import map.MapData;
import map.TestMapFactory;
import map.Wall;

import java.util.List;

public class GameWorld {
    private final GameSettings settings;

    private Level currentLevel;
    private Player player;

    private MapData mapData;

    private CollisionWorld collisionWorld;
    private CollisionBody playerBody;

    public GameWorld(GameSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("GameSettings cannot be null.");
        }

        this.settings = settings;
        loadLevel(settings.defaultLevel);
    }

    public void loadLevel(String path) {
        /*
         * Temporary transition:
         *
         * LevelLoader still loads the old level format so player settings,
         * level name, debug info, and existing config keep working.
         *
         * The actual collision/drawing map is now hardcoded MapData.
         *
         * Later:
         * LevelLoader -> LevelParser -> MapCompiler -> MapData
         */
        currentLevel = LevelLoader.loadLevel(path, settings.levelFormatVersion);

        player = currentLevel.player;

        mapData = TestMapFactory.createTestMap();

        collisionWorld = new CollisionWorld(mapData);

        playerBody = player.getBody();
    }

    public void update(InputHandler input, double deltaTime) {
        if (input == null || player == null) {
            return;
        }

        handleRotation(input, deltaTime);
        handleMovement(input, deltaTime);
    }

    private void handleRotation(InputHandler input, double deltaTime) {
        if (input.rotateLeft) {
            player.angle -= player.rotationSpeed * deltaTime;
        }

        if (input.rotateRight) {
            player.angle += player.rotationSpeed * deltaTime;
        }
    }

    private void handleMovement(InputHandler input, double deltaTime) {
        double moveX = 0;
        double moveY = 0;

        double forwardX = Math.cos(player.angle);
        double forwardY = Math.sin(player.angle);

        double rightX = Math.cos(player.angle + Math.PI / 2);
        double rightY = Math.sin(player.angle + Math.PI / 2);

        if (input.forward) {
            moveX += forwardX;
            moveY += forwardY;
        }

        if (input.backward) {
            moveX -= forwardX;
            moveY -= forwardY;
        }

        if (input.strafeLeft) {
            moveX -= rightX;
            moveY -= rightY;
        }

        if (input.strafeRight) {
            moveX += rightX;
            moveY += rightY;
        }

        double length = Math.sqrt(moveX * moveX + moveY * moveY);

        if (length == 0) {
            return;
        }

        moveX /= length;
        moveY /= length;

        double speed = player.moveSpeed * deltaTime;

        movePlayer(moveX * speed, moveY * speed);
    }

    private void movePlayer(double dx, double dy) {
        CollisionResult result = collisionWorld.moveAndApply(
                player.getBody(),
                dx,
                dy
        );

        player.setPosition(result.finalPosition);
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public MapData getMapData() {
        return mapData;
    }

    public List<LineDef> getMapLines() {
        return mapData.getLineDefs();
    }

    public CollisionWorld getCollisionWorld() {
        return collisionWorld;
    }

    public CollisionBody getPlayerBody() {
        return playerBody;
    }

    /*
     * Legacy accessors kept temporarily.
     * GamePanel should now draw MapData instead of these.
     */
    public List<Wall> getWalls() {
        return currentLevel.walls;
    }

    public List<LineWall> getLineWalls() {
        return currentLevel.lineWalls;
    }
}