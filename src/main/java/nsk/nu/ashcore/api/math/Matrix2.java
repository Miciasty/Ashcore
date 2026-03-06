package nsk.nu.ashcore.api.math;

/** Immutable 2x2 matrix, row-major. */
public record Matrix2(double m00, double m01,
                      double m10, double m11) {

    /** I2. */
    public static Matrix2 identity() {
        return new Matrix2(1,0, 0,1);
    }

    /** Build from rows. */
    public static Matrix2 ofRows(double m00, double m01,
                                 double m10, double m11) {
        return new Matrix2(m00,m01, m10,m11);
    }

    /** Build from columns (basis X,Y). */
    public static Matrix2 fromColumns(double c00, double c10,
                                      double c01, double c11) {
        return new Matrix2(
                c00, c01,
                c10, c11
        );
    }

    /** this * b */
    public Matrix2 mul(Matrix2 b) {
        double n00 = m00*b.m00 + m01*b.m10;
        double n01 = m00*b.m01 + m01*b.m11;
        double n10 = m10*b.m00 + m11*b.m10;
        double n11 = m10*b.m01 + m11*b.m11;
        return new Matrix2(n00,n01, n10,n11);
    }

    /** this * [x,y]^T -> [x',y'] */
    public double[] mul(double x, double y) {
        double nx = m00*x + m01*y;
        double ny = m10*x + m11*y;
        return new double[]{nx, ny};
    }

    public Matrix2 transpose() {
        return new Matrix2(
                m00, m10,
                m01, m11
        );
    }

    public double determinant() {
        return m00*m11 - m01*m10;
    }

    public Matrix2 inverse() {
        double det = determinant();
        if (Math.abs(det) < NumericTolerance.EPS) throw new ArithmeticException("Singular matrix");
        double inv = 1.0 / det;
        return new Matrix2(
                +m11*inv, -m01*inv,
                -m10*inv, +m00*inv
        );
    }
}