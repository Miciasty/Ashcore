package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Intersection result for ray casting.
 * Contains the parameter {@code t}, the hit point, and the outward normal if available.
 */
public record Hit(double t, Vector3 point, Vector3 normal) {
    /** @return true if {@code t} is finite (means a hit) */
    public boolean hit() { return Double.isFinite(t); }
}