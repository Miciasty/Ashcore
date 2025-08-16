package nsk.nu.ashcore.api.math;

/**
 * Inclusive integer range: [min, max].
 * Immutable, normalized; guarantees min <= max.
 */
public record IntRange(int min, int max) {

    public IntRange {
        if (min > max) throw new IllegalArgumentException("min must be <= max");
    }

    /** Creates a normalized range regardless of argument order. */
    public static IntRange of(int a, int b) { return a <= b ? new IntRange(a, b) : new IntRange(b, a); }
    /** @return true if v is within [min, max]. */
    public boolean contains(int v) {
        if (v < min) return false;
        if (v > max) return false;
        return true;
    }
    /** Clamps v into [min, max]. */
    public int clamp(int v) {
        return (int) MathUtil.clamp(v, min, max);
    }
    /** @return range length in elements assuming inclusive bounds (max - min + 1). */
    public long sizeInclusive() { return (long) max - (long) min + 1L; }
    /** @return true if this range intersects other range (inclusive). */
    public boolean intersects(IntRange other) {
        if (this.max < other.min) return false;
        if (other.max < this.min) return false;
        return true;
    }
    /** @return smallest range that contains this and v. */
    public IntRange expandToInclude(int v) {
        int nmin = Math.min(v, min);
        int nmax = Math.max(v, max);
        return (nmin == min && nmax == max) ? this : new IntRange(nmin, nmax);
    }
}