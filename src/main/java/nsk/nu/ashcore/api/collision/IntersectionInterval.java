package nsk.nu.ashcore.api.collision;

/**
 * Closed intersection interval. Query methods define parameter units and clipping rules.
 * Ray/OBB queries retain the supporting line's negative entry when the origin is inside; segment/OBB
 * queries clip to [0,1]. A miss is (+infinity,-infinity). There are no point/normal fields.
 * Construction requires ordered finite endpoints or the miss sentinel; other values throw IllegalArgumentException.
 */
public record IntersectionInterval(double tEnter, double tExit) {
    public IntersectionInterval {
        boolean miss = tEnter == Double.POSITIVE_INFINITY && tExit == Double.NEGATIVE_INFINITY;
        if (!miss && (!Double.isFinite(tEnter) || !Double.isFinite(tExit) || tEnter > tExit)) {
            throw new IllegalArgumentException("Interval must be finite and ordered, or the miss sentinel");
        }
    }

    /** @return whether the query intersects, including a single tangent parameter */
    public boolean hit() { return tEnter <= tExit; }

    static IntersectionInterval miss() { return new IntersectionInterval(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY); }
}
