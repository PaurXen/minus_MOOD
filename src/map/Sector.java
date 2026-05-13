package map;

public class Sector {
    public final int id;

    public double floorHeight;
    public double ceilingHeight;

    public String floorMaterialId;
    public String ceilingMaterialId;

    public double lightLevel;

    public Sector(int id) {
        this(id, 0, 128, "floor_default", "ceiling_default", 1.0);
    }

    public Sector(
            int id,
            double floorHeight,
            double ceilingHeight,
            String floorMaterialId,
            String ceilingMaterialId,
            double lightLevel
    ) {
        if (ceilingHeight < floorHeight) {
            throw new IllegalArgumentException("Sector ceiling height cannot be below floor height.");
        }

        this.id = id;
        this.floorHeight = floorHeight;
        this.ceilingHeight = ceilingHeight;
        this.floorMaterialId = floorMaterialId;
        this.ceilingMaterialId = ceilingMaterialId;
        this.lightLevel = lightLevel;
    }

    public double getHeight() {
        return ceilingHeight - floorHeight;
    }

    public boolean hasEnoughVerticalSpace(double bodyHeight) {
        return getHeight() >= bodyHeight;
    }
}
