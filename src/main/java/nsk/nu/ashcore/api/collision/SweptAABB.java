package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;

/**
 * First contact between a translating AABB and a static AABB, including touching boundaries.
 * No rotation, scale change, acceleration or physical response. O(1) time and additional memory.
 */
public final class SweptAABB {
    /** t is time in the velocity's time unit; a miss has positive infinity and null normal. */
    public record Result(boolean hit, double t, Vector3 normal){}

    /**
     * Tests translation vel * t for t in the closed interval [0,tMax].
     * Use common position units and finite velocity in position units per time unit. With displacement
     * as vel and tMax=1, t is a fraction of that displacement, not distance.
     * tMax must be finite and non-negative. Non-finite velocity or overflowing bound differences are rejected.
     * Starting overlap returns t=0; moving overlaps use an exit-face normal, resting overlaps use null.
     * Face ties use X before Y before Z. Floating-point rounding applies; no contact tolerance is added.
     */
    public static Result test(AxisAlignedBox moving, Vector3 vel, AxisAlignedBox box, double tMax){
        if (tMax < 0 || !Double.isFinite(tMax)) throw new IllegalArgumentException("tMax must be finite and non-negative");
        if (!Double.isFinite(vel.x()) || !Double.isFinite(vel.y()) || !Double.isFinite(vel.z())) {
            throw new IllegalArgumentException("Velocity must be finite");
        }
        double tEnter = Double.NEGATIVE_INFINITY, tExit = Double.POSITIVE_INFINITY;
        int enterAxis = -1, exitAxis = -1, enterSign = 0, exitSign = 0;
        boolean separated = false;
        for (int axis = 0; axis < 3; axis++) {
            double d = comp(vel, axis);
            double min = comp(box.min(), axis) - comp(moving.max(), axis);
            double max = comp(box.max(), axis) - comp(moving.min(), axis);
            if (!Double.isFinite(min) || !Double.isFinite(max)) throw new IllegalArgumentException("Bound difference overflow");
            if (d == 0) {
                if (min > 0 || max < 0) separated = true;
                continue;
            }
            double t0 = min / d, t1 = max / d;
            int nearSign = -1, farSign = 1;
            if (d < 0) {
                double tmp = t0; t0 = t1; t1 = tmp;
                nearSign = 1; farSign = -1;
            }
            if (t0 > tEnter) { tEnter = t0; enterAxis = axis; enterSign = nearSign; }
            if (t1 < tExit) { tExit = t1; exitAxis = axis; exitSign = farSign; }
        }
        double t = Math.max(0.0, tEnter);
        if (separated || tExit < tEnter || tExit < 0 || t > tMax) return new Result(false, Double.POSITIVE_INFINITY, null);
        int axis = tEnter < 0 ? exitAxis : enterAxis;
        int sign = tEnter < 0 ? exitSign : enterSign;
        Vector3 normal = switch (axis) {
            case 0 -> new Vector3(sign, 0, 0);
            case 1 -> new Vector3(0, sign, 0);
            case 2 -> new Vector3(0, 0, sign);
            default -> null;
        };
        return new Result(true, t, normal);
    }

    private static double comp(Vector3 v, int axis) { return axis == 0 ? v.x() : axis == 1 ? v.y() : v.z(); }
}
