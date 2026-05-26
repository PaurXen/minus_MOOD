package menu;

import input.InputHandler;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Main menu / title screen.
 *
 * <p>Displays the game title and a simple option list.
 * Navigated with UP/DOWN arrows, confirmed with ENTER.
 *
 * <p>Options:
 * <ul>
 *   <li><b>New Game</b> — transitions to {@link PlayingState}</li>
 *   <li><b>Quit</b> — exits the application</li>
 * </ul>
 */
public class MainMenuState implements GameState {

    private static final String[] OPTIONS = { "New Game", "Options", "Quit" };

    private final GameStateManager manager;
    private final int windowWidth;
    private final int windowHeight;

    private int selectedIndex = 0;

    // Prevent instant repeated input from a single key press
    private boolean upWasDown;
    private boolean downWasDown;

    public MainMenuState(GameStateManager manager, int windowWidth, int windowHeight) {
        this.manager = manager;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    @Override
    public void enter() {
        selectedIndex = 0;
        upWasDown = false;
        downWasDown = false;
    }

    @Override
    public void exit() {
        // nothing to clean up
    }

    @Override
    public void update(double deltaTime, InputHandler input) {
        if (input == null) {
            return;
        }

        // --- Menu navigation (edge-triggered: one press = one move) ---

        if (input.menuUp) {
            if (!upWasDown) {
                selectedIndex = (selectedIndex - 1 + OPTIONS.length) % OPTIONS.length;
                upWasDown = true;
            }
        } else {
            upWasDown = false;
        }

        if (input.menuDown) {
            if (!downWasDown) {
                selectedIndex = (selectedIndex + 1) % OPTIONS.length;
                downWasDown = true;
            }
        } else {
            downWasDown = false;
        }

        // --- Confirm selection (one-shot consume) ---

        if (input.consumeConfirm()) {
            selectOption(selectedIndex);
        }
    }

    private void selectOption(int index) {
        switch (index) {
            case 0 -> {
                // New Game — transition to gameplay
                manager.replace(new PlayingState(manager, windowWidth, windowHeight));
            }
            case 1 -> {
                // Options — push on top of menu
                manager.push(new OptionsState(manager, windowWidth, windowHeight));
            }
            case 2 -> {
                // Quit
                System.exit(0);
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        // Background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, windowWidth, windowHeight);

        // Title
        MenuRenderer.drawTitle(g2, "minus_MOOD", windowWidth);

        // Menu options
        MenuRenderer.drawOptions(g2, OPTIONS, selectedIndex, windowWidth);

        // Footer hint
        String footer = "UP/DOWN: select   ENTER: confirm";
        MenuRenderer.drawInfoLine(g2, footer, windowHeight - 40, windowWidth);
    }
}
