package menu;

import collision.CollisionWorld;
import config.GameConfig;
import config.GameSettings;
import enemies.Enemy;
import enemies.EnemyConfig;
import enemies.EnemyFactory;
import enemies.EnemyState;
import enemies.ai.BasicChaseAI;
import enemies.ai.EnemyAI;
import entities.Player;
import engine.GameWorld;
import input.InputHandler;
import map.LineDef;
import map.Vertex;
import math.Vec2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

/**
 * Main gameplay state — owns player, enemies, and world.
 *
 * <p>Orchestrates the per-frame update loop:
 * <ol>
 *   <li>Player movement & rotation (via {@link GameWorld})</li>
 *   <li>Enemy AI updates</li>
 *   <li>Enemy-vs-enemy push-apart collision</li>
 *   <li>Player shooting → enemy damage</li>
 *   <li>Dead enemy cleanup</li>
 * </ol>
 */
public class PlayingState implements GameState {

    // ---------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------

    /** Seconds between player shots. */
    private static final double SHOOT_COOLDOWN = 0.3;

    /** Maximum shoot distance in world units. */
    private static final double SHOOT_RANGE = 300.0;

    /** Player weapon damage per hit. */
    private static final double SHOOT_DAMAGE = 15.0;

    /** Half-angle of the player's aim cone (45° = generous Doom-style auto-aim). */
    private static final double AIM_HALF_ANGLE = Math.PI / 4;

    // ---------------------------------------------------------------
    // State
    // ---------------------------------------------------------------

    private final GameStateManager manager;
    private final GameSettings settings;
    private final GameWorld gameWorld;
    private final int windowWidth;
    private final int windowHeight;

    private boolean showDebugText;

    // --- Enemies ---
    private final List<Enemy> enemies = new ArrayList<>();
    private final EnemyAI enemyAI = new BasicChaseAI();

    // --- Player health ---
    private double playerHealth;
    private static final double PLAYER_MAX_HEALTH = 100.0;

    // --- Shooting ---
    private double shootTimer;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    public PlayingState(GameStateManager manager, int windowWidth, int windowHeight) {
        this.manager = manager;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        this.settings = GameConfig.loadGameSettings("config/game.properties");
        this.showDebugText = settings.showDebugText;
        this.gameWorld = new GameWorld(settings);
        this.playerHealth = PLAYER_MAX_HEALTH;
        this.shootTimer = 0;

        spawnTestEnemies();
    }

    /**
     * Places a few test enemies in the level.
     * Will be replaced by SpawnPoint-based spawning once the level format supports it.
     */
    private void spawnTestEnemies() {
        // Zombieman — ranged, fragile, stands at a distance
        enemies.add(EnemyFactory.create(EnemyConfig.zombieman(), 400, 200, 0));

        // Demon — melee tank, right side of the map
        enemies.add(EnemyFactory.create(EnemyConfig.demon(), 600, 300, Math.PI));

        // Imp — fast melee, bottom area
        enemies.add(EnemyFactory.create(EnemyConfig.imp(), 700, 450, Math.PI / 2));

        // Another imp for variety
        enemies.add(EnemyFactory.create(EnemyConfig.imp(), 300, 400, -Math.PI / 2));
    }

    // ---------------------------------------------------------------
    // GameState implementation
    // ---------------------------------------------------------------

    @Override
    public void enter() {
        // Game starts immediately
    }

    @Override
    public void update(double deltaTime, InputHandler input) {
        if (input == null) {
            return;
        }

        // --- Pause ---
        if (input.consumeCancel()) {
            manager.push(new PauseState(manager, windowWidth, windowHeight));
            return;
        }

        // --- Debug toggle ---
        if (input.consumeDebugToggleRequest()) {
            showDebugText = !showDebugText;
        }

        // --- Player movement & rotation ---
        gameWorld.update(input, deltaTime);

        // --- Enemy AI ---
        updateEnemies(deltaTime);

        // --- Enemy-vs-enemy push-apart ---
        resolveEnemyCollisions();

        // --- Player shooting ---
        updateShooting(deltaTime, input);

        // --- Remove dead enemies ---
        enemies.removeIf(Enemy::isDead);
    }

