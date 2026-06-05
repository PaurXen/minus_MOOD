package render;

import engine.GameWorld;
import map.LineDef;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Renders the level geometry — background and walls/LineDefs.
 */
public class MapRenderer {

    private final GameWorld gameWorld;
    private final int windowWidth;
    private final int windowHeight;

    public MapRenderer(GameWorld gameWorld, int windowWidth, int windowHeight) {
        this.gameWorld = gameWorld;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public void drawBackground(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, windowWidth, windowHeight);
    }

    public void drawMap(Graphics2D g2) {
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
                    (int) lineDef.start.x, (int) lineDef.start.y,
                    (int) lineDef.end.x, (int) lineDef.end.y
            );
        }
        g2.setStroke(oldStroke);
    }

    public GameWorld getGameWorld() {
        return this.gameWorld;
    }
}
