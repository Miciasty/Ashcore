package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.hash.Hash64;

/**
 * Stateless seed derivation: mix64(root XOR FNV-1a-64(UTF-8 tag)). Stable in Ashcore 1.x.
 * Tag text is not normalized. Repeated tags repeat seeds; distinct tags are not guaranteed collision-free
 * or statistically independent. O(tag UTF-8 length) time and temporary memory. Null tags are rejected.
 */
public final class SeedSequence {
    private final long root;
    public SeedSequence(long root){ this.root = root; }
    public long derive(String tag){
        return Hash64.mix64(root ^ Hash64.fnv1a(tag));
    }
}
