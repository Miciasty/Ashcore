package nsk.nu.ashcore.api.random;

import java.util.List;

/**
 * Deterministic weighted choice over a list of non-negative weights.
 * Uses {@link DeterministicRandom} to remain reproducible across runs.
 */
public final class WeightedPicker {

    private WeightedPicker() {}
    /**
     * Picks an index from the weights array; all weights must be >= 0 and not all zero.
     * Weights and their sum must be finite. Uses one uniform-double draw and O(n) time/O(1) memory.
     * Selection intervals are left-closed/right-open; zero-weight entries are never selected.
     * Ratios use double precision and the RNG's finite resolution. Inputs must remain stable during the call.
     * @return selected index in [0, weights.length)
     */
    public static int pickIndex(double[] weights, DeterministicRandom rng) {
        if (weights == null || weights.length == 0) throw new IllegalArgumentException("weights empty");
        if (rng == null) throw new NullPointerException("rng");

        double sum = 0.0;
        for (double w : weights) {
            if (!Double.isFinite(w) || w < 0.0) throw new IllegalArgumentException("weights must be >= 0 and finite");
            sum += w;
        }
        if (!Double.isFinite(sum) || sum <= 0.0) {
            throw new IllegalArgumentException("weights sum must be finite and > 0");
        }

        double r = rng.nextUnitDouble() * sum;
        int lastPositive = -1;
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] == 0.0) continue;
            lastPositive = i;
            if (r < weights[i]) return i;
            r -= weights[i];
        }
        return lastPositive; // rounding at the upper endpoint must not select a trailing zero
    }
    /** Picks an element from a list using corresponding weight array. */
    public static <T> T pick(List<T> items, double[] weights, DeterministicRandom rng) {
        if (items == null) throw new NullPointerException("items");
        if (weights == null) throw new NullPointerException("weights");
        if (items.size() != weights.length) throw new IllegalArgumentException("Size mismatch.");
        return items.get(pickIndex(weights, rng));
    }
}
