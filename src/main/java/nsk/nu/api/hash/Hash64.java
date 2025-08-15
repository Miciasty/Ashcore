package nsk.nu.api.hash;

/**
 * Lightweight 64-bit hashing utilities for stable identifiers and seed mixing.
 * Not cryptographic. Prefer MessageDigest for security-sensitive cases.
 */
public final class Hash64 {
    private Hash64() {}

    /** FNV-1a 64-bit hash for a byte array. */
    public static long fnv1a(byte[] data) {
        long h = 0xcbf29ce484222325L;
        for (byte b : data) {
            h ^= (b & 0xFF);
            h *= 0x100000001b3L;
        }
        return h;
    }
    /** FNV-1a over a string's UTF-8 bytes. */
    public static long fnv1a(String s) { return fnv1a(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
    /** Simple 64-bit mix (SplitMix-style) for combining hashes/seeds. */
    public static long mix64(long x) {
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }
}