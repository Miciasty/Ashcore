package nsk.nu.ashcore.api.math;

/** Mathematical floor-division and modulo for ints/longs. */
public final class DivMod {
    private DivMod() {}

    public static int floorDiv(int a, int b) {
        return Math.floorDiv(a, b);
    }

    public static int floorMod(int a, int b) {
        return Math.floorMod(a, b);
    }

    public static long floorDiv(long a, long b) {
        return Math.floorDiv(a, b);
    }

    public static long floorMod(long a, long b) {
        return Math.floorMod(a, b);
    }
}
