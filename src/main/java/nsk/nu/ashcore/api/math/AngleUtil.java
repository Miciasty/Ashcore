package nsk.nu.ashcore.api.math;

/** Angle helpers beyond Math.* (degrees + radians). */
public final class AngleUtil {
    private AngleUtil() {}
    /** Returns yaw (rad) in [-PI,PI) from XZ direction (0 along +Z). */
    public static double yawFromXZ(double x, double z){ return Math.atan2(x, z); }
    /** Returns pitch (rad) in [-PI/2,PI/2] from vector (y up). */
    public static double pitchFromVector(Vector3 v){ return Math.atan2(-v.y(), Math.hypot(v.x(), v.z())); }
    /** Builds a unit vector from yaw/pitch (rad). */
    public static Vector3 dirFromYawPitch(double yaw, double pitch){
        double cy = Math.cos(yaw), sy = Math.sin(yaw);
        double cp = Math.cos(pitch), sp = Math.sin(pitch);
        return new Vector3(sy*cp, -sp, cy*cp);
    }
    /** Shortest-path angle lerp in radians. */
    public static double lerpAngle(double a, double b, double t){
        double d = Angles.deltaRadians(a, b);
        return Angles.wrapRadians(a + d * t);
    }
}