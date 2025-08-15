package nsk.nu.api.math;

/**
 * Inclusive double range: [min, max].
 * Immutable, normalized; guarantees min <= max.
 */
public record DoubleRange(double min, double max) {

    public DoubleRange {
        if (min > max) throw new IllegalArgumentException("min must be <= max");
    }
    /** Creates a normalized range regardless of argument order. */
    public static DoubleRange of(double a, double b) { return a <= b ? new DoubleRange(a, b) : new DoubleRange(b, a); }
    /** @return true if v is within [min, max]. */
    public boolean contains(double v) {
        if (v < min) return false;
        if (v > max) return false;
        return true;
    }
    /** Clamps v into [min, max]. */
    public double clamp(double v) {
        return MathUtil.clamp(v, min, max);
    }
    /** @return width (max - min). */
    public double width() { return max - min; }
    /** @return true if this range intersects other range (inclusive). */
    public boolean intersects(DoubleRange other) {
        if (this.max < other.min) return false;
        if (other.max < this.min) return false;
        return true;
    }
    /** @return smallest range that contains this and v. */
    public DoubleRange expandToInclude(double v) {
        double nmin = Math.min(v, min);
        double nmax = Math.max(v, max);
        return (nmin == min && nmax == max) ? this : new DoubleRange(nmin, nmax);
    }
}