package nsk.nu.api.math;

public record Int2(int x, int y) {
    public Int2 add(Int2 o){ return new Int2(x + o.x, y + o.y); }
    public Int2 sub(Int2 o){ return new Int2(x - o.x, y - o.y); }
    public Int2 mul(int s){ return new Int2(x * s, y * s); }
}