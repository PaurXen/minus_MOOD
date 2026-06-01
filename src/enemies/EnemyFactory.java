package enemies;

import map.SpawnPoint;

/**
 * Creates {@link Enemy} instances from configuration or spawn points.
 */
public final class EnemyFactory {

    private EnemyFactory() {
        // utility class
    }

    /**
     * Creates an enemy at the specified position with the given facing angle.
     */
    public static Enemy create(EnemyConfig config, double x, double y, double angle) {
        return new Enemy(config, x, y, angle);
    }

    /**
     * Creates an enemy from a level file spawn point.
     * The spawn point's {@code enemyType} field must match a known config name.
     */
    public static Enemy fromSpawnPoint(SpawnPoint spawn) {
        if (spawn.enemyType == null) {
            throw new IllegalArgumentException("SpawnPoint has no enemyType set");
        }

        EnemyConfig config = switch (spawn.enemyType.toLowerCase()) {
            case "zombieman" -> EnemyConfig.zombieman();
            case "demon" -> EnemyConfig.demon();
            case "imp" -> EnemyConfig.imp();
            default -> throw new IllegalArgumentException(
                    "Unknown enemy type: " + spawn.enemyType);
        };

        return create(config, spawn.getX(), spawn.getY(), spawn.angle);
    }
}
