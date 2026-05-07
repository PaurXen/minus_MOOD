package engine;

import config.GameConfig;
import config.GameSettings;
import config.KeyBindings;
import entities.Player;
import input.InputHandler;
import io.Level;
import map.LineDef;
import map.Vertex;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class GamePanel extends JPanel {
    private final GameSettings settings;

    private final InputHandler input;
    private final GameWorld gameWorld;
    private final GameLoop gameLoop;

    private boolean showDebugText;

    public GamePanel(GameSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("GameSettings cannot be null.");
        }

        this.settings = settings;
        this.showDebugText = settings.showDebugText;

        setPreferredSize(new Dimension(settings.windowWidth, settings.windowHeight));
        setBackground(Color.BLACK);
        setFocusable(true);

        KeyBindings bindings = GameConfig.loadControls("config/controls.properties");
        input = new InputHandler(bindings);
        addKeyListener(input);

        gameWorld = new GameWorld(settings);

        gameLoop = new GameLoop(
                settings.targetFPS,
                new GameLoop.Listener() {
                    @Override
                    public void update(double deltaTime) {
                        updateGame(deltaTime);
                    }

                    @Override
                    public void render() {
                        repaint();
                    }
                }
        );
    }

    public void startGameLoop() {
        gameLoop.start();
    }

    public void stopGameLoop() {
        gameLoop.stop();
    }

    private void updateGame(double deltaTime) {
        if (input.consumeDebugToggleRequest()) {
            showDebugText = !showDebugText;
        }

        gameWorld.update(input, deltaTime);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        drawBackground(g2);
        drawMap(g2);
        drawPlayer(g2);

        if (showDebugText) {
            drawDebugInfo(g2);
            drawMapDebug(g2);
        }
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, settings.windowWidth, settings.windowHeight);
    }

    private void drawMap(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        for (LineDef lineDef : gameWorld.getMapLines()) {
            float thickness = 3.0f;

            if (lineDef.getCollisionThickness() > 0) {
                thickness = (float) lineDef.getCollisionThickness();
            }

            if (lineDef.blocksMovement()) {
                g2.setColor(Color.GRAY);
            } else if (lineDef.isTrigger()) {
                g2.setColor(Color.GREEN);
            } else {
                g2.setColor(Color.DARK_GRAY);
            }

            g2.setStroke(new BasicStroke(thickness));

            g2.drawLine(
                    (int) lineDef.start.x,
                    (int) lineDef.start.y,
                    (int) lineDef.end.x,
                    (int) lineDef.end.y
            );
        }

        g2.setStroke(oldStroke);
    }

    private void drawPlayer(Graphics2D g2) {
        Player player = gameWorld.getPlayer();

        if (player == null) {
            return;
        }

        int px = (int) player.getX();
        int py = (int) player.getY();
        int r = (int) player.getRadius();

        g2.setColor(Color.RED);
        g2.fillOval(px - r, py - r, r * 2, r * 2);

        int lineLength = 35;
        int endX = (int) (player.getX() + Math.cos(player.angle) * lineLength);
        int endY = (int) (player.getY() + Math.sin(player.angle) * lineLength);

        g2.setColor(Color.YELLOW);
        g2.drawLine(px, py, endX, endY);
    }

    private void drawDebugInfo(Graphics2D g2) {
        Player player = gameWorld.getPlayer();
        Level level = gameWorld.getCurrentLevel();

        if (player == null || level == null) {
            return;
        }

        g2.setColor(Color.WHITE);

        g2.drawString(
                settings.gameTitle + " v" + settings.gameVersion + " [" + settings.gameBuild + "]",
                20,
                25
        );

        g2.drawString("W/S: forward/backward", 20, 50);
        g2.drawString("A/D: strafe left/right", 20, 70);
        g2.drawString("Left/Right arrows: rotate", 20, 90);

        g2.drawString("Player X: " + String.format("%.2f", player.getX()), 20, 125);
        g2.drawString("Player Y: " + String.format("%.2f", player.getY()), 20, 145);
        g2.drawString("Angle: " + String.format("%.2f", player.angle), 20, 165);

        g2.drawString(
                "Level: " + level.name + " v" + level.version,
                20,
                185
        );

        g2.drawString("Debug toggle: `", 20, 205);

        g2.drawString(
                "Collision body: radius=" + String.format("%.2f", player.getRadius()),
                20,
                225
        );

        g2.drawString(
                "MapData lines: " + gameWorld.getMapData().getLineDefs().size(),
                20,
                245
        );

        g2.drawString(
                "Collision lines: " + gameWorld.getMapData().getCollisionLines().size(),
                20,
                265
        );

        g2.drawString(
                "Raycast lines: " + gameWorld.getMapData().getRaycastLines().size(),
                20,
                285
        );
    }

    private void drawMapDebug(Graphics2D g2) {
        g2.setColor(Color.CYAN);

        for (Vertex vertex : gameWorld.getMapData().getVertices()) {
            int x = (int) vertex.x;
            int y = (int) vertex.y;

            g2.fillOval(x - 3, y - 3, 6, 6);
            g2.drawString(String.valueOf(vertex.id), x + 5, y - 5);
        }

        g2.setColor(Color.ORANGE);

        for (LineDef lineDef : gameWorld.getMapLines()) {
            int midX = (int) ((lineDef.start.x + lineDef.end.x) / 2.0);
            int midY = (int) ((lineDef.start.y + lineDef.end.y) / 2.0);

            g2.drawString("L" + lineDef.id, midX + 4, midY + 4);
        }
    }
}