package map;

import math.Bounds2D;
import math.Segment2D;
import math.Vec2;

import java.util.EnumSet;
import java.util.Set;

public class LineDef {
    public final int id;

    public final Vertex start;
    public final Vertex end;

    public Sector frontSector;
    public Sector backSector;

    public SideDef frontSide;
    public SideDef backSide;

    private final EnumSet<LineFlag> flags;

    // Temporary support for legacy LineWall thickness.
    // For final LineDef maps this should usually stay 0.
    private double collisionThickness;

    public LineDef(
            int id,
            Vertex start,
            Vertex end,
            Sector frontSector,
            Sector backSector,
            SideDef frontSide,
            SideDef backSide,
            Set<LineFlag> flags
    ) {
        if (start == null) {
            throw new IllegalArgumentException("LineDef start vertex cannot be null.");
        }

        if (end == null) {
            throw new IllegalArgumentException("LineDef end vertex cannot be null.");
        }

        this.id = id;
        this.start = start;
        this.end = end;

        this.frontSector = frontSector;
        this.backSector = backSector;

        this.frontSide = frontSide;
        this.backSide = backSide;

        if (flags == null || flags.isEmpty()) {
            this.flags = EnumSet.noneOf(LineFlag.class);
        } else {
            this.flags = EnumSet.copyOf(flags);
        }

        this.collisionThickness = 0;
    }

    public LineDef(int id, Vertex start, Vertex end) {
        this(
                id,
                start,
                end,
                null,
                null,
                null,
                null,
                EnumSet.noneOf(LineFlag.class)
        );
    }

    public Vec2 getStartPosition() {
        return start.getPosition();
    }

    public Vec2 getEndPosition() {
        return end.getPosition();
    }

    public Segment2D getSegment() {
        return new Segment2D(getStartPosition(), getEndPosition());
    }

    public Bounds2D getBounds() {
        return getSegment().getBounds();
    }

    public double length() {
        return getSegment().length();
    }

    public boolean hasFlag(LineFlag flag) {
        return flags.contains(flag);
    }

    public void addFlag(LineFlag flag) {
        flags.add(flag);
    }

    public void removeFlag(LineFlag flag) {
        flags.remove(flag);
    }

    public Set<LineFlag> getFlags() {
        return EnumSet.copyOf(flags);
    }

    public boolean blocksMovement() {
        return hasFlag(LineFlag.BLOCKS_MOVEMENT);
    }

    public boolean blocksRay() {
        return hasFlag(LineFlag.BLOCKS_RAY);
    }

    public boolean blocksProjectile() {
        return hasFlag(LineFlag.BLOCKS_PROJECTILE);
    }

    public boolean isTransparent() {
        return hasFlag(LineFlag.TRANSPARENT);
    }

    public boolean isTrigger() {
        return hasFlag(LineFlag.TRIGGER);
    }

    public boolean isTwoSided() {
        return hasFlag(LineFlag.TWO_SIDED) || backSector != null;
    }

    public boolean isHidden() {
        return hasFlag(LineFlag.HIDDEN);
    }

    public Sector getOtherSector(Sector sector) {
        if (sector == null) {
            return null;
        }

        if (sector == frontSector) {
            return backSector;
        }

        if (sector == backSector) {
            return frontSector;
        }

        return null;
    }

    public double getCollisionThickness() {
        return collisionThickness;
    }

    public void setCollisionThickness(double collisionThickness) {
        if (collisionThickness < 0) {
            throw new IllegalArgumentException("Collision thickness cannot be negative.");
        }

        this.collisionThickness = collisionThickness;
    }

    @Override
    public String toString() {
        return "LineDef(" + id + ", " + start.id + " -> " + end.id + ")";
    }
}
