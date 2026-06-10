package menu;

import input.InputHandler;

import java.awt.Color;
import java.awt.Graphics2D;

public class LevelSelectState extends AbstractMenuState {
    private final LevelEntry[] levels;
    private final String[] options;

    public LevelSelectState(GameStateManager manager, int windowWidth, int windowHeight) {
        super(manager, windowWidth, windowHeight);

        this.levels = LevelCatalog.getLevels();
        this.options = new String[levels.length + 1];

        for (int i = 0; i < levels.length; i++) {
            options[i] = levels[i].getName();
        }

        options[options.length - 1] = "Back";
    }

    @Override
    protected int getOptionCount() {
        return options.length;
    }

    @Override
    protected boolean handleCancel(InputHandler input) {
        if (input.consumeCancel()) {
            manager.pop();
            return true;
        }

        return false;
    }

    @Override
    protected void onConfirm(int index) {
        if (index == options.length - 1) {
            manager.pop();
            return;
        }

        LevelEntry selectedLevel = levels[index];

        manager.replace(
                new PlayingState(
                        manager,
                        windowWidth,
                        windowHeight,
                        selectedLevel.getPath()
                )
        );
    }

    @Override
    public void render(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, windowWidth, windowHeight);

        MenuRenderer.drawTitle(g2, "SELECT LEVEL", windowWidth);
        MenuRenderer.drawOptions(g2, options, selectedIndex, windowWidth);

        if (selectedIndex < levels.length) {
            String description = levels[selectedIndex].getDescription();
            MenuRenderer.drawInfoLine(g2, description, windowHeight - 70, windowWidth);
        }

        MenuRenderer.drawInfoLine(
                g2,
                "UP/DOWN: select  ENTER: confirm  ESC: back",
                windowHeight - 40,
                windowWidth
        );
    }
}