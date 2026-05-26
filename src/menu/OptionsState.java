package menu;

import input.InputHandler;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Controls reference and settings screen.
 *
 * <p>Can be pushed from {@link MainMenuState} or {@link PauseState}.
 * Displays the keyboard control scheme. Future versions
 * may include rebindable keys, volume sliders, etc.
 *
 * <p>Press ESC or select "Back" to return.
 */
public class OptionsState implements GameState {

    private static final String[] CONTROLS_LINES = {
        "W / S          Move forward / backward",
        "A / D          Strafe left / right",
        "LEFT / RIGHT   Rotate view",
        "SPACE          Shoot",
        "` (backtick)   Toggle debug overlay",
        "ESC            Pause / Back",
        "ENTER          Confirm selection",
        "UP / DOWN      Navigate menus"
    };

    private static final String[] OPTIONS = { "Back" };

    private final GameStateManager manager;
    private final int windowWidth;
    private final int windowHeight;

    private boolean backWasDown;

    public OptionsState(GameStateManager manager, int windowWidth, int windowHeight) {
        this.manager = manager;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    @Override
    public void enter() {
        backWasDown = false;
    }

    @Override
    public void update(double deltaTime, InputHandler input) {
        if (input == null) {
            return;
        }

        // ESC to go back
        if (input.consumeCancel()) {
            manager.pop();
            return;
        }

        // Navigate to "Back" and confirm, or just press ENTER anywhere
        if (input.menuDown) {
            if (!backWasDown) {
                backWasDown = true;
                // Only one option, just go back
            }
        } else {
            backWasDown = false;
        }

        if (input.consumeConfirm()) {
            manager.pop();
        }
    }

    @Override
    public void render(Graphics2D g2) {
        // Background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, windowWidth, windowHeight);

        // Title
        MenuRenderer.drawTitle(g2, "OPTIONS", windowWidth);

        // Controls list
        g2.setFont(MenuRenderer.INFO_FONT);
        g2.setColor(Color.WHITE);

        int startY = MenuRenderer.TITLE_Y + 60;
        int lineHeight = 22;

        g2.setColor(Color.GRAY);
        g2.drawString("--- Controls ---", 200, startY);
        startY += lineHeight + 8;

        g2.setColor(Color.WHITE);
        for (int i = 0; i < CONTROLS_LINES.length; i++) {
            g2.drawString(CONTROLS_LINES[i], 200, startY + i * lineHeight);
        }

        // Back option
        int backY = Math.max(startY + CONTROLS_LINES.length * lineHeight + 40,
                MenuRenderer.OPTIONS_START_Y + 120);

        g2.setFont(MenuRenderer.OPTION_FONT);
        g2.setColor(Color.YELLOW);
        g2.drawString("> Back", 200, backY);

        // Footer
        String footer = "ESC or ENTER: return";
        MenuRenderer.drawInfoLine(g2, footer, windowHeight - 40, windowWidth);
    }

}
