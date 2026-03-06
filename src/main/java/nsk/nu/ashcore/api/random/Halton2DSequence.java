package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.math.Vector2;
import nsk.nu.ashcore.api.math.Vector3;

/**
 * Incremental 2D Halton sequence with independent bases for X and Y (default 2 and 3).
 *
 * <p>Each call is amortized O(1) because only changed base-b digits are updated.</p>
 */
public final class Halton2DSequence {
    private final HaltonSequence x;
    private final HaltonSequence y;

    /**
     * Creates a 2D Halton sequence using bases 2 (X) and 3 (Y).
     */
    public Halton2DSequence() {
        this(2, 3);
    }

    /**
     * Creates a 2D Halton sequence using custom bases.
     *
     * @param baseX base for X dimension, must be > 1
     * @param baseY base for Y dimension, must be > 1
     */
    public Halton2DSequence(int baseX, int baseY) {
        this.x = new HaltonSequence(baseX);
        this.y = new HaltonSequence(baseY);
    }

    /**
     * Resets both dimensions to the initial state.
     */
    public void reset() {
        x.reset();
        y.reset();
    }

    /**
     * Returns the next point in the unit square [0,1)^2.
     *
     * @return next low-discrepancy sample in unit square
     */
    public Vector2 nextUnitSquare() {
        return new Vector2(x.next(), y.next());
    }

    /**
     * Returns the next point mapped to the unit disk using concentric mapping.
     *
     * @return next low-discrepancy sample on unit disk
     */
    public Vector2 nextConcentricDisk() {
        Vector2 uv = nextUnitSquare();
        return LowDiscrepancy.mapToConcentricDisk(uv.x(), uv.y());
    }

    /**
     * Returns the next direction uniformly distributed on a Y-up hemisphere.
     *
     * @return unit direction on hemisphere
     */
    public Vector3 nextUniformHemisphereYUp() {
        Vector2 uv = nextUnitSquare();
        return LowDiscrepancy.mapToUniformHemisphere(uv.x(), uv.y());
    }
}