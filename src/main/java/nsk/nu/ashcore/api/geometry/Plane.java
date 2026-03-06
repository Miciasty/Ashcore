package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.NumericTolerance;
import nsk.nu.ashcore.api.math.Vector3;

/** Immutable plane in Hessian form: n·p + d = 0, where n is unit-length. */
public record Plane(Vector3 normal, double d) {
    public Plane {
        double len = normal.length();
        if (len == 0.0) throw new IllegalArgumentException("Normal must be non-zero.");
        if (Math.abs(len - 1.0) > NumericTolerance.EPS) {
            normal = normal.mul(1.0 / len);
            d = d / len;
        }
    }
    /** Signed distance from point to plane (positive in direction of the normal). */
    public double distanceTo(Vector3 p) { return normal.dot(p) + d; }
    /** Projects point onto the plane. */
    public Vector3 project(Vector3 p) { return p.sub(normal.mul(distanceTo(p))); }
}