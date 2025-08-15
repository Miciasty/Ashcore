package nsk.nu.api.geometry;

import nsk.nu.api.math.Vector3;

/**
 * Orthonormal basis (tangent, normal, bitangent) built from a single normal.
 * Useful to orient local Y-up hemisphere/samples to an arbitrary normal.
 * All operations are O(1); no allocations besides returned vectors.
 */
public final class OrthonormalBasis {
    private final Vector3 t;
    private final Vector3 n;
    private final Vector3 b;

    /** Builds a stable TBN basis from a (non-zero) normal. */
    public static OrthonormalBasis fromNormal(Vector3 normal) {
        if (normal.length() == 0) throw new IllegalArgumentException("Normal must be non-zero.");
        Vector3 n = normal.normalized();
        Vector3 a = Math.abs(n.x()) > 0.5 ? new Vector3(0, 1, 0) : new Vector3(1, 0, 0);
        Vector3 t = a.cross(n).normalized();
        Vector3 b = n.cross(t);
        return new OrthonormalBasis(t, n, b);
    }

    private OrthonormalBasis(Vector3 t, Vector3 n, Vector3 b) { this.t = t; this.n = n; this.b = b; }

    /** Transforms a local vector (x along T, y along N (up), z along B) into world space. */
    public Vector3 toWorld(Vector3 local) {
        return getVector3(local, t, n, b);
    }

    public static Vector3 getVector3(Vector3 local, Vector3 t, Vector3 n, Vector3 b) {
        double x = local.x(), y = local.y(), z = local.z();
        return new Vector3(
                t.x() * x + n.x() * y + b.x() * z,
                t.y() * x + n.y() * y + b.y() * z,
                t.z() * x + n.z() * y + b.z() * z
        );
    }

    public Vector3 tangent() { return t; }
    public Vector3 normal()  { return n; }
    public Vector3 bitangent(){ return b; }
}