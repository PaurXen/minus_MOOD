package map;

import math.Bounds2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapData {
    private final List<Vertex> vertices = new ArrayList<>();
    private final List<LineDef> lineDefs = new ArrayList<>();
    private final List<SideDef> sideDefs = new ArrayList<>();
    private final List<Sector> sectors = new ArrayList<>();
    private final List<SpawnPoint> spawnPoints = new ArrayList<>();

    public void addVertex(Vertex vertex) {
        if (vertex != null) {
            vertices.add(vertex);
        }
    }

    public void addLineDef(LineDef lineDef) {
        if (lineDef != null) {
            lineDefs.add(lineDef);
        }
    }

    public void addSideDef(SideDef sideDef) {
        if (sideDef != null) {
            sideDefs.add(sideDef);
        }
    }

    public void addSector(Sector sector) {
        if (sector != null) {
            sectors.add(sector);
        }
    }

    public void addSpawnPoint(SpawnPoint spawnPoint) {
        if (spawnPoint != null) {
            spawnPoints.add(spawnPoint);
        }
    }

    public List<Vertex> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<LineDef> getLineDefs() {
        return Collections.unmodifiableList(lineDefs);
    }

    public List<SideDef> getSideDefs() {
        return Collections.unmodifiableList(sideDefs);
    }

    public List<Sector> getSectors() {
        return Collections.unmodifiableList(sectors);
    }

    public List<SpawnPoint> getSpawnPoints() {
        return Collections.unmodifiableList(spawnPoints);
    }

    public List<LineDef> getCollisionLines() {
        List<LineDef> result = new ArrayList<>();

        for (LineDef lineDef : lineDefs) {
            if (lineDef.blocksMovement() || lineDef.isTrigger()) {
                result.add(lineDef);
            }
        }

        return result;
    }

    public List<LineDef> getRaycastLines() {
        List<LineDef> result = new ArrayList<>();

        for (LineDef lineDef : lineDefs) {
            if (!lineDef.isHidden() && (lineDef.blocksRay() || lineDef.isTransparent())) {
                result.add(lineDef);
            }
        }

        return result;
    }

    public Vertex findVertexById(int id) {
        for (Vertex vertex : vertices) {
            if (vertex.id == id) {
                return vertex;
            }
        }

        return null;
    }

    public LineDef findLineDefById(int id) {
        for (LineDef lineDef : lineDefs) {
            if (lineDef.id == id) {
                return lineDef;
            }
        }

        return null;
    }

    public SideDef findSideDefById(int id) {
        for (SideDef sideDef : sideDefs) {
            if (sideDef.id == id) {
                return sideDef;
            }
        }

        return null;
    }

    public Sector findSectorById(int id) {
        for (Sector sector : sectors) {
            if (sector.id == id) {
                return sector;
            }
        }

        return null;
    }

    public Bounds2D getBounds() {
        if (vertices.isEmpty()) {
            return new Bounds2D(0, 0, 0, 0);
        }

        double minX = vertices.get(0).x;
        double minY = vertices.get(0).y;
        double maxX = vertices.get(0).x;
        double maxY = vertices.get(0).y;

        for (Vertex vertex : vertices) {
            minX = Math.min(minX, vertex.x);
            minY = Math.min(minY, vertex.y);
            maxX = Math.max(maxX, vertex.x);
            maxY = Math.max(maxY, vertex.y);
        }

        return new Bounds2D(minX, minY, maxX, maxY);
    }
}
