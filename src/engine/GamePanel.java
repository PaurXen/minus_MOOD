package engine;

import config.GameConfig;
import config.GameSettings;
import config.KeyBindings;
import input.InputHandler;
import menu.GameStateManager;
import menu.MainMenuState;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Main game panel — the Swing component that hosts the game.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Creates and manages the {@link InputHandler} (Swing KeyListener).</li>
 *   <li>Creates the {@link GameStateManager} and pushes the initial state.</li>
 *   <li>Runs the {@link GameLoop} which delegates update/render to the state manager.</li>
 *   <li>Renders the active game state via {@code paintComponent}.</li>
 * </ul>
 *
 * <p>Gameplay logic lives in {@code menu.PlayingState}, not here.
 * Menu logic lives in {@code menu.MainMenuState} and similar classes.
 * This class is a thin shell that wires Swing to the state machine.
 */
public class GamePanel extends JPanel {

    private final GameSettings settings;

    private final InputHandler input;
    private final GameStateManager stateManager;
    private final GameLoop gameLoop;

    public GamePanel(GameSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("GameSettings cannot be null.");
        }

        this.settings = settings;

        // Swing setup
        setPreferredSize(new Dimension(settings.windowWidth, settings.windowHeight));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Input
        KeyBindings bindings = GameConfig.loadControls("config/controls.properties");
        input = new InputHandler(bindings);
        addKeyListener(input);

        // State machine — start at the main menu
        stateManager = new GameStateManager();
        stateManager.push(new MainMenuState(stateManager, settings.windowWidth, settings.windowHeight));

        // Game loop delegates everything to the state manager
        gameLoop = new GameLoop(
                settings.targetFPS,
                new GameLoop.Listener() {
                    @Override
                    public void update(double deltaTime) {
                        stateManager.update(deltaTime, input);
                    }

                    @Override
                    public void render() {
                        repaint();
                    }
                }
        );
    }

    public void startGameLoop() {
        gameLoop.start();
    }

    public void stopGameLoop() {
        gameLoop.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        stateManager.render(g2);
    }
}
