package nsk.nu.ashcore.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathMatrixQuaternionApiTest {

    @Test
    void matrix2_inverseAndMultiplication_followLinearAlgebraContract() {
        // We test that A * A^-1 is identity for a non-singular 2x2 matrix.
        Matrix2 a = Matrix2.ofRows(2, 1, 1, 1);
        Matrix2 inv = a.inverse();
        Matrix2 id = a.mul(inv);

        assertEquals(1.0, id.m00(), 1e-12);
        assertEquals(0.0, id.m01(), 1e-12);
        assertEquals(0.0, id.m10(), 1e-12);
        assertEquals(1.0, id.m11(), 1e-12);

        double[] out = a.mul(1, 2);
        assertArrayEquals(new double[]{4.0, 3.0}, out, 1e-12);
    }

    @Test
    void matrix2_inverse_throwsOnSingularMatrix() {
        // We test singular matrix rejection path.
        Matrix2 singular = Matrix2.ofRows(1, 2, 2, 4);
        assertThrows(ArithmeticException.class, singular::inverse);
    }

    @Test
    void matrix3_inverseAndMul_withVector_areConsistent() {
        // We test 3x3 inverse and vector multiplication behavior.
        Matrix3 a = Matrix3.ofRows(
                2, 0, 0,
                0, 3, 0,
                0, 0, 4
        );
        Matrix3 inv = a.inverse();
        Matrix3 id = a.mul(inv);

        assertEquals(1.0, id.m00(), 1e-12);
        assertEquals(1.0, id.m11(), 1e-12);
        assertEquals(1.0, id.m22(), 1e-12);

        Vector3 v = a.mul(new Vector3(1, 2, 3));
        assertEquals(new Vector3(2, 6, 12), v);
    }

    @Test
    void matrix3_inverse_throwsOnSingularMatrix() {
        // We test singular matrix rejection path for 3x3 inverse.
        Matrix3 singular = Matrix3.ofRows(
                1, 2, 3,
                2, 4, 6,
                0, 0, 1
        );
        assertThrows(ArithmeticException.class, singular::inverse);
    }

    @Test
    void matrix4_inverseAndInverseAffine_produceExpectedIdentity() {
        // We test full and affine inverse paths on a valid affine transform.
        Matrix4 m = Matrix4.ofRows(
                2, 0, 0, 1,
                0, 3, 0, 2,
                0, 0, 4, 3,
                0, 0, 0, 1
        );

        Matrix4 inv = m.inverse();
        Matrix4 invAffine = m.inverseAffine();
        Matrix4 idFull = m.mul(inv);
        Matrix4 idAff = m.mul(invAffine);

        assertIdentity4(idFull, 1e-12);
        assertIdentity4(idAff, 1e-12);
        assertEquals(m.determinant(), 24.0, 1e-12);
    }

    @Test
    void matrix4_inverseAffine_rejectsNonAffineMatrix() {
        // We test validation for matrices whose last row is not [0,0,0,1].
        Matrix4 nonAffine = Matrix4.ofRows(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                1, 0, 0, 1
        );
        assertThrows(IllegalArgumentException.class, nonAffine::inverseAffine);
    }

    @Test
    void matrix4_inverse_throwsOnSingularMatrix() {
        // We test singular 4x4 inverse rejection.
        Matrix4 singular = Matrix4.ofRows(
                1, 2, 3, 4,
                2, 4, 6, 8,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
        assertThrows(ArithmeticException.class, singular::inverse);
    }

    @Test
    void quaternion_rotateAndSlerp_followExpectedSemantics() {
        // We test axis-angle rotation and slerp endpoint correctness.
        Quaternion q = Quaternion.fromAxisAngle(new Vector3(0, 1, 0), Math.PI / 2.0).normalized();
        Vector3 rotated = q.rotate(new Vector3(1, 0, 0));

        assertEquals(0.0, rotated.x(), 1e-9);
        assertEquals(0.0, rotated.y(), 1e-9);
        assertEquals(-1.0, rotated.z(), 1e-9);

        Quaternion a = Quaternion.identity();
        Quaternion b = Quaternion.fromAxisAngle(new Vector3(0, 0, 1), Math.PI);

        assertEquals(a, Quaternion.slerp(a, b, 0.0));
        Quaternion atOne = Quaternion.slerp(a, b, 1.0);
        assertEquals(b.normalized().w(), atOne.w(), 1e-9);
        assertEquals(b.normalized().x(), atOne.x(), 1e-9);
        assertEquals(b.normalized().y(), atOne.y(), 1e-9);
        assertEquals(b.normalized().z(), atOne.z(), 1e-9);
    }

    private static void assertIdentity4(Matrix4 m, double eps) {
        assertEquals(1.0, m.m00(), eps);
        assertEquals(0.0, m.m01(), eps);
        assertEquals(0.0, m.m02(), eps);
        assertEquals(0.0, m.m03(), eps);

        assertEquals(0.0, m.m10(), eps);
        assertEquals(1.0, m.m11(), eps);
        assertEquals(0.0, m.m12(), eps);
        assertEquals(0.0, m.m13(), eps);

        assertEquals(0.0, m.m20(), eps);
        assertEquals(0.0, m.m21(), eps);
        assertEquals(1.0, m.m22(), eps);
        assertEquals(0.0, m.m23(), eps);

        assertEquals(0.0, m.m30(), eps);
        assertEquals(0.0, m.m31(), eps);
        assertEquals(0.0, m.m32(), eps);
        assertEquals(1.0, m.m33(), eps);
    }
}

