package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.OrientedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.geometry.Segment3;
import nsk.nu.ashcore.api.geometry.Sphere;
import nsk.nu.ashcore.api.math.Quaternion;
import nsk.nu.ashcore.api.math.Vector3;

/**
 * Intersection tests for closed primitive shapes, including touching boundaries.
 * Results describe the supplied primitives; an enclosed object's shape may differ. No collision response is computed.
 * O(1) time and additional memory; result records and hit vectors may allocate.
 */
public final class CollisionTests {
    private CollisionTests() {}

    /**
     * Full supporting-line entry/exit distances when the forward ray intersects the closed OBB.
     * An inside origin retains negative entry; an interval entirely behind the origin is a miss.
     * Local differences/rotated coordinates and every nonparallel slab ratio must be finite and representable
     * or IllegalArgumentException is thrown. Only exact zero components are parallel. No contact tolerance is
     * added; rounded rotation/slab arithmetic can misclassify tangency or tiny gaps. O(1) time/space.
     */
    public static IntersectionInterval rayVsOrientedBoxInterval(Ray ray, OrientedBox box) {
        SlabResult r = orientedBoxSlab(ray.origin(), ray.direction(), box);
        if (!r.hit || r.tExit < 0) return IntersectionInterval.miss();
        return new IntersectionInterval(r.tEnter, r.tExit);
    }

    /** First OBB contact distance, zero inside/on the box, +infinity on a miss; same limits as rayVsOrientedBoxInterval. */
    public static double rayVsOrientedBoxT(Ray ray, OrientedBox box) {
        IntersectionInterval r = rayVsOrientedBoxInterval(ray, box);
        return r.hit() ? Math.max(0.0, r.tEnter()) : Double.POSITIVE_INFINITY;
    }

    /**
     * Closed segment/OBB interval clipped to [0,1], in dimensionless fractions evaluated by segment.at(t).
     * A zero-length segment inside/on the box returns [0,1]; outside returns a miss. Both endpoints count.
     * Endpoints and b-a must be finite; other numeric limits match rayVsOrientedBoxInterval. O(1) time/space.
     */
    public static IntersectionInterval segmentVsOrientedBoxInterval(Segment3 segment, OrientedBox box) {
        requireFinite(segment.a());
        requireFinite(segment.b());
        Vector3 direction = segment.b().sub(segment.a());
        requireFinite(direction);
        SlabResult r = orientedBoxSlab(segment.a(), direction, box);
        if (!r.hit || r.tExit < 0 || r.tEnter > 1) return IntersectionInterval.miss();
        return new IntersectionInterval(Math.max(0.0, r.tEnter), Math.min(1.0, r.tExit));
    }

    /** First segment/OBB fraction, zero inside/on the box, +infinity on a miss; same limits as segmentVsOrientedBoxInterval. */
    public static double segmentVsOrientedBoxT(Segment3 segment, OrientedBox box) {
        return segmentVsOrientedBoxInterval(segment, box).tEnter();
    }

    /**
     * Closed sphere/OBB overlap, including zero extents/radius. Tests the closest box point in local coordinates.
     * Center differences and rotated coordinates must be finite or IllegalArgumentException is thrown.
     * No tolerance is added; tiny features/gaps and tangency are subject to double rounding. O(1) time/space.
     */
    public static boolean sphereVsOrientedBox(Sphere sphere, OrientedBox box) {
        return sphereVsBox(new Sphere(localPoint(sphere.center(), box), sphere.radius()), localBox(box));
    }

    /**
     * Closed AABB/OBB overlap. Requires finite bounds and representable AABB widths, otherwise throws
     * IllegalArgumentException. Other guarantees/limits match orientedBoxVsOrientedBox. O(1) time/space.
     */
    public static boolean boxVsOrientedBox(AxisAlignedBox a, OrientedBox b) {
        requireFiniteBox(a);
        Vector3 width = a.max().sub(a.min());
        requireFinite(width);
        Vector3 half = width.mul(0.5);
        return orientedBoxVsOrientedBox(new OrientedBox(a.min().add(half), half, Quaternion.identity()), b);
    }

