package nsk.nu.ashcore.api.math;

/** Immutable quaternion. Rotation operations require unit quaternions and use radians. */
public record Quaternion(double w, double x, double y, double z) {
    // Dimensionless acceptance tolerance for rotation matrix columns and determinant.
    private static final double MATRIX_EPS = 1e-9;

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

    /** Returns (w,-x,-y,-z). For a unit quaternion this is the inverse rotation. */
    public Quaternion conjugate(){ return new Quaternion(w, -x, -y, -z); }

    /**
     * Algebraic inverse, conjugate divided by squared norm; does not normalize away the magnitude.
     * Non-finite components throw IllegalArgumentException. Zero or an unrepresentable inverse throws
     * ArithmeticException; unlike normalized(), inverse() has no identity fallback for zero.
     */
    public Quaternion inverse(){
        double scale = Math.max(Math.max(Math.abs(w), Math.abs(x)), Math.max(Math.abs(y), Math.abs(z)));
        if (!Double.isFinite(scale)) throw new IllegalArgumentException("Quaternion must be finite");
        if (scale == 0) throw new ArithmeticException("Zero quaternion has no inverse");
        double sw = w / scale, sx = x / scale, sy = y / scale, sz = z / scale;
        double n = sw*sw + sx*sx + sy*sy + sz*sz;
        double iw = (sw / n) / scale, ix = (-sx / n) / scale, iy = (-sy / n) / scale, iz = (-sz / n) / scale;
        if (!Double.isFinite(iw) || !Double.isFinite(ix) || !Double.isFinite(iy) || !Double.isFinite(iz)) {
            throw new ArithmeticException("Quaternion inverse overflow");
        }
        return new Quaternion(iw, ix, iy, iz);
    }

    /**
     * Returns a row-major rotation matrix acting on column vectors, after normalized().
     * Finite non-unit values are normalized; zero gives identity, and non-finite values are rejected.
     * All quaternion/matrix conversion operations take O(1) time and additional memory.
     */
    public Matrix3 toMatrix3(){
        Quaternion q = normalized();
        double xx = q.x*q.x, yy = q.y*q.y, zz = q.z*q.z;
        double xy = q.x*q.y, xz = q.x*q.z, yz = q.y*q.z;
        double wx = q.w*q.x, wy = q.w*q.y, wz = q.w*q.z;
        return new Matrix3(
                1 - 2*(yy + zz), 2*(xy - wz), 2*(xz + wy),
                2*(xy + wz), 1 - 2*(xx + zz), 2*(yz - wx),
                2*(xz - wy), 2*(yz + wx), 1 - 2*(xx + yy)
        );
    }

    /** Returns toMatrix3() embedded in an affine 4x4 matrix with zero translation and last row [0,0,0,1]. */
    public Matrix4 toMatrix4(){
        Matrix3 m = toMatrix3();
        return new Matrix4(
                m.m00(), m.m01(), m.m02(), 0,
                m.m10(), m.m11(), m.m12(), 0,
                m.m20(), m.m21(), m.m22(), 0,
                0, 0, 0, 1
        );
    }

    /**
     * Converts a proper rotation matrix acting on column vectors to a unit quaternion.
     * Requires finite columns with squared lengths within 1e-9 of one, pairwise dot products within
     * 1e-9 of zero, and determinant within 1e-9 of +1 (all absolute, dimensionless checks).
     * Rejects scale, shear and reflection outside those limits with IllegalArgumentException.
     * Accepted rounding deviations are normalized; this is not a nearest-rotation projection.
     * Quaternion sign is not preserved by a matrix round trip; q and -q represent the same rotation.
     */
    public static Quaternion fromMatrix3(Matrix3 m){
        Vector3 cx = new Vector3(m.m00(), m.m10(), m.m20());
        Vector3 cy = new Vector3(m.m01(), m.m11(), m.m21());
        Vector3 cz = new Vector3(m.m02(), m.m12(), m.m22());
        if (!(Math.abs(cx.lengthSq() - 1) <= MATRIX_EPS && Math.abs(cy.lengthSq() - 1) <= MATRIX_EPS &&
                Math.abs(cz.lengthSq() - 1) <= MATRIX_EPS && Math.abs(cx.dot(cy)) <= MATRIX_EPS &&
                Math.abs(cx.dot(cz)) <= MATRIX_EPS && Math.abs(cy.dot(cz)) <= MATRIX_EPS &&
                Math.abs(m.determinant() - 1) <= MATRIX_EPS)) {
            throw new IllegalArgumentException("Matrix must be a finite proper rotation");
        }

        double trace = m.m00() + m.m11() + m.m22();
        Quaternion q;
        if (trace > 0) {
            double s = 2 * Math.sqrt(1 + trace);
            q = new Quaternion(s * 0.25, (m.m21() - m.m12()) / s, (m.m02() - m.m20()) / s, (m.m10() - m.m01()) / s);
        } else if (m.m00() >= m.m11() && m.m00() >= m.m22()) {
            double s = 2 * Math.sqrt(1 + m.m00() - m.m11() - m.m22());
            q = new Quaternion((m.m21() - m.m12()) / s, s * 0.25, (m.m01() + m.m10()) / s, (m.m02() + m.m20()) / s);
        } else if (m.m11() >= m.m22()) {
            double s = 2 * Math.sqrt(1 + m.m11() - m.m00() - m.m22());
            q = new Quaternion((m.m02() - m.m20()) / s, (m.m01() + m.m10()) / s, s * 0.25, (m.m12() + m.m21()) / s);
        } else {
            double s = 2 * Math.sqrt(1 + m.m22() - m.m00() - m.m11());
            q = new Quaternion((m.m10() - m.m01()) / s, (m.m02() + m.m20()) / s, (m.m12() + m.m21()) / s, s * 0.25);
        }
        return q.normalized();
    }

    /**
     * Extracts the rotation of a finite affine rigid matrix using fromMatrix3() on the upper-left block.
     * Finite translation is ignored; last-row deviations from [0,0,0,1] up to 1e-9 are accepted and discarded.
     * Scale, shear, reflection, non-finite values and a non-affine last row throw IllegalArgumentException.
     */
    public static Quaternion fromMatrix4(Matrix4 m){
        if (!(Double.isFinite(m.m03()) && Double.isFinite(m.m13()) && Double.isFinite(m.m23()) &&
                Math.abs(m.m30()) <= MATRIX_EPS && Math.abs(m.m31()) <= MATRIX_EPS &&
                Math.abs(m.m32()) <= MATRIX_EPS && Math.abs(m.m33() - 1) <= MATRIX_EPS)) {
            throw new IllegalArgumentException("Matrix must be finite and affine");
        }
        return fromMatrix3(new Matrix3(
                m.m00(), m.m01(), m.m02(),
                m.m10(), m.m11(), m.m12(),
                m.m20(), m.m21(), m.m22()
        ));
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
