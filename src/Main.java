import config.GameConfig;
import config.GameSettings;
import engine.GamePanel;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        GameSettings settings = GameConfig.loadGameSettings("config/game.properties");

        JFrame frame = new JFrame(
                settings.gameTitle + " v" + settings.gameVersion + " [" + settings.gameBuild + "]"
        );

        GamePanel panel = new GamePanel(settings);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(settings.windowResizable);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        panel.startGameLoop();
    }
}
