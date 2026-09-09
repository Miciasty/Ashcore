package nsk.nu.ashcore.api.math;

/**
 * Centralized numeric tolerances used by Ashcore.
 *
 * <p>All constants are absolute tolerances unless stated otherwise.</p>
 */
public final class NumericTolerance {
    private NumericTolerance() {}

    /** Generic epsilon for double comparisons in non-geometric utilities. */
    public static final double EPS = 1e-12;

    /** Legacy absolute threshold for selected geometric predicates; units depend on the tested quantity. */
    public static final double GEOMETRY_EPS = 1e-12;

    /** Epsilon for interpolation/angle comparisons where looser tolerance is acceptable. */
    public static final double INTERPOLATION_EPS = 1e-9;

    /**
     * Tests whether the value is near zero under absolute tolerance.
     *
     * @param value value to test
     * @param eps finite non-negative tolerance in the same units as value
     * @return true if the absolute value is at most eps; false for NaN/infinity
     * @throws IllegalArgumentException if eps is negative or not finite
     */
    public static boolean isZero(double value, double eps) {
        if (eps < 0 || !Double.isFinite(eps)) throw new IllegalArgumentException("eps must be finite and non-negative");
        return Math.abs(value) <= eps;
    }

    /**
     * Tests whether two values are near-equal under absolute tolerance.
     *
     * @param a first value
     * @param b second value
     * @param eps finite non-negative tolerance in the same units as a and b
     * @return true if the absolute difference is at most eps; false for NaN/infinity
     * @throws IllegalArgumentException if eps is negative or not finite
     */
    public static boolean near(double a, double b, double eps) {
        if (eps < 0 || !Double.isFinite(eps)) throw new IllegalArgumentException("eps must be finite and non-negative");
        return Math.abs(a - b) <= eps;
    }
}
