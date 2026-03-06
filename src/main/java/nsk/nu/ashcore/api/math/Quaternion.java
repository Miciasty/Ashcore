package nsk.nu.ashcore.api.math;

public record Quaternion(double w, double x, double y, double z) {
    public static Quaternion identity() { return new Quaternion(1,0,0,0); }

    /** Unit quaternion from axis (normalized inside) and angle in radians. */
    public static Quaternion fromAxisAngle(Vector3 axis, double angle){
        double half = angle * 0.5, s = Math.sin(half);
        Vector3 n = axis.normalized();
        return new Quaternion(Math.cos(half), n.x()*s, n.y()*s, n.z()*s);
    }

    /** Normalized quaternion (returns this if already unit). */
    public Quaternion normalized(){
        double n = Math.sqrt(w*w + x*x + y*y + z*z);
        return n == 0 ? identity() : new Quaternion(w/n, x/n, y/n, z/n);
    }

    /** Hamilton product (composition). */
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

    /** Spherical linear interpolation; returns unit quaternion. */
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
