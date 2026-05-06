package collision;

import math.Vec2;

public class CollisionResult {
    public final Vec2 startPosition;
    public final Vec2 requestedPosition;
    public final Vec2 finalPosition;

    public final boolean blocked;
    public final boolean blockedX;
    public final boolean blockedY;
    public final boolean slid;

    public CollisionResult(
            Vec2 startPosition,
            Vec2 requestedPosition,
            Vec2 finalPosition,
            boolean blockedX,
            boolean blockedY
    ) {
        this.startPosition = startPosition;
        this.requestedPosition = requestedPosition;
        this.finalPosition = finalPosition;
        this.blockedX = blockedX;
        this.blockedY = blockedY;
        this .blocked = blockedX || blockedY;

        boolean movedX = requestedPosition.x != startPosition.x;
        boolean movedY = requestedPosition.y != startPosition.y;

        this.slid = blocked && (movedX || movedY);
    }

    public static CollisionResult noCollision(Vec2 startPosition, Vec2 requestedPosition) {
        return new CollisionResult(
                startPosition,
                requestedPosition,
                requestedPosition,
                false,
                false
        );
    }

    public static CollisionResult blocked(
            Vec2 startPosition,
            Vec2 requestedPosition,
            Vec2 finalPosition,
            boolean blockedX,
            boolean blockedY
    ) {
        return new CollisionResult(
                startPosition,
                requestedPosition,
                finalPosition,
                blockedX,
                blockedY
        );
    }



}
