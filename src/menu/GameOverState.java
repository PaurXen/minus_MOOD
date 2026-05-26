package menu;

import input.InputHandler;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Death screen — shown when the player dies.
 *
 * <p>Options:
 * <ul>
 *   <li><b>Retry</b> — restart the level with a fresh {@link PlayingState}</li>
 *   <li><b>Main Menu</b> — return to the title screen</li>
 * </ul>
 */
public class GameOverState implements GameState {

    private static final String[] OPTIONS = { "Retry", "Main Menu" };

    private final GameStateManager manager;
    private final int windowWidth;
    private final int windowHeight;

    private int selectedIndex;
    private boolean upWasDown;
    private boolean downWasDown;

    public GameOverState(GameStateManager manager, int windowWidth, int windowHeight) {
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
    public void update(double deltaTime, InputHandler input) {
        if (input == null) {
            return;
        }

        // --- Navigation (edge-triggered) ---

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

        // --- Confirm ---

        if (input.consumeConfirm()) {
            selectOption(selectedIndex);
        }
    }

    private void selectOption(int index) {
        switch (index) {
            case 0 -> {
                // Retry: pop GameOverState, replace PlayingState with a fresh one
                manager.pop();
                manager.replace(new PlayingState(manager, windowWidth, windowHeight));
            }
            case 1 -> {
                // Main Menu: pop GameOverState, replace PlayingState with MainMenuState
                manager.pop();
                manager.replace(new MainMenuState(manager, windowWidth, windowHeight));
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        // Blood-red overlay
        g2.setColor(new Color(139, 0, 0, 180));
        g2.fillRect(0, 0, windowWidth, windowHeight);

        // Title
        MenuRenderer.drawTitle(g2, "YOU DIED", windowWidth);

        // Options
        MenuRenderer.drawOptions(g2, OPTIONS, selectedIndex, windowWidth);

        // Footer
        String footer = "UP/DOWN: select   ENTER: confirm";
        MenuRenderer.drawInfoLine(g2, footer, windowHeight - 40, windowWidth);
    }

}
