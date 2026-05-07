package engine;

import collision.CollisionBody;
import collision.CollisionResult;
import collision.CollisionWorld;
import config.GameSettings;
import entities.Player;
import input.InputHandler;
import io.Level;
import io.LevelLoader;
import map.LineWall;
import map.Wall;
import math.Vec2;

import java.util.List;

public class GameWorld {
    private final GameSettings settings;

    private Level currentLevel;
    private Player player;

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
        currentLevel = LevelLoader.loadLevel(path, settings.levelFormatVersion);

        player = currentLevel.player;

        collisionWorld = new CollisionWorld(
                currentLevel.walls,
                currentLevel.lineWalls
        );

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

    private void syncPlayerBodyFromPlayer() {
        playerBody.radius = player.getRadius();
        playerBody.setPosition(new Vec2(player.getX(), player.getY()));
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public CollisionWorld getCollisionWorld() {
        return collisionWorld;
    }

    public CollisionBody getPlayerBody() {
        return playerBody;
    }

    public List<Wall> getWalls() {
        return currentLevel.walls;
    }

    public List<LineWall> getLineWalls() {
        return currentLevel.lineWalls;
    }
}