package nsk.nu.ashcore.api.math;

/** Kahan compensated summation for numerically stable running sums. */
public final class KahanSummation {
    private double sum = 0.0;
    private double c = 0.0; // compensation
    public void add(double x){
        double y = x - c;
        double t = sum + y;
        c = (t - sum) - y;
        sum = t;
    }
    public double value(){ return sum; }
    public void reset(){ sum = 0.0; c = 0.0; }
}