package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.hash.Hash64;
import nsk.nu.ashcore.api.noise.PerlinNoise;
import nsk.nu.ashcore.api.stats.RunningStats;
import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class DeterminismTest {

    @Test
    void splitMix64_matchesFixedStreamAndJdkReference() {
        long[] expected = {0xe220a8397b1dcdafL, 0x6e789e6aa1b965f4L, 0x06c45d188009454fL,
                0xf88bb8a8724c81ecL, 0x1b39896a51a8749bL};
        DeterministicRandom rng = DeterministicRandoms.splitMix64(0);
        SplittableRandom reference = new SplittableRandom(0);
        for (long value : expected) {
            assertEquals(value, rng.nextLong());
            assertEquals(value, reference.nextLong());
        }
        rng = DeterministicRandoms.defaultGenerator(0);
        assertEquals("random:splitmix64", rng.id());
        assertEquals((int) expected[0], rng.nextInt());
        assertEquals((expected[1] >>> 11) * 0x1.0p-53, rng.nextUnitDouble());
        assertEquals(expected[2], rng.nextLong());
    }

    @Test
    void fnv1aAndMix64_matchFixedVectors() {
        assertEquals(0xcbf29ce484222325L, Hash64.fnv1a(""));
        assertEquals(0xaf63dc4c8601ec8cL, Hash64.fnv1a("a"));
        assertEquals(0xa430d84680aabd0bL, Hash64.fnv1a("hello"));
        assertEquals(0x85944171f73967e8L, Hash64.fnv1a("foobar"));
        assertEquals(0, Hash64.mix64(0));
        assertEquals(0xe220a8397b1dcdafL, Hash64.mix64(0x9e3779b97f4a7c15L));
        assertEquals(0xaf8f0165281fc345L, new SeedSequence(1337).derive(""));
        assertEquals(0x10d63351d8a08e18L, new SeedSequence(1337).derive("terrain"));
    }

    @Test
    void noiseConstruction_consumesRngOnceAndSamplingDoesNot() {
        DeterministicRandom rng = DeterministicRandoms.splitMix64(42);
        DeterministicRandom reference = DeterministicRandoms.splitMix64(42);
        PerlinNoise noise = new PerlinNoise(rng);
        PerlinNoise same = new PerlinNoise(DeterministicRandoms.splitMix64(42));
        for (int i = 0; i < 255; i++) reference.nextUnitDouble();
        for (int i = -20; i < 20; i++) {
            assertEquals(same.sample(i / 3.0, 0.25, -0.75), noise.sample(i / 3.0, 0.25, -0.75));
        }
        assertEquals(reference.nextLong(), rng.nextLong());
    }

    @Test
    void runningStats_repeatsForTheSameOrderedSamples() {
        RunningStats a = new RunningStats(), b = new RunningStats();
        for (double value : new double[]{1e12, 2, -4, 0.125, 17}) {
            a.add(value);
            b.add(value);
        }
        assertEquals(a.mean(), b.mean());
        assertEquals(a.variance(), b.variance());
    }
}
