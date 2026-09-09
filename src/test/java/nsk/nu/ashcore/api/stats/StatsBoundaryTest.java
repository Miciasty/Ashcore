package nsk.nu.ashcore.api.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsBoundaryTest {

    @Test
    void p2_medianTracksDescendingSamples() {
        P2Quantile median = new P2Quantile(0.5);
        for (int i = 10000; i >= 1; i--) median.add(i);
        assertEquals(5000.5, median.estimate(), 5.0);
    }

    @Test
    void p2_handlesConstantSamplesAndBootstrap() {
        P2Quantile quantile = new P2Quantile(0.95);
        for (int i = 0; i < 4; i++) quantile.add(7);
        assertTrue(Double.isNaN(quantile.estimate()));
        for (int i = 0; i < 100; i++) quantile.add(7);
        assertEquals(7.0, quantile.estimate());
    }

    @Test
    void statistics_rejectNonFiniteSamplesWithoutChangingState() {
        RunningStats stats = new RunningStats();
        P2Quantile quantile = new P2Quantile(0.5);
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> stats.add(value));
            assertThrows(IllegalArgumentException.class, () -> quantile.add(value));
        }
        assertEquals(0, stats.count());
        for (int i = 0; i < 5; i++) quantile.add(3);
        assertEquals(3, quantile.estimate());
    }
}
