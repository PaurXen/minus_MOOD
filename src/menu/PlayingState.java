package menu;

import config.GameConfig;
import config.GameSettings;
import enemies.CombatSystem;
import enemies.EnemyManager;
import engine.GameWorld;
import input.InputHandler;
import render.GameRenderer;

import java.awt.Graphics2D;

/**
 * Main gameplay state — owns world, enemies, combat, and rendering.
 *
 * <p>Responsibilities are delegated to focused helper classes:
 * <ul>
 *   <li>{@link EnemyManager} — enemy list, AI loop, collision, cleanup</li>
 *   <li>{@link CombatSystem} — shooting, damage, line-of-sight</li>
 *   <li>{@link GameRenderer} — all 2D debug drawing</li>
 * </ul>
 */
public class PlayingState implements GameState {

    private final GameStateManager manager;
    private final GameSettings settings;
    private final GameWorld gameWorld;
    private final int windowWidth;
    private final int windowHeight;

    private final EnemyManager enemyManager;
    private final CombatSystem combat;
    private final GameRenderer renderer;

    private boolean showDebugText;

    public PlayingState(GameStateManager manager, int windowWidth, int windowHeight) {
        this.manager = manager;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        this.settings = GameConfig.loadGameSettings("config/game.properties");
        this.showDebugText = settings.showDebugText;
        this.gameWorld = new GameWorld(settings);

        this.enemyManager = new EnemyManager();
        enemyManager.spawnFromLevel(gameWorld.getCurrentLevel());

        this.combat = new CombatSystem(gameWorld.getCollisionWorld());
        this.renderer = new GameRenderer(gameWorld, windowWidth, windowHeight);
    }

    // ---------------------------------------------------------------
    // GameState implementation
    // ---------------------------------------------------------------

    @Override
    public void enter() {
    }

    @Override
    public void update(double deltaTime, InputHandler input) {
        if (input == null) {
            return;
        }

        // --- Pause ---
        if (input.consumeCancel()) {
            manager.push(new PauseState(manager, windowWidth, windowHeight));
            return;
        }

        // --- Debug toggle ---
        if (input.consumeDebugToggleRequest()) {
            showDebugText = !showDebugText;
        }

        // --- Player ---
        gameWorld.update(input, deltaTime);

        // --- Enemies ---
        enemyManager.update(deltaTime,
                gameWorld.getPlayer(),
                gameWorld.getCollisionWorld(),
                combat);

        // --- Death check ---
        if (combat.isPlayerDead()) {
            manager.push(new GameOverState(manager, windowWidth, windowHeight));
            return;
        }

        // --- Combat (shooting) ---
        combat.update(deltaTime);
        if (input.attack) {
            combat.tryShoot(gameWorld.getPlayer(), enemyManager.getEnemies());
        }
    }

    @Override
    public void render(Graphics2D g2) {
        renderer.render(g2,
                enemyManager.getEnemies(),
                combat.getPlayerHealth(),
                showDebugText,
                settings.gameTitle,
                settings.gameVersion,
                settings.gameBuild);
    }
}
