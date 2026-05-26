package enemies.ai;

import collision.CollisionWorld;
import enemies.Enemy;
import enemies.EnemyState;
import entities.Player;
import math.Vec2;

/**
 * Basic chase-and-attack AI.
 *
 * <p>Behavior:
 * <ol>
 *   <li>Always detects the player (no line-of-sight check yet — Phase 4).</li>
 *   <li>Moves toward the player using {@link CollisionWorld#moveAndApply} for
 *       wall-sliding collision resolution.</li>
 *   <li>When within {@link EnemyConfig#attackRange}, transitions to ATTACK state.</li>
 *   <li>After each attack, cooldown is handled by {@link Enemy#canAttack()} /
 *       {@link Enemy#resetAttackTimer()}.</li>
 * </ol>
 *
 * <p>This is the simplest useful AI — good enough for a playable prototype.
 * More sophisticated AIs (patrol routes, flanking, fleeing) can be added
 * later by implementing new {@link EnemyAI} strategies.
 */
public class BasicChaseAI implements EnemyAI {

    @Override
    public void update(Enemy enemy, Player player, CollisionWorld collision, double deltaTime) {
        if (enemy.getState() == EnemyState.DEATH || enemy.getState() == EnemyState.HURT) {
            return;
        }

        double dist = enemy.getPosition().distanceTo(player.getPosition());

        switch (enemy.getState()) {
            case IDLE, PATROL -> {
                // Always detect the player in Phase 2.
                // Line-of-sight checks will be added when raycasting is ready (Phase 4).
                enemy.setState(EnemyState.CHASE);
            }

            case CHASE -> {
                if (dist <= enemy.getAttackRange()) {
                    enemy.setState(EnemyState.ATTACK);
                } else {
                    moveToward(enemy, player, collision, deltaTime);
                }
            }

            case ATTACK -> {
                // The AI does NOT call resetAttackTimer() — PlayingState handles
                // damage application and cooldown reset. This keeps the AI
                // stateless and lets the game state control damage timing.
                //
                // Leave ATTACK if the player moves out of range (with 30% hysteresis
                // so melee enemies don't flicker CHASE↔ATTACK at the boundary).
                if (dist > enemy.getAttackRange() * 1.3) {
                    enemy.setState(EnemyState.CHASE);
                }
            }
        }
    }

    /**
     * Steers the enemy toward the player, resolving movement through
     * the collision world for wall sliding.
     */
    private void moveToward(Enemy enemy, Player player, CollisionWorld collision, double dt) {
        Vec2 toPlayer = player.getPosition().subtract(enemy.getPosition());
        double dist = toPlayer.length();

        if (dist < 0.001) {
            return; // already on top of player
        }

        Vec2 direction = toPlayer.divide(dist); // normalized
        double speed = enemy.getConfig().moveSpeed * dt;

        collision.moveAndApply(enemy.getBody(), direction.x * speed, direction.y * speed);

        // Face the player
        enemy.angle = Math.atan2(direction.y, direction.x);
    }
}
