package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.math.Vector2;
import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomApiTest {

    @Test
    void deterministicRandoms_produceReproducibleSequences() {
        // We test deterministic contract for identical seeds and same generator implementation.
        DeterministicRandom a = DeterministicRandoms.defaultGenerator(123L);
        DeterministicRandom b = DeterministicRandoms.defaultGenerator(123L);

        for (int i = 0; i < 10; i++) {
            assertEquals(a.nextLong(), b.nextLong());
        }
    }

    @Test
    void deterministicRandom_defaultMethods_returnExpectedRanges() {
        // We test default nextInt/nextUnitDouble behavior from the interface.
        DeterministicRandom rng = DeterministicRandoms.splitMix64(7L);

        int value = rng.nextInt();
        double unit = rng.nextUnitDouble();

        assertNotEquals(0, value); // non-degenerate smoke assertion
        assertTrue(unit >= 0.0 && unit < 1.0);
    }

    @Test
    void deterministicRandoms_fromDerivedSeed_validatesArguments() {
        // We test explicit null guards for derivation helper.
        SeedSequence seq = new SeedSequence(1L);

        assertThrows(NullPointerException.class, () -> DeterministicRandoms.fromDerivedSeed(null, "x"));
        assertThrows(NullPointerException.class, () -> DeterministicRandoms.fromDerivedSeed(seq, null));
    }

    @Test
    void distributions_coverValidationAndFiniteOutputs() {
        // We test parameter guards and that generated samples are finite.
        DeterministicRandom rng = DeterministicRandoms.defaultGenerator(99L);

        assertThrows(IllegalArgumentException.class, () -> Distributions.exponential(rng, 0.0));
        assertEquals(0, Distributions.poisson(rng, 0.0));

        assertTrue(Double.isFinite(Distributions.gaussian01(rng)));
        assertTrue(Double.isFinite(Distributions.exponential(rng, 2.0)));
        assertTrue(Distributions.poisson(rng, 3.0) >= 0);
    }

    @Test
    void haltonSequence_generatesKnownFirstValuesAndSupportsReset() {
        // We test canonical first values for base-2 radical inverse sequence.
        HaltonSequence seq = new HaltonSequence(2);

        assertEquals(0.5, seq.next(), 1e-12);
        assertEquals(0.25, seq.next(), 1e-12);
        assertEquals(0.75, seq.next(), 1e-12);
        assertEquals(3, seq.index());

        seq.reset();
        assertEquals(0, seq.index());
        assertEquals(0.5, seq.next(), 1e-12);
    }

    @Test
    void halton2d_and_halton3d_returnValuesInExpectedDomains() {
        // We test output-domain contracts for square/cube and mapped directions.
        Halton2DSequence h2 = new Halton2DSequence();
        Vector2 uv = h2.nextUnitSquare();
        assertTrue(uv.x() >= 0.0 && uv.x() < 1.0);
        assertTrue(uv.y() >= 0.0 && uv.y() < 1.0);

        Halton3DSequence h3 = new Halton3DSequence();
        Vector3 cube = h3.nextUnitCube();
        assertTrue(cube.x() >= 0.0 && cube.x() < 1.0);
        assertTrue(cube.y() >= 0.0 && cube.y() < 1.0);
        assertTrue(cube.z() >= 0.0 && cube.z() < 1.0);

        Vector3 sphereDir = h3.nextUnitSphereDirection();
        Vector3 spherePoint = h3.nextUnitSpherePoint();
        Vector3 hemi = h3.nextHemisphereYUp();
        Vector3 cone0 = h3.nextConeYUp(0.0);

        assertEquals(1.0, sphereDir.length(), 1e-9);
        assertTrue(spherePoint.length() <= 1.0 + 1e-9);
        assertTrue(hemi.y() >= 0.0);
        assertEquals(1.0, cone0.y(), 1e-12);
    }

    @Test
    void permutation_shuffle_isDeterministicForSameSeed() {
        // We test deterministic in-place Fisher-Yates behavior.
        int[] a = {1, 2, 3, 4, 5, 6};
        int[] b = {1, 2, 3, 4, 5, 6};

        Permutation.shuffle(a, DeterministicRandoms.defaultGenerator(55L));
        Permutation.shuffle(b, DeterministicRandoms.defaultGenerator(55L));

        assertArrayEquals(a, b);
        Arrays.sort(a);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, a);
    }

    @Test
    void seedSequence_isStableAndTagSensitive() {
        // We test stable derivation for same tag and different output for different tags.
        SeedSequence seq = new SeedSequence(123L);
        long a = seq.derive("alpha");
        long b = seq.derive("alpha");
        long c = seq.derive("beta");

        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    @Test
    void weightedPicker_validatesInputAndSupportsDeterministicChoice() {
        // We test validation paths and deterministic picking with a single non-zero weight.
        DeterministicRandom rng = DeterministicRandoms.defaultGenerator(7L);
        assertThrows(IllegalArgumentException.class, () -> WeightedPicker.pickIndex(new double[]{}, rng));
        assertThrows(NullPointerException.class, () -> WeightedPicker.pickIndex(new double[]{1.0}, null));
        assertThrows(IllegalArgumentException.class, () -> WeightedPicker.pickIndex(new double[]{-1.0, 1.0}, rng));
        assertThrows(IllegalArgumentException.class, () -> WeightedPicker.pickIndex(new double[]{Double.POSITIVE_INFINITY, 1.0}, rng));

        int idx = WeightedPicker.pickIndex(new double[]{0.0, 5.0, 0.0}, rng);
        assertEquals(1, idx);

        String picked = WeightedPicker.pick(List.of("a", "b", "c"), new double[]{0.0, 1.0, 0.0}, rng);
        assertEquals("b", picked);
        assertThrows(NullPointerException.class, () -> WeightedPicker.pick(null, new double[]{1.0}, rng));
        assertThrows(NullPointerException.class, () -> WeightedPicker.pick(List.of("a"), null, rng));
    }

    @Test
    void weightedSampler_buildAndSample_coverContracts() {
        // We test alias-table validation and deterministic selection for degenerate weights.
        assertThrows(IllegalArgumentException.class, () -> WeightedSampler.build(new double[]{}));
        assertThrows(IllegalArgumentException.class, () -> WeightedSampler.build(new double[]{-1.0, 1.0}));
        assertThrows(IllegalArgumentException.class, () -> WeightedSampler.build(new double[]{0.0, 0.0}));

        WeightedSampler sampler = WeightedSampler.build(new double[]{0.0, 3.0, 0.0});
        DeterministicRandom rng = DeterministicRandoms.defaultGenerator(3L);

        for (int i = 0; i < 20; i++) {
            assertEquals(1, sampler.sampleIndex(rng));
        }
    }
}
