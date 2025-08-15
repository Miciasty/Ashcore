package nsk.nu.api.geometry;

import nsk.nu.api.math.Vector3;

/** Immutable plane in Hessian form: n·p + d = 0, where n is unit-length. */
public record Plane(Vector3 normal, double d) {
    public Plane {
        if (normal.length() == 0) throw new IllegalArgumentException("Normal must be non-zero.");
    }
    /** Signed distance from point to plane (positive in direction of the normal). */
    public double distanceTo(Vector3 p) { return normal.dot(p) + d; }
    /** Projects point onto the plane. */
    public Vector3 project(Vector3 p) { return p.sub(normal.mul(distanceTo(p))); }
}