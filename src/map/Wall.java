package map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Wall {
    public double x;
    public double y;
    public double width;
    public double height;

    public Wall(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle getBounds() {
        return new Rectangle(
                (int) x,
                (int) y,
                (int) width,
                (int) height
        );
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.GRAY);
        g2.fillRect((int) x, (int) y, (int) width, (int) height);

        g2.setColor(Color.DARK_GRAY);
        g2.drawRect((int) x, (int) y, (int) width, (int) height);
    }
}
