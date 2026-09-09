package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Axis-aligned bounding box defined by inclusive {@code min} and {@code max} corners.
 * Suitable for fast broad-phase tests and simple hitboxes.
 * Reversed bounds are rejected; zero extent represents a closed lower-dimensional box.
 * The constructor retains legacy acceptance of NaN/infinite corners for adapters that validate them later.
 * Collision queries require finite bounds. contains returns false if the box or point is non-finite.
 */
public record AxisAlignedBox(Vector3 min, Vector3 max) {
    public AxisAlignedBox {
        if (min == null) throw new NullPointerException("min");
        if (max == null) throw new NullPointerException("max");
        if (min.x() > max.x() || min.y() > max.y() || min.z() > max.z()) {
            throw new IllegalArgumentException("min must be <= max on all axes");
        }
    }

    /**
     * @return true if the point lies inside or on the boundary; false for non-finite points
     */
    public boolean contains(Vector3 p) {
        if (!Double.isFinite(min.x()) || !Double.isFinite(min.y()) || !Double.isFinite(min.z()) ||
                !Double.isFinite(max.x()) || !Double.isFinite(max.y()) || !Double.isFinite(max.z())) return false;
        if (!Double.isFinite(p.x()) || !Double.isFinite(p.y()) || !Double.isFinite(p.z())) return false;
        if (p.x() < min.x() || p.x() > max.x()) return false;
        if (p.y() < min.y() || p.y() > max.y()) return false;
        if (p.z() < min.z() || p.z() > max.z()) return false;
        return true;
    }
}
