package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.OrientedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.geometry.Segment3;
import nsk.nu.ashcore.api.geometry.Sphere;
import nsk.nu.ashcore.api.math.Quaternion;
import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class OrientedBoxQueriesTest {
    private final OrientedBox unit = new OrientedBox(Vector3.ZERO, new Vector3(1, 1, 1), Quaternion.identity());

    @Test
    void construction_normalizesOrientationAndRejectsInvalidData() {
        OrientedBox large = new OrientedBox(Vector3.ZERO, Vector3.ZERO, new Quaternion(1e308, 0, 0, 1e308));
        assertVector(new Vector3(0, 1, 0), large.orientation().rotate(new Vector3(1, 0, 0)));
        for (double invalid : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new OrientedBox(Vector3.ZERO, new Vector3(invalid, 1, 1), Quaternion.identity()));
        }
        assertThrows(IllegalArgumentException.class,
                () -> new OrientedBox(new Vector3(Double.NaN, 0, 0), Vector3.ZERO, Quaternion.identity()));
        assertThrows(IllegalArgumentException.class,
                () -> new OrientedBox(Vector3.ZERO, Vector3.ZERO, new Quaternion(0, 0, 0, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new OrientedBox(Vector3.ZERO, Vector3.ZERO, new Quaternion(Double.NaN, 0, 0, 0)));
        assertThrows(NullPointerException.class, () -> new OrientedBox(null, Vector3.ZERO, Quaternion.identity()));
        assertThrows(NullPointerException.class, () -> new OrientedBox(Vector3.ZERO, null, Quaternion.identity()));
        assertThrows(NullPointerException.class, () -> new OrientedBox(Vector3.ZERO, Vector3.ZERO, null));
    }

    @Test
    void rayInterval_preservesEntryExitAndInsideEntry() {
        Ray ray = new Ray(new Vector3(-3, 0, 0), new Vector3(10, 0, 0));
        assertInterval(2, 4, CollisionTests.rayVsOrientedBoxInterval(ray, unit));
        assertEquals(2, CollisionTests.rayVsOrientedBoxT(ray, unit));
        Ray inside = new Ray(Vector3.ZERO, new Vector3(1, 0, 0));
        assertInterval(-1, 1, CollisionTests.rayVsOrientedBoxInterval(inside, unit));
        assertEquals(0, CollisionTests.rayVsOrientedBoxT(inside, unit));
        Ray boundary = new Ray(new Vector3(1, 0, 0), new Vector3(1, 0, 0));
        assertInterval(-2, 0, CollisionTests.rayVsOrientedBoxInterval(boundary, unit));
        Ray away = new Ray(new Vector3(2, 0, 0), new Vector3(1, 0, 0));
        assertMiss(CollisionTests.rayVsOrientedBoxInterval(away, unit));
        assertEquals(Double.POSITIVE_INFINITY, CollisionTests.rayVsOrientedBoxT(away, unit));
        assertMiss(CollisionTests.rayVsOrientedBoxInterval(new Ray(new Vector3(-3, 2, 0), new Vector3(1, 0, 0)), unit));
        assertInterval(2, 4, CollisionTests.rayVsOrientedBoxInterval(new Ray(new Vector3(-3, 1, 1), new Vector3(1, 0, 0)), unit));
    }

    @Test
    void rayInterval_handlesSinglePointTangencyAndDegenerateBoxes() {
        Ray tangent = new Ray(new Vector3(-2, 0, 0), new Vector3(1, 1, 0));
        double corner = Math.sqrt(2);
        assertInterval(corner, corner, CollisionTests.rayVsOrientedBoxInterval(tangent, unit));
        OrientedBox point = new OrientedBox(Vector3.ZERO, Vector3.ZERO, Quaternion.identity());
        assertInterval(2, 2, CollisionTests.rayVsOrientedBoxInterval(new Ray(new Vector3(-2, 0, 0), new Vector3(1, 0, 0)), point));
        OrientedBox plane = new OrientedBox(Vector3.ZERO, new Vector3(1, 1, 0), Quaternion.identity());
        assertInterval(1, 1, CollisionTests.rayVsOrientedBoxInterval(new Ray(new Vector3(0, 0, -1), new Vector3(0, 0, 1)), plane));
    }

    @Test
    void segmentInterval_isClippedIncludesEndpointsAndStationaryPoints() {
        Segment3 segment = new Segment3(new Vector3(-3, 0, 0), new Vector3(3, 0, 0));
        assertInterval(1.0/3, 2.0/3, CollisionTests.segmentVsOrientedBoxInterval(segment, unit));
        assertEquals(1.0/3, CollisionTests.segmentVsOrientedBoxT(segment, unit));
        assertInterval(0, 0.5, CollisionTests.segmentVsOrientedBoxInterval(new Segment3(Vector3.ZERO, new Vector3(2, 0, 0)), unit));
        assertInterval(0.5, 1, CollisionTests.segmentVsOrientedBoxInterval(new Segment3(new Vector3(2, 0, 0), Vector3.ZERO), unit));
        assertInterval(1, 1, CollisionTests.segmentVsOrientedBoxInterval(new Segment3(new Vector3(-2, 0, 0), new Vector3(-1, 0, 0)), unit));
        assertInterval(0, 1, CollisionTests.segmentVsOrientedBoxInterval(new Segment3(Vector3.ZERO, Vector3.ZERO), unit));
        Vector3 edge = new Vector3(1, 1, 1), outside = new Vector3(2, 0, 0);
        assertInterval(0, 1, CollisionTests.segmentVsOrientedBoxInterval(new Segment3(edge, edge), unit));
        assertMiss(CollisionTests.segmentVsOrientedBoxInterval(new Segment3(outside, outside), unit));
        assertMiss(CollisionTests.segmentVsOrientedBoxInterval(new Segment3(outside, new Vector3(3, 0, 0)), unit));
        assertEquals(Double.POSITIVE_INFINITY, CollisionTests.segmentVsOrientedBoxT(new Segment3(outside, outside), unit));
    }

    @Test
    void sphereQueries_includeFaceEdgeCornerContainmentAndPoints() {
        assertTrue(CollisionTests.sphereVsOrientedBox(new Sphere(new Vector3(2, 0, 0), 1), unit));
        assertFalse(CollisionTests.sphereVsOrientedBox(new Sphere(new Vector3(2.001, 0, 0), 1), unit));
        assertTrue(CollisionTests.sphereVsOrientedBox(new Sphere(new Vector3(4, 5, 1), 5), unit));
        assertFalse(CollisionTests.sphereVsOrientedBox(new Sphere(new Vector3(4, 5, 1), 4.99), unit));
        assertTrue(CollisionTests.sphereVsOrientedBox(new Sphere(Vector3.ZERO, 0), unit));
        assertTrue(CollisionTests.sphereVsOrientedBox(new Sphere(Vector3.ZERO, 10), unit));
        OrientedBox point = new OrientedBox(new Vector3(1, 0, 0), Vector3.ZERO, Quaternion.identity());
        assertTrue(CollisionTests.sphereVsOrientedBox(new Sphere(Vector3.ZERO, 1), point));
    }

    @Test
    void zeroRotation_agreesWithExistingPrimitiveQueries() {
        AxisAlignedBox aligned = new AxisAlignedBox(new Vector3(-1, -1, -1), new Vector3(1, 1, 1));
        Random random = new Random(311);
        for (int i = 0; i < 100; i++) {
            Vector3 origin = vector(random, 6), direction = vector(random, 2);
            Ray ray = new Ray(origin, direction);
            Segment3 segment = new Segment3(origin, origin.add(direction));
            Sphere sphere = new Sphere(origin, random.nextDouble()*2);
            assertEquals(CollisionTests.rayVsBoxT(ray, aligned), CollisionTests.rayVsOrientedBoxT(ray, unit));
            assertEquals(CollisionTests.segmentVsBoxT(segment, aligned), CollisionTests.segmentVsOrientedBoxT(segment, unit));
            assertEquals(CollisionTests.sphereVsBox(sphere, aligned), CollisionTests.sphereVsOrientedBox(sphere, unit));
            OrientedBox other = new OrientedBox(origin, new Vector3(0.5, 0.5, 0.5), Quaternion.identity());
            boolean expected = Math.abs(origin.x()) <= 1.5 && Math.abs(origin.y()) <= 1.5 && Math.abs(origin.z()) <= 1.5;
            assertEquals(expected, CollisionTests.orientedBoxVsOrientedBox(unit, other));
            assertEquals(expected, CollisionTests.boxVsOrientedBox(aligned, other));
        }
    }

    @Test
    void rotatedBoxes_canMissDespiteOverlappingAxisAlignedBounds() {
        Quaternion q = Quaternion.fromAxisAngle(new Vector3(0, 0, 1), Math.PI/4);
        Vector3 half = new Vector3(2, 0.1, 0.1);
        OrientedBox a = new OrientedBox(Vector3.ZERO, half, q);
        OrientedBox b = new OrientedBox(q.rotate(new Vector3(0, 0.3, 0)), half, q);
        AxisAlignedBox ab = bounds(a), bb = bounds(b);
        assertTrue(ab.min().x() < bb.max().x() && bb.min().x() < ab.max().x());
        assertTrue(ab.min().y() < bb.max().y() && bb.min().y() < ab.max().y());
        assertFalse(CollisionTests.orientedBoxVsOrientedBox(a, b));
        assertFalse(CollisionTests.orientedBoxVsOrientedBox(b, a));
        assertTrue(CollisionTests.boxVsOrientedBox(ab, b));
    }

    @Test
    void boxPairs_coverTouchingContainmentDegeneracyAndNearlyParallelAxes() {
        assertTrue(CollisionTests.orientedBoxVsOrientedBox(unit, unit));
        assertTrue(CollisionTests.orientedBoxVsOrientedBox(unit,
                new OrientedBox(Vector3.ZERO, new Vector3(0.1, 0.1, 0.1), Quaternion.fromAxisAngle(new Vector3(1, 2, 3), 0.7))));
        for (Vector3 center : new Vector3[]{new Vector3(2, 0, 0), new Vector3(2, 2, 0), new Vector3(2, 2, 2)}) {
            assertTrue(CollisionTests.orientedBoxVsOrientedBox(unit, new OrientedBox(center, unit.halfExtents(), Quaternion.identity())));
        }
        assertFalse(CollisionTests.orientedBoxVsOrientedBox(unit,
                new OrientedBox(new Vector3(Math.nextUp(2.0), 0, 0), unit.halfExtents(), Quaternion.identity())));
        OrientedBox point = new OrientedBox(Vector3.ZERO, Vector3.ZERO, Quaternion.identity());
        assertTrue(CollisionTests.orientedBoxVsOrientedBox(point, unit));
        assertTrue(CollisionTests.orientedBoxVsOrientedBox(point, point));
        assertFalse(CollisionTests.orientedBoxVsOrientedBox(point,
                new OrientedBox(new Vector3(0, 0, 0.001), Vector3.ZERO, Quaternion.identity())));
        OrientedBox line = new OrientedBox(Vector3.ZERO, new Vector3(1, 0, 0), Quaternion.identity());
        assertTrue(CollisionTests.orientedBoxVsOrientedBox(line,
                new OrientedBox(Vector3.ZERO, new Vector3(0, 1, 0), Quaternion.identity())));
        for (double angle : new double[]{1e-6, 1e-10, 1e-14}) {
            Quaternion q = Quaternion.fromAxisAngle(new Vector3(1, 2, 3), angle);
            assertTrue(CollisionTests.orientedBoxVsOrientedBox(unit, new OrientedBox(new Vector3(1.9, 0, 0), unit.halfExtents(), q)));
            assertFalse(CollisionTests.orientedBoxVsOrientedBox(unit, new OrientedBox(new Vector3(2.01, 0, 0), unit.halfExtents(), q)));
        }
    }

    @Test
    void queries_preserveCommonRigidTransformAndScaleAwayFromBoundary() {
        Quaternion rotation = Quaternion.fromAxisAngle(new Vector3(1, 2, -3), 0.63);
        Vector3 shift = new Vector3(7, -3, 11);
        OrientedBox transformed = transform(unit, rotation, shift);
        Ray ray = new Ray(new Vector3(-3, 0.2, 0.3), new Vector3(1, 0, 0));
        Ray movedRay = new Ray(rotation.rotate(ray.origin()).add(shift), rotation.rotate(ray.direction()));
        assertInterval(2, 4, CollisionTests.rayVsOrientedBoxInterval(movedRay, transformed));
        Segment3 segment = new Segment3(ray.origin(), new Vector3(3, 0.2, 0.3));
        assertInterval(1.0/3, 2.0/3, CollisionTests.segmentVsOrientedBoxInterval(
                new Segment3(rotation.rotate(segment.a()).add(shift), rotation.rotate(segment.b()).add(shift)), transformed));
        for (double x : new double[]{0, 1.5, 3}) {
            Sphere sphere = new Sphere(new Vector3(x, 0, 0), 0.75);
            assertEquals(CollisionTests.sphereVsOrientedBox(sphere, unit),
                    CollisionTests.sphereVsOrientedBox(new Sphere(rotation.rotate(sphere.center()).add(shift), sphere.radius()), transformed));
            OrientedBox other = new OrientedBox(sphere.center(), unit.halfExtents(), Quaternion.identity());
            assertEquals(CollisionTests.orientedBoxVsOrientedBox(unit, other),
                    CollisionTests.orientedBoxVsOrientedBox(transformed, transform(other, rotation, shift)));
        }
        for (double scale : new double[]{1e-140, 1, 1e140, 1e300}) {
            OrientedBox scaled = new OrientedBox(Vector3.ZERO, unit.halfExtents().mul(scale), Quaternion.identity());
            assertEquals(2, CollisionTests.rayVsOrientedBoxT(new Ray(new Vector3(-3*scale, 0, 0), new Vector3(1, 0, 0)), scaled)/scale, 1e-12);
            assertTrue(CollisionTests.orientedBoxVsOrientedBox(scaled,
                    new OrientedBox(new Vector3(1.5*scale, 0, 0), scaled.halfExtents(), Quaternion.identity())));
            assertFalse(CollisionTests.orientedBoxVsOrientedBox(scaled,
                    new OrientedBox(new Vector3(3*scale, 0, 0), scaled.halfExtents(), Quaternion.identity())));
        }
    }

    @Test
    void boxPairs_agreeWithIndependentVertexAndEdgeClippingReference() {
        Random random = new Random(90731);
        int hits = 0, misses = 0, edgeAxisMisses = 0;
        for (int i = 0; i < 600; i++) {
            OrientedBox a = randomBox(random), b = randomBox(random);
            boolean expected = referenceIntersects(a, b);
            assertEquals(expected, CollisionTests.orientedBoxVsOrientedBox(a, b), "Pair " + i + ": " + a + " / " + b);
            assertEquals(expected, CollisionTests.orientedBoxVsOrientedBox(b, a), "Reversed pair " + i);
            if (expected) hits++; else {
                misses++;
                if (faceProjectionsOverlap(a, b)) edgeAxisMisses++;
            }
        }
        assertTrue(hits > 20 && misses > 20);
        assertTrue(edgeAxisMisses > 0, "Fixtures must catch an implementation testing only six face axes");
    }

    @Test
    void unsupportedArithmetic_isRejectedInsteadOfReportedAsMiss() {
        Vector3 max = new Vector3(Double.MAX_VALUE, 0, 0), min = max.mul(-1);
        OrientedBox extreme = new OrientedBox(max, Vector3.ZERO, Quaternion.identity());
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.rayVsOrientedBoxInterval(new Ray(min, new Vector3(1, 0, 0)), extreme));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.sphereVsOrientedBox(new Sphere(min, 0), extreme));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.orientedBoxVsOrientedBox(extreme, new OrientedBox(min, Vector3.ZERO, Quaternion.identity())));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.boxVsOrientedBox(new AxisAlignedBox(min, max), unit));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.segmentVsOrientedBoxInterval(new Segment3(min, max), unit));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.segmentVsOrientedBoxInterval(new Segment3(new Vector3(Double.NaN, 0, 0), Vector3.ZERO), unit));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.rayVsOrientedBoxInterval(new Ray(Vector3.ZERO, new Vector3(1, Double.MIN_VALUE, 0)), unit));
        assertThrows(IllegalArgumentException.class, () -> new IntersectionInterval(2, 1));
        assertThrows(IllegalArgumentException.class, () -> new IntersectionInterval(Double.NaN, 1));
    }

    private static boolean referenceIntersects(OrientedBox a, OrientedBox b) {
        return verticesOrEdgesIntersect(a, b) || verticesOrEdgesIntersect(b, a);
    }

    private static boolean verticesOrEdgesIntersect(OrientedBox a, OrientedBox b) {
        Vector3[] corners = corners(a);
        for (int i = 0; i < 8; i++) {
            if (inside(corners[i], b)) return true;
            for (int bit = 1; bit <= 4; bit *= 2) {
                if ((i & bit) == 0 && clipEdge(corners[i], corners[i | bit], b)) return true;
            }
        }
        return false;
    }

    private static boolean inside(Vector3 point, OrientedBox box) {
        Vector3 p = box.orientation().conjugate().rotate(point.sub(box.center()));
        return Math.abs(p.x()) <= box.halfExtents().x() && Math.abs(p.y()) <= box.halfExtents().y() && Math.abs(p.z()) <= box.halfExtents().z();
    }

    // Clip each edge against six world-space half-planes; this reference never uses separating axes or production queries.
    private static boolean clipEdge(Vector3 start, Vector3 end, OrientedBox box) {
        double enter = 0, exit = 1;
        Vector3[] axes = axes(box);
        for (int axis = 0; axis < 3; axis++) {
            for (int sign : new int[]{-1, 1}) {
                Vector3 normal = axes[axis].mul(sign);
                double first = normal.dot(start.sub(box.center())) - component(box.halfExtents(), axis);
                double last = normal.dot(end.sub(box.center())) - component(box.halfExtents(), axis);
                if (first > 0 && last > 0) return false;
                if (first > 0) enter = Math.max(enter, first/(first-last));
                if (last > 0) exit = Math.min(exit, first/(first-last));
            }
        }
        return enter <= exit;
    }

    private static boolean faceProjectionsOverlap(OrientedBox a, OrientedBox b) {
        Vector3[] av = corners(a), bv = corners(b);
        for (OrientedBox box : new OrientedBox[]{a, b}) {
            for (Vector3 axis : axes(box)) {
                double amin = Double.POSITIVE_INFINITY, amax = Double.NEGATIVE_INFINITY;
                double bmin = Double.POSITIVE_INFINITY, bmax = Double.NEGATIVE_INFINITY;
                for (int i = 0; i < 8; i++) {
                    amin = Math.min(amin, av[i].dot(axis)); amax = Math.max(amax, av[i].dot(axis));
                    bmin = Math.min(bmin, bv[i].dot(axis)); bmax = Math.max(bmax, bv[i].dot(axis));
                }
                if (amax < bmin || bmax < amin) return false;
            }
        }
        return true;
    }

    private static Vector3[] axes(OrientedBox box) {
        return new Vector3[]{box.orientation().rotate(new Vector3(1, 0, 0)), box.orientation().rotate(new Vector3(0, 1, 0)),
                box.orientation().rotate(new Vector3(0, 0, 1))};
    }

    private static Vector3[] corners(OrientedBox box) {
        Vector3[] corners = new Vector3[8];
        Vector3 h = box.halfExtents();
        for (int i = 0; i < 8; i++) {
            Vector3 local = new Vector3((i & 1) == 0 ? -h.x() : h.x(), (i & 2) == 0 ? -h.y() : h.y(), (i & 4) == 0 ? -h.z() : h.z());
            corners[i] = box.center().add(box.orientation().rotate(local));
        }
        return corners;
    }

    private static AxisAlignedBox bounds(OrientedBox box) {
        Vector3 min = box.center(), max = min;
        for (Vector3 corner : corners(box)) { min = min.min(corner); max = max.max(corner); }
        return new AxisAlignedBox(min, max);
    }

    private static OrientedBox randomBox(Random random) {
        Vector3 h = new Vector3(0.1 + random.nextDouble()*1.5, 0.1 + random.nextDouble()*1.5, 0.1 + random.nextDouble()*1.5);
        return new OrientedBox(vector(random, 4), h, Quaternion.fromAxisAngle(vector(random, 2), random.nextDouble()*Math.PI));
    }

    private static Vector3 vector(Random random, double width) {
        return new Vector3((random.nextDouble()-0.5)*width, (random.nextDouble()-0.5)*width, (random.nextDouble()-0.5)*width);
    }

    private static OrientedBox transform(OrientedBox box, Quaternion rotation, Vector3 shift) {
        return new OrientedBox(rotation.rotate(box.center()).add(shift), box.halfExtents(), rotation.mul(box.orientation()));
    }

    private static double component(Vector3 v, int axis) { return axis == 0 ? v.x() : axis == 1 ? v.y() : v.z(); }
    private static void assertVector(Vector3 expected, Vector3 actual) { assertTrue(expected.distance(actual) < 1e-12, actual.toString()); }
    private static void assertInterval(double enter, double exit, IntersectionInterval actual) {
        assertTrue(actual.hit());
        assertEquals(enter, actual.tEnter(), 1e-12);
        assertEquals(exit, actual.tExit(), 1e-12);
    }
    private static void assertMiss(IntersectionInterval interval) {
        assertFalse(interval.hit());
        assertEquals(Double.POSITIVE_INFINITY, interval.tEnter());
        assertEquals(Double.NEGATIVE_INFINITY, interval.tExit());
    }
}
