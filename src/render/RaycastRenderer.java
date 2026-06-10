package render;

import entities.Player;
import map.LineDef;
import map.Sector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaycastRenderer {

    private static final double FOV = Math.toRadians(60.0);

    private double[] zBuffer;
    private int[] columnPixels;
    private BufferedImage columnBuffer;
    private int lastScreenWidth;
    private int lastScreenHeight;

    private final Map<String, BufferedImage> textures = new HashMap<>();

    public RaycastRenderer() {
        textures.put("wall_default", createBrickTexture());
        textures.put("floor_default", textures.get("wall_default"));
        textures.put("ceiling_default", textures.get("wall_default"));
    }

    public double[] getZBuffer() {
        return zBuffer;
    }

    public void render(
            Graphics2D g,
            Player player,
            List<LineDef> walls,
            int screenWidth,
            int screenHeight
    ) {
        ensureBuffers(screenWidth, screenHeight);

        g.setColor(new Color(45, 45, 45));
        g.fillRect(0, 0, screenWidth, screenHeight / 2);

        g.setColor(new Color(25, 25, 25));
        g.fillRect(0, screenHeight / 2, screenWidth, screenHeight / 2);

        for (int x = 0; x < screenWidth; x++) {

            double rayAngle = player.angle
                    - FOV / 2.0
                    + ((double) x / screenWidth) * FOV;

            double rayDirX = Math.cos(rayAngle);
            double rayDirY = Math.sin(rayAngle);

            double nearestDistance = Double.MAX_VALUE;
            LineDef hitWall = null;

            for (LineDef wall : walls) {
                if (!wall.blocksRay()) {
                    continue;
                }

                double hit = raySegmentIntersection(
                        player.getX(), player.getY(),
                        rayDirX, rayDirY,
                        wall.start.x, wall.start.y,
                        wall.end.x, wall.end.y
                );

                if (hit > 0 && hit < nearestDistance) {
                    nearestDistance = hit;
                    hitWall = wall;
                }
            }

            if (hitWall == null) {
                zBuffer[x] = Double.MAX_VALUE;
                continue;
            }

            double correctedDistance = nearestDistance
                    * Math.cos(rayAngle - player.angle);

            if (correctedDistance < 1.0) {
                correctedDistance = 1.0;
            }

            zBuffer[x] = correctedDistance;

            double wallWorldHeight = 128.0;
            Sector sector = hitWall.frontSector;
            if (sector != null) {
                wallWorldHeight = sector.getHeight();
            }

            int wallHeight = (int) ((wallWorldHeight * screenHeight)
                    / correctedDistance);

            int startY = screenHeight / 2 - wallHeight / 2;
            int endY = startY + wallHeight;

            int shade = Math.max(60,
                    255 - (int) (correctedDistance * 0.08));

            double hitX = player.getX() + rayDirX * nearestDistance;
            double hitY = player.getY() + rayDirY * nearestDistance;

            drawTexturedWallColumn(
                    g, x, startY, endY, screenHeight,
                    hitWall, hitX, hitY, shade
            );
        }
    }

    private void ensureBuffers(int screenWidth, int screenHeight) {
        if (zBuffer == null || zBuffer.length != screenWidth) {
            zBuffer = new double[screenWidth];
            lastScreenWidth = screenWidth;
        }
        if (columnPixels == null || columnPixels.length < screenHeight) {
            columnPixels = new int[screenHeight];
        }
        if (columnBuffer == null
                || columnBuffer.getHeight() < screenHeight
                || columnBuffer.getWidth() < 1) {
            columnBuffer = new BufferedImage(
                    1, screenHeight, BufferedImage.TYPE_INT_RGB);
        }
        lastScreenHeight = screenHeight;
    }

    private BufferedImage getWallTexture(LineDef wall) {
        String materialId = "wall_default";
        if (wall.frontSide != null
                && wall.frontSide.middleMaterialId != null) {
            materialId = wall.frontSide.middleMaterialId;
        }
        return textures.getOrDefault(materialId, textures.get("wall_default"));
    }

    private void drawTexturedWallColumn(
            Graphics2D g,
            int screenX,
            int startY,
            int endY,
            int screenHeight,
            LineDef wall,
            double hitX,
            double hitY,
            int shade
    ) {
        BufferedImage texture = getWallTexture(wall);

        if (texture == null) {
            g.setColor(new Color(shade, shade, shade));
            g.drawLine(screenX, startY, screenX, endY);
            return;
        }

        double wallDx = wall.end.x - wall.start.x;
        double wallDy = wall.end.y - wall.start.y;
        double wallLength = Math.sqrt(wallDx * wallDx + wallDy * wallDy);

        if (wallLength <= 0.00001) {
            return;
        }

        double hitDx = hitX - wall.start.x;
        double hitDy = hitY - wall.start.y;
        double distanceAlongWall = (hitDx * wallDx + hitDy * wallDy) / wallLength;

        double textureWorldSize = 64.0;
        double tiledU = (distanceAlongWall / textureWorldSize)
                - Math.floor(distanceAlongWall / textureWorldSize);

        int texX = (int) (tiledU * (texture.getWidth() - 1));
        int texWidth = texture.getWidth();
        int texHeight = texture.getHeight();

        int visibleStartY = Math.max(startY, 0);
        int visibleEndY = Math.min(endY, screenHeight - 1);
        int projectedHeight = endY - startY;

        if (projectedHeight <= 0 || visibleEndY < visibleStartY) {
            return;
        }

        int colHeight = visibleEndY - visibleStartY + 1;

        // Bit-shift shade factor
        int sfR = shade;
        int sfG = shade;
        int sfB = shade;

        // Pre-fetch texture row to avoid per-pixel getRGB overhead
        // Build the column in one pass with raw int manipulation
        for (int i = 0; i < colHeight; i++) {
            double texYRatio = (double) (visibleStartY + i - startY) / projectedHeight;
            int texY = (int) (texYRatio * (texHeight - 1));

            int rgb = texture.getRGB(texX, texY);

            int r = (((rgb >> 16) & 0xFF) * sfR) / 255;
            int gr = (((rgb >> 8) & 0xFF) * sfG) / 255;
            int b = ((rgb & 0xFF) * sfB) / 255;

            columnPixels[i] = (r << 16) | (gr << 8) | b;
        }

        // Draw entire column in one native call
        columnBuffer.setRGB(0, 0, 1, colHeight, columnPixels, 0, 1);
        g.drawImage(columnBuffer,
                screenX, visibleStartY,
                screenX + 1, visibleEndY + 1,
                0, 0, 1, colHeight,
                null);
    }

    private BufferedImage createBrickTexture() {
        int size = 64;
        BufferedImage image = new BufferedImage(
                size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(new Color(130, 45, 35));
        g.fillRect(0, 0, size, size);

        int brickWidth = 16;
        int brickHeight = 4;

        g.setColor(new Color(70, 20, 20));
        for (int y = 0; y < size; y += brickHeight) {
            g.drawLine(0, y, size, y);
            int offset = ((y / brickHeight) % 2 == 0) ? 0 : brickWidth / 2;
            for (int x = -offset; x < size; x += brickWidth) {
                g.drawLine(x, y, x, y + brickHeight);
            }
        }

        g.setColor(new Color(170, 65, 50));
        for (int y = 1; y < size; y += brickHeight) {
            int offset = ((y / brickHeight) % 2 == 0) ? 0 : brickWidth / 2;
            for (int x = -offset + 1; x < size; x += brickWidth) {
                g.fillRect(x, y, brickWidth - 2, brickHeight - 1);
            }
        }

        g.dispose();
        return image;
    }

    private double raySegmentIntersection(
            double rx, double ry,
            double rdx, double rdy,
            double x1, double y1,
            double x2, double y2
    ) {
        double sdx = x2 - x1;
        double sdy = y2 - y1;

        double denom = rdx * sdy - rdy * sdx;
        if (Math.abs(denom) < 0.00001) {
            return -1;
        }

        double t = ((x1 - rx) * sdy - (y1 - ry) * sdx) / denom;
        double u = ((x1 - rx) * rdy - (y1 - ry) * rdx) / denom;

        if (t >= 0 && u >= 0 && u <= 1) {
            return t;
        }
        return -1;
    }
}
