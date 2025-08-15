package nsk.nu.api.math;

/** Mathematical floor-division and modulo for ints/longs. */
public final class DivMod {
    private DivMod(){}
    public static int floorDiv(int a, int b){ int q = a / b; int r = a % b; return (r!=0 && ((r>0) != (b>0))) ? (q-1) : q; }
    public static int floorMod(int a, int b){ int r = a % b; return (r<0) ? r + Math.abs(b) : r; }
    public static long floorDiv(long a, long b){ long q = a / b; long r = a % b; return (r!=0 && ((r>0)!=(b>0))) ? (q-1) : q; }
    public static long floorMod(long a, long b){ long r = a % b; return (r<0) ? r + Math.abs(b) : r; }
}
