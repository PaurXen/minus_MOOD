package render;

import enemies.Enemy;
import enemies.EnemyState;
import engine.GameWorld;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class GameRenderer {

    private static final double FOV = Math.toRadians(60.0);

    private static final double PLAYER_EYE_HEIGHT = 64.0;
    private static final double ENEMY_FLOOR_HEIGHT = 0.0;

    private static final long DEATH_ANIMATION_MS = 600;

    private final IdentityHashMap<Enemy, Long> deathAnimationStart = new IdentityHashMap<>();

    private final MapRenderer mapRenderer;
    private final DebugRenderer debugRenderer;
    private final RaycastRenderer raycastRenderer;
    private final GameWorld gameWorld;

    private final int windowWidth;
    private final int windowHeight;

    private final Map<String, BufferedImage> enemySprites = new HashMap<>();

    public GameRenderer(GameWorld gameWorld, int windowWidth, int windowHeight) {
        this.mapRenderer = new MapRenderer(gameWorld, windowWidth, windowHeight);
        this.debugRenderer = new DebugRenderer(gameWorld, windowWidth);
        this.raycastRenderer = new RaycastRenderer();
        this.gameWorld = gameWorld;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        loadEnemySprite("demon", "/sprites/demon.png");
        loadEnemySprite("zombieman", "/sprites/zombieman.png");
        loadEnemySprite("imp", "/sprites/imp.png");
    }

    public void render(Graphics2D g2,
                       List<Enemy> enemies,
                       double playerHealth,
                       boolean showDebugText,
                       String gameTitle,
                       String gameVersion,
                       String gameBuild) {

        raycastRenderer.render(
                g2,
                gameWorld.getPlayer(),
                gameWorld.getMapLines(),
                windowWidth,
                windowHeight
        );

        drawEnemySprites(
                g2,
                enemies,
                raycastRenderer.getZBuffer()
        );

        drawPlayerHUD(g2, playerHealth);

        //if (showDebugText) {
        //    debugRenderer.drawDebugInfo(
        //            g2,
        //            enemies,
        //            playerHealth,
        //            gameTitle,
        //            gameVersion,
        //            gameBuild
        //    );
//
        //    debugRenderer.drawMapDebug(g2);
        //}
    }

    private void loadEnemySprite(String id, String path) {
        try {
            BufferedImage image = ImageIO.read(
                    getClass().getResourceAsStream(path)
            );

            enemySprites.put(id, image);

        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            System.err.println("Nie udało się wczytać sprite'a: " + path);
        }
    }

    private void drawEnemySprites(Graphics2D g2,
                                  List<Enemy> enemies,
                                  double[] zBuffer) {

        if (gameWorld.getPlayer() == null || zBuffer == null) {
            return;
        }

        var player = gameWorld.getPlayer();

        List<Enemy> sortedEnemies = new ArrayList<>(enemies);

        sortedEnemies.sort(
                Comparator.comparingDouble(
                        enemy -> -distanceSquaredToPlayer(enemy)
                )
        );

        for (Enemy enemy : sortedEnemies) {

            double dx = enemy.getX() - player.getX();
            double dy = enemy.getY() - player.getY();

            double rawDistance = Math.sqrt(dx * dx + dy * dy);

            if (rawDistance < 1.0) {
                continue;
            }

            double angleToEnemy = Math.atan2(dy, dx);

            double relativeAngle = normalizeAngle(
                    angleToEnemy - player.angle
            );

            double halfFov = FOV / 2.0;

            if (Math.abs(relativeAngle) > halfFov) {
                continue;
            }

            double correctedDistance =
                    rawDistance * Math.cos(relativeAngle);

            if (correctedDistance < 1.0) {
                continue;
            }

            int screenX = (int) (
                    ((relativeAngle + halfFov) / FOV) * windowWidth
            );

            double deathProgress = getDeathProgress(enemy);

            if (enemy.getState() == EnemyState.DEATH && deathProgress >= 1.0) {
                continue;
            }

            BufferedImage sprite = getSpriteForEnemy(enemy);

            double heightScale = getEnemyHeightScale(enemy);
            double widthScale = getEnemyWidthScale(enemy);

            int spriteHeight = (int) (
                    (enemy.getHeight() * heightScale * windowHeight)
                            / correctedDistance
            );

            double aspectRatio = 1.0;

            if (sprite != null && sprite.getHeight() > 0) {
                aspectRatio =
                        (double) sprite.getWidth()
                                / sprite.getHeight();
            }

            int spriteWidth = (int) (
                    spriteHeight * aspectRatio * widthScale
            );

            int startX = screenX - spriteWidth / 2;
            int endX = screenX + spriteWidth / 2;

            int projectedFloorY = calculateProjectedFloorY(correctedDistance);

            double footPaddingRatio = getEnemyFootPaddingRatio(enemy);
            int footPadding = (int) (spriteHeight * footPaddingRatio);

            int endY = projectedFloorY + footPadding;
            int startY = endY - spriteHeight;

            if (enemy.getState() == EnemyState.DEATH) {
                int originalHeight = endY - startY;

                int sink = (int) (originalHeight * 0.35 * deathProgress);
                int newHeight = (int) (originalHeight * (1.0 - 0.45 * deathProgress));

                endY += sink;
                startY = endY - newHeight;

                int centerX = (startX + endX) / 2;
                int newWidth = (int) ((endX - startX) * (1.0 + 0.25 * deathProgress));

                startX = centerX - newWidth / 2;
                endX = centerX + newWidth / 2;
            }

            if (sprite == null) {
                drawFallbackEnemy(
                        g2,
                        enemy,
                        startX,
                        endX,
                        startY,
                        endY,
                        zBuffer,
                        correctedDistance
                );
            } else {
                drawTexturedEnemy(
                        g2,
                        enemy,
                        sprite,
                        startX,
                        endX,
                        startY,
                        endY,
                        zBuffer,
                        correctedDistance,
                        deathProgress
                );
            }
        }
    }

    private int calculateProjectedFloorY(double correctedDistance) {
        return (int) (
                windowHeight / 2.0
                        + ((PLAYER_EYE_HEIGHT - ENEMY_FLOOR_HEIGHT)
                        * windowHeight)
                        / correctedDistance
        );
    }

    private BufferedImage getSpriteForEnemy(Enemy enemy) {
        String name = enemy.getConfig().name.toLowerCase();

        return switch (name) {
            case "demon" -> enemySprites.get("demon");
            case "zombieman" -> enemySprites.get("zombieman");
            case "imp" -> enemySprites.get("imp");
            default -> null;
        };
    }

    private double getEnemyHeightScale(Enemy enemy) {
        String name = enemy.getConfig().name.toLowerCase();

        return switch (name) {
            case "demon" -> 2.0;
            case "zombieman" -> 1.5;
            case "imp" -> 1.2;
            default -> 1.0;
        };
    }

    private double getEnemyWidthScale(Enemy enemy) {
        String name = enemy.getConfig().name.toLowerCase();

        return switch (name) {
            case "demon" -> 1.5;
            case "zombieman" -> 1.0;
            case "imp" -> 0.7;
            default -> 1.0;
        };
    }

    private double getEnemyFootPaddingRatio(Enemy enemy) {
        String name = enemy.getConfig().name.toLowerCase();

        return switch (name) {
            case "demon" -> 0.10;
            case "zombieman" -> 0.08;
            case "imp" -> 0.06;
            default -> 0.08;
        };
    }

    private void drawTexturedEnemy(Graphics2D g2,
                                   Enemy enemy,
                                   BufferedImage sprite,
                                   int startX,
                                   int endX,
                                   int startY,
                                   int endY,
                                   double[] zBuffer,
                                   double distance,
                                   double deathProgress) {

        Composite oldComposite = g2.getComposite();

        try {
            if (enemy.getState() == EnemyState.DEATH) {
                float alpha = (float) Math.max(0.0, 1.0 - deathProgress);

                g2.setComposite(
                        AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER,
                                alpha
                        )
                );
            }

            int spriteWidth = endX - startX;
            int spriteHeight = endY - startY;

            if (spriteWidth <= 0 || spriteHeight <= 0) {
                return;
            }

            for (int x = startX; x < endX; x++) {

                if (x < 0 || x >= windowWidth) {
                    continue;
                }

                if (distance > zBuffer[x]) {
                    continue;
                }

                double texXRatio = (double) (x - startX) / spriteWidth;
                int texX = (int) (texXRatio * (sprite.getWidth() - 1));

                for (int y = startY; y < endY; y++) {

                    if (y < 0 || y >= windowHeight) {
                        continue;
                    }

                    double texYRatio = (double) (y - startY) / spriteHeight;
                    int texY = (int) (texYRatio * (sprite.getHeight() - 1));

                    int argb = sprite.getRGB(texX, texY);

                    int alpha = (argb >> 24) & 0xff;

                    if (alpha < 10) {
                        continue;
                    }

                    Color pixel = new Color(argb, true);

                    if (enemy.getState() == EnemyState.DEATH) {
                        int red = (int) (
                                pixel.getRed() * (1.0 - deathProgress)
                                        + 255 * deathProgress
                        );

                        int green = (int) (
                                pixel.getGreen() * (1.0 - deathProgress)
                        );

                        int blue = (int) (
                                pixel.getBlue() * (1.0 - deathProgress)
                        );

                        pixel = new Color(
                                Math.min(255, red),
                                Math.max(0, green),
                                Math.max(0, blue),
                                alpha
                        );
                    }

                    g2.setColor(pixel);
                    g2.drawLine(x, y, x, y);
                }
            }

            drawZombiemanMuzzleFlash(
                    g2,
                    enemy,
                    startX,
                    endX,
                    startY,
                    endY
            );

        } finally {
            g2.setComposite(oldComposite);
        }
    }

    private void drawZombiemanMuzzleFlash(Graphics2D g2,
                                          Enemy enemy,
                                          int startX,
                                          int endX,
                                          int startY,
                                          int endY) {

        if (!enemy.getConfig().name.equalsIgnoreCase("Zombieman")
                || enemy.getState() != EnemyState.ATTACK) {
            return;
        }

        long now = System.currentTimeMillis();
        long flashCycle = now % 500;

        if (flashCycle >= 250) {
            return;
        }

        int spriteHeight = endY - startY;
        int flashSize = Math.max(12, spriteHeight * 4 / 5);

        int flashX = (startX + endX) / 2 + flashSize / 3;
        int flashY = startY + spriteHeight / 3;

        g2.setColor(new Color(255, 220, 80, 153));
        g2.fillOval(
                flashX - flashSize / 2,
                flashY - flashSize / 2,
                flashSize,
                flashSize
        );

        g2.setColor(new Color(255, 255, 255, 153));
        g2.fillOval(
                flashX - flashSize / 4,
                flashY - flashSize / 4,
                flashSize / 2,
                flashSize / 2
        );
    }

    private void drawFallbackEnemy(Graphics2D g2,
                                   Enemy enemy,
                                   int startX,
                                   int endX,
                                   int startY,
                                   int endY,
                                   double[] zBuffer,
                                   double distance) {

        for (int x = startX; x < endX; x++) {

            if (x < 0 || x >= windowWidth) {
                continue;
            }

            if (distance > zBuffer[x]) {
                continue;
            }

            g2.setColor(getEnemyColor(enemy));
            g2.drawLine(x, startY, x, endY);
        }
    }

    private Color getEnemyColor(Enemy enemy) {
        String name = enemy.getConfig().name.toLowerCase();

        return switch (name) {
            case "demon" -> new Color(170, 40, 30);
            case "zombieman" -> new Color(90, 130, 70);
            case "imp" -> new Color(150, 85, 45);
            default -> Color.RED;
        };
    }

    private double distanceSquaredToPlayer(Enemy enemy) {
        var player = gameWorld.getPlayer();

        double dx = enemy.getX() - player.getX();
        double dy = enemy.getY() - player.getY();

        return dx * dx + dy * dy;
    }

    private double normalizeAngle(double angle) {
        while (angle < -Math.PI) {
            angle += Math.PI * 2.0;
        }

        while (angle > Math.PI) {
            angle -= Math.PI * 2.0;
        }

        return angle;
    }

    private double getDeathProgress(Enemy enemy) {
        if (enemy.getState() != EnemyState.DEATH) {
            deathAnimationStart.remove(enemy);
            return 0.0;
        }

        long now = System.currentTimeMillis();

        deathAnimationStart.putIfAbsent(enemy, now);

        long start = deathAnimationStart.get(enemy);
        double progress = (double) (now - start) / DEATH_ANIMATION_MS;

        return Math.max(0.0, Math.min(1.0, progress));
    }

    private void drawPlayerHUD(Graphics2D g2, double playerHealth) {
        int barX = 20;
        int barY = windowHeight - 40;
        int barWidth = 200;
        int barHeight = 16;

        EntityRenderer.fillHealthBar(
                g2,
                barX,
                barY,
                barWidth,
                barHeight,
                playerHealth / 100.0
        );

        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);

        String healthText = String.format("HP: %.0f / 100", playerHealth);
        g2.drawString(healthText, barX, barY - 4);

        if (playerHealth < 30.0) {
            int alpha = (int) (
                    60 + 40 * Math.sin(
                            System.nanoTime() / 200_000_000.0
                    )
            );

            g2.setColor(
                    new Color(
                            255,
                            0,
                            0,
                            Math.min(alpha, 80)
                    )
            );

            g2.fillRect(0, 0, windowWidth, windowHeight);
        }
    }
}