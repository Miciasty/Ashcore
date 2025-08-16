package nsk.nu.ashcore.api.math;

/** Immutable 3x3 matrix, row-major. */
public record Matrix3(
        double m00, double m01, double m02,
        double m10, double m11, double m12,
        double m20, double m21, double m22) {

    /** I3. */
    public static Matrix3 identity() {
        return new Matrix3(1,0,0, 0,1,0, 0,0,1);
    }

    /** Build from rows. */
    public static Matrix3 ofRows(
            double m00, double m01, double m02,
            double m10, double m11, double m12,
            double m20, double m21, double m22) {
        return new Matrix3(m00,m01,m02, m10,m11,m12, m20,m21,m22);
    }

    /** Build from column vectors (basis X,Y,Z). */
    public static Matrix3 fromColumns(Vector3 cx, Vector3 cy, Vector3 cz) {
        return new Matrix3(
                cx.x(), cy.x(), cz.x(),
                cx.y(), cy.y(), cz.y(),
                cx.z(), cy.z(), cz.z());
    }

    /** this * v */
    public Vector3 mul(Vector3 v) {
        double x = m00*v.x() + m01*v.y() + m02*v.z();
        double y = m10*v.x() + m11*v.y() + m12*v.z();
        double z = m20*v.x() + m21*v.y() + m22*v.z();
        return new Vector3(x, y, z);
    }

    /** this * b */
    public Matrix3 mul(Matrix3 b) {
        double n00 = m00*b.m00 + m01*b.m10 + m02*b.m20;
        double n01 = m00*b.m01 + m01*b.m11 + m02*b.m21;
        double n02 = m00*b.m02 + m01*b.m12 + m02*b.m22;

        double n10 = m10*b.m00 + m11*b.m10 + m12*b.m20;
        double n11 = m10*b.m01 + m11*b.m11 + m12*b.m21;
        double n12 = m10*b.m02 + m11*b.m12 + m12*b.m22;

        double n20 = m20*b.m00 + m21*b.m10 + m22*b.m20;
        double n21 = m20*b.m01 + m21*b.m11 + m22*b.m21;
        double n22 = m20*b.m02 + m21*b.m12 + m22*b.m22;

        return new Matrix3(n00,n01,n02, n10,n11,n12, n20,n21,n22);
    }

    public Matrix3 transpose() {
        return new Matrix3(
                m00,m10,m20,
                m01,m11,m21,
                m02,m12,m22);
    }

    public double determinant() {
        return  m00*(m11*m22 - m12*m21)
                - m01*(m10*m22 - m12*m20)
                + m02*(m10*m21 - m11*m20);
    }

    public Matrix3 inverse() {
        double det = determinant();
        if (Math.abs(det) < 1e-12) throw new ArithmeticException("Singular matrix");
        double inv = 1.0 / det;

        double i00 =  (m11*m22 - m12*m21) * inv;
        double i01 = -(m01*m22 - m02*m21) * inv;
        double i02 =  (m01*m12 - m02*m11) * inv;

        double i10 = -(m10*m22 - m12*m20) * inv;
        double i11 =  (m00*m22 - m02*m20) * inv;
        double i12 = -(m00*m12 - m02*m10) * inv;

        double i20 =  (m10*m21 - m11*m20) * inv;
        double i21 = -(m00*m21 - m01*m20) * inv;
        double i22 =  (m00*m11 - m01*m10) * inv;

        return new Matrix3(i00,i01,i02, i10,i11,i12, i20,i21,i22);
    }
}