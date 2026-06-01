package render;

import enemies.Enemy;
import engine.GameWorld;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Top-level render orchestrator — delegates to the three specialized
 * renderers ({@link MapRenderer}, {@link EntityRenderer}, {@link DebugRenderer})
 * and draws the player HUD.
 */
public class GameRenderer {

    private final MapRenderer mapRenderer;
    private final EntityRenderer entityRenderer;
    private final DebugRenderer debugRenderer;
    private final int windowWidth;
    private final int windowHeight;

    public GameRenderer(GameWorld gameWorld, int windowWidth, int windowHeight) {
        this.mapRenderer = new MapRenderer(gameWorld, windowWidth, windowHeight);
        this.entityRenderer = new EntityRenderer(gameWorld);
        this.debugRenderer = new DebugRenderer(gameWorld, windowWidth);
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    // ---------------------------------------------------------------
    // Public render entry point
    // ---------------------------------------------------------------

    public void render(Graphics2D g2, List<Enemy> enemies, double playerHealth,
                       boolean showDebugText, String gameTitle, String gameVersion, String gameBuild) {
        mapRenderer.drawBackground(g2);
        mapRenderer.drawMap(g2);
        entityRenderer.drawEnemies(g2, enemies);
        entityRenderer.drawPlayer(g2);
        drawPlayerHUD(g2, playerHealth);

        if (showDebugText) {
            debugRenderer.drawDebugInfo(g2, enemies, playerHealth,
                    gameTitle, gameVersion, gameBuild);
            debugRenderer.drawMapDebug(g2);
        }
    }

    // ---------------------------------------------------------------
    // HUD
    // ---------------------------------------------------------------

    private void drawPlayerHUD(Graphics2D g2, double playerHealth) {
        int barX = 20;
        int barY = windowHeight - 40;
        int barWidth = 200;
        int barHeight = 16;

        EntityRenderer.fillHealthBar(g2, barX, barY, barWidth, barHeight,
                playerHealth / 100.0);

        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);

        String healthText = String.format("HP: %.0f / 100", playerHealth);
        g2.drawString(healthText, barX, barY - 4);

        if (playerHealth < 30.0) {
            int alpha = (int) (60 + 40 * Math.sin(System.nanoTime() / 200_000_000.0));
            g2.setColor(new Color(255, 0, 0, Math.min(alpha, 80)));
            g2.fillRect(0, 0, windowWidth, windowHeight);
        }
    }
}
