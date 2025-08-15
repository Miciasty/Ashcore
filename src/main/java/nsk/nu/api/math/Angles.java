package nsk.nu.api.math;

/**
 * Angle utilities for normalized arithmetic on radians and degrees.
 * Pure functions, no allocation, deterministic.
 */
public final class Angles {
    private Angles() {}
    /** Wraps radians to the range [-PI, PI). */
    public static double wrapRadians(double r) {
        r = Math.IEEEremainder(r, Math.PI * 2.0);
        return r < -Math.PI ? r + 2.0 * Math.PI : (r >= Math.PI ? r - 2.0 * Math.PI : r);
    }
    /** Wraps degrees to the range [0, 360). */
    public static double wrapDegrees360(double deg) {
        double r = deg % 360.0;
        return r < 0 ? r + 360.0 : r;
    }
    /** Smallest signed difference a->b in radians, result in [-PI, PI). */
    public static double deltaRadians(double a, double b) { return wrapRadians(b - a); }
    /** Smallest signed difference a->b in degrees, result in (-180, 180]. */
    public static double deltaDegrees(double a, double b) {
        double d = ((b - a + 540.0) % 360.0) - 180.0;
        return d == -180.0 ? 180.0 : d;
    }
    /** Converts a 2D heading vector (x,z) to yaw in radians (0 along +Z). */
    public static double yawFromXZ(double x, double z) { return Math.atan2(x, z); }
}