package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.NumericTolerance;
import nsk.nu.ashcore.api.math.Vector3;

/**
 * Collection of static, allocation-free intersection tests.
 * Centralizes shared "slab" logic to avoid duplication.
 */
public final class CollisionTests {
    private CollisionTests() {}

    /**
     * Ray vs axis-aligned box using the slab method.
     * @return t of the first intersection or {@code Double.POSITIVE_INFINITY} if no hit
     */
    public static double rayVsBoxT(Ray ray, AxisAlignedBox box) {
        SlabResult r = rayBoxSlab(ray, box);
        if (!r.hit || r.tExit < 0.0) return Double.POSITIVE_INFINITY;
        return Math.max(0.0, r.tEnter);
    }

    /**
     * Full ray vs box hit information: t, intersection point and face normal.
     */
    public static Hit rayVsBoxHit(Ray ray, AxisAlignedBox box) {
        SlabResult r = rayBoxSlab(ray, box);
        if (!r.hit || r.tExit < 0.0) return new Hit(Double.POSITIVE_INFINITY, null, null);

        boolean startedInside = r.tEnter < 0.0;
        double t = startedInside ? 0.0 : r.tEnter;
        int axis = startedInside ? r.exitAxis : r.enterAxis;
        int sign = startedInside ? r.exitSign : r.enterSign;

        Vector3 point = ray.at(t);
        Vector3 normal = switch (axis) {
            case 0 -> new Vector3(sign, 0, 0);
            case 1 -> new Vector3(0, sign, 0);
            case 2 -> new Vector3(0, 0, sign);
            default -> null;
        };
        return new Hit(t, point, normal);
    }


    /**
     * Core slab computation shared by {@link #rayVsBoxT} and {@link #rayVsBoxHit}.
     * No arrays are allocated; components are accessed by axis index.
     */
    private static SlabResult rayBoxSlab(Ray ray, AxisAlignedBox box) {
        double tEnter = 0.0;
        double tExit = Double.POSITIVE_INFINITY;
        int enterAxis = -1, enterSign = 0;
        int exitAxis = -1, exitSign = 0;

        for (int axis = 0; axis < 3; axis++) {
            double o = comp(ray.origin(), axis);
            double d = comp(ray.direction(), axis);
            double min = comp(box.min(), axis);
            double max = comp(box.max(), axis);

            if (Math.abs(d) < NumericTolerance.GEOMETRY_EPS) {
                if (o < min || o > max) return SlabResult.miss();
                continue;
            }

            double t0 = (min - o) / d;
            double t1 = (max - o) / d;
            int nearSign = -1;
            int farSign = 1;

            if (t0 > t1) {
                double tmp = t0; t0 = t1; t1 = tmp;
                nearSign = 1;
                farSign = -1;
            }

            if (t0 > tEnter) { tEnter = t0; enterAxis = axis; enterSign = nearSign; }
            if (t1 < tExit)  { tExit = t1;  exitAxis = axis;  exitSign = farSign; }

            if (tExit < tEnter) return SlabResult.miss();
        }

        return SlabResult.hit(tEnter, tExit, enterAxis, enterSign, exitAxis, exitSign);
    }

    /** Returns x/y/z component by axis index: 0=x, 1=y, 2=z. */
    private static double comp(Vector3 v, int axis) {
        return axis == 0 ? v.x() : axis == 1 ? v.y() : v.z();
    }

    /**
     * Internal result of the slab algorithm.
     */
    private static record SlabResult(
            boolean hit,
            double tEnter,
            double tExit,
            int enterAxis,
            int enterSign,
            int exitAxis,
            int exitSign
    ) {
        static SlabResult hit(double tEnter, double tExit, int enterAxis, int enterSign, int exitAxis, int exitSign) {
            return new SlabResult(true, tEnter, tExit, enterAxis, enterSign, exitAxis, exitSign);
        }
        static SlabResult miss() {
            return new SlabResult(false, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -1, 0, -1, 0);
        }
    }
}