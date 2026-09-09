package nsk.nu.ashcore.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuaternionConversionTest {

    @Test
    void conjugateAndInverse_preserveAlgebraAndUndoRotation() {
        assertEquals(new Quaternion(1, -2, -3, -4), new Quaternion(1, 2, 3, 4).conjugate());
        for (double scale : new double[]{1e-300, 1, 1e300}) {
            Quaternion q = new Quaternion(scale, scale, -scale, scale);
            assertIdentity(q.mul(q.inverse()));
            assertIdentity(q.inverse().mul(q));
        }
        Quaternion q = Quaternion.fromAxisAngle(new Vector3(1, 2, 3), 1.2);
        Vector3 v = new Vector3(3, -2, 7);
        assertVector(v, q.inverse().rotate(q.rotate(v)));
        assertVector(v, q.conjugate().rotate(q.rotate(v)));
    }

    @Test
    void inverse_rejectsZeroNonFiniteAndUnrepresentableResults() {
        assertThrows(ArithmeticException.class, () -> new Quaternion(0, 0, 0, 0).inverse());
        assertThrows(ArithmeticException.class, () -> new Quaternion(Double.MIN_VALUE, 0, 0, 0).inverse());
        assertThrows(IllegalArgumentException.class, () -> new Quaternion(Double.NaN, 0, 0, 0).inverse());
        assertThrows(IllegalArgumentException.class, () -> new Quaternion(1, Double.POSITIVE_INFINITY, 0, 0).inverse());
    }

    @Test
    void matrixConversions_roundTripAllHalfTurnBranchesAndAngles() {
        for (Vector3 axis : new Vector3[]{new Vector3(1, 0, 0), new Vector3(0, 1, 0), new Vector3(0, 0, 1), new Vector3(1, -2, 3)}) {
            for (double angle : new double[]{0, 1e-10, Math.PI / 2, Math.PI, Math.PI + 0.1, -1.2}) {
                Quaternion q = Quaternion.fromAxisAngle(axis, angle);
                Matrix3 m = q.toMatrix3();
                Quaternion from3 = Quaternion.fromMatrix3(m), from4 = Quaternion.fromMatrix4(q.toMatrix4());
                for (Vector3 v : new Vector3[]{new Vector3(1, 0, 0), new Vector3(0, 1, 0), new Vector3(0, 0, 1)}) {
                    assertVector(q.rotate(v), m.mul(v));
                    assertVector(q.rotate(v), from3.rotate(v));
                    assertVector(q.rotate(v), from4.rotate(v));
                }
            }
        }
    }

    @Test
    void matrixConversion_matchesKnownRotationCompositionAndTranslationRules() {
        Quaternion y = Quaternion.fromAxisAngle(new Vector3(0, 1, 0), Math.PI / 2);
        assertVector(new Vector3(0, 0, -1), y.toMatrix3().mul(new Vector3(1, 0, 0)));
        Quaternion x = Quaternion.fromAxisAngle(new Vector3(1, 0, 0), Math.PI / 2);
        Vector3 v = new Vector3(1, 2, 3);
        assertVector(y.mul(x).rotate(v), y.toMatrix3().mul(x.toMatrix3()).mul(v));
        Matrix4 translation = new Matrix4(1,0,0,7, 0,1,0,8, 0,0,1,9, 0,0,0,1);
        assertVector(y.rotate(v), Quaternion.fromMatrix4(translation.mul(y.toMatrix4())).rotate(v));
        assertArrayEquals(new double[]{0, 0, 0, 1}, y.toMatrix4().mul(0, 0, 0, 1));
        assertEquals(Matrix3.identity(), new Quaternion(0, 0, 0, 0).toMatrix3());
        assertVector(new Quaternion(1, 0, 1, 0).normalized().rotate(v), new Quaternion(1e300, 0, 1e300, 0).toMatrix3().mul(v));
    }

    @Test
    void fromMatrix_rejectsScaleShearReflectionPerspectiveAndNonFiniteValues() {
        for (Matrix3 invalid : new Matrix3[]{
                new Matrix3(2,0,0, 0,1,0, 0,0,1),
                new Matrix3(1,1,0, 0,1,0, 0,0,1),
                new Matrix3(-1,0,0, 0,1,0, 0,0,1),
                new Matrix3(Double.NaN,0,0, 0,1,0, 0,0,1),
                new Matrix3(Double.POSITIVE_INFINITY,0,0, 0,1,0, 0,0,1)}) {
            assertThrows(IllegalArgumentException.class, () -> Quaternion.fromMatrix3(invalid));
        }
        assertThrows(IllegalArgumentException.class,
                () -> Quaternion.fromMatrix4(new Matrix4(1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0.1,1)));
        assertThrows(IllegalArgumentException.class,
                () -> Quaternion.fromMatrix4(new Matrix4(1,0,0,Double.NaN, 0,1,0,0, 0,0,1,0, 0,0,0,1)));
        assertDoesNotThrow(() -> Quaternion.fromMatrix3(new Matrix3(1 + 1e-10,0,0, 0,1,0, 0,0,1)));
        assertThrows(IllegalArgumentException.class,
                () -> Quaternion.fromMatrix3(new Matrix3(1 + 1e-8,0,0, 0,1,0, 0,0,1)));
    }

    private static void assertVector(Vector3 expected, Vector3 actual) {
        assertEquals(expected.x(), actual.x(), 2e-12);
        assertEquals(expected.y(), actual.y(), 2e-12);
        assertEquals(expected.z(), actual.z(), 2e-12);
    }

    private static void assertIdentity(Quaternion q) {
        assertEquals(1, q.w(), 2e-15);
        assertEquals(0, q.x(), 2e-15);
        assertEquals(0, q.y(), 2e-15);
        assertEquals(0, q.z(), 2e-15);
    }
}
