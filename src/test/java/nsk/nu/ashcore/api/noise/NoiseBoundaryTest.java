package nsk.nu.ashcore.api.noise;

import nsk.nu.ashcore.api.random.DeterministicRandoms;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoiseBoundaryTest {

    @Test
    void noise_rejectsCoordinatesOutsideItsLattice() {
        PerlinNoise perlin = new PerlinNoise(DeterministicRandoms.splitMix64(0));
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, 0x1.0p31, Math.nextDown(-0x1.0p31)}) {
            assertThrows(IllegalArgumentException.class, () -> perlin.sample(value, 0));
            assertThrows(IllegalArgumentException.class, () -> perlin.sample(0, 0, value));
            assertThrows(IllegalArgumentException.class, () -> HashGridNoise.value3D(0, value, 0, 0));
        }
        assertThrows(IllegalArgumentException.class, () -> HashGridNoise.value3D(Integer.MAX_VALUE, 0, 0, 0));
        assertEquals(0.0, perlin.sample(Integer.MIN_VALUE, 0));
        assertTrue(Double.isFinite(perlin.sample(Math.nextDown(0x1.0p31), 0.25)));
        assertTrue(Double.isFinite(HashGridNoise.value3D(Math.nextDown((double) Integer.MAX_VALUE), 0.25, 0.5, 0)));
    }

    @Test
    void fractalNoise_countsCallbacksAndReturnsUnnormalizedSum() {
        int[] calls = {0};
        Noise2D constant = (x, y) -> { calls[0]++; return 1; };
        assertEquals(0, FractalNoise.fbm(constant, 0, 0, 0, 2, 0.5));
        assertEquals(0, calls[0]);
        assertEquals(1.75, FractalNoise.fbm(constant, 0, 0, 3, 2, 0.5));
        assertEquals(3, calls[0]);
    }
}
