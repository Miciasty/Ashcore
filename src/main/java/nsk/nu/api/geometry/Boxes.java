package nsk.nu.api.geometry;

import nsk.nu.api.math.Vector3;

/** Extra helpers for AxisAlignedBox. */
public final class Boxes {
    private Boxes(){}
    /** @return union box that contains a and b. */
    public static AxisAlignedBox union(AxisAlignedBox a, AxisAlignedBox b){
        return new AxisAlignedBox(
                new Vector3(Math.min(a.min().x(), b.min().x()),
                        Math.min(a.min().y(), b.min().y()),
                        Math.min(a.min().z(), b.min().z())),
                new Vector3(Math.max(a.max().x(), b.max().x()),
                        Math.max(a.max().y(), b.max().y()),
                        Math.max(a.max().z(), b.max().z()))
        );
    }
    /** Expands box by margin in all directions. */
    public static AxisAlignedBox expand(AxisAlignedBox b, double m){
        return new AxisAlignedBox(
                new Vector3(b.min().x()-m, b.min().y()-m, b.min().z()-m),
                new Vector3(b.max().x()+m, b.max().y()+m, b.max().z()+m)
        );
    }
    /** Inclusive overlap test. */
    public static boolean overlaps(AxisAlignedBox a, AxisAlignedBox b){
        if (a.max().x() < b.min().x() || b.max().x() < a.min().x()) return false;
        if (a.max().y() < b.min().y() || b.max().y() < a.min().y()) return false;
        if (a.max().z() < b.min().z() || b.max().z() < a.min().z()) return false;
        return true;
    }
}