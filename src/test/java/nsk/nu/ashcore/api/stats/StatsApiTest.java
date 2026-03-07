package nsk.nu.ashcore.api.stats;

import nsk.nu.ashcore.api.random.DeterministicRandoms;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatsApiTest {

    @Test
    void exponentialMovingAverage_validatesAlphaAndUpdatesState() {
        // We test alpha guard, initialization semantics, and recursive EMA update.
        assertThrows(IllegalArgumentException.class, () -> new ExponentialMovingAverage(0.0));
        assertThrows(IllegalArgumentException.class, () -> new ExponentialMovingAverage(1.1));

        ExponentialMovingAverage ema = new ExponentialMovingAverage(0.5);
        assertTrue(Double.isNaN(ema.value()));
        assertEquals(10.0, ema.add(10.0), 1e-12);
        assertEquals(15.0, ema.add(20.0), 1e-12);
    }

    @Test
    void exponentialMovingAverage_withHalfLife_createsOperationalInstance() {
        // We test convenience factory and finite output guarantee.
        ExponentialMovingAverage ema = ExponentialMovingAverage.withHalfLife(4.0);
        double value = ema.add(42.0);
        assertTrue(Double.isFinite(value));
    }

    @Test
    void runningStats_computesMeanAndVariance() {
        // We test Welford one-pass statistics on a simple known sequence.
        RunningStats stats = new RunningStats();
        assertTrue(Double.isNaN(stats.mean()));
        assertTrue(Double.isNaN(stats.variance()));

        stats.add(1.0);
        stats.add(2.0);
        stats.add(3.0);

        assertEquals(3, stats.count());
        assertEquals(2.0, stats.mean(), 1e-12);
        assertEquals(2.0 / 3.0, stats.variance(), 1e-12);
        assertEquals(1.0, stats.sampleVariance(), 1e-12);
    }

    @Test
    void windowedMean_tracksSlidingAverage() {
        // We test fixed-window behavior: before and after buffer saturation.
        assertThrows(IllegalArgumentException.class, () -> new WindowedMean(0));

        WindowedMean mean = new WindowedMean(3);
        assertTrue(Double.isNaN(mean.mean()));

        assertEquals(1.0, mean.add(1.0), 1e-12);
        assertEquals(1.5, mean.add(2.0), 1e-12);
        assertEquals(2.0, mean.add(3.0), 1e-12);
        assertEquals(3.0, mean.add(4.0), 1e-12); // window now [2,3,4]
        assertEquals(3.0, mean.mean(), 1e-12);
    }

    @Test
    void slidingWindowMinMax_tracksCurrentWindowExtrema() {
        // We test amortized deque logic over a moving window.
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowMinMax(0));

        SlidingWindowMinMax mm = new SlidingWindowMinMax(3);
        assertTrue(Double.isNaN(mm.min()));
        assertTrue(Double.isNaN(mm.max()));

        mm.add(3);
        mm.add(1);
        mm.add(2);
        assertEquals(1.0, mm.min(), 1e-12);
        assertEquals(3.0, mm.max(), 1e-12);

        mm.add(4); // window becomes [1,2,4]
        assertEquals(1.0, mm.min(), 1e-12);
        assertEquals(4.0, mm.max(), 1e-12);
    }

    @Test
    void reservoirSampler_tracksCapacityAndSupportsReset() {
        // We test reservoir size accounting, snapshots, and reset semantics.
        assertThrows(IllegalArgumentException.class, () -> new ReservoirSampler<>(0, DeterministicRandoms.defaultGenerator(1)));
        assertThrows(NullPointerException.class, () -> new ReservoirSampler<>(2, null));

        ReservoirSampler<Integer> sampler = new ReservoirSampler<>(2, DeterministicRandoms.defaultGenerator(123L));
        sampler.offer(1);
        sampler.offer(2);
        sampler.offer(3);

        assertEquals(2, sampler.capacity());
        assertEquals(2, sampler.size());
        assertEquals(3, sampler.seenCount());
        assertTrue(sampler.isFull());
        List<Integer> snapshot = sampler.snapshot();
        assertEquals(2, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(99));
        assertEquals(2, sampler.snapshotRaw().length);

        sampler.reset();
        assertEquals(0, sampler.size());
        assertEquals(0, sampler.seenCount());
        assertFalse(sampler.isFull());
    }

    @Test
    void p2Quantile_validatesQ_andProducesEstimateAfterBootstrap() {
        // We test quantile parameter guard, bootstrap NaN phase, estimate, and reset.
        assertThrows(IllegalArgumentException.class, () -> new P2Quantile(0.0));
        assertThrows(IllegalArgumentException.class, () -> new P2Quantile(1.0));

        P2Quantile q50 = new P2Quantile(0.5);
        assertTrue(Double.isNaN(q50.estimate()));

        for (int i = 1; i <= 100; i++) q50.add(i);
        double estimate = q50.estimate();

        // P² is approximate; we assert a conservative band around true median 50.5.
        assertTrue(estimate > 40.0 && estimate < 60.0);

        q50.reset();
        assertTrue(Double.isNaN(q50.estimate()));
    }
}

