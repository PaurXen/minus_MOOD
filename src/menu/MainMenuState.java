package menu;

import java.awt.Color;
import java.awt.Graphics2D;

public class MainMenuState extends AbstractMenuState {

    private static final String[] OPTIONS = { "New Game", "Options", "Quit" };

    public MainMenuState(GameStateManager manager, int windowWidth, int windowHeight) {
        super(manager, windowWidth, windowHeight);
    }

    @Override
    protected int getOptionCount() {
        return OPTIONS.length;
    }

    @Override
    protected void onConfirm(int index) {
        switch (index) {
            case 0 -> manager.replace(new PlayingState(manager, windowWidth, windowHeight));
            case 1 -> manager.push(new OptionsState(manager, windowWidth, windowHeight));
            case 2 -> System.exit(0);
        }
    }

    @Override
    public void render(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, windowWidth, windowHeight);

        MenuRenderer.drawTitle(g2, "minus_MOOD", windowWidth);
        MenuRenderer.drawOptions(g2, OPTIONS, selectedIndex, windowWidth);

        String footer = "UP/DOWN: select   ENTER: confirm";
        MenuRenderer.drawInfoLine(g2, footer, windowHeight - 40, windowWidth);
    }
}
