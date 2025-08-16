package nsk.nu.ashcore.api.math;

/**
 * Small, general-purpose math helpers that Java doesn't provide directly
 * or where a clear, intent-revealing name helps readability.
 */
public final class MathUtil {
    private MathUtil() {}
    /** Clamps v into [min, max]. Assumes min <= max. */
    public static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
    /** Maps v from [inMin,inMax] to [outMin,outMax] with clamping at input range. */
    public static double mapRange(double v, double inMin, double inMax, double outMin, double outMax) {
        if (inMax == inMin) return outMin; // avoid division by zero
        double t = clamp((v - inMin) / (inMax - inMin), 0.0, 1.0);
        return outMin + (outMax - outMin) * t;
    }
    /** Returns t such that lerp(a,b,t) == v; clamps to [0,1] when a==b returns 0. */
    public static double inverseLerp(double a, double b, double v) {
        if (a == b) return 0.0;
        return clamp((v - a) / (b - a), 0.0, 1.0);
    }
    /** Linear interpolation between a and b at t in [0,1]. */
    public static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    /** True if |a-b| <= eps. */
    public static boolean near(double a, double b, double eps) { return Math.abs(a - b) <= eps; }
    /** 3D hypot: sqrt(x^2 + y^2 + z^2) with correct scaling for large values. */
    public static double hypot3(double x, double y, double z) {
        double m = Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z));
        if (m == 0.0) return 0.0;
        x /= m; y /= m; z /= m;
        return m * Math.sqrt(x*x + y*y + z*z);
    }
}