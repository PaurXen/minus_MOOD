package enemies;

import entities.Entity;
import entities.EntityBody;

/**
 * Base class for all enemy entities in the game.
 *
 * <p>Extends {@link Entity} to inherit position, angle, and collision body.
 * Adds health, state machine, and attack cooldown management.
 *
 * <p>Specific behaviors (chase, patrol, etc.) are implemented via the
 * {@code EnemyAI} strategy interface (to be added in Phase 2).
 */
public class Enemy extends Entity {

    private final EnemyConfig config;

    private EnemyState state;
    private double health;
    private double attackTimer;

    /** Duration of the hurt stagger in seconds. */
    private double hurtTimer;
    private static final double HURT_DURATION = 0.2;

    /** Duration of the death animation in seconds. */
    private double deathTimer;
    private static final double DEATH_DURATION = 0.6;

    /**
     * Constructs an enemy at the given position.
     *
     * @param config enemy type configuration (health, speed, damage, etc.)
     * @param x      spawn X coordinate
     * @param y      spawn Y coordinate
     * @param angle  initial facing angle in radians
     */
    public Enemy(EnemyConfig config, double x, double y, double angle) {
        super(x, y, angle, new EntityBody(x, y, config.radius, config.height, 0));
        this.config = config;
        this.health = config.health;
        this.state = EnemyState.IDLE;
        this.attackTimer = 0;
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /**
     * Per-frame update. Advances state timers and delegates to AI.
     *
     * <p>Subclasses or AI strategies should call this via {@code super.update()}
     * or override to provide custom behavior.
     *
     * @param deltaTime seconds since last frame
     */
    public void update(double deltaTime) {
        if (!active) {
            return;
        }

        switch (state) {
            case HURT -> {
                hurtTimer -= deltaTime;
                if (hurtTimer <= 0) {
                    state = EnemyState.IDLE;
                }
            }
            case DEATH -> {
                deathTimer -= deltaTime;
                if (deathTimer <= 0) {
                    active = false;
                }
            }
            default -> attackTimer = Math.max(0, attackTimer - deltaTime);
        }
    }

    /**
     * Apply damage to this enemy. Triggers HURT state (stagger) or DEATH.
     *
     * @param amount damage points to subtract from health
     */
    public void takeDamage(double amount) {
        if (state == EnemyState.DEATH) {
            return;
        }

        health -= amount;

        if (health <= 0) {
            state = EnemyState.DEATH;
            deathTimer = DEATH_DURATION;
        } else {
            state = EnemyState.HURT;
            hurtTimer = HURT_DURATION;
        }
    }

    /**
     * Checks whether the enemy can perform an attack right now.
     * An attack is allowed when the cooldown timer has elapsed and the enemy
     * is not in HURT or DEATH state.
     *
     * @return true if an attack can be initiated
     */
    public boolean canAttack() {
        return attackTimer <= 0
                && state != EnemyState.HURT
                && state != EnemyState.DEATH;
    }

    /**
     * Resets the attack cooldown timer to the configured value.
     * Call this after performing an attack.
     */
    public void resetAttackTimer() {
        attackTimer = config.attackCooldown;
    }

    /**
     * Returns whether the death animation has finished playing.
     *
     * @return true when the enemy is ready to be removed from the world
     */
    public boolean isDead() {
        return state == EnemyState.DEATH && !active;
    }

    // ---------------------------------------------------------------
    // Getters / setters
    // ---------------------------------------------------------------

    public EnemyConfig getConfig() {
        return config;
    }

    public EnemyState getState() {
        return state;
    }

    public void setState(EnemyState newState) {
        if (state == EnemyState.DEATH) {
            return; // cannot transition out of death
        }
        this.state = newState;
    }

    public double getHealth() {
        return health;
    }

    public double getRadius() {
        return config.radius;
    }

    public double getHeight() {
        return config.height;
    }

    public double getAttackDamage() {
        return config.attackDamage;
    }

    public double getDetectionRange() {
        return config.detectionRange;
    }

    public double getAttackRange() {
        return config.attackRange;
    }
}
