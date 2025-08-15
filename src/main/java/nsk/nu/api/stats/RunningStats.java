package nsk.nu.api.stats;

/** One-pass running mean/variance using Welford's algorithm. */
public final class RunningStats {
    private long n = 0;
    private double mean = 0;
    private double m2 = 0;

    /** Adds a new sample. */
    public void add(double x) {
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
    /** @return unbiased sample variance (NaN if <2 samples) */
    public double sampleVariance() { return n < 2 ? Double.NaN : m2 / (n - 1); }
}