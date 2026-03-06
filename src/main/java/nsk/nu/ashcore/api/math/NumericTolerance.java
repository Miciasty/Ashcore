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

    /** Epsilon for geometric predicates (parallel tests, near-zero lengths). */
    public static final double GEOMETRY_EPS = 1e-12;

    /** Epsilon for interpolation/angle comparisons where looser tolerance is acceptable. */
    public static final double INTERPOLATION_EPS = 1e-9;

    /**
     * Tests whether the value is near zero under absolute tolerance.
     *
     * @param value value to test
     * @param eps non-negative tolerance
     * @return true if |value| <= eps
     */
    public static boolean isZero(double value, double eps) {
        return Math.abs(value) <= eps;
    }

    /**
     * Tests whether two values are near-equal under absolute tolerance.
     *
     * @param a first value
     * @param b second value
     * @param eps non-negative tolerance
     * @return true if |a - b| <= eps
     */
    public static boolean near(double a, double b, double eps) {
        return Math.abs(a - b) <= eps;
    }
}
