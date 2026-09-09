package nsk.nu.ashcore.api.math;

import nsk.nu.ashcore.api.geometry.Plane;
import nsk.nu.ashcore.api.geometry.Ray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizationTest {

    @Test
    void ray_normalizesFiniteDirectionsAcrossDoubleRange() {
        for (double scale : new double[]{1e308, Double.MAX_VALUE, 1e-300, Double.MIN_VALUE}) {
            Ray ray = new Ray(new Vector3(1, 2, 3), new Vector3(scale, 0, 0));
            assertEquals(new Vector3(1, 0, 0), ray.direction());
            assertEquals(new Vector3(4, 2, 3), ray.at(3));

            Vector3 diagonal = new Vector3(scale, -scale, scale).normalized();
            assertEquals(1.0, diagonal.length(), 2e-15);
            assertEquals(1.0 / Math.sqrt(3), diagonal.x(), 2e-15);
            assertEquals(-diagonal.x(), diagonal.y());
            assertEquals(diagonal.x(), diagonal.z());
        }
        assertSame(Vector3.ZERO, Vector3.ZERO.normalized());
        assertThrows(IllegalArgumentException.class, () -> new Ray(Vector3.ZERO, Vector3.ZERO));
    }

    @Test
    void normalization_rejectsNonFiniteComponents() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            Vector3 v = new Vector3(1, value, 0);
            assertThrows(IllegalArgumentException.class, v::normalized);
            assertThrows(IllegalArgumentException.class, () -> new Ray(Vector3.ZERO, v));
            assertThrows(IllegalArgumentException.class, () -> new Ray(v, new Vector3(1, 0, 0)));
            assertThrows(IllegalArgumentException.class, () -> new Quaternion(1, 0, value, 0).normalized());
            assertThrows(IllegalArgumentException.class, () -> Quaternion.fromAxisAngle(new Vector3(1, 0, 0), value));
        }
    }

    @Test
    void quaternion_preservesZeroFallbackAndExtremeRotations() {
        assertEquals(Quaternion.identity(), new Quaternion(0, 0, 0, 0).normalized());
        assertThrows(IllegalArgumentException.class, () -> Quaternion.fromAxisAngle(Vector3.ZERO, 1));
        for (double scale : new double[]{1e308, Double.MIN_VALUE}) {
            Quaternion q = new Quaternion(scale, 0, scale, 0).normalized();
            assertEquals(1.0, q.w()*q.w() + q.y()*q.y(), 2e-15);
            Vector3 rotated = q.rotate(new Vector3(1, 0, 0));
            assertEquals(0.0, rotated.x(), 2e-15);
            assertEquals(-1.0, rotated.z(), 2e-15);
            Quaternion axis = Quaternion.fromAxisAngle(new Vector3(0, scale, 0), Math.PI / 2);
            assertEquals(rotated.z(), axis.rotate(new Vector3(1, 0, 0)).z(), 2e-15);
        }
    }

    @Test
    void plane_normalizesNormalAndOffsetTogether() {
        for (double scale : new double[]{1e308, Double.MIN_VALUE}) {
            Plane plane = new Plane(new Vector3(0, scale, 0), -scale);
            assertEquals(new Vector3(0, 1, 0), plane.normal());
            assertEquals(0.0, plane.distanceTo(new Vector3(2, 1, 4)));
            Plane diagonal = new Plane(new Vector3(scale, scale, 0), -scale);
            assertEquals(0.0, diagonal.distanceTo(new Vector3(1, 0, 0)), 2e-15);
        }
    }
}
