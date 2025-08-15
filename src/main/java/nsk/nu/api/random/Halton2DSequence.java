package nsk.nu.api.random;

import nsk.nu.api.math.Double2;
import nsk.nu.api.math.Vector3;

/**
 * Incremental 2D Halton sequence with independent bases for X and Y (default 2 and 3).
 * Each {@link #nextUnitSquare()} call is amortized O(1) by updating only changed base-b digits.
 *
 * Typical use:
 * <pre>
 *   var seq = new Halton2DSequence();          // bases 2 and 3
 *   Double2 uv = seq.nextUnitSquare();         // [0,1)^2
 *   Double2 disk = seq.nextConcentricDisk();   // unit disk
 *   Vector3 hemi = seq.nextUniformHemisphereYUp(); // hemisphere (y up)
 * </pre>
 */
public final class Halton2DSequence {
    private final HaltonSequence x;
    private final HaltonSequence y;

    /** Creates a 2D Halton sequence with bases 2 (X) and 3 (Y). */
    public Halton2DSequence() {
        this(2, 3);
    }

    /**
     * @param baseX prime base for X (>1)
     * @param baseY prime base for Y (>1), different from baseX for best coverage
     */
    public Halton2DSequence(int baseX, int baseY) {
        this.x = new HaltonSequence(baseX);
        this.y = new HaltonSequence(baseY);
    }
    /** Resets both dimensions to index 0. */
    public void reset() { x.reset(); y.reset(); }
    /** @return next point in the unit square [0,1)^2 using bases (baseX, baseY). */
    public Double2 nextUnitSquare() {
        return new Double2(x.next(), y.next());
    }
    /** @return next point mapped from [0,1)^2 to the unit disk using concentric mapping. */
    public Double2 nextConcentricDisk() {
        Double2 uv = nextUnitSquare();
        return LowDiscrepancy.mapToConcentricDisk(uv.x(), uv.y());
    }
    /** @return next direction uniformly distributed on the unit hemisphere (y up). */
    public Vector3 nextUniformHemisphereYUp() {
        Double2 uv = nextUnitSquare();
        return LowDiscrepancy.mapToUniformHemisphere(uv.x(), uv.y());
    }
}