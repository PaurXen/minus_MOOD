package menu;

import java.awt.Color;
import java.awt.Graphics2D;

public class GameOverState extends AbstractMenuState {

    private static final String[] OPTIONS = { "Retry", "Main Menu" };

    public GameOverState(GameStateManager manager, int windowWidth, int windowHeight) {
        super(manager, windowWidth, windowHeight);
    }

    @Override
    protected int getOptionCount() {
        return OPTIONS.length;
    }

    @Override
    protected void onConfirm(int index) {
        switch (index) {
            case 0 -> {
                manager.pop();
                manager.replace(new PlayingState(manager, windowWidth, windowHeight));
            }
            case 1 -> {
                manager.pop();
                manager.replace(new MainMenuState(manager, windowWidth, windowHeight));
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        g2.setColor(new Color(139, 0, 0, 180));
        g2.fillRect(0, 0, windowWidth, windowHeight);

        MenuRenderer.drawTitle(g2, "YOU DIED", windowWidth);
        MenuRenderer.drawOptions(g2, OPTIONS, selectedIndex, windowWidth);

        String footer = "UP/DOWN: select   ENTER: confirm";
        MenuRenderer.drawInfoLine(g2, footer, windowHeight - 40, windowWidth);
    }
}
