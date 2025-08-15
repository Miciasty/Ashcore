package nsk.nu.api.stats;

/** Fixed-size windowed mean over the last N samples. */
public final class WindowedMean {
    private final double[] buf;
    private int idx = 0, count = 0;
    private double sum = 0;

    public WindowedMean(int window) {
        if (window <= 0) throw new IllegalArgumentException("window > 0");
        this.buf = new double[window];
    }
    /** Adds a sample, evicting the oldest when the buffer is full. */
    public double add(double x) {
        if (count < buf.length) {
            buf[idx++] = x; sum += x; count++; if (idx == buf.length) idx = 0;
        } else {
            double old = buf[idx];
            buf[idx++] = x; if (idx == buf.length) idx = 0;
            sum += x - old;
        }
        return sum / count;
    }
    /** @return current mean over available samples (less than window until filled) */
    public double mean() { return count == 0 ? Double.NaN : sum / count; }
}