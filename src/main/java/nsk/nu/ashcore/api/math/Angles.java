package nsk.nu.ashcore.api.math;

/**
 * Utility methods for angle normalization, angular deltas, and yaw/pitch conversions.
 *
 * <p>Conventions used by this class:
 * <ul>
 *   <li>Yaw: {@code 0} points along {@code +Z}; positive yaw rotates toward {@code +X}.</li>
 *   <li>Pitch: negative values point upward ({@code y > 0}).</li>
 *   <li>All interpolation methods operate on the shortest angular arc.</li>
 * </ul>
 */
public final class Angles {
    private Angles() {
        // Utility class; no instances.
    }

    /**
     * Normalizes an angle in radians to {@code [-PI, PI)}.
     *
     * @param radians source angle in radians
     * @return normalized angle in {@code [-PI, PI)}
     */
    public static double wrapRadians(double radians) {
        double r = Math.IEEEremainder(radians, Math.PI * 2.0);
        if (r < -Math.PI) return r + 2.0 * Math.PI;
        if (r >= Math.PI) return r - 2.0 * Math.PI;
        return r;
    }

    /**
     * Normalizes an angle in degrees to {@code [0, 360)}.
     *
     * @param degrees source angle in degrees
     * @return normalized angle in {@code [0, 360)}
     */
    public static double wrapDegrees360(double degrees) {
        double r = degrees % 360.0;
        return r < 0.0 ? r + 360.0 : r;
    }

    /**
     * Normalizes an angle in degrees to {@code (-180, 180]}.
     *
     * @param degrees source angle in degrees
     * @return normalized angle in {@code (-180, 180]}
     */
    public static double wrapDegrees180(double degrees) {
        double r = ((degrees + 540.0) % 360.0) - 180.0;
        return r == -180.0 ? 180.0 : r;
    }

    /**
     * Computes the signed shortest angular difference {@code a -> b} in radians.
     *
     * @param a start angle in radians
     * @param b target angle in radians
     * @return shortest signed delta in {@code [-PI, PI)}
     */
    public static double deltaRadians(double a, double b) {
        return wrapRadians(b - a);
    }

    /**
     * Computes the signed shortest angular difference {@code a -> b} in degrees.
     *
     * @param a start angle in degrees
     * @param b target angle in degrees
     * @return shortest signed delta in {@code (-180, 180]}
     */
    public static double deltaDegrees(double a, double b) {
        return wrapDegrees180(b - a);
    }

    /**
     * Computes yaw from an XZ direction.
     *
     * @param x direction X component
     * @param z direction Z component
     * @return yaw in radians, normalized by {@link Math#atan2(double, double)} to {@code [-PI, PI]}
     */
    public static double yawFromXZ(double x, double z) {
        return Math.atan2(x, z);
    }

    /**
     * Computes pitch from a 3D direction vector.
     *
     * <p>Uses the class convention where upward directions produce negative pitch.
     *
     * @param v direction vector
     * @return pitch in radians in {@code [-PI/2, PI/2]}
     */
    public static double pitchFromVector(Vector3 v) {
        return Math.atan2(-v.y(), Math.hypot(v.x(), v.z()));
    }

    /**
     * Builds a unit direction vector from yaw and pitch.
     *
     * <p>Uses the same yaw/pitch convention as {@link #yawFromXZ(double, double)}
     * and {@link #pitchFromVector(Vector3)}.
     *
     * @param yaw yaw in radians
     * @param pitch pitch in radians
     * @return unit direction vector
     */
    public static Vector3 dirFromYawPitch(double yaw, double pitch) {
        double cy = Math.cos(yaw);
        double sy = Math.sin(yaw);
        double cp = Math.cos(pitch);
        double sp = Math.sin(pitch);
        return new Vector3(sy * cp, -sp, cy * cp);
    }

    /**
     * Interpolates between two angles in radians along the shortest arc.
     *
     * <p>Parameter {@code t} is not clamped.
     *
     * @param a start angle in radians
     * @param b target angle in radians
     * @param t interpolation factor
     * @return interpolated angle normalized to {@code [-PI, PI)}
     */
    public static double lerpRadians(double a, double b, double t) {
        return wrapRadians(a + deltaRadians(a, b) * t);
    }

    /**
     * Backward-compatible alias for {@link #lerpRadians(double, double, double)}.
     *
     * @param a start angle in radians
     * @param b target angle in radians
     * @param t interpolation factor
     * @return interpolated angle normalized to {@code [-PI, PI)}
     */
    @Deprecated(forRemoval = false, since = "1.1")
    public static double lerpAngle(double a, double b, double t) {
        return lerpRadians(a, b, t);
    }
}