package menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Shared drawing utilities for menu screens.
 *
 * <p>All coordinates assume the Swing {@code Graphics2D} convention:
 * origin at top-left of the panel, Y increases downward.
 *
 * <p>This class uses a monospaced font for a retro Doom-like aesthetic.
 * Colors follow the classic red/yellow/white terminal palette.
 */
public final class MenuRenderer {

    private MenuRenderer() {
        // utility class
    }

    /** Title font — large, bold, red. */
    public static final Font TITLE_FONT = new Font("Monospaced", Font.BOLD, 48);

    /** Option text font — medium, plain, white. */
    public static final Font OPTION_FONT = new Font("Monospaced", Font.PLAIN, 24);

    /** Small informational text font. */
    public static final Font INFO_FONT = new Font("Monospaced", Font.PLAIN, 14);

    /** Y-position of the title text. */
    public static final int TITLE_Y = 120;

    /** Y-position of the first menu option. */
    public static final int OPTIONS_START_Y = 300;

    /** Vertical spacing between menu option lines. */
    public static final int OPTION_SPACING = 40;

    // ---------------------------------------------------------------
    // Drawing helpers
    // ---------------------------------------------------------------

    /**
     * Draws the game title centered at {@link #TITLE_Y}.
     *
     * @param g2          graphics context
     * @param title       the title string
     * @param windowWidth width of the window for centering
     */
    public static void drawTitle(Graphics2D g2, String title, int windowWidth) {
        g2.setFont(TITLE_FONT);
        g2.setColor(Color.RED);
        FontMetrics fm = g2.getFontMetrics();
        int x = (windowWidth - fm.stringWidth(title)) / 2;
        g2.drawString(title, x, TITLE_Y);
    }

    /**
     * Draws a vertical list of menu options. The currently selected option is
     * drawn in yellow with a bullet marker; others are drawn in white.
     *
     * @param g2            graphics context
     * @param options       the menu option labels
     * @param selectedIndex index of the currently highlighted option
     * @param windowWidth   width of the window for centering
     * @return the Y coordinate just below the last option (for chaining)
     */
    public static int drawOptions(Graphics2D g2, String[] options,
                                   int selectedIndex, int windowWidth) {
        g2.setFont(OPTION_FONT);

        for (int i = 0; i < options.length; i++) {
            String label = (i == selectedIndex ? "> " : "  ") + options[i];

            g2.setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int x = (windowWidth - fm.stringWidth(label)) / 2;
            int y = OPTIONS_START_Y + i * OPTION_SPACING;

            g2.drawString(label, x, y);
        }

        return OPTIONS_START_Y + options.length * OPTION_SPACING;
    }

    /**
     * Draws an informational line centered on the screen.
     *
     * @param g2          graphics context
     * @param text        the text to draw
     * @param y           Y position
     * @param windowWidth width of the window for centering
     */
    public static void drawInfoLine(Graphics2D g2, String text, int y, int windowWidth) {
        g2.setFont(INFO_FONT);
        g2.setColor(Color.GRAY);
        FontMetrics fm = g2.getFontMetrics();
        int x = (windowWidth - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }
}
