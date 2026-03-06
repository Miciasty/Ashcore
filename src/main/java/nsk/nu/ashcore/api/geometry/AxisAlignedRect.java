package nsk.nu.ashcore.api.geometry;
import nsk.nu.ashcore.api.math.Vector2;

/**
 * Immutable axis-aligned rectangle in 2D defined by inclusive min/max corners.
 *
 * <p>This is the 2D counterpart of {@link AxisAlignedBox} (3D AABB).</p>
 */
public record AxisAlignedRect(Vector2 min, Vector2 max) {

    /**
     * Creates a rectangle and validates normalized bounds.
     *
     * @param min minimum corner (minX, minY)
     * @param max maximum corner (maxX, maxY)
     * @throws IllegalArgumentException if min is greater than max on any axis
     */
    public AxisAlignedRect {
        if (min.x() > max.x() || min.y() > max.y()) {
            throw new IllegalArgumentException("min must be <= max on both axes");
        }
    }

    /**
     * Creates a normalized rectangle regardless of argument order.
     *
     * @param a first corner
     * @param b second corner
     * @return rectangle with ordered min/max corners
     */
    public static AxisAlignedRect of(Vector2 a, Vector2 b) {
        return new AxisAlignedRect(
                new Vector2(Math.min(a.x(), b.x()), Math.min(a.y(), b.y())),
                new Vector2(Math.max(a.x(), b.x()), Math.max(a.y(), b.y()))
        );
    }

    /**
     * Tests whether the point lies inside or on the boundary.
     *
     * @param p point to test
     * @return true if p is inside or on the rectangle boundary
     */
    public boolean contains(Vector2 p) {
        return p.x() >= min.x() && p.x() <= max.x()
                && p.y() >= min.y() && p.y() <= max.y();
    }

    /**
     * Tests inclusive overlap with another rectangle.
     *
     * @param other rectangle to test
     * @return true if the rectangles overlap or touch
     */
    public boolean overlaps(AxisAlignedRect other) {
        if (max.x() < other.min.x() || other.max.x() < min.x()) return false;
        if (max.y() < other.min.y() || other.max.y() < min.y()) return false;
        return true;
    }

    /**
     * Expands rectangle by a uniform margin in all directions.
     *
     * @param margin expansion amount; may be zero or positive
     * @return expanded rectangle
     */
    public AxisAlignedRect expand(double margin) {
        return new AxisAlignedRect(
                new Vector2(min.x() - margin, min.y() - margin),
                new Vector2(max.x() + margin, max.y() + margin)
        );
    }

    /**
     * Returns the union rectangle that contains this rectangle and {@code other}.
     *
     * @param other rectangle to include
     * @return union rectangle
     */
    public AxisAlignedRect union(AxisAlignedRect other) {
        return new AxisAlignedRect(
                new Vector2(Math.min(min.x(), other.min.x()), Math.min(min.y(), other.min.y())),
                new Vector2(Math.max(max.x(), other.max.x()), Math.max(max.y(), other.max.y()))
        );
    }

    /**
     * Clamps a point to the rectangle.
     *
     * @param p source point
     * @return closest point inside or on the rectangle
     */
    public Vector2 clamp(Vector2 p) {
        double cx = Math.min(Math.max(p.x(), min.x()), max.x());
        double cy = Math.min(Math.max(p.y(), min.y()), max.y());
        return new Vector2(cx, cy);
    }

    /**
     * Returns rectangle width.
     *
     * @return maxX - minX
     */
    public double width() {
        return max.x() - min.x();
    }

    /**
     * Returns rectangle height.
     *
     * @return maxY - minY
     */
    public double height() {
        return max.y() - min.y();
    }

    /**
     * Returns rectangle area.
     *
     * @return width * height
     */
    public double area() {
        return width() * height();
    }

    /**
     * Returns rectangle center point.
     *
     * @return midpoint between min and max
     */
    public Vector2 center() {
        return new Vector2((min.x() + max.x()) * 0.5, (min.y() + max.y()) * 0.5);
    }
}