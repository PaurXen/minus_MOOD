package menu;

import input.InputHandler;

import java.awt.Color;
import java.awt.Graphics2D;

public class PauseState extends AbstractMenuState {

    private static final String[] OPTIONS = { "Resume", "Options", "Quit to Menu" };

    public PauseState(GameStateManager manager, int windowWidth, int windowHeight) {
        super(manager, windowWidth, windowHeight);
    }

    @Override
    protected int getOptionCount() {
        return OPTIONS.length;
    }

    @Override
    protected boolean handleCancel(InputHandler input) {
        if (input.consumeCancel()) {
            manager.pop(); // ESC = resume
            return true;
        }
        return false;
    }

    @Override
    protected void onConfirm(int index) {
        switch (index) {
            case 0 -> manager.pop(); // Resume
            case 1 -> manager.push(new OptionsState(manager, windowWidth, windowHeight));
            case 2 -> {
                manager.pop();
                manager.replace(new MainMenuState(manager, windowWidth, windowHeight));
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, windowWidth, windowHeight);

        MenuRenderer.drawTitle(g2, "PAUSED", windowWidth);
        MenuRenderer.drawOptions(g2, OPTIONS, selectedIndex, windowWidth);

        String footer = "ESC: resume   UP/DOWN: select   ENTER: confirm";
        MenuRenderer.drawInfoLine(g2, footer, windowHeight - 40, windowWidth);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
