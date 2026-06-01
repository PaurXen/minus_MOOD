package enemies;

import collision.CollisionWorld;
import enemies.ai.BasicChaseAI;
import entities.Player;
import math.Vec2;

import java.util.List;

/**
 * Handles all combat logic: player shooting, enemy attacks, damage,
 * line-of-sight checks, and player health tracking.
 *
 * <p>Does NOT handle game state transitions — the caller checks
 * {@link #isPlayerDead()} and pushes GameOverState itself.
 */
public class CombatSystem {

    private static final double SHOOT_COOLDOWN = 0.3;
    private static final double SHOOT_RANGE = 300.0;
    private static final double SHOOT_DAMAGE = 15.0;
    private static final double AIM_HALF_ANGLE = Math.PI / 8; // 22.5 = 45 total cone

    private final CollisionWorld collisionWorld;

    private double playerHealth;
    private double shootTimer;

    public CombatSystem(CollisionWorld collisionWorld) {
        this.collisionWorld = collisionWorld;
        this.playerHealth = 100.0;
        this.shootTimer = 0;
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    /**
     * Updates shooting cooldown. Call {@link #tryShoot} separately
     * when the player presses attack.
     */
    public void update(double deltaTime) {
        shootTimer = Math.max(0, shootTimer - deltaTime);
    }

    /**
     * Attempts a player shot. Returns the enemy that was hit, or null.
     */
    public Enemy tryShoot(Player player, List<Enemy> enemies) {
        if (shootTimer > 0) {
            return null;
        }
        shootTimer = SHOOT_COOLDOWN;

        Enemy hit = findHitEnemy(player, enemies);
        if (hit != null) {
            hit.takeDamage(SHOOT_DAMAGE);
        }
        return hit;
    }

    /**
     * Applies damage from an enemy to the player.
     * The caller should check {@link #isPlayerDead()} afterwards
     * and handle the game-over transition.
     */
    public void applyEnemyAttack(Enemy enemy) {
        playerHealth = Math.max(0, playerHealth - enemy.getAttackDamage());
        enemy.resetAttackTimer();
    }

    // ---------------------------------------------------------------
    // Hit detection
    // ---------------------------------------------------------------

    private Enemy findHitEnemy(Player player, List<Enemy> enemies) {
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

            if (!isWithinAimCone(player, enemy)) {
                continue;
            }

            if (!BasicChaseAI.hasLineOfSight(
                    player.getPosition(), enemy.getPosition(), collisionWorld)) {
                continue;
            }

            closestDist = dist;
            closest = enemy;
        }

        return closest;
    }

    private boolean isWithinAimCone(Player player, Enemy enemy) {
        Vec2 toEnemy = enemy.getPosition().subtract(player.getPosition());
        double angleToEnemy = Math.atan2(toEnemy.y, toEnemy.x);
        double angleDiff = normalizeAngle(angleToEnemy - player.angle);
        return Math.abs(angleDiff) < AIM_HALF_ANGLE;
    }

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
    // Getters
    // ---------------------------------------------------------------

    public double getPlayerHealth() {
        return playerHealth;
    }

    public double getPlayerMaxHealth() {
        return 100.0;
    }

    public boolean isPlayerDead() {
        return playerHealth <= 0;
    }
}
