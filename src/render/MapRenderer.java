package render;

import engine.GameWorld;
import map.LineDef;
import map.LevelExit;
import math.Bounds2D;
import enemies.Enemy;
import enemies.EnemyState;
import java.util.List;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Renders the level geometry — background and walls/LineDefs.
 */
public class MapRenderer {

    private final GameWorld gameWorld;
    private final int windowWidth;
    private final int windowHeight;

    public MapRenderer(GameWorld gameWorld, int windowWidth, int windowHeight) {
        this.gameWorld = gameWorld;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public void drawBackground(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, windowWidth, windowHeight);
    }

    public void drawMap(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        for (LineDef lineDef : gameWorld.getMapLines()) {
            float thickness = 3.0f;
            if (lineDef.getCollisionThickness() > 0) {
                thickness = (float) lineDef.getCollisionThickness();
            }

            if (lineDef.blocksMovement()) {
                g2.setColor(Color.GRAY);
            } else if (lineDef.isTrigger()) {
                g2.setColor(Color.GREEN);
            } else {
                g2.setColor(Color.DARK_GRAY);
            }

            g2.setStroke(new BasicStroke(thickness));
            g2.drawLine(
                    (int) lineDef.start.x, (int) lineDef.start.y,
                    (int) lineDef.end.x, (int) lineDef.end.y
            );
        }
        g2.setStroke(oldStroke);
    }

    public GameWorld getGameWorld() {
        return this.gameWorld;
    }

    public void drawOverlayMap(Graphics2D g2, List<Enemy> enemies) {
        if (gameWorld == null || gameWorld.getMapData() == null) {
            return;
        }

        Bounds2D bounds = gameWorld.getMapData().getBounds();

        double mapWidth = bounds.width();
        double mapHeight = bounds.height();

        if (mapWidth <= 0 || mapHeight <= 0) {
            return;
        }

        int padding = 40;
        int panelX = 40;
        int panelY = 40;
        int panelW = windowWidth - 80;
        int panelH = windowHeight - 80;

        g2.setColor(new Color(0, 0, 0, 105));
        g2.fillRect(panelX, panelY, panelW, panelH);

        g2.setColor(Color.WHITE);
        g2.drawRect(panelX, panelY, panelW, panelH);
        g2.drawString("MAP - press M to close", panelX + 16, panelY + 24);

        double scaleX = (panelW - padding * 2.0) / mapWidth;
        double scaleY = (panelH - padding * 2.0) / mapHeight;
        double scale = Math.min(scaleX, scaleY);

        double offsetX = panelX + panelW / 2.0 - (bounds.minX + mapWidth / 2.0) * scale;
        double offsetY = panelY + panelH / 2.0 - (bounds.minY + mapHeight / 2.0) * scale;

        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.0f));

        for (LineDef lineDef : gameWorld.getMapLines()) {
            if (lineDef.blocksMovement()) {
                g2.setColor(Color.LIGHT_GRAY);
            } else if (lineDef.isTrigger()) {
                g2.setColor(Color.GREEN);
            } else {
                g2.setColor(Color.DARK_GRAY);
            }

            int x1 = (int) (lineDef.start.x * scale + offsetX);
            int y1 = (int) (lineDef.start.y * scale + offsetY);
            int x2 = (int) (lineDef.end.x * scale + offsetX);
            int y2 = (int) (lineDef.end.y * scale + offsetY);

            g2.drawLine(x1, y1, x2, y2);
        }

        drawEnemiesOnOverlayMap(g2, enemies, scale, offsetX, offsetY);
        drawPlayerOnOverlayMap(g2, scale, offsetX, offsetY);
        drawExitOnOverlayMap(g2, scale, offsetX, offsetY);

        g2.setStroke(oldStroke);
    }

    private void drawEnemiesOnOverlayMap(
            Graphics2D g2,
            List<Enemy> enemies,
            double scale,
            double offsetX,
            double offsetY
    ) {
        if (enemies == null) {
            return;
        }

        for (Enemy enemy : enemies) {
            if (enemy == null) {
                continue;
            }

            if (enemy.getState() == EnemyState.DEATH) {
                continue;
            }

            int x = (int) (enemy.getX() * scale + offsetX);
            int y = (int) (enemy.getY() * scale + offsetY);

            g2.setColor(getEnemyMapColor(enemy));
            g2.fillOval(x - 4, y - 4, 8, 8);

            g2.setColor(Color.BLACK);
            g2.drawOval(x - 4, y - 4, 8, 8);
        }
    }

    private Color getEnemyMapColor(Enemy enemy) {
        String name = enemy.getConfig().name.toLowerCase();

        return switch (name) {
            case "demon" -> new Color(220, 60, 50);
            case "zombieman" -> new Color(120, 220, 90);
            case "imp" -> new Color(230, 150, 70);
            default -> Color.RED;
        };
    }
    private void drawPlayerOnOverlayMap(
            Graphics2D g2,
            double scale,
            double offsetX,
            double offsetY
    ) {
        if (gameWorld.getPlayer() == null) {
            return;
        }

        double playerX = gameWorld.getPlayer().getX();
        double playerY = gameWorld.getPlayer().getY();
        double angle = gameWorld.getPlayer().angle;

        int x = (int) (playerX * scale + offsetX);
        int y = (int) (playerY * scale + offsetY);

        g2.setColor(Color.CYAN);
        g2.fillOval(x - 5, y - 5, 10, 10);

        int dirX = (int) (x + Math.cos(angle) * 18);
        int dirY = (int) (y + Math.sin(angle) * 18);

        g2.drawLine(x, y, dirX, dirY);
    }

    private void drawExitOnOverlayMap(
            Graphics2D g2,
            double scale,
            double offsetX,
            double offsetY
    ) {
        LevelExit exit = gameWorld.getLevelExit();

        if (exit == null) {
            return;
        }

        int x = (int) (exit.getX() * scale + offsetX);
        int y = (int) (exit.getY() * scale + offsetY);

        if (exit.isUnlocked()) {
            g2.setColor(Color.GREEN);
        } else {
            g2.setColor(Color.RED);
        }

        g2.fillRect(x - 5, y - 5, 10, 10);
    }
}
