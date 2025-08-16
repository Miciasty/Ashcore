package nsk.nu.ashcore.api.hash;

/** Morton Z-order encode/decode for 2D/3D with signed bias. */
public final class Morton {
    private Morton(){}
    private static long bias(int v){ return (long)v - Integer.MIN_VALUE; }
    private static long part1By1(long x){ x&=0x00000000FFFFFFFFL; x=(x^(x<<16))&0x0000FFFF0000FFFFL;
        x=(x^(x<<8))&0x00FF00FF00FF00FFL; x=(x^(x<<4))&0x0F0F0F0F0F0F0F0FL;
        x=(x^(x<<2))&0x3333333333333333L; x=(x^(x<<1))&0x5555555555555555L; return x; }
    private static long compact1By1(long x){ x&=0x5555555555555555L; x=(x^(x>>1))&0x3333333333333333L;
        x=(x^(x>>2))&0x0F0F0F0F0F0F0F0FL; x=(x^(x>>4))&0x00FF00FF00FF00FFL;
        x=(x^(x>>8))&0x0000FFFF0000FFFFL; x=(x^(x>>16))&0x00000000FFFFFFFFL; return x; }
    public static long encode2D(int x, int y){
        long X = part1By1(bias(x)), Y = part1By1(bias(y));
        return (Y<<1) | X;
    }
    public static int decode2D_X(long code){
        long X = compact1By1(code); return (int)(X + Integer.MIN_VALUE);
    }
    public static int decode2D_Y(long code){
        long Y = compact1By1(code>>>1); return (int)(Y + Integer.MIN_VALUE);
    }

    private static long part1By2(long x){ x&=0x00000000000003FFL;
        x=(x|x<<16)&0x00000000FF0000FFL; x=(x|x<<8)&0x000000F00F00F00FL;
        x=(x|x<<4)&0x00000C30C30C30C3L; x=(x|x<<2)&0x0000249249249249L; return x; }
    private static long compact1By2(long x){ x&=0x0000249249249249L;
        x=(x^(x>>2))&0x00000C30C30C30C3L; x=(x^(x>>4))&0x000000F00F00F00FL;
        x=(x^(x>>8))&0x00000000FF0000FFL; x=(x^(x>>16))&0x00000000000003FFL; return x; }

    public static long encode3D(int x, int y, int z){
        long X=part1By2(bias(x)&0x1FFFFFL), Y=part1By2(bias(y)&0x1FFFFFL), Z=part1By2(bias(z)&0x1FFFFFL);
        return (Z<<2) | (Y<<1) | X;
    }
    public static int decode3D_X(long code){ long X=compact1By2(code); return (int)((X|(~0L<<21)) + Integer.MIN_VALUE); }
    public static int decode3D_Y(long code){ long Y=compact1By2(code>>>1); return (int)((Y|(~0L<<21)) + Integer.MIN_VALUE); }
    public static int decode3D_Z(long code){ long Z=compact1By2(code>>>2); return (int)((Z|(~0L<<21)) + Integer.MIN_VALUE); }
}