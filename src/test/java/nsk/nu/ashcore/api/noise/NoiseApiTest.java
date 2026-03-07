package nsk.nu.ashcore.api.noise;

import nsk.nu.ashcore.api.random.DeterministicRandom;
import nsk.nu.ashcore.api.random.DeterministicRandoms;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoiseApiTest {

    @Test
    void perlinNoise_isDeterministicForSameSeed() {
        // We test deterministic contract: same seed and coordinates must yield same value.
        DeterministicRandom r1 = DeterministicRandoms.defaultGenerator(1234L);
        DeterministicRandom r2 = DeterministicRandoms.defaultGenerator(1234L);

        PerlinNoise n1 = new PerlinNoise(r1);
        PerlinNoise n2 = new PerlinNoise(r2);

        assertEquals(n1.sample(1.25, -3.5), n2.sample(1.25, -3.5), 1e-12);
        assertEquals(n1.sample(1.25, -3.5, 7.0), n2.sample(1.25, -3.5, 7.0), 1e-12);
    }

    @Test
    void hashGridNoise_isDeterministicAndBounded() {
        // We test value noise determinism and [0,1) output range for 2D sampling.
        double v1 = HashGridNoise.value2D(10, 20, 99L);
        double v2 = HashGridNoise.value2D(10, 20, 99L);

        assertEquals(v1, v2, 0.0);
        assertTrue(v1 >= 0.0 && v1 < 1.0);
    }

    @Test
    void hashGridNoise_value3d_isDeterministic() {
        // We test deterministic trilinear interpolation over hashed corners.
        double a = HashGridNoise.value3D(1.2, 3.4, -5.6, 42L);
        double b = HashGridNoise.value3D(1.2, 3.4, -5.6, 42L);
        double c = HashGridNoise.value3D(1.2, 3.4, -5.6, 43L);

        assertEquals(a, b, 0.0);
        assertNotEquals(a, c);
    }

    @Test
    void fractalNoise_fbmAndTurbulence_followExpectedAmplitudeSums() {
        // We test deterministic amplitude accumulation using constant base-noise functions.
        Noise2D one2d = (x, y) -> 1.0;
        Noise3D one3d = (x, y, z) -> 1.0;
        Noise2D minusOne2d = (x, y) -> -1.0;

        assertEquals(1.75, FractalNoise.fbm(one2d, 0, 0, 3, 2.0, 0.5), 1e-12);
        assertEquals(1.75, FractalNoise.fbm(one3d, 0, 0, 0, 3, 2.0, 0.5), 1e-12);
        assertEquals(1.75, FractalNoise.turbulence(minusOne2d, 0, 0, 3, 2.0, 0.5), 1e-12);
    }

    @Test
    void fractalNoise_ridge_usesInvertedAbsoluteSignal() {
        // We test ridge formula on a constant noise source for a predictable closed form.
        Noise2D constant = (x, y) -> 0.2;

        // v = 1 - |0.2| = 0.8, v^2 = 0.64, amps: 0.5 + 0.25 + 0.125
        double expected = 0.64 * (0.5 + 0.25 + 0.125);
        double actual = FractalNoise.ridge(constant, 0, 0, 3, 2.0, 0.5);

        assertEquals(expected, actual, 1e-12);
    }
}

