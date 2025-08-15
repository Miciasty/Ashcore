package nsk.nu.api.stats;

import nsk.nu.api.random.DeterministicRandom;

/** Reservoir sampling (Algorithm R) for k samples from a stream of unknown length. */
public final class ReservoirSampler<T> {
    private final Object[] res;
    private final DeterministicRandom rng;
    private int seen = 0;
    private int size = 0;

    public ReservoirSampler(int k, DeterministicRandom rng) {
        if (k <= 0) throw new IllegalArgumentException("k > 0");
        this.res = new Object[k];
        this.rng = rng;
    }

    @SuppressWarnings("unchecked")
    public T[] snapshot() { return (T[]) res.clone(); }

    public void offer(T item) {
        seen++;
        if (size < res.length) {
            res[size++] = item;
        } else {
            int j = (int) Math.floor(rng.nextUnitDouble() * seen);
            if (j < res.length) res[j] = item;
        }
    }
}
