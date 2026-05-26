package enemies.ai;

import collision.CollisionWorld;
import enemies.Enemy;
import entities.Player;

/**
 * Strategy interface for enemy behavior.
 *
 * <p>Each implementation encapsulates a distinct AI pattern:
 * chase-and-attack, patrol, ambush, etc. The AI is called every frame
 * for each living enemy and decides state transitions and movement.
 *
 * <p>Implementations are stateless — all mutable state lives on {@link Enemy}.
 * This makes AI behaviors swappable at runtime (e.g. power-up that confuses enemies).
 */
public interface EnemyAI {

    /**
     * Per-frame AI update for a single enemy.
     *
     * @param enemy     the enemy to control
     * @param player    the player (target to chase / attack)
     * @param collision the collision world for movement resolution
     * @param deltaTime seconds since last frame
     */
    void update(Enemy enemy, Player player, CollisionWorld collision, double deltaTime);
}
