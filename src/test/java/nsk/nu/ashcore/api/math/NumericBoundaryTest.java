package nsk.nu.ashcore.api.math;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.collision.CollisionTests;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumericBoundaryTest {

    @Test
    void tolerance_requiresFiniteNonNegativeEpsilon() {
        for (double eps : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> NumericTolerance.near(1, 1, eps));
            assertThrows(IllegalArgumentException.class, () -> NumericTolerance.isZero(0, eps));
        }
        assertTrue(NumericTolerance.isZero(1e-12, 1e-12));
        assertFalse(NumericTolerance.isZero(Math.nextUp(1e-12), 1e-12));
        assertTrue(NumericTolerance.near(1, 1, 0));
        assertFalse(NumericTolerance.near(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0));
        assertFalse(NumericTolerance.isZero(Double.NaN, 1));
    }

    @Test
    void matrixInverse_rejectsNonFiniteAndSingularInputs() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY}) {
            Matrix4 matrix = new Matrix4(1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,value);
            assertThrows(IllegalArgumentException.class, matrix::inverse);
            assertThrows(IllegalArgumentException.class, matrix::inverseAffine);
        }
        Matrix4 singular = new Matrix4(0,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1);
        assertThrows(ArithmeticException.class, singular::inverse);
        assertThrows(ArithmeticException.class, singular::inverseAffine);
    }

    @Test
    void matrix_distinguishesPointsDirectionsAndCompositionOrder() {
        Matrix4 translate = new Matrix4(1,0,0,2, 0,1,0,3, 0,0,1,4, 0,0,0,1);
        Matrix4 scale = new Matrix4(2,0,0,0, 0,3,0,0, 0,0,4,0, 0,0,0,1);
        Matrix4 combined = translate.mul(scale);
        assertArrayEquals(new double[]{4, 6, 8, 1}, combined.mul(1, 1, 1, 1), 1e-12);
        assertArrayEquals(new double[]{2, 3, 4, 0}, combined.mul(1, 1, 1, 0), 1e-12);
        assertArrayEquals(new double[]{1, 1, 1, 1}, combined.inverseAffine().mul(4, 6, 8, 1), 1e-12);
        assertArrayEquals(new double[]{1, 1, 1, 1}, combined.inverse().mul(4, 6, 8, 1), 1e-12);
    }

    @Test
    void box_retainsNonFiniteBoundsForAdaptersButQueriesRejectThem() {
        AxisAlignedBox point = new AxisAlignedBox(Vector3.ZERO, Vector3.ZERO);
        assertTrue(point.contains(Vector3.ZERO));
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY}) {
            Vector3 invalid = new Vector3(value, 0, 0);
            assertFalse(point.contains(invalid));
            AxisAlignedBox legacy = new AxisAlignedBox(Vector3.ZERO, invalid);
            assertFalse(legacy.contains(Vector3.ZERO));
            assertThrows(IllegalArgumentException.class,
                    () -> CollisionTests.rayVsBoxT(new Ray(Vector3.ZERO, new Vector3(1, 0, 0)), legacy));
        }
    }
}
