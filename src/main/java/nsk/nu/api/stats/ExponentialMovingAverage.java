package nsk.nu.api.stats;

/** Simple exponential moving average with optional half-life configuration. */
public final class ExponentialMovingAverage {
    private final double alpha;
    private boolean initialized = false;
    private double value;

    /** @param alpha smoothing factor in (0,1]; higher means faster response */
    public ExponentialMovingAverage(double alpha) {
        if (alpha <= 0 || alpha > 1) throw new IllegalArgumentException("alpha in (0,1]");
        this.alpha = alpha;
    }
    /** Creates EMA from half-life in samples: alpha = 1 - 0.5^(1/halfLife). */
    public static ExponentialMovingAverage withHalfLife(double halfLife) {
        double alpha = 1.0 - Math.pow(0.5, 1.0 / halfLife);
        return new ExponentialMovingAverage(alpha);
    }
    /** Adds a sample and returns the updated EMA value. */
    public double add(double x) {
        value = initialized ? (alpha * x + (1 - alpha) * value) : x;
        initialized = true;
        return value;
    }
    /** @return current EMA value (NaN if uninitialized) */
    public double value() { return initialized ? value : Double.NaN; }
}