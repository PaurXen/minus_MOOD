package enemies;

/**
 * Immutable configuration for an enemy type.
 *
 * <p>Different enemy types (zombieman, demon, imp, etc.) are defined purely by
 * varying these values — no subclass explosion needed. This keeps the codebase
 * simple and makes it trivial to add new enemy types from config files later.
 */
public class EnemyConfig {

    /** Human-readable name (e.g. "Zombieman", "Demon"). */
    public final String name;

    /** Maximum health points. */
    public final double health;

    /** Movement speed in world-units per second. */
    public final double moveSpeed;

    /** Damage dealt per successful attack. */
    public final double attackDamage;

    /**
     * Attack range in world-units.
     * For melee enemies this is roughly their collision radius + a small buffer.
     * For ranged enemies this is the maximum shooting distance.
     */
    public final double attackRange;

    /** Minimum seconds between consecutive attacks. */
    public final double attackCooldown;

    /**
     * Maximum distance (world-units) at which the enemy can detect the player.
     * Detection also requires line-of-sight (when raycasting is available)
     * and the player to be within the detection cone.
     */
    public final double detectionRange;

    /**
     * Field-of-view cone half-angle in radians.
     * {@code Math.PI / 2} = 90° cone (45° each side).
     * {@code Math.PI} = full 360° awareness.
     */
    public final double detectionAngle;

    /** Collision body radius in world-units. */
    public final double radius;

    /** Entity height in world-units (used for sprite scaling in raycasting renderer). */
    public final double height;

    public EnemyConfig(
            String name,
            double health,
            double moveSpeed,
            double attackDamage,
            double attackRange,
            double attackCooldown,
            double detectionRange,
            double detectionAngle,
            double radius,
            double height
    ) {
        if (health <= 0) throw new IllegalArgumentException("health must be > 0");
        if (moveSpeed < 0) throw new IllegalArgumentException("moveSpeed cannot be negative");
        if (attackDamage < 0) throw new IllegalArgumentException("attackDamage cannot be negative");
        if (attackRange < 0) throw new IllegalArgumentException("attackRange cannot be negative");
        if (attackCooldown < 0) throw new IllegalArgumentException("attackCooldown cannot be negative");
        if (detectionRange < 0) throw new IllegalArgumentException("detectionRange cannot be negative");
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        if (height < 0) throw new IllegalArgumentException("height cannot be negative");

        this.name = name;
        this.health = health;
        this.moveSpeed = moveSpeed;
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.detectionRange = detectionRange;
        this.detectionAngle = detectionAngle;
        this.radius = radius;
        this.height = height;
    }

    // --- Pre-built configs for common enemy types ---

    /** Slow melee enemy: tanky, short range, high damage. */
    public static EnemyConfig demon() {
        return new EnemyConfig(
                "Demon",
                150.0,    // health
                130.0,     // moveSpeed
                20.0,     // attackDamage
                30.0,     // attackRange (melee)
                0.8,      // attackCooldown
                300.0,    // detectionRange
                Math.PI,  // detectionAngle (360°)
                16.0,     // radius
                56.0      // height
        );
    }

    /** Ranged enemy: fragile, keeps distance, shoots projectiles. */
    public static EnemyConfig zombieman() {
        return new EnemyConfig(
                "Zombieman",
                60.0,     // health
                150.0,    // moveSpeed
                8.0,      // attackDamage
                400.0,    // attackRange (ranged)
                1.2,      // attackCooldown
                500.0,    // detectionRange
                Math.PI,  // detectionAngle (360°)
                12.0,     // radius
                56.0      // height
        );
    }

    /** Fast melee enemy: low health, quick, swarms the player. */
    public static EnemyConfig imp() {
        return new EnemyConfig(
                "Imp",
                40.0,     // health
                180.0,    // moveSpeed
                10.0,     // attackDamage
                28.0,     // attackRange (melee)
                0.5,      // attackCooldown
                350.0,    // detectionRange
                Math.PI,  // detectionAngle (360°)
                10.0,     // radius
                56.0      // height
        );
    }
}
