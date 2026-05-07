package map;

import java.awt.Color;
import java.awt.Graphics2D;

public class LineWall {
    public double x1;
    public double y1;
    public double x2;
    public double y2;
    public double thickness;

    public LineWall(double x1, double y1, double x2, double y2, double thickness) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.thickness = thickness;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.GRAY);

        java.awt.Stroke oldStroke = g2.getStroke();

        g2.setStroke(new java.awt.BasicStroke((float) thickness));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        g2.setStroke(oldStroke);
    }
}
