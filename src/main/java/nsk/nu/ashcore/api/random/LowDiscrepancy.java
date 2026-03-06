package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.math.Vector2;
import nsk.nu.ashcore.api.math.Vector3;

import static nsk.nu.ashcore.api.geometry.OrthonormalBasis.getVector3;

/**
 * Low-discrepancy helpers and mappings from [0,1)^N to common geometric domains.
 */
public final class LowDiscrepancy {
    private LowDiscrepancy() {  }

    /**
     * Halton value for index n and base b.
     *
     * @param n sequence index, must be >= 0
     * @param b base, must be > 1
     * @return Halton radical-inverse value in [0,1)
     */
    public static double halton(int n, int b) {
        if (n < 0) throw new IllegalArgumentException("index >= 0 required");
        if (b <= 1) throw new IllegalArgumentException("base > 1 required");

        double f = 1.0;
        double r = 0.0;
        while (n > 0) {
            f /= b;
            r += f * (n % b);
            n /= b;
        }
        return r;
    }

    /**
     * Shirley-Chiu concentric mapping from [0,1)^2 to unit disk.
     *
     * @param u first sample in [0,1)
     * @param v second sample in [0,1)
     * @return point on unit disk
     */
    public static Vector2 mapToConcentricDisk(double u, double v) {
        double sx = 2.0 * u - 1.0;
        double sy = 2.0 * v - 1.0;

        if (sx == 0.0 && sy == 0.0) return Vector2.ZERO;

        double r;
        double theta;

        if (Math.abs(sx) > Math.abs(sy)) {
            r = sx;
            theta = (Math.PI / 4.0) * (sy / sx);
        } else {
            r = sy;
            theta = (Math.PI / 2.0) - (Math.PI / 4.0) * (sx / sy);
        }

        return new Vector2(r * Math.cos(theta), r * Math.sin(theta));
    }

    /**
     * Uniform hemisphere (Y-up) mapping from [0,1)^2.
     *
     * @param u azimuth sample in [0,1)
     * @param v cosine-theta sample in [0,1)
     * @return unit direction on hemisphere
     */
    public static Vector3 mapToUniformHemisphere(double u, double v) {
        double phi = 2.0 * Math.PI * u;
        double cosTheta = v;
        double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));

        double x = Math.cos(phi) * sinTheta;
        double y = cosTheta;
        double z = Math.sin(phi) * sinTheta;
        return new Vector3(x, y, z);
    }

    /**
     * Uniform sphere surface mapping from [0,1)^2.
     *
     * @param u first sample in [0,1)
     * @param v second sample in [0,1)
     * @return unit direction on sphere
     */
    public static Vector3 mapToUniformSphere(double u, double v) {
        double phi = 2.0 * Math.PI * u;
        double cosTheta = 1.0 - 2.0 * v;
        double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));
        return new Vector3(Math.cos(phi) * sinTheta, cosTheta, Math.sin(phi) * sinTheta);
    }

    /**
     * Uniform sphere volume mapping from [0,1)^3.
     *
     * @param u first sample in [0,1)
     * @param v second sample in [0,1)
     * @param w third sample in [0,1)
     * @return point inside unit sphere
     */
    public static Vector3 mapToUniformSphereVolume(double u, double v, double w) {
        Vector3 dir = mapToUniformSphere(u, v);
        double r = Math.cbrt(w);
        return dir.mul(r);
    }

    /**
     * Uniform cone mapping around +Y axis.
     *
     * @param u first sample in [0,1)
     * @param v second sample in [0,1)
     * @param cosThetaMax cosine of cone half-angle
     * @return unit direction inside cone
     */
    public static Vector3 mapToUniformCone(double u, double v, double cosThetaMax) {
        double phi = 2.0 * Math.PI * u;
        double cosTheta = (1.0 - v) + v * cosThetaMax;
        double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));
        return new Vector3(Math.cos(phi) * sinTheta, cosTheta, Math.sin(phi) * sinTheta);
    }

    /**
     * Orients a local Y-up direction to an arbitrary world-space normal.
     *
     * @param local local direction where +Y is the local normal axis
     * @param n target world-space normal (non-zero)
     * @return world-space oriented direction
     */
    public static Vector3 orientYUpToNormal(Vector3 local, Vector3 n) {
        Vector3 a = Math.abs(n.x()) > 0.5 ? new Vector3(0, 1, 0) : new Vector3(1, 0, 0);
        Vector3 t = a.cross(n).normalized();
        Vector3 b = n.cross(t);
        return getVector3(local, t, n, b);
    }
}