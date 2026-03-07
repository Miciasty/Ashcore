package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Axis-aligned bounding box defined by inclusive {@code min} and {@code max} corners.
 * Suitable for fast broad-phase tests and simple hitboxes.
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
     * @return true if the point lies inside or on the boundary of this box
     */
    public boolean contains(Vector3 p) {
        if (p.x() < min.x() || p.x() > max.x()) return false;
        if (p.y() < min.y() || p.y() > max.y()) return false;
        if (p.z() < min.z() || p.z() > max.z()) return false;
        return true;
    }
}
