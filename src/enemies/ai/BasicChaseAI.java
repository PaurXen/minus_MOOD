package enemies.ai;

import collision.CollisionResult;
import collision.CollisionWorld;
import enemies.Enemy;
import enemies.EnemyState;
import entities.Player;
import math.Vec2;

import java.util.HashMap;
import java.util.Map;

/**
 * Chase-and-attack AI with line-of-sight-aware detection, tracking,
 * and last-known-position search behavior.
 *
 * <p>When the enemy loses sight of the player, it moves to the player's
 * last known position before giving up and returning to IDLE.
 */
public class BasicChaseAI implements EnemyAI {

    /** How long the enemy searches before giving up (seconds). */
    private static final double SEARCH_DURATION = 3.0;

    /** How close the enemy must get to the last known position to consider it reached. */
    private static final double REACHED_THRESHOLD = 15.0;

    private final Map<Enemy, Vec2> lastKnownPos = new HashMap<>();
    private final Map<Enemy, Double> searchTimers = new HashMap<>();

    @Override
    public void update(Enemy enemy, Player player, CollisionWorld collision, double deltaTime) {
        if (enemy.getState() == EnemyState.DEATH || enemy.getState() == EnemyState.HURT) {
            return;
        }

        double dist = enemy.getPosition().distanceTo(player.getPosition());
        boolean canSee = dist <= enemy.getDetectionRange()
                && hasLineOfSight(enemy.getPosition(), player.getPosition(), collision);

        switch (enemy.getState()) {
            case IDLE -> {
                if (canSee) {
                    enemy.setState(EnemyState.CHASE);
                }
            }

            case PATROL -> {
                // Searching — moving toward last known position
                if (canSee) {
                    // Re-acquired player!
                    lastKnownPos.remove(enemy);
                    searchTimers.remove(enemy);
                    enemy.setState(EnemyState.CHASE);
                } else {
                    searchTowardLastKnown(enemy, collision, deltaTime);
                }
            }

            case CHASE -> {
                if (canSee) {
                    // Update last known position while player is visible
                    lastKnownPos.put(enemy, player.getPosition());

                    if (dist <= enemy.getAttackRange()) {
                        enemy.setState(EnemyState.ATTACK);
                    } else {
                        moveToward(enemy, player.getPosition(), collision, deltaTime);
                    }
                } else {
                    // Lost sight — go search last known position
                    enemy.setState(EnemyState.PATROL);
                }
            }

            case ATTACK -> {
                // Face the player
                Vec2 toPlayer = player.getPosition().subtract(enemy.getPosition());
                if (toPlayer.length() > 0.001) {
                    enemy.angle = Math.atan2(toPlayer.y, toPlayer.x);
                }

                if (canSee) {
                    // Update last known position
                    lastKnownPos.put(enemy, player.getPosition());

                    if (dist > enemy.getAttackRange() * 1.3) {
                        enemy.setState(EnemyState.CHASE);
                    }
                } else {
                    // Lost sight — go search
                    enemy.setState(EnemyState.PATROL);
                }
            }
        }
    }

    /**
     * Moves the enemy toward its last known player position.
     * Gives up after SEARCH_DURATION seconds or when close enough.
     */
    private void searchTowardLastKnown(Enemy enemy, CollisionWorld collision, double dt) {
        Vec2 target = lastKnownPos.get(enemy);
        if (target == null) {
            // No last known position — give up immediately
            enemy.setState(EnemyState.IDLE);
            return;
        }

        // Tick the search timer
        double timer = searchTimers.getOrDefault(enemy, SEARCH_DURATION) - dt;
        if (timer <= 0) {
            lastKnownPos.remove(enemy);
            searchTimers.remove(enemy);
            enemy.setState(EnemyState.IDLE);
            return;
        }
        searchTimers.put(enemy, timer);

        // Move toward last known position
        double dist = enemy.getPosition().distanceTo(target);
        if (dist < REACHED_THRESHOLD) {
            // Reached — give up (player isn't here)
            lastKnownPos.remove(enemy);
            searchTimers.remove(enemy);
            enemy.setState(EnemyState.IDLE);
        } else {
            moveToward(enemy, target, collision, dt);
        }
    }

    /**
     * Steers the enemy toward a target position with wall-sliding.
     * When the direct path is blocked, tries to slide perpendicular
     * to the target direction to hug the wall and find a way around.
     */
    private void moveToward(Enemy enemy, Vec2 target, CollisionWorld collision, double dt) {
        Vec2 toTarget = target.subtract(enemy.getPosition());
        double dist = toTarget.length();
        if (dist < 0.001) return;

        Vec2 direction = toTarget.divide(dist);
        double speed = enemy.getConfig().moveSpeed * dt;

        // Try direct movement
        CollisionResult result = collision.move(
                enemy.getBody(), direction.x * speed, direction.y * speed);
        enemy.getBody().setPosition(result.finalPosition);

        // If blocked, try sliding along the wall
        if (result.blocked) {
            // Try both perpendicular directions (left, then right)
            for (int side = -1; side <= 1 && result.blocked; side += 2) {
                // Slide with 80% lateral + 30% forward component
                double slideX = direction.x * speed * 0.3 + direction.y * side * speed * 0.8;
                double slideY = direction.y * speed * 0.3 - direction.x * side * speed * 0.8;
                result = collision.move(enemy.getBody(), slideX, slideY);
                enemy.getBody().setPosition(result.finalPosition);
            }
        }

        enemy.angle = Math.atan2(direction.y, direction.x);
    }

    /**
     * Checks whether a straight line between two positions is unobstructed
     * by walls. Steps along the path and queries the collision world.
     */
    public static boolean hasLineOfSight(Vec2 from, Vec2 to, CollisionWorld collision) {
        Vec2 dir = to.subtract(from);
        double maxDist = dir.length();
        if (maxDist < 1.0) return true;

        double step = 1.5;
        Vec2 stepVec = dir.divide(maxDist).multiply(step);
        Vec2 pos = from.add(stepVec);
        double traveled = step;

        while (traveled < maxDist) {
            if (collision.isPositionBlocked(pos, 1.0)) return false;
            pos = pos.add(stepVec);
            traveled += step;
        }
        return true;
    }
}
