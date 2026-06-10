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
import java.awt.RenderingHints;

public class GameRenderer {

    private static final double FOV = Math.toRadians(60.0);

    private static final double PLAYER_EYE_HEIGHT = 64.0;
    private static final double ENEMY_FLOOR_HEIGHT = 0.0;

    private static final long DEATH_ANIMATION_MS = 600;

    private BufferedImage weaponSprite;
    private long weaponFireStartTime = -1;

    private static final long WEAPON_FIRE_ANIMATION_MS = 180;

    private final IdentityHashMap<Enemy, Long> deathAnimationStart = new IdentityHashMap<>();
    private final Map<String, BufferedImage> hudFaces = new HashMap<>();

    private final MapRenderer mapRenderer;
    private final DebugRenderer debugRenderer;
    private final RaycastRenderer raycastRenderer = new RaycastRenderer();
    private final GameWorld gameWorld;

    private final int windowWidth;
    private final int windowHeight;

    private final Map<String, BufferedImage> enemySprites = new HashMap<>();

    public GameRenderer(GameWorld gameWorld, int windowWidth, int windowHeight) {
        this.mapRenderer = new MapRenderer(gameWorld, windowWidth, windowHeight);
        this.debugRenderer = new DebugRenderer(gameWorld, windowWidth);
        this.gameWorld = gameWorld;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        loadEnemySprite("demon", "/sprites/demon.png");
        loadEnemySprite("zombieman", "/sprites/zombieman.png");
        loadEnemySprite("imp", "/sprites/imp.png");

        loadHudFace("healthy", "/hud/face_healthy.png");
        loadHudFace("hurt", "/hud/face_hurt.png");
        loadHudFace("critical", "/hud/face_critical.png");
        loadHudFace("dead", "/hud/face_dead.png");
        loadWeaponSprite("/sprites/weapon.png");
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
        drawWeapon(g2);
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
    public void startPlayerShootAnimation() {
        weaponFireStartTime = System.currentTimeMillis();
    }

    private void loadWeaponSprite(String path) {
        try {
            weaponSprite = ImageIO.read(
                    getClass().getResourceAsStream(path)
            );
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            System.err.println("Nie udało się wczytać sprite'a broni: " + path);
        }
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

    private void loadHudFace(String id, String path) {

        try {

            BufferedImage image =
                    ImageIO.read(
                            getClass().getResourceAsStream(path)
                    );

            hudFaces.put(id, image);

        } catch (IOException | IllegalArgumentException | NullPointerException e) {

            System.err.println(
                    "Nie udało się wczytać twarzy HUD: "
                            + path
            );
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

        int spriteWidth = endX - startX;
        int spriteHeight = endY - startY;

        if (spriteWidth <= 0 || spriteHeight <= 0) {
            return;
        }

        float alpha = 1.0f;
        Composite oldComposite = null;

        if (enemy.getState() == EnemyState.DEATH) {
            alpha = (float) Math.max(0.0, 1.0 - deathProgress);
            oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alpha));
        }

        try {
            int srcW = sprite.getWidth();
            int srcH = sprite.getHeight();

            // Draw one column at a time with z-buffer clipping.
            // Each column is a single g.drawImage call — no per-pixel loops.
            for (int x = startX; x < endX; x++) {
                if (x < 0 || x >= windowWidth) continue;
                if (distance > zBuffer[x]) continue;

                // Clip column to screen bounds
                int colStartY = Math.max(startY, 0);
                int colEndY = Math.min(endY, windowHeight);
                if (colEndY <= colStartY) continue;

                double texXRatio = (double) (x - startX) / spriteWidth;
                int texX = (int) (texXRatio * (srcW - 1));

                // Draw one column of the sprite, scaled to fit
                g2.drawImage(sprite,
                        x, colStartY, x + 1, colEndY,       // dest rect
                        texX, 0, texX + 1, srcH,             // source rect
                        null);
            }
        } finally {
            if (oldComposite != null) {
                g2.setComposite(oldComposite);
            }
        }

        drawZombiemanMuzzleFlash(g2, enemy, startX, endX, startY, endY);
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
        int hudHeight = 92;
        int hudY = windowHeight - hudHeight;

        g2.setColor(new Color(35, 35, 35));
        g2.fillRect(0, hudY, windowWidth, hudHeight);

        g2.setColor(new Color(90, 90, 90));
        g2.drawLine(0, hudY, windowWidth, hudY);

        g2.setColor(new Color(15, 15, 15));
        g2.fillRect(18, hudY + 14, 180, 60);

        g2.setColor(new Color(80, 80, 80));
        g2.drawRect(18, hudY + 14, 180, 60);

        int hp = (int) Math.max(0, Math.min(100, playerHealth));

        Color hpColor;
        if (hp > 60) {
            hpColor = new Color(40, 220, 70);
        } else if (hp > 30) {
            hpColor = new Color(230, 190, 40);
        } else {
            hpColor = new Color(230, 40, 30);
        }

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("HEALTH", 32, hudY + 34);

        drawBigNumber(g2, hp, 36, hudY + 66, hpColor);
        g2.setColor(hpColor);
        g2.drawString("%", 128, hudY + 66);

        drawFaceImage(
                g2,
                windowWidth / 2 - 42,
                hudY + 8,
                84,
                76,
                hp
        );

        g2.setColor(new Color(15, 15, 15));
        g2.fillRect(windowWidth - 198, hudY + 14, 180, 60);

        g2.setColor(new Color(80, 80, 80));
        g2.drawRect(windowWidth - 198, hudY + 14, 180, 60);

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("STATUS", windowWidth - 178, hudY + 34);

        if (hp <= 0) {
            g2.setColor(new Color(180, 0, 0));
            g2.drawString("DEAD", windowWidth - 178, hudY + 62);
        } else if (hp < 30) {
            g2.setColor(new Color(255, 60, 40));
            g2.drawString("CRITICAL", windowWidth - 178, hudY + 62);
        } else {
            g2.setColor(new Color(80, 220, 80));
            g2.drawString("OK", windowWidth - 178, hudY + 62);
        }

        if (playerHealth < 30.0) {
            int alpha = (int) (
                    50 + 35 * Math.sin(
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
    private BufferedImage getHudFaceForHealth(int hp) {

        if (hp <= 0) {
            return hudFaces.get("dead");
        }

        if (hp < 25) {
            return hudFaces.get("critical");
        }

        if (hp < 60) {
            return hudFaces.get("hurt");
        }

        return hudFaces.get("healthy");
    }

    private void drawFaceImage(Graphics2D g2,
                               int x,
                               int y,
                               int width,
                               int height,
                               int hp) {

        g2.setColor(new Color(18, 18, 18));
        g2.fillRect(x, y, width, height);

        g2.setColor(new Color(100, 100, 100));
        g2.drawRect(x, y, width, height);

        BufferedImage face = getHudFaceForHealth(hp);

        if (face == null) {

            g2.setColor(Color.RED);
            g2.drawString(
                    "NO FACE",
                    x + 8,
                    y + height / 2
            );

            return;
        }

        g2.drawImage(
                face,
                x + 2,
                y + 2,
                width - 4,
                height - 4,
                null
        );
    }

    private void drawWeapon(Graphics2D g2) {
        if (weaponSprite == null) {
            return;
        }

        long now = System.currentTimeMillis();

        double fireProgress = 1.0;

        if (weaponFireStartTime > 0) {
            fireProgress =
                    (double) (now - weaponFireStartTime)
                            / WEAPON_FIRE_ANIMATION_MS;

            if (fireProgress >= 1.0) {
                fireProgress = 1.0;
                weaponFireStartTime = -1;
            }
        }

        double recoil = 0.0;

        if (fireProgress < 1.0) {
            recoil = Math.sin(fireProgress * Math.PI) * 36.0;
        }

        double weaponScale = 1.2;

        int weaponWidth =
                (int) ((windowWidth / 3.0) * weaponScale);

        int weaponHeight =
                (int) (
                        weaponWidth
                                * ((double) weaponSprite.getHeight()
                                / weaponSprite.getWidth())
                );

        int weaponX = windowWidth / 2 - weaponWidth / 2;
        int weaponY =
                windowHeight
                        - weaponHeight
                        - 20
                        + (int) recoil;

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        g2.drawImage(
                weaponSprite,
                weaponX,
                weaponY,
                weaponWidth,
                weaponHeight,
                null
        );

        if (fireProgress < 0.35) {
            drawWeaponMuzzleFlash(
                    g2,
                    weaponX,
                    weaponY,
                    weaponWidth,
                    weaponHeight
            );
        }
    }

    private void drawWeaponMuzzleFlash(Graphics2D g2,
                                       int weaponX,
                                       int weaponY,
                                       int weaponWidth,
                                       int weaponHeight) {

        int flashSize = weaponWidth / 5;

        int flashX = weaponX + weaponWidth / 2;
        int flashY = weaponY + weaponHeight / 5;

        g2.setColor(new Color(255, 220, 80, 170));
        g2.fillOval(
                flashX - flashSize / 2,
                flashY - flashSize / 2,
                flashSize,
                flashSize
        );

        g2.setColor(new Color(255, 255, 255, 190));
        g2.fillOval(
                flashX - flashSize / 4,
                flashY - flashSize / 4,
                flashSize / 2,
                flashSize / 2
        );
    }

    private void drawBigNumber(Graphics2D g2,
                               int value,
                               int x,
                               int y,
                               Color color) {

        g2.setColor(color);
        g2.setFont(g2.getFont().deriveFont(28.0f));
        g2.drawString(String.valueOf(value), x, y);
        g2.setFont(g2.getFont().deriveFont(12.0f));
    }
}