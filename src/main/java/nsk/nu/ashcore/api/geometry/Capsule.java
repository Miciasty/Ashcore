package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.math.Vector3;

/**
 * Closed capsule: points within radius of segment AB, including its spherical ends.
 * Endpoints must be finite and radius finite/non-negative; violations throw IllegalArgumentException.
 * Equal endpoints form a sphere; zero radius forms a segment. All coordinates and radius share position units.
 * Queries take O(1) time/space and may allocate. Differences, axis length, projections and hit time must remain
 * representable; double rounding limits near tangency/large relative scales apply. No contact epsilon is added.
 */
public record Capsule(Vector3 a, Vector3 b, double radius) {
    public Capsule {
        requireFinite(a); requireFinite(b);
        if (radius < 0 || !Double.isFinite(radius)) throw new IllegalArgumentException("Radius must be finite and non-negative");
    }

    /**
     * Squared Euclidean distance to the solid capsule: max(0,distanceToAxis-radius)^2, zero inside/on it.
     * Input points must be finite. The final square may overflow to infinity or underflow to zero.
     */
    public double distanceSqTo(Vector3 p){
        double distance = Math.max(0.0, distanceToAxis(p) - radius);
        return distance * distance;
    }

    /**
     * First ray contact distance, zero inside/on the capsule and positive infinity on a miss or behind-ray hit.
     * Tests the finite cylinder and both endpoint spheres; axial rays still test the spherical ends.
     */
    public double rayIntersectT(Ray ray){
        if (distanceToAxis(ray.origin()) <= radius) return 0;
        double first = CollisionTests.rayVsSphereT(ray, new Sphere(a, radius));
        Vector3 ab = b.sub(a);
        double length = ab.length();
        if (length == 0) return first;
        first = Math.min(first, CollisionTests.rayVsSphereT(ray, new Sphere(b, radius)));

        Vector3 axis = ab.normalized();
        Vector3 offset = ray.origin().sub(a);
        Vector3 radialOrigin = axis.cross(offset);
        Vector3 radialDirection = axis.cross(ray.direction());
        double radialSpeed = radialDirection.length();
        if (radialSpeed != 0) {
            double radialHit = CollisionTests.rayVsSphereT(new Ray(radialOrigin, radialDirection), new Sphere(Vector3.ZERO, radius));
            double t = radialHit / radialSpeed;
            if (Double.isFinite(t)) {
                double along = offset.dot(axis) + t * ray.direction().dot(axis);
                if (along >= 0 && along <= length) first = Math.min(first, t);
            }
        }
        return first;
    }

    private double distanceToAxis(Vector3 p) {
        requireFinite(p);
        Vector3 ab = b.sub(a), offset = p.sub(a);
        requireFinite(ab); requireFinite(offset);
        double length = ab.length();
        if (!Double.isFinite(length)) throw new IllegalArgumentException("Capsule axis length overflow");
        if (length == 0) return offset.length();
        Vector3 axis = ab.normalized();
        double along = offset.dot(axis);
        if (!Double.isFinite(along)) throw new IllegalArgumentException("Capsule projection overflow");
        return offset.sub(axis.mul(Math.max(0.0, Math.min(length, along)))).length();
    }

    private static void requireFinite(Vector3 v) {
        if (!Double.isFinite(v.x()) || !Double.isFinite(v.y()) || !Double.isFinite(v.z())) {
            throw new IllegalArgumentException("Coordinates and differences must be finite");
        }
    }
}
