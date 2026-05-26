package enemies;

/**
 * Finite state machine states for enemy behavior.
 *
 * <p>Transitions follow the classic Doom-like pattern:
 * IDLE → PATROL (timeout) → CHASE (player detected) → ATTACK (in range) → HURT (damaged) → DEATH (health ≤ 0).
 */
public enum EnemyState {
    /** Standing still, scanning for the player. */
    IDLE,

    /** Moving between waypoints; no player detected. */
    PATROL,

    /** Actively pursuing the player after detection. */
    CHASE,

    /** Performing an attack (melee swing, ranged shot, etc.). */
    ATTACK,

    /** Flinch/stagger after taking damage; brief cooldown before next action. */
    HURT,

    /** Death animation playing; entity will be removed once animation completes. */
    DEATH
}
