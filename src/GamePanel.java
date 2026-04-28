import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 900;
    public static final int HEIGHT = 600;

    private Thread gameThread;
    private boolean running = false;

    private InputHandler input = new InputHandler();
    private Player player = new Player(120, 120, 0);

    private ArrayList<Wall> walls = new ArrayList<>();
    private ArrayList<LineWall> lineWalls = new ArrayList<>();

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(input);

        createTestMap();
    }

    private void createTestMap() {
        walls.add(new Wall(250, 100, 250, 40));
        walls.add(new Wall(250, 300, 250, 40));
        walls.add(new Wall(100, 200, 60, 250));
        walls.add(new Wall(600, 150, 60, 300));

        // Tilted test walls
        lineWalls.add(new LineWall(300, 420, 520, 520, 18));
        lineWalls.add(new LineWall(650, 80, 800, 230, 18));

        // Border walls
        walls.add(new Wall(0, 0, WIDTH, 20));
        walls.add(new Wall(0, HEIGHT - 20, WIDTH, 20));
        walls.add(new Wall(0, 0, 20, HEIGHT));
        walls.add(new Wall(WIDTH - 20, 0, 20, HEIGHT));
    }

    public void startGameLoop() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        final double targetFPS = 60.0;
        final double drawInterval = 1_000_000_000.0 / targetFPS;

        long lastTime = System.nanoTime();
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / drawInterval;
            lastTime = now;

            while (delta >= 1) {
                update(1.0 / targetFPS);
                delta--;
            }

            repaint();

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update(double deltaTime) {
        handleRotation(deltaTime);
        handleMovement(deltaTime);
    }

    private void handleRotation(double deltaTime) {
        if (input.rotateLeft) {
            player.angle -= player.rotationSpeed * deltaTime;
        }

        if (input.rotateRight) {
            player.angle += player.rotationSpeed * deltaTime;
        }
    }

    private void handleMovement(double deltaTime) {
        double moveX = 0;
        double moveY = 0;

        double forwardX = Math.cos(player.angle);
        double forwardY = Math.sin(player.angle);

        double rightX = Math.cos(player.angle + Math.PI / 2);
        double rightY = Math.sin(player.angle + Math.PI / 2);

        if (input.forward) {
            moveX += forwardX;
            moveY += forwardY;
        }

        if (input.backward) {
            moveX -= forwardX;
            moveY -= forwardY;
        }

        if (input.strafeLeft) {
            moveX -= rightX;
            moveY -= rightY;
        }

        if (input.strafeRight) {
            moveX += rightX;
            moveY += rightY;
        }

        double length = Math.sqrt(moveX * moveX + moveY * moveY);

        if (length > 0) {
            moveX /= length;
            moveY /= length;

            double speed = player.moveSpeed * deltaTime;

            movePlayer(moveX * speed, moveY * speed);
        }
    }
    private void movePlayer(double dx, double dy) {
        double newX = player.x + dx;
        double newY = player.y + dy;

        if (!collidesWithWall(newX, player.y)) {
            player.x = newX;
        }

        if (!collidesWithWall(player.x, newY)) {
            player.y = newY;
        }
    }

    private double distancePointToSegment(
            double px,
            double py,
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        double segmentX = x2 - x1;
        double segmentY = y2 - y1;

        double pointX = px - x1;
        double pointY = py - y1;

        double segmentLengthSquared = segmentX * segmentX + segmentY * segmentY;

        if (segmentLengthSquared == 0) {
            double dx = px - x1;
            double dy = py - y1;
            return Math.sqrt(dx * dx + dy * dy);
        }

        double t = (pointX * segmentX + pointY * segmentY) / segmentLengthSquared;

        t = clamp(t, 0, 1);

        double closestX = x1 + t * segmentX;
        double closestY = y1 + t * segmentY;

        double dx = px - closestX;
        double dy = py - closestY;

        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean collidesWithLineWall(double x, double y) {
        for (LineWall wall : lineWalls) {
            double distance = distancePointToSegment(
                    x,
                    y,
                    wall.x1,
                    wall.y1,
                    wall.x2,
                    wall.y2
            );

            double collisionDistance = player.radius + wall.thickness / 2.0;

            if (distance < collisionDistance) {
                return true;
            }
        }

        return false;
    }

    private boolean collidesWithWall(double x, double y) {
        for (Wall wall : walls) {
            if (circleIntersectsRectangle(
                    x,
                    y,
                    player.radius,
                    wall.x,
                    wall.y,
                    wall.width,
                    wall.height
            )) {
                return true;
            }
        }

        if (collidesWithLineWall(x, y)) {
            return true;
        }

        return false;
    }

    private boolean circleIntersectsRectangle(
            double circleX,
            double circleY,
            double radius,
            double rectX,
            double rectY,
            double rectWidth,
            double rectHeight
    ) {
        double closestX = clamp(circleX, rectX, rectX + rectWidth);
        double closestY = clamp(circleY, rectY, rectY + rectHeight);

        double distanceX = circleX - closestX;
        double distanceY = circleY - closestY;

        double distanceSquared = distanceX * distanceX + distanceY * distanceY;

        return distanceSquared < radius * radius;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        drawBackground(g2);
        drawWalls(g2);
        drawPlayer(g2);
        drawDebugInfo(g2);
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawWalls(Graphics2D g2) {
        for (Wall wall : walls) {
            wall.draw(g2);
        }

        for (LineWall lineWall : lineWalls) {
            lineWall.draw(g2);
        }
    }

    private void drawPlayer(Graphics2D g2) {
        int px = (int) player.x;
        int py = (int) player.y;
        int r = (int) player.radius;

        // Player circle
        g2.setColor(Color.RED);
        g2.fillOval(px - r, py - r, r * 2, r * 2);

        // Direction line
        int lineLength = 35;
        int endX = (int) (player.x + Math.cos(player.angle) * lineLength);
        int endY = (int) (player.y + Math.sin(player.angle) * lineLength);

        g2.setColor(Color.YELLOW);
        g2.drawLine(px, py, endX, endY);
    }

    private void drawDebugInfo(Graphics2D g2) {
        g2.setColor(Color.WHITE);

        g2.drawString("W/S: forward/backward", 20, 25);
        g2.drawString("A/D: strafe left/right", 20, 45);
        g2.drawString("Left/Right arrows: rotate", 20, 65);

        g2.drawString("Player X: " + String.format("%.2f", player.x), 20, 100);
        g2.drawString("Player Y: " + String.format("%.2f", player.y), 20, 120);
        g2.drawString("Angle: " + String.format("%.2f", player.angle), 20, 140);
    }
}