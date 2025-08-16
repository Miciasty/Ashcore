package nsk.nu.ashcore.api.interpolation;

import nsk.nu.ashcore.api.math.Vector3;

/** Catmull-Rom spline utilities (centripetal, tension=0.5). */
public final class CatmullRom {

    private CatmullRom() {}
    /** Scalar Catmull-Rom between p1 and p2 with neighbors p0, p3. t in [0,1]. */
    public static double eval(double p0, double p1, double p2, double p3, double t) {
        double t2 = t*t, t3 = t2*t;
        return 0.5 * ((2*p1) + (-p0 + p2)*t + (2*p0 - 5*p1 + 4*p2 - p3)*t2 + (-p0 + 3*p1 - 3*p2 + p3)*t3);
    }
    /** Vector variant. */
    public static Vector3 eval(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3, double t) {
        return new Vector3(
                eval(p0.x(), p1.x(), p2.x(), p3.x(), t),
                eval(p0.y(), p1.y(), p2.y(), p3.y(), t),
                eval(p0.z(), p1.z(), p2.z(), p3.z(), t)
        );
    }
}