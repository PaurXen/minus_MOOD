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

    private final Map<String, BufferedImage> textures = new HashMap<>();

    public RaycastRenderer() {
        textures.put("wall_default", createBrickTexture());
        textures.put("floor_default", createBrickTexture());
        textures.put("ceiling_default", createBrickTexture());
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
        zBuffer = new double[screenWidth];

        g.setColor(new Color(45, 45, 45));
        g.fillRect(0, 0, screenWidth, screenHeight / 2);

        g.setColor(new Color(25, 25, 25));
        g.fillRect(0, screenHeight / 2, screenWidth, screenHeight / 2);

        for (int x = 0; x < screenWidth; x++) {

            double rayAngle =
                    player.angle
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
                        player.getX(),
                        player.getY(),
                        rayDirX,
                        rayDirY,
                        wall.start.x,
                        wall.start.y,
                        wall.end.x,
                        wall.end.y
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

            double correctedDistance =
                    nearestDistance *
                            Math.cos(rayAngle - player.angle);

            if (correctedDistance < 1.0) {
                correctedDistance = 1.0;
            }

            zBuffer[x] = correctedDistance;

            double wallWorldHeight = 128.0;

            Sector sector = hitWall.frontSector;
            if (sector != null) {
                wallWorldHeight = sector.getHeight();
            }

            int wallHeight =
                    (int) ((wallWorldHeight * screenHeight)
                            / correctedDistance);

            int startY = screenHeight / 2 - wallHeight / 2;
            int endY = startY + wallHeight;

            int shade =
                    Math.max(
                            60,
                            255 - (int) (correctedDistance * 0.08)
                    );

            double hitX =
                    player.getX() + rayDirX * nearestDistance;

            double hitY =
                    player.getY() + rayDirY * nearestDistance;

            BufferedImage texture =
                    getWallTexture(hitWall);

            drawTexturedWallColumn(
                    g,
                    x,
                    startY,
                    endY,
                    screenHeight,
                    hitWall,
                    hitX,
                    hitY,
                    texture,
                    shade
            );
        }
    }

    private BufferedImage getWallTexture(LineDef wall) {
        String materialId = "wall_default";

        if (wall.frontSide != null
                && wall.frontSide.middleMaterialId != null) {
            materialId = wall.frontSide.middleMaterialId;
        }

        return textures.getOrDefault(
                materialId,
                textures.get("wall_default")
        );
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
            BufferedImage texture,
            int shade
    ) {
        if (texture == null) {
            g.setColor(new Color(shade, shade, shade));
            g.drawLine(screenX, startY, screenX, endY);
            return;
        }

        double wallDx = wall.end.x - wall.start.x;
        double wallDy = wall.end.y - wall.start.y;

        double wallLengthSquared =
                wallDx * wallDx + wallDy * wallDy;

        if (wallLengthSquared <= 0.00001) {
            return;
        }

        double hitDx = hitX - wall.start.x;
        double hitDy = hitY - wall.start.y;

        double distanceAlongWall =
                (hitDx * wallDx + hitDy * wallDy)
                        / Math.sqrt(wallLengthSquared);

        double textureWorldSize = 64.0;

        double tiledU =
                (distanceAlongWall / textureWorldSize)
                        - Math.floor(distanceAlongWall / textureWorldSize);

        int texX =
                (int) (tiledU * (texture.getWidth() - 1));

        int visibleStartY = Math.max(startY, 0);
        int visibleEndY = Math.min(endY, screenHeight - 1);

        int projectedHeight = endY - startY;

        if (projectedHeight <= 0) {
            return;
        }

        for (int y = visibleStartY; y <= visibleEndY; y++) {

            double texYRatio =
                    (double) (y - startY) / projectedHeight;

            int texY =
                    (int) (texYRatio * (texture.getHeight() - 1));

            int rgb = texture.getRGB(texX, texY);

            Color texColor = new Color(rgb);

            int r = texColor.getRed() * shade / 255;
            int gr = texColor.getGreen() * shade / 255;
            int b = texColor.getBlue() * shade / 255;

            g.setColor(new Color(r, gr, b));
            g.drawLine(screenX, y, screenX, y);
        }
    }

    private BufferedImage createBrickTexture() {

        int size = 64;

        BufferedImage image =
                new BufferedImage(
                        size,
                        size,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D g = image.createGraphics();

        // tło cegieł
        g.setColor(new Color(130, 45, 35));
        g.fillRect(0, 0, size, size);

        int brickWidth = 16;
        int brickHeight = 4;

        // fugi
        g.setColor(new Color(70, 20, 20));

        for (int y = 0; y < size; y += brickHeight) {

            g.drawLine(0, y, size, y);

            int offset =
                    ((y / brickHeight) % 2 == 0)
                            ? 0
                            : brickWidth / 2;

            for (int x = -offset; x < size; x += brickWidth) {
                g.drawLine(
                        x,
                        y,
                        x,
                        y + brickHeight
                );
            }
        }

        // wnętrze cegieł
        g.setColor(new Color(170, 65, 50));

        for (int y = 1; y < size; y += brickHeight) {

            int offset =
                    ((y / brickHeight) % 2 == 0)
                            ? 0
                            : brickWidth / 2;

            for (int x = -offset + 1; x < size; x += brickWidth) {

                g.fillRect(
                        x,
                        y,
                        brickWidth - 2,
                        brickHeight - 1
                );
            }
        }

        g.dispose();

        return image;
    }

    private double raySegmentIntersection(
            double rx,
            double ry,
            double rdx,
            double rdy,
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        double sdx = x2 - x1;
        double sdy = y2 - y1;

        double denom =
                rdx * sdy - rdy * sdx;

        if (Math.abs(denom) < 0.00001) {
            return -1;
        }

        double t =
                ((x1 - rx) * sdy
                        - (y1 - ry) * sdx)
                        / denom;

        double u =
                ((x1 - rx) * rdy
                        - (y1 - ry) * rdx)
                        / denom;

        if (t >= 0 && u >= 0 && u <= 1) {
            return t;
        }

        return -1;
    }

    private double clamp(
            double value,
            double min,
            double max
    ) {
        return Math.max(min, Math.min(max, value));
    }
}