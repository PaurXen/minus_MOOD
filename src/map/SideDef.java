package map;

public class SideDef {
    public final int id;

    public String materialId;
    public String upperMaterialId;
    public String middleMaterialId;
    public String lowerMaterialId;

    public double xOffset;
    public double yOffset;

    public boolean transparent;

    public SideDef(int id, String materialId) {
        this.id = id;
        this.materialId = materialId;

        this.upperMaterialId = materialId;
        this.middleMaterialId = materialId;
        this.lowerMaterialId = materialId;

        this.xOffset = 0;
        this.yOffset = 0;

        this.transparent = false;
    }

    public SideDef(
            int id,
            String upperMaterialId,
            String middleMaterialId,
            String lowerMaterialId,
            double xOffset,
            double yOffset,
            boolean transparent
    ) {
        this.id = id;

        this.upperMaterialId = upperMaterialId;
        this.middleMaterialId = middleMaterialId;
        this.lowerMaterialId = lowerMaterialId;

        this.materialId = middleMaterialId;

        this.xOffset = xOffset;
        this.yOffset = yOffset;

        this.transparent = transparent;
    }
}
