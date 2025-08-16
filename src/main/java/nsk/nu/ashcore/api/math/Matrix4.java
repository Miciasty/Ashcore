package nsk.nu.ashcore.api.math;

/** Immutable 4x4 matrix, row-major. */
public record Matrix4(double m00, double m01, double m02, double m03,
                      double m10, double m11, double m12, double m13,
                      double m20, double m21, double m22, double m23,
                      double m30, double m31, double m32, double m33) {

    private static final double EPS = 1e-12;

    /** I4. */
    public static Matrix4 identity() {
        return new Matrix4(
                1,0,0,0,
                0,1,0,0,
                0,0,1,0,
                0,0,0,1
        );
    }

    /** Build from rows. */
    public static Matrix4 ofRows(
            double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {
        return new Matrix4(
                m00,m01,m02,m03,
                m10,m11,m12,m13,
                m20,m21,m22,m23,
                m30,m31,m32,m33
        );
    }

    /** Build from columns (basis X,Y,Z, and translation or W column). */
    public static Matrix4 fromColumns(
            double c00, double c10, double c20, double c30, // col 0
            double c01, double c11, double c21, double c31, // col 1
            double c02, double c12, double c22, double c32, // col 2
            double c03, double c13, double c23, double c33  // col 3
    ) {
        return new Matrix4(
                c00, c01, c02, c03,
                c10, c11, c12, c13,
                c20, c21, c22, c23,
                c30, c31, c32, c33
        );
    }

    /** this * b */
    public Matrix4 mul(Matrix4 b) {
        double n00 = m00*b.m00 + m01*b.m10 + m02*b.m20 + m03*b.m30;
        double n01 = m00*b.m01 + m01*b.m11 + m02*b.m21 + m03*b.m31;
        double n02 = m00*b.m02 + m01*b.m12 + m02*b.m22 + m03*b.m32;
        double n03 = m00*b.m03 + m01*b.m13 + m02*b.m23 + m03*b.m33;

        double n10 = m10*b.m00 + m11*b.m10 + m12*b.m20 + m13*b.m30;
        double n11 = m10*b.m01 + m11*b.m11 + m12*b.m21 + m13*b.m31;
        double n12 = m10*b.m02 + m11*b.m12 + m12*b.m22 + m13*b.m32;
        double n13 = m10*b.m03 + m11*b.m13 + m12*b.m23 + m13*b.m33;

        double n20 = m20*b.m00 + m21*b.m10 + m22*b.m20 + m23*b.m30;
        double n21 = m20*b.m01 + m21*b.m11 + m22*b.m21 + m23*b.m31;
        double n22 = m20*b.m02 + m21*b.m12 + m22*b.m22 + m23*b.m32;
        double n23 = m20*b.m03 + m21*b.m13 + m22*b.m23 + m23*b.m33;

        double n30 = m30*b.m00 + m31*b.m10 + m32*b.m20 + m33*b.m30;
        double n31 = m30*b.m01 + m31*b.m11 + m32*b.m21 + m33*b.m31;
        double n32 = m30*b.m02 + m31*b.m12 + m32*b.m22 + m33*b.m32;
        double n33 = m30*b.m03 + m31*b.m13 + m32*b.m23 + m33*b.m33;

        return new Matrix4(
                n00,n01,n02,n03,
                n10,n11,n12,n13,
                n20,n21,n22,n23,
                n30,n31,n32,n33
        );
    }

    /** this * [x,y,z,w]^T -> [x',y',z',w'] */
    public double[] mul(double x, double y, double z, double w) {
        double nx = m00*x + m01*y + m02*z + m03*w;
        double ny = m10*x + m11*y + m12*z + m13*w;
        double nz = m20*x + m21*y + m22*z + m23*w;
        double nw = m30*x + m31*y + m32*z + m33*w;
        return new double[]{nx, ny, nz, nw};
    }

    public Matrix4 transpose() {
        return new Matrix4(
                m00,m10,m20,m30,
                m01,m11,m21,m31,
                m02,m12,m22,m32,
                m03,m13,m23,m33
        );
    }

    /** det(4x4). */
    public double determinant() {
        // Precompute 2x2 minors shared by cofactors
        double s0 = m00*m11 - m10*m01;
        double s1 = m00*m12 - m10*m02;
        double s2 = m00*m13 - m10*m03;
        double s3 = m01*m12 - m11*m02;
        double s4 = m01*m13 - m11*m03;
        double s5 = m02*m13 - m12*m03;

        double c5 = m22*m33 - m32*m23;
        double c4 = m21*m33 - m31*m23;
        double c3 = m21*m32 - m31*m22;
        double c2 = m20*m33 - m30*m23;
        double c1 = m20*m32 - m30*m22;
        double c0 = m20*m31 - m30*m21;

        return s0*c5 - s1*c4 + s2*c3 + s3*c2 - s4*c1 + s5*c0;
    }

    /** Full inverse via adjugate. */
    public Matrix4 inverse() {
        double s0 = m00*m11 - m10*m01;
        double s1 = m00*m12 - m10*m02;
        double s2 = m00*m13 - m10*m03;
        double s3 = m01*m12 - m11*m02;
        double s4 = m01*m13 - m11*m03;
        double s5 = m02*m13 - m12*m03;

        double c5 = m22*m33 - m32*m23;
        double c4 = m21*m33 - m31*m23;
        double c3 = m21*m32 - m31*m22;
        double c2 = m20*m33 - m30*m23;
        double c1 = m20*m32 - m30*m22;
        double c0 = m20*m31 - m30*m21;

        double det = s0*c5 - s1*c4 + s2*c3 + s3*c2 - s4*c1 + s5*c0;
        if (Math.abs(det) < EPS) throw new ArithmeticException("Singular matrix");
        double invDet = 1.0 / det;

        double n00 = (+m11*c5 - m12*c4 + m13*c3) * invDet;
        double n01 = (-m01*c5 + m02*c4 - m03*c3) * invDet;
        double n02 = (+m31*s5 - m32*s4 + m33*s3) * invDet;
        double n03 = (-m21*s5 + m22*s4 - m23*s3) * invDet;

        double n10 = (-m10*c5 + m12*c2 - m13*c1) * invDet;
        double n11 = (+m00*c5 - m02*c2 + m03*c1) * invDet;
        double n12 = (-m30*s5 + m32*s2 - m33*s1) * invDet;
        double n13 = (+m20*s5 - m22*s2 + m23*s1) * invDet;

        double n20 = (+m10*c4 - m11*c2 + m13*c0) * invDet;
        double n21 = (-m00*c4 + m01*c2 - m03*c0) * invDet;
        double n22 = (+m30*s4 - m31*s2 + m33*s0) * invDet;
        double n23 = (-m20*s4 + m21*s2 - m23*s0) * invDet;

        double n30 = (-m10*c3 + m11*c1 - m12*c0) * invDet;
        double n31 = (+m00*c3 - m01*c1 + m02*c0) * invDet;
        double n32 = (-m30*s3 + m31*s1 - m32*s0) * invDet;
        double n33 = (+m20*s3 - m21*s1 + m22*s0) * invDet;

        return new Matrix4(
                n00,n01,n02,n03,
                n10,n11,n12,n13,
                n20,n21,n22,n23,
                n30,n31,n32,n33
        );
    }

    /**
     * Fast inverse for an affine matrix:
     * [A t; 0 0 0 1], where A is 3x3 (rotation+scale+shear).
     * Verifies the last row ~ [0,0,0,1].
     */
    public Matrix4 inverseAffine() {
        if (Math.abs(m30) > EPS || Math.abs(m31) > EPS || Math.abs(m32) > EPS || Math.abs(m33 - 1.0) > EPS) {
            throw new IllegalArgumentException("Not affine [*,*,*,*; *,*,*,*; *,*,*,*; 0,0,0,1]");
        }

        double a00 = m00, a01 = m01, a02 = m02;
        double a10 = m10, a11 = m11, a12 = m12;
        double a20 = m20, a21 = m21, a22 = m22;

        double detA =  a00*(a11*a22 - a12*a21)
                - a01*(a10*a22 - a12*a20)
                + a02*(a10*a21 - a11*a20);
        if (Math.abs(detA) < EPS) throw new ArithmeticException("Singular affine 3x3");

        double invA00 = ( a11*a22 - a12*a21) / detA;
        double invA01 = (-a01*a22 + a02*a21) / detA;
        double invA02 = ( a01*a12 - a02*a11) / detA;

        double invA10 = (-a10*a22 + a12*a20) / detA;
        double invA11 = ( a00*a22 - a02*a20) / detA;
        double invA12 = (-a00*a12 + a02*a10) / detA;

        double invA20 = ( a10*a21 - a11*a20) / detA;
        double invA21 = (-a00*a21 + a01*a20) / detA;
        double invA22 = ( a00*a11 - a01*a10) / detA;

        double tx = m03, ty = m13, tz = m23;

        double itx = -(invA00*tx + invA01*ty + invA02*tz);
        double ity = -(invA10*tx + invA11*ty + invA12*tz);
        double itz = -(invA20*tx + invA21*ty + invA22*tz);

        return new Matrix4(
                invA00, invA01, invA02, itx,
                invA10, invA11, invA12, ity,
                invA20, invA21, invA22, itz,
                0,      0,      0,      1
        );
    }
}