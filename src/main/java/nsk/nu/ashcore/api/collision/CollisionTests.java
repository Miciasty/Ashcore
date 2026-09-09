package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.geometry.Segment3;
import nsk.nu.ashcore.api.geometry.Sphere;
import nsk.nu.ashcore.api.math.Vector3;

/**
 * Intersection tests for closed primitive shapes, including touching boundaries.
 * Results describe the supplied primitives; an enclosed object's shape may differ. No collision response is computed.
 * O(1) time and additional memory; result records and hit vectors may allocate.
 */
public final class CollisionTests {
    private CollisionTests() {}

    /**
     * First ray/sphere contact distance, zero for an origin inside/on the sphere, positive infinity for a miss.
     * Tangency and a zero-radius sphere count as contact. Uses the ray's normalized direction.
     * Origin-center differences must be finite; invalid differences throw IllegalArgumentException.
     * Scaling avoids squared-coordinate overflow, but rounding can lose tiny features at large relative scales.
     * No tolerance is added; the returned distance must fit in double. O(1) time and additional memory.
     */
    public static double rayVsSphereT(Ray ray, Sphere sphere) {
        Vector3 offset = ray.origin().sub(sphere.center());
        requireFinite(offset);
        double scale = Math.max(maxAbs(offset), sphere.radius());
        if (scale == 0) return 0;
        Vector3 o = offset.div(scale);
        double radius = sphere.radius() / scale;
        if (o.length() <= radius) return 0;

        Vector3 d = ray.direction();
        double length = d.length();
        double projection = o.dot(d) / length;
        if (projection >= 0) return Double.POSITIVE_INFINITY;
        double perpendicular = o.cross(d).length() / length;
        if (perpendicular > radius) return Double.POSITIVE_INFINITY;
        // Separate square roots avoid squaring a very small radius; perpendicular distance avoids b*b-c cancellation.
        double halfChord = Math.sqrt(radius - perpendicular) * Math.sqrt(radius + perpendicular);
        return Math.max(0.0, (-projection - halfChord) / length) * scale;
    }

    /**
     * Whether two closed spheres overlap/touch, including zero-radius points. All positions/radii share units.
     * Center differences must be finite or IllegalArgumentException is thrown. Comparisons use scaled doubles
     * without a contact tolerance; features below relative floating-point precision may be lost. O(1) time/space.
     */
    public static boolean sphereVsSphere(Sphere a, Sphere b) {
        Vector3 offset = a.center().sub(b.center());
        requireFinite(offset);
        double scale = Math.max(maxAbs(offset), Math.max(a.radius(), b.radius()));
        return scale == 0 || offset.div(scale).length() <= a.radius() / scale + b.radius() / scale;
    }

    /**
     * Whether a closed sphere overlaps/touches a finite AABB, including degenerate shapes. O(1) time/space.
     * Uses distance to the closest box point with no added tolerance. Bounds and center/closest-point
     * differences must be finite or IllegalArgumentException is thrown; relative rounding limits apply.
     */
    public static boolean sphereVsBox(Sphere sphere, AxisAlignedBox box) {
        requireFiniteBox(box);
        Vector3 offset = sphere.center().sub(CollisionUtils.closestPointOnBox(sphere.center(), box));
        requireFinite(offset);
        double scale = Math.max(maxAbs(offset), sphere.radius());
        return scale == 0 || offset.div(scale).length() <= sphere.radius() / scale;
    }

    /**
     * First segment/AABB contact as a fraction t in [0,1], evaluated by segment.at(t); positive infinity on a miss.
     * Both endpoints and touching boundaries are included. Starting inside/on the box gives zero; a zero-length
     * segment is a point test. t is dimensionless, not distance. Endpoints, bounds and b-a must be finite or
     * IllegalArgumentException is thrown. Slab differences/ratios must remain representable. O(1) time/space.
     */
    public static double segmentVsBoxT(Segment3 segment, AxisAlignedBox box) {
        requireFinite(segment.a());
        requireFinite(segment.b());
        Vector3 direction = segment.b().sub(segment.a());
        requireFinite(direction);
        SlabResult r = boxSlab(segment.a(), direction, box);
        if (!r.hit || r.tExit < 0.0 || r.tEnter > 1.0) return Double.POSITIVE_INFINITY;
        return Math.max(0.0, r.tEnter);
    }

    /**
     * Ray vs axis-aligned box using the slab method.
     * Only exactly zero direction components are parallel. Coordinate differences and the hit distance
     * must be representable as doubles; no scale-independent robustness or error bound is promised.
     * Non-finite box bounds throw IllegalArgumentException.
     * @return distance to first contact, zero when starting inside/on the box, or positive infinity on a miss
     */
    public static double rayVsBoxT(Ray ray, AxisAlignedBox box) {
        SlabResult r = rayBoxSlab(ray, box);
        if (!r.hit || r.tExit < 0.0) return Double.POSITIVE_INFINITY;
        return Math.max(0.0, r.tEnter);
    }

    /**
     * Full ray vs box hit information: distance, point and outward face normal.
     * When starting inside, t is zero and point is the origin, but normal is the exit face's normal.
     * Equal face parameters use X before Y before Z. A miss has infinite t and null point/normal.
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
        return boxSlab(ray.origin(), ray.direction(), box);
    }

    private static SlabResult boxSlab(Vector3 origin, Vector3 direction, AxisAlignedBox box) {
        requireFiniteBox(box);
        double tEnter = Double.NEGATIVE_INFINITY;
        double tExit = Double.POSITIVE_INFINITY;
        int enterAxis = -1, enterSign = 0;
        int exitAxis = -1, exitSign = 0;

        for (int axis = 0; axis < 3; axis++) {
            double o = comp(origin, axis);
            double d = comp(direction, axis);
            double min = comp(box.min(), axis);
            double max = comp(box.max(), axis);

            if (d == 0.0) {
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

    private static void requireFiniteBox(AxisAlignedBox box) {
        requireFinite(box.min());
        requireFinite(box.max());
    }

    private static void requireFinite(Vector3 v) {
        if (!Double.isFinite(v.x()) || !Double.isFinite(v.y()) || !Double.isFinite(v.z())) {
            throw new IllegalArgumentException("Coordinates and differences must be finite");
        }
    }

    private static double maxAbs(Vector3 v) { return Math.max(Math.abs(v.x()), Math.max(Math.abs(v.y()), Math.abs(v.z()))); }

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
