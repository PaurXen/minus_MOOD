package menu;

import config.GameConfig;
import config.GameSettings;
import enemies.CombatSystem;
import enemies.EnemyManager;
import engine.GameWorld;
import input.InputHandler;
import render.GameRenderer;
import map.LevelExit;

import java.awt.Graphics2D;

/**
 * Main gameplay state — owns world, enemies, combat, and rendering.
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
    private final String levelPath;

    private boolean showDebugText;

    public PlayingState(GameStateManager manager, int windowWidth, int windowHeight) {
        this(manager, windowWidth, windowHeight, null);
    }

    public PlayingState(
            GameStateManager manager,
            int windowWidth,
            int windowHeight,
            String levelPath
    ) {
        this.manager = manager;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        this.settings = GameConfig.loadGameSettings("config/game.properties");
        this.showDebugText = settings.showDebugText;

        if (levelPath == null || levelPath.trim().isEmpty()) {
            this.levelPath = settings.defaultLevel;
        } else {
            this.levelPath = levelPath;
        }

        this.gameWorld = new GameWorld(settings, this.levelPath);

        this.enemyManager = new EnemyManager();
        enemyManager.spawnFromLevel(gameWorld.getCurrentLevel());

        this.combat = new CombatSystem(gameWorld.getCollisionWorld());
        this.renderer = new GameRenderer(gameWorld, windowWidth, windowHeight);
    }

    @Override
    public void enter() {
    }

    @Override
    public void update(double deltaTime, InputHandler input) {
        if (input == null) {
            return;
        }

        if (input.consumeCancel()) {
            manager.push(new PauseState(manager, windowWidth, windowHeight));
            return;
        }

        if (input.consumeDebugToggleRequest()) {
            showDebugText = !showDebugText;
        }

        gameWorld.update(input, deltaTime);

        enemyManager.update(
                deltaTime,
                gameWorld.getPlayer(),
                gameWorld.getCollisionWorld(),
                combat
        );

        updateLevelExit(input);

        if (combat.isPlayerDead()) {
            manager.push(new GameOverState(manager, windowWidth, windowHeight, levelPath));
            return;
        }

        combat.update(deltaTime);

        if (input.consumeAttackRequest()) {
            renderer.startPlayerShootAnimation();

            combat.tryShoot(
                    gameWorld.getPlayer(),
                    enemyManager.getEnemies()
            );
        }
    }

    @Override
    public void render(Graphics2D g2) {
        renderer.render(
                g2,
                enemyManager.getEnemies(),
                combat.getPlayerHealth(),
                showDebugText,
                settings.gameTitle,
                settings.gameVersion,
                settings.gameBuild
        );
    }

    private void updateLevelExit(InputHandler input) {
        LevelExit levelExit = gameWorld.getLevelExit();

        if (levelExit == null) {
            return;
        }

        boolean allEnemiesDead = enemyManager.getAliveCount() == 0;
        levelExit.setUnlocked(allEnemiesDead);

        if (!input.consumeInteract()) {
            return;
        }

        boolean playerNearExit = levelExit.isInRange(
                gameWorld.getPlayer().getX(),
                gameWorld.getPlayer().getY()
        );

        if (playerNearExit && levelExit.isUnlocked()) {
            manager.replace(new MainMenuState(manager, windowWidth, windowHeight));
        }
    }
}