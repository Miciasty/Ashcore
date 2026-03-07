package nsk.nu.ashcore.api.hash;

/**
 * Morton Z-order encode/decode helpers.
 *
 * <p>2D supports full signed 32-bit integers via biasing.
 * 3D supports signed 21-bit integers (required to fit 3 axes into 64 bits).</p>
 */
public final class Morton {
    private Morton() {}

    private static final int MORTON3_SHIFT = 20;
    private static final int MORTON3_MIN = -(1 << MORTON3_SHIFT);
    private static final int MORTON3_MAX = (1 << MORTON3_SHIFT) - 1;
    private static final long MORTON3_MASK = (1L << (MORTON3_SHIFT + 1)) - 1L;

    private static long bias(int v) {
        return (long) v - Integer.MIN_VALUE;
    }

    private static long part1By1(long x) {
        x &= 0x00000000FFFFFFFFL;
        x = (x ^ (x << 16)) & 0x0000FFFF0000FFFFL;
        x = (x ^ (x << 8)) & 0x00FF00FF00FF00FFL;
        x = (x ^ (x << 4)) & 0x0F0F0F0F0F0F0F0FL;
        x = (x ^ (x << 2)) & 0x3333333333333333L;
        x = (x ^ (x << 1)) & 0x5555555555555555L;
        return x;
    }

    private static long compact1By1(long x) {
        x &= 0x5555555555555555L;
        x = (x ^ (x >> 1)) & 0x3333333333333333L;
        x = (x ^ (x >> 2)) & 0x0F0F0F0F0F0F0F0FL;
        x = (x ^ (x >> 4)) & 0x00FF00FF00FF00FFL;
        x = (x ^ (x >> 8)) & 0x0000FFFF0000FFFFL;
        x = (x ^ (x >> 16)) & 0x00000000FFFFFFFFL;
        return x;
    }

    public static long encode2D(int x, int y) {
        long X = part1By1(bias(x)), Y = part1By1(bias(y));
        return (Y << 1) | X;
    }

    public static int decode2D_X(long code) {
        long X = compact1By1(code);
        return (int) (X + Integer.MIN_VALUE);
    }

    public static int decode2D_Y(long code) {
        long Y = compact1By1(code >>> 1);
        return (int) (Y + Integer.MIN_VALUE);
    }

    private static long part1By2(long x) {
        x &= MORTON3_MASK;
        x = (x | (x << 32)) & 0x001F00000000FFFFL;
        x = (x | (x << 16)) & 0x001F0000FF0000FFL;
        x = (x | (x << 8)) & 0x100F00F00F00F00FL;
        x = (x | (x << 4)) & 0x10C30C30C30C30C3L;
        x = (x | (x << 2)) & 0x1249249249249249L;
        return x;
    }

    private static long compact1By2(long x) {
        x &= 0x1249249249249249L;
        x = (x ^ (x >> 2)) & 0x10C30C30C30C30C3L;
        x = (x ^ (x >> 4)) & 0x100F00F00F00F00FL;
        x = (x ^ (x >> 8)) & 0x001F0000FF0000FFL;
        x = (x ^ (x >> 16)) & 0x001F00000000FFFFL;
        x = (x ^ (x >> 32)) & MORTON3_MASK;
        return x;
    }

    /**
     * 3D variant supports signed 21-bit coordinates in range [-1048576, 1048575].
     */
    public static long encode3D(int x, int y, int z) {
        long X = part1By2(encodeSigned21(x));
        long Y = part1By2(encodeSigned21(y));
        long Z = part1By2(encodeSigned21(z));
        return (Z << 2) | (Y << 1) | X;
    }

    public static int decode3D_X(long code) {
        return decodeSigned21(compact1By2(code));
    }

    public static int decode3D_Y(long code) {
        return decodeSigned21(compact1By2(code >>> 1));
    }

    public static int decode3D_Z(long code) {
        return decodeSigned21(compact1By2(code >>> 2));
    }

    private static long encodeSigned21(int value) {
        if (value < MORTON3_MIN || value > MORTON3_MAX) {
            throw new IllegalArgumentException("3D Morton coordinate out of range [-1048576, 1048575]: " + value);
        }
        return (long) (value - MORTON3_MIN);
    }

    private static int decodeSigned21(long encoded) {
        return (int) encoded + MORTON3_MIN;
    }
}
