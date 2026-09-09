package nsk.nu.ashcore.api.math;

/** Immutable quaternion. Rotation operations require unit quaternions and use radians. */
public record Quaternion(double w, double x, double y, double z) {
    public static Quaternion identity() { return new Quaternion(1,0,0,0); }

    /**
     * Unit quaternion from a finite, non-zero axis (normalized inside) and finite angle in radians.
     * Positive angles follow the right-hand rule. Invalid inputs throw IllegalArgumentException.
     */
    public static Quaternion fromAxisAngle(Vector3 axis, double angle){
        if (!Double.isFinite(angle)) throw new IllegalArgumentException("Angle must be finite");
        double half = angle * 0.5, s = Math.sin(half);
        Vector3 n = axis.normalized();
        if (n.x() == 0 && n.y() == 0 && n.z() == 0) throw new IllegalArgumentException("Axis must be non-zero");
        return new Quaternion(Math.cos(half), n.x()*s, n.y()*s, n.z()*s);
    }

    /**
     * Unit quaternion within floating-point rounding; the zero quaternion returns identity for compatibility.
     * Non-finite components throw IllegalArgumentException. Finite extreme components are scaled safely.
     */
    public Quaternion normalized(){
        double scale = Math.max(Math.max(Math.abs(w), Math.abs(x)), Math.max(Math.abs(y), Math.abs(z)));
        if (!Double.isFinite(scale)) throw new IllegalArgumentException("Quaternion must be finite");
        if (scale == 0) return identity();
        double sw = w / scale, sx = x / scale, sy = y / scale, sz = z / scale;
        double n = Math.sqrt(sw*sw + sx*sx + sy*sy + sz*sz);
        return new Quaternion(sw/n, sx/n, sy/n, sz/n);
    }

    /** Hamilton product; for unit rotations, this.mul(b) applies b first, then this. */
    public Quaternion mul(Quaternion b){
        return new Quaternion(
                w*b.w - x*b.x - y*b.y - z*b.z,
                w*b.x + x*b.w + y*b.z - z*b.y,
                w*b.y - x*b.z + y*b.w + z*b.x,
                w*b.z + x*b.y - y*b.x + z*b.w
        );
    }

    /** Rotates vector v by this quaternion (assumes unit). */
    public Vector3 rotate(Vector3 v){
        double qx=x, qy=y, qz=z, qw=w;
        double ix =  qw*v.x() + qy*v.z() - qz*v.y();
        double iy =  qw*v.y() + qz*v.x() - qx*v.z();
        double iz =  qw*v.z() + qx*v.y() - qy*v.x();
        double iw = -qx*v.x() - qy*v.y() - qz*v.z();
        return new Vector3(
                ix*qw + iw*(-qx) + iy*(-z) - iz*(-y),
                iy*qw + iw*(-qy) + iz*(-x) - ix*(-z),
                iz*qw + iw*(-qz) + ix*(-y) - iy*(-x)
        );
    }

    /**
     * Shortest-arc spherical interpolation of finite unit quaternions for t in [0,1].
     * Inputs are caller-validated; returns a unit quaternion within rounding error.
     * Angles below INTERPOLATION_EPS radians use normalized linear interpolation.
     */
    public static Quaternion slerp(Quaternion a, Quaternion b, double t){
        double dot = a.w*b.w + a.x*b.x + a.y*b.y + a.z*b.z;
        double sign = dot < 0 ? -1 : 1;
        double bw = b.w*sign, bx = b.x*sign, by = b.y*sign, bz = b.z*sign;

        double omega = Math.acos(Math.max(-1, Math.min(1, a.w*bw + a.x*bx + a.y*by + a.z*bz)));
        if (omega < NumericTolerance.INTERPOLATION_EPS) {
            return new Quaternion(
                    a.w + (bw - a.w)*t,
                    a.x + (bx - a.x)*t,
                    a.y + (by - a.y)*t,
                    a.z + (bz - a.z)*t
            ).normalized();
        }
        double s1 = Math.sin((1 - t) * omega) / Math.sin(omega);
        double s2 = Math.sin(t * omega) / Math.sin(omega);
        return new Quaternion(
                a.w*s1 + bw*s2, a.x*s1 + bx*s2, a.y*s1 + by*s2, a.z*s1 + bz*s2
        );
    }
}
