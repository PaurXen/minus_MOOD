package enemies;

/**
 * Creates {@link Enemy} instances from configuration and spawn data.
 *
 * <p>In later phases this will read from {@code SpawnPoint} entries in level files.
 * For Phase 2, enemies are created directly with position coordinates.
 *
 * <p>Usage:
 * <pre>{@code
 *   Enemy demon = EnemyFactory.create(EnemyConfig.demon(), 400, 300, 0);
 *   Enemy zombie = EnemyFactory.create(EnemyConfig.zombieman(), 500, 200, Math.PI);
 * }</pre>
 */
public final class EnemyFactory {

    private EnemyFactory() {
        // utility class
    }

    /**
     * Creates an enemy at the specified position with the given facing angle.
     *
     * @param config enemy type configuration
     * @param x      spawn X coordinate (world units)
     * @param y      spawn Y coordinate (world units)
     * @param angle  initial facing angle in radians
     * @return a new, active enemy in IDLE state
     */
    public static Enemy create(EnemyConfig config, double x, double y, double angle) {
        return new Enemy(config, x, y, angle);
    }
}
