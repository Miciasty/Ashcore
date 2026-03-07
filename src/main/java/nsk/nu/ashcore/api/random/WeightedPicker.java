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
        for (int i = 0; i < weights.length; i++) {
            r -= weights[i];
            if (r <= 0.0) return i;
        }
        return weights.length - 1;
    }
    /** Picks an element from a list using corresponding weight array. */
    public static <T> T pick(List<T> items, double[] weights, DeterministicRandom rng) {
        if (items == null) throw new NullPointerException("items");
        if (weights == null) throw new NullPointerException("weights");
        if (items.size() != weights.length) throw new IllegalArgumentException("Size mismatch.");
        return items.get(pickIndex(weights, rng));
    }
}
