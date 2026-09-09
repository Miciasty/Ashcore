package nsk.nu.ashcore.api.random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeightedSamplerBoundaryTest {

    @Test
    void sampler_preservesRatiosWhenSumOrProductOverflows() {
        for (double scale : new double[]{1e308, Double.MAX_VALUE, 1e-300}) {
            WeightedSampler sampler = WeightedSampler.build(new double[]{scale, scale * 0.25, 0});
            DeterministicRandom rng = DeterministicRandoms.splitMix64(42);
            int first = 0;
            for (int i = 0; i < 30000; i++) {
                int index = sampler.sampleIndex(rng);
                assertNotEquals(2, index);
                if (index == 0) first++;
            }
            assertEquals(24000, first, 400);
        }
    }

    @Test
    void sampler_rejectsInvalidWeightsAndOwnsItsTables() {
        for (double weight : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> WeightedSampler.build(new double[]{1, weight}));
        }
        assertThrows(IllegalArgumentException.class, () -> WeightedSampler.build(new double[0]));
        assertThrows(IllegalArgumentException.class, () -> WeightedSampler.build(new double[]{0, 0}));
        double[] weights = {0, 1};
        WeightedSampler sampler = WeightedSampler.build(weights);
        weights[0] = 100;
        DeterministicRandom rng = DeterministicRandoms.splitMix64(0);
        for (int i = 0; i < 100; i++) assertEquals(1, sampler.sampleIndex(rng));
    }
}
