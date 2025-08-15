package nsk.nu.api.stats;

import java.util.Arrays;

/**
 * P² (P-squared) online quantile estimator for a single quantile q in (0,1).
 * Keeps 5 markers and updates positions/heights per sample. O(1) time, O(1) memory.
 *
 * References: Jain, Chlamtac (1985) - "The P^2 Algorithm for Dynamic Calculation of Quantiles..."
 */
public final class P2Quantile {
    private final double q;
    private long n = 0;

    private final int[]  pos  = new int[5];
    private final double[] npos = new double[5];
    private final double[] pInc = new double[5];
    private final double[] h = new double[5];
    private boolean initialized = false;

    /**
     * @param quantile quantile in (0,1), e.g. 0.95 for 95th percentile
     */
    public P2Quantile(double quantile) {
        if (!(quantile > 0.0 && quantile < 1.0)) throw new IllegalArgumentException("q in (0,1)");
        this.q = quantile;
        pInc[0] = 0.0;
        pInc[1] = q * 0.5;
        pInc[2] = q;
        pInc[3] = (1.0 + q) * 0.5;
        pInc[4] = 1.0;
    }

    /** Adds a sample. O(1) time. */
    public void add(double x) {
        if (!initialized) {
            bootstrap(x);
            return;
        }
        n++;

        int k;
        if (x < h[0]) { h[0] = x; k = 0; }
        else if (x < h[1]) { k = 0; }
        else if (x < h[2]) { k = 1; }
        else if (x < h[3]) { k = 2; }
        else if (x <= h[4]) { k = 3; }
        else { h[4] = x; k = 3; }

        for (int i = k + 1; i < 5; i++) pos[i]++;

        for (int i = 0; i < 5; i++) npos[i] += pInc[i];

        for (int i = 1; i <= 3; i++) {
            double d = npos[i] - pos[i];
            int s = d >= 1.0 ? 1 : (d <= -1.0 ? -1 : 0);
            if (s == 0) continue;

            if ((s == 1 && pos[i + 1] - pos[i] > 1) || (s == -1 && pos[i - 1] - pos[i] < -1)) {
                double qip = parabolic(i, s);
                if (qip > h[i - 1] && qip < h[i + 1]) {
                    h[i] = qip;
                } else {
                    h[i] = linear(i, s);
                }
                pos[i] += s;
            }
        }
    }

    /** @return current estimate (NaN until at least 5 samples were seen). */
    public double estimate() {
        return initialized ? h[2] : Double.NaN;
    }

    /** Resets the estimator. */
    public void reset() {
        n = 0;
        initialized = false;
        Arrays.fill(pos, 0);
        Arrays.fill(npos, 0);
        Arrays.fill(h, 0);
    }

    private void bootstrap(double x) {
        h[(int) Math.min(n, 4)] = x; // collect up to 5 raw samples
        n++;
        if (n == 5) {
            Arrays.sort(h);
            for (int i = 0; i < 5; i++) pos[i] = i + 1;          // 1..5
            for (int i = 0; i < 5; i++) npos[i] = 1 + pInc[i] * (n - 1);
            initialized = true;
        }
    }

    private double parabolic(int i, int s) {
        double h_im1 = h[i - 1], h_i = h[i], h_ip1 = h[i + 1];
        int    n_im1 = pos[i - 1], n_i = pos[i], n_ip1 = pos[i + 1];

        double a = (n_i - n_im1 + s) * (h_ip1 - h_i) / (n_ip1 - n_i);
        double b = (n_ip1 - n_i - s) * (h_i - h_im1) / (n_i - n_im1);
        return h_i + (a + b) / (n_ip1 - n_im1);
    }

    private double linear(int i, int s) {
        return h[i] + s * (h[i + s] - h[i]) / (pos[i + s] - pos[i]);
    }
}