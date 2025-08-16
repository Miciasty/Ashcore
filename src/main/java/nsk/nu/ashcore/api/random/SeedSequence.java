package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.hash.Hash64;

/** Derives independent deterministic seeds from a root seed + tag. */
public final class SeedSequence {
    private final long root;
    public SeedSequence(long root){ this.root = root; }
    public long derive(String tag){
        return Hash64.mix64(root ^ Hash64.fnv1a(tag));
    }
}