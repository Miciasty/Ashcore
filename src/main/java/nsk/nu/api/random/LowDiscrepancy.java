package nsk.nu.api.random;

import nsk.nu.api.math.Double2;
import nsk.nu.api.math.Vector3;

/** Low-discrepancy helpers and mappings from [0,1)^2 to common domains. */
public final class LowDiscrepancy {
    private LowDiscrepancy() {}

    /** Halton value for index n and base b (>1). O(log_b n). */
    public static double halton(int n, int b) {
        double f = 1.0, r = 0.0;
        while (n > 0) { f /= b; r += f * (n % b); n /= b; }
        return r;
    }

    /**
     * Shirley & Chiu concentric mapping from [0,1)^2 to unit disk (area-preserving).
     * Deterministic, branch-light implementation.
     */
    public static Double2 mapToConcentricDisk(double u, double v) {
        double sx = 2*u - 1;
        double sy = 2*v - 1;
        if (sx == 0 && sy == 0) return new Double2(0, 0);

        double r, theta;
        if (Math.abs(sx) > Math.abs(sy)) {
            r = sx;
            theta = Math.PI/4 * (sy / sx);
        } else {
            r = sy;
            theta = Math.PI/2 - Math.PI/4 * (sx / sy);
        }
        return new Double2(r * Math.cos(theta), r * Math.sin(theta));
    }

    /**
     * Uniform hemisphere (y up) mapping from [0,1)^2 (u is azimuth, v is cos(theta)).
     * Returns a unit-length Vector3. No allocations beyond the Vector3 itself.
     */
    public static Vector3 mapToUniformHemisphere(double u, double v) {
        double phi = 2 * Math.PI * u;
        double cosTheta = v;
        double sinTheta = Math.sqrt(Math.max(0.0, 1 - cosTheta*cosTheta));
        double x = Math.cos(phi) * sinTheta;
        double y = cosTheta;
        double z = Math.sin(phi) * sinTheta;
        return new Vector3(x, y, z);
    }
    /** Unit sphere surface: uniform direction. u in [0,1), v in [0,1). */
    public static Vector3 mapToUniformSphere(double u, double v) {
        double phi = 2 * Math.PI * u;
        double cosTheta = 1.0 - 2.0 * v;
        double sinTheta = Math.sqrt(1.0 - cosTheta*cosTheta);
        return new Vector3(Math.cos(phi)*sinTheta, cosTheta, Math.sin(phi)*sinTheta);
    }
    /** Unit sphere volume: uniform point inside sphere (radius ~ cbrt(w)). */
    public static Vector3 mapToUniformSphereVolume(double u, double v, double w) {
        Vector3 dir = mapToUniformSphere(u, v);
        double r = Math.cbrt(w);
        return dir.mul(r);
    }
    /** Uniform cone around +Y; cosThetaMax = cos(halfAngle). */
    public static Vector3 mapToUniformCone(double u, double v, double cosThetaMax) {
        double phi = 2 * Math.PI * u;
        double cosTheta = (1 - v) + v * cosThetaMax;  // lerp(1, cosMax, v)
        double sinTheta = Math.sqrt(1.0 - cosTheta*cosTheta);
        return new Vector3(Math.cos(phi)*sinTheta, cosTheta, Math.sin(phi)*sinTheta);
    }

    /**
     * Orients a local direction (assumed y-up) to an arbitrary world-space normal n.
     * Builds a TBN basis with minimal branching. n must be non-zero and normalized enough.
     */
    public static Vector3 orientYUpToNormal(Vector3 local, Vector3 n) {
        Vector3 a = Math.abs(n.x()) > 0.5 ? new Vector3(0,1,0) : new Vector3(1,0,0);
        Vector3 t = a.cross(n).normalized();
        Vector3 b = n.cross(t);
        double x = local.x(), y = local.y(), z = local.z();
        return new Vector3(
                t.x()*x + n.x()*y + b.x()*z,
                t.y()*x + n.y()*y + b.y()*z,
                t.z()*x + n.z()*y + b.z()*z
        );
    }
}