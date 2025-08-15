package nsk.nu.api.geometry;

import nsk.nu.api.math.Vector3;

/**
 * Small geometry helpers on vectors and simple primitives.
 */
public final class GeometryUtils {
    private GeometryUtils() {}
    /** Reflects vector v about unit normal n. */
    public static Vector3 reflect(Vector3 v, Vector3 n) {
        double k = 2.0 * v.dot(n);
        return v.sub(n.mul(k));
    }
    /** Projects v onto unit vector n (if n not unit, projection still works with scale factor). */
    public static Vector3 project(Vector3 v, Vector3 n) {
        return n.mul(v.dot(n) / n.dot(n));
    }
    /** Rejects v from n (component of v orthogonal to n). */
    public static Vector3 reject(Vector3 v, Vector3 n) { return v.sub(project(v, n)); }
    /** Closest point on segment AB to point P, returns the point. */
    public static Vector3 closestPointOnSegment(Vector3 a, Vector3 b, Vector3 p) {
        Vector3 ab = b.sub(a);
        double t = (p.sub(a)).dot(ab) / ab.dot(ab);
        if (t <= 0) return a;
        if (t >= 1) return b;
        return a.add(ab.mul(t));
    }
}