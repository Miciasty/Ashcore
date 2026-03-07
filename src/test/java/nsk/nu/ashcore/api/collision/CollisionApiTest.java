package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Plane;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollisionApiTest {

    @Test
    void rayVsBoxT_returnsFiniteDistanceOnHit() {
        // We test the slab intersection contract: hit returns finite positive t.
        Ray ray = new Ray(new Vector3(-2, 0, 0), new Vector3(1, 0, 0));
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, -1, -1), new Vector3(1, 1, 1));

        double t = CollisionTests.rayVsBoxT(ray, box);

        assertEquals(2.0, t, 1e-12);
    }

    @Test
    void rayVsBoxT_returnsInfinityOnMiss() {
        // We test the miss path: no overlap in ray direction means +INF.
        Ray ray = new Ray(new Vector3(-2, 3, 0), new Vector3(1, 0, 0));
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, -1, -1), new Vector3(1, 1, 1));

        assertEquals(Double.POSITIVE_INFINITY, CollisionTests.rayVsBoxT(ray, box));
    }

    @Test
    void rayVsBoxHit_fromInside_returnsTZeroAndExitNormal() {
        // We test inside-origin behavior: hit time is clamped to 0 and normal points to exit face.
        Ray ray = new Ray(new Vector3(0.5, 0.0, 0.0), new Vector3(1, 0, 0));
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, -1, -1), new Vector3(1, 1, 1));

        Hit hit = CollisionTests.rayVsBoxHit(ray, box);

        assertTrue(hit.hit());
        assertEquals(0.0, hit.t(), 1e-12);
        assertEquals(new Vector3(1, 0, 0), hit.normal());
        assertEquals(ray.origin(), hit.point());
    }

    @Test
    void collisionUtils_closestPointAndDistanceSq_areConsistent() {
        // We test that clamp point lies on boundary and distanceSq matches Euclidean delta.
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(2, 2, 2));
        Vector3 p = new Vector3(3, 1, -1);

        Vector3 closest = CollisionUtils.closestPointOnBox(p, box);
        double d2 = CollisionUtils.distanceSqPointBox(p, box);

        assertEquals(new Vector3(2, 1, 0), closest);
        assertEquals(2.0, d2, 1e-12);
    }

    @Test
    void collisionUtils_rayVsPlaneT_handlesParallelAndForwardHit() {
        // We test two branches: parallel ray returns +INF, forward ray returns finite t.
        Plane plane = new Plane(new Vector3(0, 1, 0), 0.0); // y = 0
        Ray parallel = new Ray(new Vector3(0, 1, 0), new Vector3(1, 0, 0));
        Ray down = new Ray(new Vector3(0, 1, 0), new Vector3(0, -1, 0));

        assertEquals(Double.POSITIVE_INFINITY, CollisionUtils.rayVsPlaneT(parallel, plane));
        assertEquals(1.0, CollisionUtils.rayVsPlaneT(down, plane), 1e-12);
    }

    @Test
    void hit_hitFlagReflectsFiniteParameter() {
        // We test the record helper: finite t means hit, +INF means no hit.
        Hit ok = new Hit(0.25, new Vector3(0, 0, 0), new Vector3(0, 1, 0));
        Hit miss = new Hit(Double.POSITIVE_INFINITY, null, null);

        assertTrue(ok.hit());
        assertFalse(miss.hit());
    }

    @Test
    void sweptAabb_movingBox_hitsWithinTMax() {
        // We test continuous collision in time units: t is distance/speed and must fit tMax.
        AxisAlignedBox moving = new AxisAlignedBox(new Vector3(-2, 0, 0), new Vector3(-1, 1, 1));
        AxisAlignedBox target = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(1, 1, 1));
        Vector3 velocity = new Vector3(2, 0, 0);

        SweptAABB.Result result = SweptAABB.test(moving, velocity, target, 1.0);

        assertTrue(result.hit());
        assertEquals(0.5, result.t(), 1e-12);
        assertEquals(new Vector3(-1, 0, 0), result.normal());
    }

    @Test
    void sweptAabb_zeroVelocity_reportsOverlapAtTimeZero() {
        // We test zero-speed special case: already overlapping returns hit at t=0.
        AxisAlignedBox a = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(1, 1, 1));
        AxisAlignedBox b = new AxisAlignedBox(new Vector3(0.5, 0, 0), new Vector3(1.5, 1, 1));

        SweptAABB.Result result = SweptAABB.test(a, Vector3.ZERO, b, 10.0);

        assertTrue(result.hit());
        assertEquals(0.0, result.t(), 1e-12);
        assertNull(result.normal());
    }
}

