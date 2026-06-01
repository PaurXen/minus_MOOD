package render;

import enemies.Enemy;
import enemies.EnemyState;
import enemies.ai.BasicChaseAI;
import entities.Player;
import engine.GameWorld;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Renders entity visuals — player, enemies, health bars.
 */
public class EntityRenderer {

    private final GameWorld gameWorld;

    public EntityRenderer(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    // ---------------------------------------------------------------
    // Player
    // ---------------------------------------------------------------

    public void drawPlayer(Graphics2D g2) {
        Player player = gameWorld.getPlayer();
        if (player == null) return;

        int px = (int) player.getX();
        int py = (int) player.getY();
        int r = (int) player.getRadius();

        g2.setColor(Color.RED);
        g2.fillOval(px - r, py - r, r * 2, r * 2);

        int lineLength = 35;
        int endX = (int) (player.getX() + Math.cos(player.angle) * lineLength);
        int endY = (int) (player.getY() + Math.sin(player.angle) * lineLength);
        g2.setColor(Color.YELLOW);
        g2.drawLine(px, py, endX, endY);
    }

    // ---------------------------------------------------------------
    // Enemies
    // ---------------------------------------------------------------

    public void drawEnemies(Graphics2D g2, List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            drawEnemy2D(g2, enemy);
        }
    }

    public void drawEnemy2D(Graphics2D g2, Enemy enemy) {
        int x = (int) enemy.getX();
        int y = (int) enemy.getY();
        int r = (int) enemy.getRadius();

        boolean isRanged = enemy.getAttackRange() > enemy.getRadius() * 3;
        if (enemy.getState() == EnemyState.ATTACK && isRanged) {
            Player player = gameWorld.getPlayer();
            if (player != null && BasicChaseAI.hasLineOfSight(
                    enemy.getPosition(), player.getPosition(),
                    gameWorld.getCollisionWorld())) {
                g2.setColor(new Color(255, 255, 0, 120));
                g2.drawLine(x, y, (int) player.getX(), (int) player.getY());
            }
        }

        Color color = switch (enemy.getState()) {
            case IDLE, PATROL -> new Color(255, 140, 0);
            case CHASE -> new Color(220, 50, 50);
            case ATTACK -> new Color(200, 0, 200);
            case HURT -> Color.WHITE;
            case DEATH -> new Color(60, 60, 60);
        };

        g2.setColor(color);
        g2.fillOval(x - r, y - r, r * 2, r * 2);
        g2.setColor(color.darker());
        g2.drawOval(x - r, y - r, r * 2, r * 2);

        if (enemy.getState() != EnemyState.DEATH) {
            int lineLen = (int) (r * 1.5);
            int endX = x + (int) (Math.cos(enemy.angle) * lineLen);
            int endY = y + (int) (Math.sin(enemy.angle) * lineLen);
            g2.setColor(Color.WHITE);
            g2.drawLine(x, y, endX, endY);
        }

        if (enemy.getState() != EnemyState.DEATH) {
            int barW = r * 2;
            int barH = 3;
            int bx = x - barW / 2;
            fillHealthBar(g2, bx, y - r - 6, barW, barH,
                    enemy.getHealth() / enemy.getConfig().health);
        }

        g2.setColor(Color.LIGHT_GRAY);
        String label = enemy.getConfig().name;
        int labelW = g2.getFontMetrics().stringWidth(label);
        g2.drawString(label, x - labelW / 2, y + r + 14);

        if (enemy.getState() == EnemyState.HURT) {
            g2.setColor(new Color(255, 255, 255, 120));
            g2.fillOval(x - r - 4, y - r - 4, (r + 4) * 2, (r + 4) * 2);
        }
    }

    // ---------------------------------------------------------------
    // Shared health bar (package-private — used by GameRenderer for HUD)
    // ---------------------------------------------------------------

    static void fillHealthBar(Graphics2D g2, int x, int y,
                               int width, int height, double percent) {
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x, y, width, height);

        int fillWidth = (int) (width * percent);
        if (percent > 0.5) g2.setColor(Color.GREEN);
        else if (percent > 0.25) g2.setColor(Color.YELLOW);
        else g2.setColor(Color.RED);
        g2.fillRect(x, y, fillWidth, height);

        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
    }
}
