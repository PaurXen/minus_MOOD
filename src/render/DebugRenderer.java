package render;

import enemies.Enemy;
import enemies.EnemyState;
import entities.Player;
import engine.GameWorld;
import map.LineDef;
import map.Vertex;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Renders debug overlays — player stats, map data, vertex/linedef labels.
 */
public class DebugRenderer {

    private final GameWorld gameWorld;
    private final int windowWidth;

    public DebugRenderer(GameWorld gameWorld, int windowWidth) {
        this.gameWorld = gameWorld;
        this.windowWidth = windowWidth;
    }

    public void drawDebugInfo(Graphics2D g2, List<Enemy> enemies, double playerHealth,
                               String gameTitle, String gameVersion, String gameBuild) {
        Player player = gameWorld.getPlayer();
        map.Level level = gameWorld.getCurrentLevel();
        if (player == null || level == null) return;

        g2.setColor(Color.WHITE);
        g2.drawString(gameTitle + " v" + gameVersion + " [" + gameBuild + "]", 20, 25);
        g2.drawString("W/S: forward/backward", 20, 50);
        g2.drawString("A/D: strafe left/right", 20, 70);
        g2.drawString("Left/Right arrows: rotate", 20, 90);
        g2.drawString("SPACE: shoot", 20, 110);
        g2.drawString("Player HP: " + String.format("%.0f", playerHealth) + " / 100", 20, 130);
        g2.drawString("Player X: " + String.format("%.2f", player.getX()), 20, 155);
        g2.drawString("Player Y: " + String.format("%.2f", player.getY()), 20, 165);
        g2.drawString("Angle: " + String.format("%.2f", player.angle), 20, 185);
        g2.drawString("Level: " + level.getName() + " v" + level.getVersion(), 20, 205);

        long aliveCount = enemies.stream()
                .filter(e -> e.getState() != EnemyState.DEATH).count();
        g2.drawString("Enemies alive: " + aliveCount + " / " + enemies.size(), 20, 225);
        g2.drawString("Debug toggle: `", 20, 245);
        g2.drawString("Collision body: radius=" + String.format("%.2f", player.getRadius()), 20, 275);
        g2.drawString("MapData lines: " + gameWorld.getMapData().getLineDefs().size(), 20, 295);
        g2.drawString("Collision lines: " + gameWorld.getMapData().getCollisionLines().size(), 20, 315);
        g2.drawString("Raycast lines: " + gameWorld.getMapData().getRaycastLines().size(), 20, 335);
    }

    public void drawMapDebug(Graphics2D g2) {
        g2.setColor(Color.CYAN);
        for (Vertex vertex : gameWorld.getMapData().getVertices()) {
            int x = (int) vertex.x;
            int y = (int) vertex.y;
            g2.fillOval(x - 3, y - 3, 6, 6);
            g2.drawString(String.valueOf(vertex.id), x + 5, y - 5);
        }

        g2.setColor(Color.ORANGE);
        for (LineDef lineDef : gameWorld.getMapLines()) {
            int midX = (int) ((lineDef.start.x + lineDef.end.x) / 2.0);
            int midY = (int) ((lineDef.start.y + lineDef.end.y) / 2.0);
            g2.drawString("L" + lineDef.id, midX + 4, midY + 4);
        }
    }
}
