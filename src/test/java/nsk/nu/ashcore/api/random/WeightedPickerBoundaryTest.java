package nsk.nu.ashcore.api.random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeightedPickerBoundaryTest {

    @Test
    void picker_neverSelectsZeroWeightAtLowerBoundary() {
        DeterministicRandom rng = DeterministicRandoms.splitMix64(-0x9e3779b97f4a7c15L);
        assertEquals(1, WeightedPicker.pickIndex(new double[]{0, 1}, rng));
    }

    @Test
    void picker_usesHalfOpenIntervalsAndSkipsTrailingZerosAfterRounding() {
        assertEquals(2, WeightedPicker.pickIndex(new double[]{1, 0, 1}, fixed(0.5)));
        assertEquals(0, WeightedPicker.pickIndex(new double[]{Double.MIN_VALUE, 0}, fixed(Math.nextDown(1.0))));
        assertEquals(1, WeightedPicker.pickIndex(new double[]{0, 1, 0}, fixed(Math.nextDown(1.0))));
    }

    private static DeterministicRandom fixed(double value) {
        return new DeterministicRandom() {
            @Override public String id() { return "test:fixed"; }
            @Override public long nextLong() { throw new AssertionError("Only nextUnitDouble is expected"); }
            @Override public double nextUnitDouble() { return value; }
        };
    }
}
