package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
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
        var r = rayBoxSlab(ray, box);
        return r.hit ? r.tMin : Double.POSITIVE_INFINITY;
    }

    /**
     * Full ray vs box hit information: t, intersection point and face normal.
     */
    public static Hit rayVsBoxHit(Ray ray, AxisAlignedBox box) {
        var r = rayBoxSlab(ray, box);
        if (!r.hit) return new Hit(Double.POSITIVE_INFINITY, null, null);

        Vector3 point = ray.at(r.tMin);
        Vector3 normal = switch (r.axis) {
            case 0 -> new Vector3(r.sign, 0, 0);
            case 1 -> new Vector3(0, r.sign, 0);
            default -> new Vector3(0, 0, r.sign);
        };
        return new Hit(r.tMin, point, normal);
    }

    /**
     * Core slab computation shared by {@link #rayVsBoxT} and {@link #rayVsBoxHit}.
     * No arrays are allocated; components are accessed by axis index.
     */
    private static SlabResult rayBoxSlab(Ray ray, AxisAlignedBox box) {
        double tMin = 0.0;
        double tMax = Double.POSITIVE_INFINITY;
        int hitAxis = -1;
        int hitSign = 0;

        for (int axis = 0; axis < 3; axis++) {
            double o = comp(ray.origin(), axis);
            double d = comp(ray.direction(), axis);
            double min = comp(box.min(), axis);
            double max = comp(box.max(), axis);

            double invD = 1.0 / d;
            double t0 = (min - o) * invD;
            double t1 = (max - o) * invD;
            int sign0 = invD >= 0 ? -1 : 1;

            if (invD < 0) {
                double tmp = t0; t0 = t1; t1 = tmp;
                sign0 = -sign0;
            }

            if (t0 > tMin) { tMin = t0; hitAxis = axis; hitSign = sign0; }
            if (t1 < tMax) { tMax = t1; }
            if (tMax < tMin) return SlabResult.miss();
        }
        return SlabResult.hit(tMin, hitAxis, hitSign);
    }

    /** Returns x/y/z component by axis index: 0=x, 1=y, 2=z. */
    private static double comp(Vector3 v, int axis) {
        return axis == 0 ? v.x() : axis == 1 ? v.y() : v.z();
    }

    /**
     * Internal result of the slab algorithm.
     */
    private static record SlabResult(boolean hit, double tMin, int axis, int sign) {
        static SlabResult hit(double tMin, int axis, int sign) { return new SlabResult(true, tMin, axis, sign); }
        static SlabResult miss() { return new SlabResult(false, Double.POSITIVE_INFINITY, -1, 0); }
    }
}