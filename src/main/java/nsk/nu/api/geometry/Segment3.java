package nsk.nu.api.geometry;

import nsk.nu.api.math.Vector3;

/** Closed line segment between points a and b. */
public record Segment3(Vector3 a, Vector3 b) {
    public Vector3 at(double t) { return a.add(b.sub(a).mul(t)); }
    public double length() { return b.sub(a).length(); }
}