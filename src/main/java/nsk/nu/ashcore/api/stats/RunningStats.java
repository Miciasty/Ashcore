package nsk.nu.ashcore.api.stats;

/**
 * One-pass running mean/variance using Welford's algorithm; O(1) time per sample and O(1) memory.
 * Results depend on sample order and floating-point rounding. Not thread-safe.
 * Samples must be finite; differences and accumulated squared deviations must remain representable.
 * Mean uses sample units; variance uses their square. Counts above 2^53 lose precision in double arithmetic.
 */
public final class RunningStats {
    private long n = 0;
    private double mean = 0;
    private double m2 = 0;

    /** Adds a finite sample; rejects non-finite input or Long.MAX_VALUE count before mutation. */
    public void add(double x) {
        if (!Double.isFinite(x)) throw new IllegalArgumentException("Sample must be finite");
        if (n == Long.MAX_VALUE) throw new IllegalStateException("Sample limit reached");
        n++;
        double delta = x - mean;
        mean += delta / n;
        m2 += delta * (x - mean);
    }
    /** @return number of samples seen */
    public long count() { return n; }
    /** @return current mean (NaN if no samples) */
    public double mean() { return n == 0 ? Double.NaN : mean; }
    /** @return population variance (NaN if no samples) */
    public double variance() { return n < 1 ? Double.NaN : m2 / n; }
    /** @return unbiased sample variance (NaN if fewer than two samples) */
    public double sampleVariance() { return n < 2 ? Double.NaN : m2 / (n - 1); }
}
