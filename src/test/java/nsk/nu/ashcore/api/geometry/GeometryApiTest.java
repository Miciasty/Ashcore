package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.Vector2;
import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeometryApiTest {

    @Test
    void axisAlignedBox_contains_includesBoundary() {
        // We test inclusive boundary semantics documented by AxisAlignedBox.contains.
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(1, 1, 1));

        assertTrue(box.contains(new Vector3(0, 0, 0)));
        assertTrue(box.contains(new Vector3(1, 1, 1)));
        assertFalse(box.contains(new Vector3(1.01, 0.5, 0.5)));
    }

    @Test
    void axisAlignedBox_constructor_rejectsInvertedBounds() {
        // We test constructor guard for min<=max invariant on all axes.
        assertThrows(IllegalArgumentException.class,
                () -> new AxisAlignedBox(new Vector3(2, 0, 0), new Vector3(1, 1, 1)));
    }

    @Test
    void axisAlignedRect_of_normalizesAndProvidesDerivedValues() {
        // We test normalization and derived geometry metrics (size, area, center, clamp).
        AxisAlignedRect rect = AxisAlignedRect.of(new Vector2(4, 1), new Vector2(1, 5));

        assertEquals(new Vector2(1, 1), rect.min());
        assertEquals(new Vector2(4, 5), rect.max());
        assertEquals(3.0, rect.width(), 1e-12);
        assertEquals(4.0, rect.height(), 1e-12);
        assertEquals(12.0, rect.area(), 1e-12);
        assertEquals(new Vector2(2.5, 3.0), rect.center());
        assertEquals(new Vector2(4, 2), rect.clamp(new Vector2(9, 2)));
    }

    @Test
    void axisAlignedRect_constructor_rejectsInvertedBounds() {
        // We test constructor guard for min<=max invariant.
        assertThrows(IllegalArgumentException.class,
                () -> new AxisAlignedRect(new Vector2(2, 0), new Vector2(1, 1)));
    }

    @Test
    void boxes_helpers_coverUnionExpandAndOverlap() {
        // We test basic AABB helper behavior used by broad-phase logic.
        AxisAlignedBox a = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(1, 1, 1));
        AxisAlignedBox b = new AxisAlignedBox(new Vector3(1, 1, 1), new Vector3(2, 2, 2));

        AxisAlignedBox u = Boxes.union(a, b);
        AxisAlignedBox e = Boxes.expand(a, 0.5);

        assertEquals(new Vector3(0, 0, 0), u.min());
        assertEquals(new Vector3(2, 2, 2), u.max());
        assertEquals(new Vector3(-0.5, -0.5, -0.5), e.min());
        assertEquals(new Vector3(1.5, 1.5, 1.5), e.max());
        assertTrue(Boxes.overlaps(a, b)); // touching boundary is inclusive overlap
    }

    @Test
    void capsule_distanceSq_zeroInsideAndDegenerateSegmentHandled() {
        // We test inside-distance clamp to zero and degenerate capsule behavior.
        Capsule c = new Capsule(new Vector3(0, 0, 0), new Vector3(0, 2, 0), 1.0);
        Capsule degenerate = new Capsule(new Vector3(0, 0, 0), new Vector3(0, 0, 0), 1.0);

        assertEquals(0.0, c.distanceSqTo(new Vector3(0.5, 1.0, 0.0)), 1e-12);
        assertEquals(1.25, degenerate.distanceSqTo(new Vector3(1.5, 0, 0)), 1e-12);
    }

    @Test
    void capsule_rayIntersectT_hitsSphereWhenSegmentDegenerate() {
        // We test ray hit against degenerate capsule which falls back to sphere intersection.
        Capsule degenerate = new Capsule(new Vector3(0, 0, 0), new Vector3(0, 0, 0), 1.0);
        Ray ray = new Ray(new Vector3(-2, 0, 0), new Vector3(1, 0, 0));

        assertEquals(1.0, degenerate.rayIntersectT(ray), 1e-12);
    }

    @Test
    void geometryUtils_reflectProjectRejectAndClosestPoint() {
        // We test vector decomposition helpers and closest-point clamping on segment.
        Vector3 v = new Vector3(1, -1, 0);
        Vector3 n = new Vector3(0, 1, 0);

        assertEquals(new Vector3(1, 1, 0), GeometryUtils.reflect(v, n));
        Vector3 projected = GeometryUtils.project(v, n);
        Vector3 rejected = GeometryUtils.reject(v, n);
        assertVector3Equals(new Vector3(0, -1, 0), projected, 1e-12);
        assertVector3Equals(new Vector3(1, 0, 0), rejected, 1e-12);

        Vector3 cp = GeometryUtils.closestPointOnSegment(new Vector3(0, 0, 0), new Vector3(2, 0, 0), new Vector3(3, 1, 0));
        assertEquals(new Vector3(2, 0, 0), cp);
    }

    @Test
    void orthonormalBasis_fromNormal_producesOrthogonalUnitVectors() {
        // We test TBN frame validity and local-to-world mapping for local up vector.
        OrthonormalBasis basis = OrthonormalBasis.fromNormal(new Vector3(0, 1, 0));
        Vector3 t = basis.tangent();
        Vector3 n = basis.normal();
        Vector3 b = basis.bitangent();

        assertEquals(1.0, t.length(), 1e-12);
        assertEquals(1.0, n.length(), 1e-12);
        assertEquals(1.0, b.length(), 1e-12);
        assertEquals(0.0, t.dot(n), 1e-12);
        assertEquals(0.0, n.dot(b), 1e-12);
        assertEquals(0.0, t.dot(b), 1e-12);
        assertEquals(n, basis.toWorld(new Vector3(0, 1, 0)));
    }

    @Test
    void plane_constructor_normalizesAndProjectWorks() {
        // We test Hessian normalization contract and projection onto the plane.
        Plane p = new Plane(new Vector3(0, 2, 0), -2.0); // equivalent to y=1 after normalization

        assertEquals(1.0, p.normal().length(), 1e-12);
        assertEquals(0.0, p.distanceTo(new Vector3(5, 1, -3)), 1e-12);
        assertEquals(new Vector3(2, 1, 4), p.project(new Vector3(2, 3, 4)));
    }

    @Test
    void ray_constructor_normalizesDirection_andAtEvaluatesPoint() {
        // We test direction normalization in constructor and parametric evaluation.
        Ray ray = new Ray(new Vector3(1, 2, 3), new Vector3(10, 0, 0));

        assertEquals(new Vector3(1, 0, 0), ray.direction());
        assertEquals(new Vector3(4, 2, 3), ray.at(3.0));
        assertThrows(IllegalArgumentException.class, () -> new Ray(Vector3.ZERO, Vector3.ZERO));
    }

    @Test
    void segment3_andSphere_records_exposeExpectedState() {
        // We test basic record behavior and helper methods for segment and sphere.
        Segment3 s = new Segment3(new Vector3(0, 0, 0), new Vector3(0, 3, 4));
        Sphere sp = new Sphere(new Vector3(1, 2, 3), 2.5);

        assertEquals(5.0, s.length(), 1e-12);
        assertEquals(new Vector3(0, 1.5, 2), s.at(0.5));
        assertEquals(new Vector3(1, 2, 3), sp.center());
        assertEquals(2.5, sp.radius(), 1e-12);
    }

    private static void assertVector3Equals(Vector3 expected, Vector3 actual, double eps) {
        assertEquals(expected.x(), actual.x(), eps);
        assertEquals(expected.y(), actual.y(), eps);
        assertEquals(expected.z(), actual.z(), eps);
    }
}