    /**
     * Closed static OBB/OBB overlap using six face axes and nine edge cross-product axes, including degeneracies.
     * Exactly zero cross products are skipped; nearly parallel axes are tested without an angular cutoff.
     * Finite center differences are required or IllegalArgumentException is thrown. Scaled projections avoid
     * overflow but do not give exact predicates or an error bound near contact/large relative scales.
     * No shape inflation/contact epsilon is applied. O(1) time/space; fixed-size arrays/vectors may allocate.
     */
    public static boolean orientedBoxVsOrientedBox(OrientedBox a, OrientedBox b) {
        Vector3 delta = b.center().sub(a.center());
        requireFinite(delta);
        double scale = Math.max(maxAbs(delta), Math.max(maxAbs(a.halfExtents()), maxAbs(b.halfExtents())));
        if (scale == 0) return true;
        delta = delta.div(scale);
        Vector3 ah = a.halfExtents().div(scale), bh = b.halfExtents().div(scale);
        Vector3[] aa = boxAxes(a), ba = boxAxes(b);
        for (int i = 0; i < 3; i++) {
            if (separated(delta, ah, aa, bh, ba, aa[i]) || separated(delta, ah, aa, bh, ba, ba[i])) return false;
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Vector3 axis = aa[i].cross(ba[j]);
                if (maxAbs(axis) != 0 && separated(delta, ah, aa, bh, ba, axis.normalized())) return false;
            }
        }
        return true;
    }

    /**
     * Sphere/sphere contact witnesses. Normal points from A's center to B's; coincident centers choose global +X.
     * Depth is radiusA + radiusB - centerDistance, including full containment. Distinct-center argument swaps
     * negate the normal and swap witnesses; the global coincident-center tie is an explicit exception.
     * No contact epsilon is added. Finite representable differences, depth and surface coordinates are required;
     * unsupported arithmetic throws IllegalArgumentException. O(1) time/space. See Contact for field meanings.
     */
    public static Contact sphereVsSphereContact(Sphere a, Sphere b) {
        if (!sphereVsSphere(a, b)) return Contact.miss();
        Vector3 delta = b.center().sub(a.center());
        double scale = Math.max(maxAbs(delta), Math.max(a.radius(), b.radius()));
        Vector3 normal = maxAbs(delta) == 0 ? new Vector3(1, 0, 0) : delta.normalized();
        double depth = scale == 0 ? 0 : Math.max(0.0, a.radius()/scale + b.radius()/scale - delta.div(scale).length()) * scale;
        return new Contact(depth, normal, a.center().add(normal.mul(a.radius())), b.center().sub(normal.mul(b.radius())));
    }

    /**
     * Sphere (A)/AABB (B) contact. Outside centers use the normal toward the closest box point and radius minus
     * distance as depth. Inside/on centers choose the nearest face, ties X-min, X-max, Y-min, Y-max, Z-min, Z-max;
     * normal is opposite that face's outward direction and depth is radius plus face clearance.
     * Witnesses satisfy pointA-pointB = normal*depth within rounding, including full containment/zero sizes.
     * Common rigid transformations preserve unique results only when the transformed box is still axis aligned;
     * global face tie choices need not rotate with the scene. Numeric limits match sphereVsSphereContact; finite
     * bounds and face differences are required. No epsilon is added. O(1) time/space.
     */
    public static Contact sphereVsBoxContact(Sphere sphere, AxisAlignedBox box) {
        if (!sphereVsBox(sphere, box)) return Contact.miss();
        Vector3 center = sphere.center();
        Vector3 pointB = CollisionUtils.closestPointOnBox(center, box);
        Vector3 delta = pointB.sub(center);
        Vector3 normal;
        double depth;
        if (maxAbs(delta) != 0) {
            normal = delta.normalized();
            depth = Math.max(0.0, sphere.radius() - delta.length());
        } else {
            double nearest = Double.POSITIVE_INFINITY;
            int faceAxis = -1, faceSign = 0;
            for (int axis = 0; axis < 3; axis++) {
                double lower = comp(center, axis) - comp(box.min(), axis);
                double upper = comp(box.max(), axis) - comp(center, axis);
                if (!Double.isFinite(lower) || !Double.isFinite(upper)) {
                    throw new IllegalArgumentException("Face differences must be finite");
                }
                if (lower < nearest) { nearest = lower; faceAxis = axis; faceSign = -1; }
                if (upper < nearest) { nearest = upper; faceAxis = axis; faceSign = 1; }
            }
            normal = axisVector(faceAxis).mul(-faceSign);
            double face = faceSign < 0 ? comp(box.min(), faceAxis) : comp(box.max(), faceAxis);
            pointB = switch (faceAxis) {
                case 0 -> center.withX(face);
                case 1 -> center.withY(face);
                default -> center.withZ(face);
            };
            depth = sphere.radius() + nearest;
        }
        return new Contact(depth, normal, center.add(normal.mul(sphere.radius())), pointB);
    }

    /** Reverses sphereVsBoxContact's arguments, witnesses and normal, including face ties; identical units and limits. */
    public static Contact boxVsSphereContact(AxisAlignedBox box, Sphere sphere) {
        Contact c = sphereVsBoxContact(sphere, box);
        return c.hit() ? new Contact(c.depth(), c.normal().mul(-1), c.pointB(), c.pointA()) : c;
    }

    private static SlabResult orientedBoxSlab(Vector3 origin, Vector3 direction, OrientedBox box) {
        Vector3 localDirection = box.orientation().conjugate().rotate(direction);
        requireFinite(localDirection);
        // Preserve the input parameter: constructing a new Ray would renormalize this direction.
        return boxSlab(localPoint(origin, box), localDirection, localBox(box), true);
    }

    private static Vector3 localPoint(Vector3 point, OrientedBox box) {
        Vector3 delta = point.sub(box.center());
        requireFinite(delta);
        Vector3 local = box.orientation().conjugate().rotate(delta);
        requireFinite(local);
        return local;
    }

    private static AxisAlignedBox localBox(OrientedBox box) {
        return new AxisAlignedBox(box.halfExtents().mul(-1), box.halfExtents());
    }

    private static Vector3[] boxAxes(OrientedBox box) {
        return new Vector3[]{box.orientation().rotate(axisVector(0)), box.orientation().rotate(axisVector(1)),
                box.orientation().rotate(axisVector(2))};
    }

    private static Vector3 axisVector(int axis) {
        return switch (axis) {
            case 0 -> new Vector3(1, 0, 0);
            case 1 -> new Vector3(0, 1, 0);
            default -> new Vector3(0, 0, 1);
        };
    }

    private static boolean separated(Vector3 delta, Vector3 ah, Vector3[] aa, Vector3 bh, Vector3[] ba, Vector3 axis) {
        double radiusA = 0, radiusB = 0;
        for (int i = 0; i < 3; i++) {
            radiusA += comp(ah, i) * Math.abs(aa[i].dot(axis));
            radiusB += comp(bh, i) * Math.abs(ba[i].dot(axis));
        }
        return Math.abs(delta.dot(axis)) > radiusA + radiusB;
    }

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
        return boxSlab(origin, direction, box, false);
    }

    private static SlabResult boxSlab(Vector3 origin, Vector3 direction, AxisAlignedBox box, boolean checked) {
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
            if (checked && (!Double.isFinite(t0) || !Double.isFinite(t1))) {
                throw new IllegalArgumentException("Slab differences and ratios must be representable");
            }
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
