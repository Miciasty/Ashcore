package nsk.nu.api.random;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * O(1) weighted sampling using the Alias Method (Walker/Vose).
 * Build cost: O(n) time and O(n) memory. Each sample: O(1).
 *
 * Use when weights are stable and you need many draws. For few draws or frequently
 * changing weights, prefer a simple O(n) loop or prefix-sum + binary search (O(log n)).
 */
public final class WeightedSampler {
    private final double[] prob;
    private final int[] alias;
    private final int n;

    private WeightedSampler(double[] prob, int[] alias) {
        this.prob = prob;
        this.alias = alias;
        this.n = prob.length;
    }
    /**
     * Builds a sampler for the given non-negative weights. At least one weight must be > 0.
     * @param weights non-negative weights
     * @return sampler ready to draw in O(1)
     */
    public static WeightedSampler build(double[] weights) {
        int n = weights.length;
        if (n == 0) throw new IllegalArgumentException("weights empty");
        double sum = 0;
        for (double w : weights) {
            if (w < 0) throw new IllegalArgumentException("negative weight");
            sum += w;
        }
        if (sum <= 0) throw new IllegalArgumentException("all weights are zero");

        double[] p = new double[n];
        Deque<Integer> small = new ArrayDeque<>();
        Deque<Integer> large = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            p[i] = weights[i] * n / sum;
            if (p[i] < 1.0) small.add(i); else large.add(i);
        }

        double[] prob = new double[n];
        int[] alias = new int[n];

        while (!small.isEmpty() && !large.isEmpty()) {
            int s = small.removeLast();
            int l = large.removeLast();
            prob[s] = p[s];
            alias[s] = l;
            p[l] = (p[l] + p[s]) - 1.0;
            if (p[l] < 1.0) small.add(l); else large.add(l);
        }
        while (!large.isEmpty()) prob[large.removeLast()] = 1.0;
        while (!small.isEmpty()) prob[small.removeLast()] = 1.0;

        return new WeightedSampler(prob, alias);
    }
    /**
     * Draws an index using a single uniform double from {@link DeterministicRandom}.
     * @return index in [0, n)
     */
    public int sampleIndex(DeterministicRandom rng) {
        double u = rng.nextUnitDouble() * n;
        int i = (int) u;
        double frac = u - i;
        return (frac < prob[i]) ? i : alias[i];
    }
}