    @Override
    public void render(Graphics2D g2) {
        drawBackground(g2);
        drawMap(g2);
        drawEnemies(g2);
        drawPlayer(g2);
        drawPlayerHUD(g2);

        if (showDebugText) {
            drawDebugInfo(g2);
            drawMapDebug(g2);
        }
    }

    // ---------------------------------------------------------------
    // Enemy updates
    // ---------------------------------------------------------------

    private void updateEnemies(double deltaTime) {
        CollisionWorld collision = gameWorld.getCollisionWorld();
        Player player = gameWorld.getPlayer();

        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);

            if (enemy.getState() == EnemyState.DEATH) {
                continue;
            }

            enemyAI.update(enemy, player, collision, deltaTime);

            // Enemy attacking player?
            if (enemy.getState() == EnemyState.ATTACK && enemy.canAttack()) {
                double dist = enemy.getPosition().distanceTo(player.getPosition());
                if (dist <= enemy.getAttackRange()) {
                    applyEnemyAttack(enemy);
                }
            }
        }
    }

    /**
     * Applies damage from an enemy attack to the player and resets the enemy's
     * attack cooldown. Transitions to GameOverState when health reaches zero.
     */
    private void applyEnemyAttack(Enemy enemy) {
        playerHealth = Math.max(0, playerHealth - enemy.getAttackDamage());
        enemy.resetAttackTimer();

        if (playerHealth <= 0) {
            manager.push(new GameOverState(manager, windowWidth, windowHeight));
        }
    }

    /**
     * Pushes overlapping enemies apart so they don't stack on top of each other.
     * Simple circle-vs-circle separation — cheap and effective.
     */
    private void resolveEnemyCollisions() {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy a = enemies.get(i);
            if (a.getState() == EnemyState.DEATH) {
                continue;
            }

            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy b = enemies.get(j);
                if (b.getState() == EnemyState.DEATH) {
                    continue;
                }

                double minDist = a.getRadius() + b.getRadius();
                Vec2 diff = b.getPosition().subtract(a.getPosition());
                double dist = diff.length();

                if (dist < minDist && dist > 0.001) {
                    double overlap = minDist - dist;
                    Vec2 push = diff.divide(dist).multiply(overlap / 2.0);
                    a.moveBy(-push.x, -push.y);
                    b.moveBy(push.x, push.y);
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Shooting
    // ---------------------------------------------------------------

    private void updateShooting(double deltaTime, InputHandler input) {
        shootTimer = Math.max(0, shootTimer - deltaTime);

        if (input.attack && shootTimer <= 0) {
            performShot();
            shootTimer = SHOOT_COOLDOWN;
        }
    }

    private void performShot() {
        Player player = gameWorld.getPlayer();
        Enemy hit = findHitEnemy(player);

        if (hit != null) {
            hit.takeDamage(SHOOT_DAMAGE);
        }
    }

    /**
     * Finds the closest enemy hit by the player's shot.
     * Uses a cone-shaped aim check (Doom-style auto-aim).
     *
     * @return the closest hit enemy, or {@code null} if nothing was hit
     */
    private Enemy findHitEnemy(Player player) {
        Enemy closest = null;
        double closestDist = SHOOT_RANGE;

        for (Enemy enemy : enemies) {
            if (enemy.getState() == EnemyState.DEATH) {
                continue;
            }

            double dist = enemy.getPosition().distanceTo(player.getPosition());
            if (dist > closestDist) {
                continue;
            }

            // Is the enemy within the player's aim cone?
            if (!isWithinAimCone(player, enemy)) {
                continue;
            }

            // TODO Phase 4: add raycast line-of-sight check through walls
            closestDist = dist;
            closest = enemy;
        }

        return closest;
    }

    /**
     * Checks whether an enemy is within the player's aiming cone.
     */
    private boolean isWithinAimCone(Player player, Enemy enemy) {
        Vec2 toEnemy = enemy.getPosition().subtract(player.getPosition());
        double angleToEnemy = Math.atan2(toEnemy.y, toEnemy.x);
        double angleDiff = normalizeAngle(angleToEnemy - player.angle);
        return Math.abs(angleDiff) < AIM_HALF_ANGLE;
    }

    /**
     * Normalizes an angle to the range [-PI, PI].
     */
    private static double normalizeAngle(double angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    // ---------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------

    private void drawBackground(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, windowWidth, windowHeight);
    }

    private void drawMap(Graphics2D g2) {
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
                    (int) lineDef.start.x,
                    (int) lineDef.start.y,
                    (int) lineDef.end.x,
                    (int) lineDef.end.y
            );
        }

        g2.setStroke(oldStroke);
    }

    private void drawEnemies(Graphics2D g2) {
        for (Enemy enemy : enemies) {
            drawEnemy2D(g2, enemy);
        }
    }

    /**
     * Draws a single enemy in the top-down debug view.
     * Color indicates state, circle size matches collision radius,
     * and a small line shows facing direction.
     */
    private void drawEnemy2D(Graphics2D g2, Enemy enemy) {
        int x = (int) enemy.getX();
        int y = (int) enemy.getY();
        int r = (int) enemy.getRadius();

        // Ranged attack visual: draw a beam/line from enemy to player during ATTACK
        if (enemy.getState() == EnemyState.ATTACK && enemy.getAttackRange() > 60) {
            Player player = gameWorld.getPlayer();
            if (player != null) {
                g2.setColor(new Color(255, 255, 0, 120));
                g2.drawLine(x, y, (int) player.getX(), (int) player.getY());
            }
        }

        // Color by state
        Color color = switch (enemy.getState()) {
            case IDLE, PATROL -> new Color(255, 140, 0);       // orange
            case CHASE -> new Color(220, 50, 50);              // red
            case ATTACK -> new Color(200, 0, 200);             // magenta/purple
            case HURT -> Color.WHITE;
            case DEATH -> new Color(60, 60, 60);               // dark gray
        };

        // Body circle
        g2.setColor(color);
        g2.fillOval(x - r, y - r, r * 2, r * 2);

        // Outline
        g2.setColor(color.darker());
        g2.drawOval(x - r, y - r, r * 2, r * 2);

        // Direction indicator (skip for dead enemies)
        if (enemy.getState() != EnemyState.DEATH) {
            int lineLen = (int) (r * 1.5);
            int endX = x + (int) (Math.cos(enemy.angle) * lineLen);
            int endY = y + (int) (Math.sin(enemy.angle) * lineLen);
            g2.setColor(Color.WHITE);
            g2.drawLine(x, y, endX, endY);
        }

        // Health bar above enemy
        if (enemy.getState() != EnemyState.DEATH) {
            drawHealthBar(g2, enemy, x, y - r - 6);
        }

        // Label (name) below enemy
        g2.setColor(Color.LIGHT_GRAY);
        String label = enemy.getConfig().name;
        int labelW = g2.getFontMetrics().stringWidth(label);
        g2.drawString(label, x - labelW / 2, y + r + 14);

        // Hurt flash
        if (enemy.getState() == EnemyState.HURT) {
            g2.setColor(new Color(255, 255, 255, 120));
            g2.fillOval(x - r - 4, y - r - 4, (r + 4) * 2, (r + 4) * 2);
        }
    }

    /**
     * Draws a small health bar above an enemy.
     */
    private void drawHealthBar(Graphics2D g2, Enemy enemy, int centerX, int topY) {
        int barWidth = (int) (enemy.getRadius() * 2);
        int barHeight = 3;
        int x = centerX - barWidth / 2;

        double healthPercent = enemy.getHealth() / enemy.getConfig().health;

        // Background (empty)
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x, topY, barWidth, barHeight);

        // Foreground (remaining health) — green → yellow → red gradient
        int fillWidth = (int) (barWidth * healthPercent);
        if (healthPercent > 0.5) {
            g2.setColor(Color.GREEN);
        } else if (healthPercent > 0.25) {
            g2.setColor(Color.YELLOW);
        } else {
            g2.setColor(Color.RED);
        }
        g2.fillRect(x, topY, fillWidth, barHeight);

        // Border
        g2.setColor(Color.BLACK);
        g2.drawRect(x, topY, barWidth, barHeight);
    }

    private void drawPlayer(Graphics2D g2) {
        Player player = gameWorld.getPlayer();
        if (player == null) {
            return;
        }

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

    /**
     * Draws the player HUD — health bar and status.
     */
    private void drawPlayerHUD(Graphics2D g2) {
        int barX = 20;
        int barY = windowHeight - 40;
        int barWidth = 200;
        int barHeight = 16;

        double healthPercent = playerHealth / PLAYER_MAX_HEALTH;

        // Background
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Health fill
        if (healthPercent > 0.5) {
            g2.setColor(Color.GREEN);
        } else if (healthPercent > 0.25) {
            g2.setColor(Color.YELLOW);
        } else {
            g2.setColor(Color.RED);
        }
        g2.fillRect(barX, barY, (int) (barWidth * healthPercent), barHeight);

        // Border
        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);

        // Label
        g2.setColor(Color.WHITE);
        String healthText = String.format("HP: %.0f / %.0f", playerHealth, PLAYER_MAX_HEALTH);
        g2.drawString(healthText, barX, barY - 4);

        // Hurt flash (full-screen red overlay when recently hit)
        if (playerHealth < PLAYER_MAX_HEALTH * 0.3) {
            int alpha = (int) (60 + 40 * Math.sin(System.nanoTime() / 200_000_000.0));
            g2.setColor(new Color(255, 0, 0, Math.min(alpha, 80)));
            g2.fillRect(0, 0, windowWidth, windowHeight);
        }
    }

    private void drawDebugInfo(Graphics2D g2) {
        Player player = gameWorld.getPlayer();
        map.Level level = gameWorld.getCurrentLevel();

        if (player == null || level == null) {
            return;
        }

        g2.setColor(Color.WHITE);

        g2.drawString(
                settings.gameTitle + " v" + settings.gameVersion + " [" + settings.gameBuild + "]",
                20, 25
        );
        g2.drawString("W/S: forward/backward", 20, 50);
        g2.drawString("A/D: strafe left/right", 20, 70);
        g2.drawString("Left/Right arrows: rotate", 20, 90);
        g2.drawString("SPACE: shoot", 20, 110);

        g2.drawString("Player HP: " + String.format("%.0f", playerHealth)
                + " / " + String.format("%.0f", PLAYER_MAX_HEALTH), 20, 130);

        g2.drawString("Player X: " + String.format("%.2f", player.getX()), 20, 155);
        g2.drawString("Player Y: " + String.format("%.2f", player.getY()), 20, 165);
        g2.drawString("Angle: " + String.format("%.2f", player.angle), 20, 185);

        g2.drawString(
                "Level: " + level.getName() + " v" + level.getVersion(),
                20, 205
        );

        // Enemy stats
        long aliveCount = enemies.stream()
                .filter(e -> e.getState() != EnemyState.DEATH)
                .count();
        g2.drawString("Enemies alive: " + aliveCount + " / " + enemies.size(),
                20, 225);

        g2.drawString("Debug toggle: `", 20, 245);

        g2.drawString(
                "Collision body: radius=" + String.format("%.2f", player.getRadius()),
                20, 275
        );
        g2.drawString(
                "MapData lines: " + gameWorld.getMapData().getLineDefs().size(),
                20, 295
        );
        g2.drawString(
                "Collision lines: " + gameWorld.getMapData().getCollisionLines().size(),
                20, 315
        );
        g2.drawString(
                "Raycast lines: " + gameWorld.getMapData().getRaycastLines().size(),
                20, 335
        );
    }

    private void drawMapDebug(Graphics2D g2) {
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
