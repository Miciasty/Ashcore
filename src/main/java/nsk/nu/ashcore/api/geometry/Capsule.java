package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.NumericTolerance;
import nsk.nu.ashcore.api.math.Vector3;

public record Capsule(Vector3 a, Vector3 b, double radius) {
    /** Squared distance from point p to the capsule surface (0 when inside). */
    public double distanceSqTo(Vector3 p){
        Vector3 ab = b.sub(a), ap = p.sub(a);
        double abLen2 = ab.dot(ab);
        double r2 = radius * radius;

        if (abLen2 == 0.0) {
            double d2 = ap.dot(ap) - r2;
            return Math.max(0.0, d2);
        }

        double t = Math.max(0.0, Math.min(1.0, ap.dot(ab) / abLen2));
        Vector3 closest = a.add(ab.mul(t));
        double d2 = p.sub(closest).dot(p.sub(closest)) - r2;
        return Math.max(0.0, d2);
    }

    /** Ray vs capsule; returns t or +INF if no hit. */
    public double rayIntersectT(Ray ray){
        Vector3 pa = ray.origin().sub(a);
        Vector3 ba = b.sub(a);
        Vector3 rd = ray.direction();

        double baDotRd = ba.dot(rd);
        double baDotPa = ba.dot(pa);
        double rdDotPa = rd.dot(pa);
        double baLen2  = ba.dot(ba);
        double r2 = radius*radius;

        if (baLen2 < NumericTolerance.GEOMETRY_EPS) {
            return V(ray, rd, r2, a);
        }

        double aTerm = baLen2 - baDotRd*baDotRd;
        double bTerm = baLen2*rdDotPa - baDotPa*baDotRd;
        double cTerm = baLen2*pa.dot(pa) - baDotPa*baDotPa - r2*baLen2;

        if (Math.abs(aTerm) < NumericTolerance.GEOMETRY_EPS) return Double.POSITIVE_INFINITY;
        double disc = bTerm*bTerm - aTerm*cTerm;
        if (disc < 0) return Double.POSITIVE_INFINITY;
        double t = (-bTerm - Math.sqrt(disc)) / aTerm;
        if (t < 0) return Double.POSITIVE_INFINITY;

        double y = baDotPa + t*baDotRd;
        if (y < 0) {
            return V(ray, rd, r2, a);
        } else if (y > baLen2) {
            return V(ray, rd, r2, b);
        }
        return t;
    }

    private double V(Ray ray, Vector3 rd, double r2, Vector3 a) {
        Vector3 oc = ray.origin().sub(a);
        double b = oc.dot(rd);
        double c = oc.dot(oc) - r2;
        double D = b*b - c;
        return D < 0 ? Double.POSITIVE_INFINITY : Math.max(0, -b - Math.sqrt(D));
    }
}