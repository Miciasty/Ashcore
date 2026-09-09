package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.geometry.Segment3;
import nsk.nu.ashcore.api.geometry.Sphere;
import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrimitiveIntersectionTest {
    private final Sphere sphere = new Sphere(Vector3.ZERO, 1);
    private final AxisAlignedBox box = new AxisAlignedBox(Vector3.ZERO, new Vector3(1, 1, 1));

    @Test
    void raySphere_distinguishesEntryTangencyInsideAndBehind() {
        assertEquals(1, CollisionTests.rayVsSphereT(new Ray(new Vector3(-2, 0, 0), new Vector3(2, 0, 0)), sphere), 1e-12);
        assertEquals(2, CollisionTests.rayVsSphereT(new Ray(new Vector3(-2, 1, 0), new Vector3(1, 0, 0)), sphere), 1e-12);
        assertEquals(0, CollisionTests.rayVsSphereT(new Ray(Vector3.ZERO, new Vector3(1, 0, 0)), sphere));
        assertEquals(0, CollisionTests.rayVsSphereT(new Ray(new Vector3(1, 0, 0), new Vector3(1, 0, 0)), sphere));
        assertEquals(Double.POSITIVE_INFINITY,
                CollisionTests.rayVsSphereT(new Ray(new Vector3(2, 0, 0), new Vector3(1, 0, 0)), sphere));
        assertEquals(Double.POSITIVE_INFINITY,
                CollisionTests.rayVsSphereT(new Ray(new Vector3(-2, Math.nextUp(1.0), 0), new Vector3(1, 0, 0)), sphere));
    }

    @Test
    void raySphere_preservesSmallRadiusAtLargeDistanceAndSupportsScaling() {
        assertEquals(1e12 - 1, CollisionTests.rayVsSphereT(new Ray(new Vector3(-1e12, 0, 0), new Vector3(1, 0, 0)), sphere));
        for (double scale : new double[]{1e-150, 1, 1e150, 1e300}) {
            Sphere scaled = new Sphere(Vector3.ZERO, scale);
            double t = CollisionTests.rayVsSphereT(new Ray(new Vector3(-2*scale, 0, 0), new Vector3(1, 0, 0)), scaled);
            assertEquals(1, t / scale, 1e-12);
        }
        Sphere point = new Sphere(Vector3.ZERO, 0);
        assertEquals(2, CollisionTests.rayVsSphereT(new Ray(new Vector3(-2, 0, 0), new Vector3(1, 0, 0)), point));
        assertEquals(Double.POSITIVE_INFINITY,
                CollisionTests.rayVsSphereT(new Ray(new Vector3(-2, 0.01, 0), new Vector3(1, 0, 0)), point));
    }

    @Test
    void sphereSphere_includesTouchingAndIsSymmetric() {
        for (double x : new double[]{0, 1, 2, 3}) {
            Sphere other = new Sphere(new Vector3(x, 0, 0), 1);
            assertEquals(x <= 2, CollisionTests.sphereVsSphere(sphere, other));
            assertEquals(CollisionTests.sphereVsSphere(sphere, other), CollisionTests.sphereVsSphere(other, sphere));
        }
        assertTrue(CollisionTests.sphereVsSphere(new Sphere(Vector3.ZERO, 0), new Sphere(Vector3.ZERO, 0)));
        assertFalse(CollisionTests.sphereVsSphere(sphere, new Sphere(new Vector3(Math.nextUp(2.0), 0, 0), 1)));
        assertTrue(CollisionTests.sphereVsSphere(new Sphere(Vector3.ZERO, 1e308), new Sphere(new Vector3(1e308, 0, 0), 1e308)));
    }

    @Test
    void sphereBox_testsFaceCornerInsideAndDegenerateShapes() {
        assertTrue(CollisionTests.sphereVsBox(new Sphere(new Vector3(-1, 0.5, 0.5), 1), box));
        assertFalse(CollisionTests.sphereVsBox(new Sphere(new Vector3(-1.01, 0.5, 0.5), 1), box));
        assertTrue(CollisionTests.sphereVsBox(new Sphere(new Vector3(-3, -4, 0), 5), box));
        assertFalse(CollisionTests.sphereVsBox(new Sphere(new Vector3(-3, -4, 0), 4.99), box));
        assertTrue(CollisionTests.sphereVsBox(new Sphere(new Vector3(0.5, 0.5, 0.5), 0), box));
        assertTrue(CollisionTests.sphereVsBox(sphere, new AxisAlignedBox(new Vector3(1, 0, 0), new Vector3(1, 0, 0))));
    }

    @Test
    void segmentBox_returnsFractionAndIncludesBothEndpoints() {
        Segment3 segment = new Segment3(new Vector3(-2, 0.5, 0.5), new Vector3(2, 0.5, 0.5));
        double t = CollisionTests.segmentVsBoxT(segment, box);
        assertEquals(0.5, t);
        assertEquals(new Vector3(0, 0.5, 0.5), segment.at(t));
        assertEquals(0.25, CollisionTests.segmentVsBoxT(new Segment3(segment.b(), segment.a()), box));
        assertEquals(1, CollisionTests.segmentVsBoxT(new Segment3(new Vector3(-1, 0, 0), Vector3.ZERO), box));
        assertEquals(0, CollisionTests.segmentVsBoxT(new Segment3(Vector3.ZERO, new Vector3(-1, 0, 0)), box));
        assertEquals(Double.POSITIVE_INFINITY,
                CollisionTests.segmentVsBoxT(new Segment3(new Vector3(-2, 0, 0), new Vector3(-1, 0, 0)), box));
    }

    @Test
    void segmentBox_handlesPointsParallelDirectionsAndTinySegments() {
        assertEquals(0, CollisionTests.segmentVsBoxT(new Segment3(Vector3.ZERO, Vector3.ZERO), box));
        Vector3 outside = new Vector3(2, 2, 2);
        assertEquals(Double.POSITIVE_INFINITY, CollisionTests.segmentVsBoxT(new Segment3(outside, outside), box));
        assertEquals(Double.POSITIVE_INFINITY,
                CollisionTests.segmentVsBoxT(new Segment3(new Vector3(-1, 2, 0), new Vector3(2, 2, 0)), box));
        assertEquals(0.5, CollisionTests.segmentVsBoxT(new Segment3(new Vector3(-1e-300, 0, 0), new Vector3(1e-300, 0, 0)), box));
    }

    @Test
    void primitives_rejectInvalidShapeDataAndUnsupportedDifferences() {
        for (double radius : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new Sphere(Vector3.ZERO, radius));
        }
        assertThrows(NullPointerException.class, () -> new Sphere(null, 1));
        Vector3 invalid = new Vector3(Double.NaN, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> new Sphere(invalid, 1));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.sphereVsBox(sphere, new AxisAlignedBox(invalid, invalid)));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.segmentVsBoxT(new Segment3(invalid, Vector3.ZERO), box));
        Vector3 min = new Vector3(-Double.MAX_VALUE, 0, 0), max = new Vector3(Double.MAX_VALUE, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.segmentVsBoxT(new Segment3(min, max), box));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.rayVsSphereT(new Ray(min, new Vector3(1, 0, 0)), new Sphere(max, 1)));
    }
}
