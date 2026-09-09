package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollisionBoundaryTest {
    private final AxisAlignedBox unit = new AxisAlignedBox(Vector3.ZERO, new Vector3(1, 1, 1));

    @Test
    void ray_keepsSmallNonZeroDirectionComponents() {
        Ray ray = new Ray(new Vector3(-1, -1e13, 0.5), new Vector3(1e-13, 1, 0));
        AxisAlignedBox target = new AxisAlignedBox(Vector3.ZERO, new Vector3(1, 1e13, 1));
        assertEquals(1e13, CollisionTests.rayVsBoxT(ray, target), 0.01);
    }

    @Test
    void ray_includesContactAndUsesXBeforeYOnCornerTies() {
        Hit hit = CollisionTests.rayVsBoxHit(new Ray(new Vector3(-1, -1, 0), new Vector3(1, 1, 0)), unit);
        assertEquals(Math.sqrt(2), hit.t(), 1e-12);
        assertEquals(new Vector3(-1, 0, 0), hit.normal());
        assertEquals(0.0, CollisionTests.rayVsBoxT(new Ray(Vector3.ZERO, new Vector3(-1, 0, 0)), unit));
        assertEquals(Double.POSITIVE_INFINITY,
                CollisionTests.rayVsBoxT(new Ray(new Vector3(-1, 2, 0), new Vector3(1, 0, 0)), unit));
    }

    @Test
    void sweep_includesEndTimeAndRejectsInvalidLimitsEvenAtRest() {
        AxisAlignedBox moving = new AxisAlignedBox(new Vector3(-2, 0, 0), new Vector3(-1, 1, 1));
        assertTrue(SweptAABB.test(moving, new Vector3(2, 0, 0), unit, 0.5).hit());
        assertFalse(SweptAABB.test(moving, new Vector3(2, 0, 0), unit, Math.nextDown(0.5)).hit());
        assertFalse(SweptAABB.test(moving, Vector3.ZERO, unit, 0).hit());
        assertTrue(SweptAABB.test(unit, Vector3.ZERO, unit, 0).hit());
        for (double limit : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> SweptAABB.test(unit, Vector3.ZERO, unit, limit));
        }
        AxisAlignedBox invalid = new AxisAlignedBox(new Vector3(2, 0, 0), new Vector3(3, Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> SweptAABB.test(invalid, Vector3.ZERO, unit, 1));
    }

    @Test
    void sweep_handlesVelocityWhoseLengthOverflows() {
        AxisAlignedBox point = new AxisAlignedBox(new Vector3(-1, -1, 0), new Vector3(-1, -1, 0));
        SweptAABB.Result result = SweptAABB.test(point, new Vector3(Double.MAX_VALUE, Double.MAX_VALUE, 0), unit, 1);
        assertTrue(result.hit());
        assertEquals(1.0, result.t() * Double.MAX_VALUE, 1e-15);
    }
}
