package nsk.nu.ashcore.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VectorArithmeticExtensionTest {

    @Test
    void vector3_operationsUseCommonUnitsAndLeaveInputsUnchanged() {
        Vector3 a = new Vector3(1, 2, 3), b = new Vector3(1, 5, 7);
        assertEquals(5, a.distance(b));
        assertEquals(25, a.distanceSq(b));
        assertEquals(14, a.lengthSq());
        assertEquals(new Vector3(0.5, 1, 1.5), a.div(2));
        assertThrows(ArithmeticException.class, () -> a.div(0));
        assertEquals(new Vector3(1, 3.5, 5), a.lerp(b, 0.5));
        assertEquals(new Vector3(1, 8, 11), a.lerp(b, 2));
        assertEquals(a, a.min(b));
        assertEquals(b, a.max(b));
        assertEquals(new Vector3(7, 8, 9), a.withX(7).withY(8).withZ(9));
        assertEquals(new Vector3(1, 2, 3), a);
    }

    @Test
    void vectorDistances_handleExtremeMagnitudesWithoutSquaring() {
        for (double value : new double[]{1e-300, 1e300}) {
            assertEquals(value, Vector2.ZERO.distance(new Vector2(value, 0)));
            assertEquals(value, Vector3.ZERO.distance(new Vector3(0, value, 0)));
            assertEquals(value, Vector4.ZERO.distance(new Vector4(0, 0, 0, value)));
        }
        assertEquals(5, new Vector4(1, 2, 3, 4).distance(new Vector4(1, 2, 6, 8)));
        assertEquals(25, new Vector4(1, 2, 3, 4).distanceSq(new Vector4(1, 2, 6, 8)));
    }
}
