package enemies;

import collision.CollisionWorld;
import enemies.ai.BasicChaseAI;
import enemies.ai.EnemyAI;
import entities.Player;
import map.Level;
import map.SpawnPoint;
import map.SpawnType;
import math.Vec2;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the enemy list: spawning from level data, AI updates,
 * push-apart collision, and cleanup of dead enemies.
 */
public class EnemyManager {

    private final List<Enemy> enemies = new ArrayList<>();
    private final EnemyAI enemyAI = new BasicChaseAI();

    // ---------------------------------------------------------------
    // Spawning
    // ---------------------------------------------------------------

    /** Reads enemy spawn points from a level and creates enemies. */
    public void spawnFromLevel(Level level) {
        enemies.clear();
        for (SpawnPoint spawn : level.mapData.getSpawnPoints()) {
            if (spawn.type == SpawnType.ENEMY) {
                enemies.add(EnemyFactory.fromSpawnPoint(spawn));
            }
        }
    }

    // ---------------------------------------------------------------
    // Per-frame update
    // ---------------------------------------------------------------

    /**
     * Runs AI + attack checks + collision resolution for all living enemies.
     *
     * @param combat the combat system (for applying enemy-to-player damage)
     */
    public void update(double deltaTime, Player player, CollisionWorld collision, CombatSystem combat) {
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);

            if (enemy.getState() == EnemyState.DEATH) {
                continue;
            }

            enemyAI.update(enemy, player, collision, deltaTime);

            if (enemy.getState() == EnemyState.ATTACK && enemy.canAttack()) {
                double dist = enemy.getPosition().distanceTo(player.getPosition());
                if (dist <= enemy.getAttackRange()
                        && BasicChaseAI.hasLineOfSight(
                            enemy.getPosition(), player.getPosition(), collision)) {
                    combat.applyEnemyAttack(enemy);
                }
            }
        }

        resolveEnemyCollisions();
        pushPlayerFromEnemies(player);
        enemies.removeIf(Enemy::isDead);
    }

    // ---------------------------------------------------------------
    // Collision
    // ---------------------------------------------------------------

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

    private void pushPlayerFromEnemies(Player player) {
        double pr = player.getRadius();

        for (Enemy e : enemies) {
            if (e.getState() == EnemyState.DEATH) {
                continue;
            }

            double minDist = pr + e.getRadius();
            Vec2 diff = player.getPosition().subtract(e.getPosition());
            double dist = diff.length();

            if (dist < minDist && dist > 0.001) {
                double overlap = minDist - dist;
                Vec2 push = diff.divide(dist).multiply(overlap);
                player.moveBy(push.x, push.y);
            }
        }
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public long getAliveCount() {
        return enemies.stream().filter(e -> e.getState() != EnemyState.DEATH).count();
    }
}
