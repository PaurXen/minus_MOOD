package menu;

import input.InputHandler;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Pause overlay — pushed on top of {@link PlayingState}.
 *
 * <p>Freezes gameplay (blocks updates) but renders on top of the still-visible
 * game view. Navigated with UP/DOWN arrows, confirmed with ENTER.
 *
 * <p>Options:
 * <ul>
 *   <li><b>Resume</b> — pop back to gameplay</li>
 *   <li><b>Options</b> — push the controls reference screen</li>
 *   <li><b>Quit to Menu</b> — pop back to main menu (discards current game)</li>
 * </ul>
 */
public class PauseState implements GameState {

    private static final String[] OPTIONS = { "Resume", "Options", "Quit to Menu" };

    private final GameStateManager manager;
    private final int windowWidth;
    private final int windowHeight;

    private int selectedIndex;
    private boolean upWasDown;
    private boolean downWasDown;

    public PauseState(GameStateManager manager, int windowWidth, int windowHeight) {
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
    }

    @Override
    public void update(double deltaTime, InputHandler input) {
        if (input == null) {
            return;
        }

        // ESC also resumes (same as "Resume" option)
        if (input.consumeCancel()) {
            manager.pop();
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
            case 0 -> manager.pop(); // Resume
            case 1 -> manager.push(new OptionsState(manager, windowWidth, windowHeight));
            case 2 -> {
                // Quit to menu: pop PauseState, then replace PlayingState with MainMenuState
                manager.pop();
                manager.replace(new MainMenuState(manager, windowWidth, windowHeight));
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        // Semi-transparent dark overlay
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, windowWidth, windowHeight);

        // Title
        MenuRenderer.drawTitle(g2, "PAUSED", windowWidth);

        // Options
        MenuRenderer.drawOptions(g2, OPTIONS, selectedIndex, windowWidth);

        // Footer
        String footer = "ESC: resume   UP/DOWN: select   ENTER: confirm";
        MenuRenderer.drawInfoLine(g2, footer, windowHeight - 40, windowWidth);
    }

    /**
     * Blocking: prevents PlayingState from updating (game freezes).
     * Rendering still happens because GameStateManager renders all states bottom-up.
     */
    @Override
    public boolean isBlocking() {
        return true;
    }
}
