package nsk.nu.ashcore.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VectorNormalizationExtensionTest {

    @Test
    void vector2And4_normalizeExtremeFiniteValues() {
        for (double scale : new double[]{Double.MIN_VALUE, 1e-300, 1e308, Double.MAX_VALUE}) {
            Vector2 v2 = new Vector2(scale, -scale).normalized();
            assertEquals(1, v2.length(), 2e-15);
            assertEquals(1 / Math.sqrt(2), v2.x(), 2e-15);
            assertEquals(-v2.x(), v2.y());
            Vector4 v4 = new Vector4(scale, -scale, scale, -scale).normalized();
            assertEquals(new Vector4(0.5, -0.5, 0.5, -0.5), v4);
            assertEquals(1, v4.length(), 2e-15);
        }
        assertSame(Vector2.ZERO, Vector2.ZERO.normalized());
        assertSame(Vector4.ZERO, Vector4.ZERO.normalized());
    }

    @Test
    void vector2And4_rejectNonFiniteNormalization() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new Vector2(0, value).normalized());
            assertThrows(IllegalArgumentException.class, () -> new Vector4(0, 0, value, 0).normalized());
        }
    }
}
