package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.stats.P2Quantile;
import nsk.nu.ashcore.api.stats.ReservoirSampler;
import nsk.nu.ashcore.api.stats.RunningStats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CounterBoundaryTest {

    @Test
    void halton_rejectsExhaustionBeforeMutationAndCanReset() throws Exception {
        HaltonSequence sequence = new HaltonSequence(2);
        sequence.next();
        setCounter(sequence, "index", Integer.MAX_VALUE);
        assertThrows(IllegalStateException.class, sequence::next);
        assertEquals(Integer.MAX_VALUE, sequence.index());
        sequence.reset();
        assertEquals(0.5, sequence.next());
    }

    @Test
    void statistics_rejectCounterOverflowBeforeChangingResults() throws Exception {
        RunningStats stats = new RunningStats();
        stats.add(7);
        setCounter(stats, "n", Long.MAX_VALUE);
        assertThrows(IllegalStateException.class, () -> stats.add(8));
        assertEquals(7.0, stats.mean());

        P2Quantile quantile = new P2Quantile(0.5);
        for (int i = 0; i < 5; i++) quantile.add(7);
        setCounter(quantile, "n", (long) Integer.MAX_VALUE);
        assertThrows(IllegalStateException.class, () -> quantile.add(8));
        assertEquals(7.0, quantile.estimate());
        quantile.reset();
        assertDoesNotThrow(() -> quantile.add(8));

        ReservoirSampler<Integer> reservoir = new ReservoirSampler<>(1, DeterministicRandoms.splitMix64(0));
        reservoir.offer(7);
        setCounter(reservoir, "seen", Long.MAX_VALUE);
        assertThrows(IllegalStateException.class, () -> reservoir.offer(8));
        assertEquals(7, reservoir.snapshot().getFirst());
        reservoir.reset();
        assertDoesNotThrow(() -> reservoir.offer(8));
    }

    // Reach limits without billions of calls; assertions check public behavior and preserved state.
    private static void setCounter(Object instance, String name, Object value) throws Exception {
        var field = instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }
}
