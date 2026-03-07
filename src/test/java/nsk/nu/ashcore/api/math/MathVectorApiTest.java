package nsk.nu.ashcore.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathVectorApiTest {

    @Test
    void vector2_supportsArithmeticAndGeometry() {
        // We test core 2D vector operations and geometric helpers.
        Vector2 a = new Vector2(3, 4);
        Vector2 b = new Vector2(1, 2);

        assertEquals(new Vector2(4, 6), a.add(b));
        assertEquals(new Vector2(2, 2), a.sub(b));
        assertEquals(new Vector2(6, 8), a.mul(2));
        assertEquals(new Vector2(1.5, 2.0), a.div(2));
        assertEquals(11.0, a.dot(b), 1e-12);
        assertEquals(2.0, a.crossZ(b), 1e-12);
        assertEquals(25.0, a.lengthSq(), 1e-12);
        assertEquals(5.0, a.length(), 1e-12);
        assertEquals(2.0, a.distance(new Vector2(3, 2)), 1e-12);
        assertEquals(new Vector2(0.6, 0.8), a.normalized());
        assertEquals(new Vector2(-4, 3), a.perpLeft());
        assertEquals(new Vector2(4, -3), a.perpRight());
        assertEquals(new Vector2(2, 3), b.lerp(new Vector2(3, 4), 0.5));
        assertEquals(new Vector2(1, 2), a.min(b));
        assertEquals(new Vector2(3, 4), a.max(b));
    }

    @Test
    void vector2_zeroNormalizationAndDivisionByZero_edgeCases() {
        // We test explicit edge behavior for zero vector normalization and invalid division.
        assertEquals(Vector2.ZERO, Vector2.ZERO.normalized());
        assertThrows(ArithmeticException.class, () -> new Vector2(1, 1).div(0.0));
    }

    @Test
    void vector2i_supportsDiscreteOperationsAndConversions() {
        // We test integer 2D vector arithmetic and float/int conversion helpers.
        Vector2i a = new Vector2i(2, -3);
        Vector2i b = new Vector2i(-1, 4);

        assertEquals(new Vector2i(1, 1), a.add(b));
        assertEquals(new Vector2i(3, -7), a.sub(b));
        assertEquals(new Vector2i(4, -6), a.mul(2));
        assertEquals(-14, a.dot(b));
        assertEquals(5, a.crossZ(b));
        assertEquals(13L, a.lengthSq());
        assertEquals(new Vector2i(9, -3), a.withX(9));
        assertEquals(new Vector2i(2, 9), a.withY(9));

        Vector2 f = new Vector2(1.8, -1.2);
        assertEquals(new Vector2i(1, -2), Vector2i.fromFloor(f));
        assertEquals(new Vector2i(2, -1), Vector2i.fromRound(f));
        assertEquals(new Vector2i(2, -1), Vector2i.fromCeil(f));
        assertEquals(new Vector2(2.0, -3.0), a.toVector2());
    }

    @Test
    void vector3_supportsCoreOperations() {
        // We test 3D vector operations used across geometry and collision layers.
        Vector3 a = new Vector3(1, 0, 0);
        Vector3 b = new Vector3(0, 1, 0);

        assertEquals(new Vector3(1, 1, 0), a.add(b));
        assertEquals(new Vector3(1, -1, 0), a.sub(b));
        assertEquals(new Vector3(2, 0, 0), a.mul(2));
        assertEquals(0.0, a.dot(b), 1e-12);
        assertEquals(new Vector3(0, 0, 1), a.cross(b));
        assertEquals(1.0, a.length(), 1e-12);
        assertEquals(a, a.normalized());
        assertEquals(Vector3.ZERO, Vector3.ZERO.normalized());
    }

    @Test
    void vector3i_supportsArithmeticAndFloatConversions() {
        // We test integer 3D vector APIs and rounding helper constructors.
        Vector3i v = new Vector3i(1, 2, 3);
        Vector3i o = new Vector3i(-1, 0, 1);

        assertEquals(new Vector3i(0, 2, 4), v.add(o));
        assertEquals(new Vector3i(2, 2, 2), v.sub(o));
        assertEquals(new Vector3i(2, 4, 6), v.mul(2));
        assertEquals(2, v.dot(o));
        assertEquals(new Vector3i(2, -4, 2), v.cross(o));
        assertEquals(14L, v.lengthSq());
        assertEquals(new Vector3i(9, 2, 3), v.withX(9));
        assertEquals(new Vector3i(1, 9, 3), v.withY(9));
        assertEquals(new Vector3i(1, 2, 9), v.withZ(9));
        assertEquals(new Vector3(1, 2, 3), v.toVector3());

        Vector3 f = new Vector3(1.8, -1.2, 0.5);
        assertEquals(new Vector3i(1, -2, 0), Vector3i.fromFloor(f));
        assertEquals(new Vector3i(2, -1, 1), Vector3i.fromRound(f));
        assertEquals(new Vector3i(2, -1, 1), Vector3i.fromCeil(f));
    }

    @Test
    void vector4_supportsHomogeneousFriendlyOperations() {
        // We test 4D floating vector operations for matrix and homogeneous workflows.
        Vector4 a = new Vector4(1, 2, 3, 1);
        Vector4 b = new Vector4(2, 1, 0, -1);

        assertEquals(new Vector4(3, 3, 3, 0), a.add(b));
        assertEquals(new Vector4(-1, 1, 3, 2), a.sub(b));
        assertEquals(new Vector4(2, 4, 6, 2), a.mul(2));
        assertEquals(new Vector4(0.5, 1.0, 1.5, 0.5), a.div(2));
        assertEquals(3.0, a.dot(b), 1e-12);
        assertEquals(new Vector3(1, 2, 3), a.xyz());
        assertEquals(new Vector4(1.5, 1.5, 1.5, 0.0), a.lerp(b, 0.5));
        assertEquals(new Vector4(1, 1, 0, -1), a.min(b));
        assertEquals(new Vector4(2, 2, 3, 1), a.max(b));
        assertEquals(new Vector4(9, 2, 3, 1), a.withX(9));
        assertEquals(new Vector4(1, 9, 3, 1), a.withY(9));
        assertEquals(new Vector4(1, 2, 9, 1), a.withZ(9));
        assertEquals(new Vector4(1, 2, 3, 9), a.withW(9));
    }

    @Test
    void vector4i_supportsDiscrete4dOperationsAndConversions() {
        // We test integer 4D arithmetic and conversion helpers.
        Vector4i a = new Vector4i(1, 2, 3, 4);
        Vector4i b = new Vector4i(4, 3, 2, 1);

        assertEquals(new Vector4i(5, 5, 5, 5), a.add(b));
        assertEquals(new Vector4i(-3, -1, 1, 3), a.sub(b));
        assertEquals(new Vector4i(2, 4, 6, 8), a.mul(2));
        assertEquals(20, a.dot(b));
        assertEquals(30L, a.lengthSq());
        assertEquals(new Vector3i(1, 2, 3), a.xyz());
        assertEquals(new Vector4(1, 2, 3, 4), a.toVector4());
        assertEquals(new Vector4i(9, 2, 3, 4), a.withX(9));
        assertEquals(new Vector4i(1, 9, 3, 4), a.withY(9));
        assertEquals(new Vector4i(1, 2, 9, 4), a.withZ(9));
        assertEquals(new Vector4i(1, 2, 3, 9), a.withW(9));
        assertEquals(new Vector4i(1, 2, 3, 4), Vector4i.fromVector3i(new Vector3i(1, 2, 3), 4));

        Vector4 f = new Vector4(1.8, -1.2, 0.5, -0.5);
        assertEquals(new Vector4i(1, -2, 0, -1), Vector4i.fromFloor(f));
        assertEquals(new Vector4i(2, -1, 1, 0), Vector4i.fromRound(f));
        assertEquals(new Vector4i(2, -1, 1, 0), Vector4i.fromCeil(f));
    }
}

