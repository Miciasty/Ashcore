(() => {
  const { code, table, note } = window.WIKI_HTML;

  window.WIKI_PAGES.push(
    {
      id: 'math', category: 'Math & geometry', title: 'Vectors and scalar math',
      description: 'Calculate positions, directions, angles, ranges, and numeric comparisons with explicit units.',
      kind: 'reference', readingTime: 8,
      intro: `<p>The <code>nsk.nu.ashcore.api.math</code> package supplies immutable vectors and small numeric operations. A vector stores numbers; your application decides whether they represent blocks, meters, velocity, or a dimensionless direction.</p><p>Use one coordinate system and one position unit within each calculation. Ashcore does not attach a Minecraft world, entity, or block origin to a vector.</p>`,
      sections: [
        { id: 'vector-types', title: 'Choose a vector type', html:
          table(['Type', 'Components', 'Useful operations'], [
            ['<code>Vector2</code>', 'Two <code>double</code> values', '<code>crossZ</code>, <code>perpLeft</code>, <code>perpRight</code>, distance and interpolation'],
            ['<code>Vector3</code>', 'Three <code>double</code> values', '<code>cross</code>, component replacements, distance and interpolation'],
            ['<code>Vector4</code>', 'Four <code>double</code> values', '<code>fromVector3(v, w)</code>, <code>xyz()</code>, distance and interpolation'],
            ['<code>Vector2i</code>, <code>Vector3i</code>, <code>Vector4i</code>', 'Integer components', 'Discrete coordinates, integer arithmetic, and conversion to floating point']
          ]) + `<p>Operations such as <code>add</code>, <code>sub</code>, <code>mul</code>, and <code>normalized</code> leave the input unchanged. Read components with record accessors such as <code>x()</code>. Record equality compares stored components; use a numeric tolerance for computed geometry.</p>` },
        { id: 'move-toward-a-point', title: 'Move toward a point', html:
          `<p>This example moves at most two blocks from <code>start</code> toward <code>target</code>. Subtract the positions to obtain an offset. Normalize that offset to obtain a direction, then multiply by the distance.</p>` +
          code('java', 'VectorExample.java', `import nsk.nu.ashcore.api.math.Vector3;

public class VectorExample {
    public static void main(String[] args) {
        Vector3 start = new Vector3(10, 64, 10);
        Vector3 target = new Vector3(13, 64, 14);
        Vector3 offset = target.sub(start);
        double stepBlocks = Math.min(2.0, offset.length());
        Vector3 next = start.add(offset.normalized().mul(stepBlocks));

        if (next.distance(new Vector3(11.2, 64, 11.6)) > 1e-12) {
            throw new AssertionError("Unexpected next position");
        }
        if (!start.equals(new Vector3(10, 64, 10))) {
            throw new AssertionError("The original position changed");
        }
        System.out.println(next);
    }
}`) + `<p>The target is five blocks away. The result is approximately <code>(11.2, 64, 11.6)</code>. If both positions are equal, the zero offset stays zero and the position stays unchanged.</p>` },
        { id: 'normalization-and-arithmetic', title: 'Normalization and arithmetic boundaries', html:
          table(['Operation', 'Contract'], [
            ['<code>Vector2/3/4.normalized()</code>', 'Returns a unit vector within rounding. A zero vector returns itself. NaN or infinite components throw <code>IllegalArgumentException</code>. Finite extreme values are scaled before normalization.'],
            ['<code>div(s)</code>', 'Both positive and negative zero throw <code>ArithmeticException</code>. Other inputs follow Java <code>double</code> arithmetic.'],
            ['<code>length()</code>, <code>distance(other)</code>', 'Distance in the component unit. Uses <code>Math.hypot</code>; an unrepresentable result is infinity. Coordinate subtraction can still overflow.'],
            ['<code>lengthSq()</code>, <code>distanceSq(other)</code>', 'Squared component units. Products may overflow or underflow even when the unsquared result is representable.'],
            ['<code>lerp(to, t)</code>', '<code>this + (to - this) * t</code>. The dimensionless factor is not clamped; values outside <code>[0, 1]</code> extrapolate.'],
            ['<code>min</code>, <code>max</code>', 'Component comparisons use <code>Math.min</code>/<code>Math.max</code> and propagate NaN.']
          ]) + `<p>Floating-point vector constructors accept non-finite components. Construction alone does not validate values for a later geometry query. Integer vector arithmetic can overflow; even a <code>long</code> squared-length sum can overflow at extreme component values.</p><p><code>fromFloor</code>, <code>fromRound</code>, and <code>fromCeil</code> use the corresponding Java rounding operation and an integer conversion. Validate finite input and the target integer range when converting external coordinates.</p>` },
        { id: 'angles', title: 'Angles and direction conventions', html:
          `<p><code>Angles.dirFromYawPitch</code> takes radians. Yaw zero points along +Z, positive yaw turns toward +X, and negative pitch points upward along +Y. Convert platform angles explicitly after checking the platform's sign convention.</p>` +
          code('java', 'AngleExample.java', `import nsk.nu.ashcore.api.math.Angles;
import nsk.nu.ashcore.api.math.Vector3;

public class AngleExample {
    public static void main(String[] args) {
        double yawRadians = Math.toRadians(90);
        double pitchRadians = Math.toRadians(-30);
        Vector3 direction = Angles.dirFromYawPitch(yawRadians, pitchRadians);
        Vector3 expected = new Vector3(Math.sqrt(3) / 2, 0.5, 0);
        if (direction.distance(expected) > 1e-12) {
            throw new AssertionError("Unexpected direction");
        }

        // Work around wrapDegrees180's negative-input limitation in 1.2.0.
        double wrapped = Angles.wrapDegrees180(Angles.wrapDegrees360(-720));
        if (wrapped != 0) throw new AssertionError("Unexpected wrapped angle");
        System.out.println(direction);
    }
}`) + table(['Method', 'Unit and result'], [
            ['<code>wrapRadians</code>', 'Radians in <code>[-π, π)</code> for finite input.'],
            ['<code>wrapDegrees360</code>', 'Degrees, wrapping negative input into the usual <code>[0, 360)</code> interval; floating-point rounding applies near the boundary.'],
            ['<code>wrapDegrees180</code>', 'Intended range <code>(-180, 180]</code>, with a negative-input limitation described below.'],
            ['<code>yawFromXZ(x, z)</code>', 'Radians from <code>atan2(x, z)</code>, in <code>[-π, π]</code>.'],
            ['<code>pitchFromVector(v)</code>', 'Radians in <code>[-π/2, π/2]</code>; upward vectors produce negative pitch.'],
            ['<code>deltaRadians(a, b)</code>, <code>lerpRadians(a, b, t)</code>', 'Shortest signed arc. Interpolation does not clamp <code>t</code>.'],
            ['<code>deltaDegrees(a, b)</code>', 'Uses <code>wrapDegrees180(b - a)</code> and inherits its negative-input limitation.']
          ]) + note('Negative degree wrapping in 1.2.0', '<p><code>wrapDegrees180(-720)</code> returns <code>-360</code>, outside its documented range. Normalize finite degree input with <code>wrapDegrees360</code> first, as above. For a shortest degree delta, normalize both angles first and then call <code>deltaDegrees</code>. This is a known implementation limitation, not an alternative angle convention.</p>', true) + `<p><code>lerpAngle</code> is a deprecated alias for <code>lerpRadians</code>. Use the explicit unit name in new code.</p>` },
        { id: 'ranges-and-scalar-helpers', title: 'Ranges and scalar helpers', html:
          table(['API', 'Behavior'], [
            ['<code>IntRange</code>, <code>DoubleRange</code>', 'Inclusive <code>[min, max]</code> bounds. Constructors reject reversed finite bounds; <code>of(a, b)</code> orders the arguments. Touching ranges intersect.'],
            ['<code>IntRange.sizeInclusive()</code>', 'Element count as <code>long</code>: <code>max - min + 1</code>.'],
            ['<code>DoubleRange.width()</code>', 'Numeric width <code>max - min</code>, in the bounds’ unit.'],
            ['<code>MathUtil.clamp(v, min, max)</code>', 'Clamps to inclusive bounds. Caller supplies <code>min &lt;= max</code>.'],
            ['<code>mapRange</code>', 'Clamps the input interpolation factor to <code>[0, 1]</code>; equal input bounds return <code>outMin</code>.'],
            ['<code>inverseLerp(a, b, v)</code>', 'Clamped dimensionless factor; equal endpoints return zero.'],
            ['<code>MathUtil.lerp(a, b, t)</code>', 'Returns <code>a + (b - a) * t</code>; implementation does not clamp <code>t</code>.'],
            ['<code>DivMod.floorDiv</code>, <code>floorMod</code>', 'Java floor division and modulo for <code>int</code>/<code>long</code>. With a positive divisor, negative coordinates receive a non-negative remainder.'],
            ['<code>KahanSummation</code>', 'Mutable compensated sum. <code>add</code> accumulates, <code>value</code> reads, and <code>reset</code> clears sum and compensation.']
          ]) + `<p>For a 16-block cell width, <code>DivMod.floorDiv(-1, 16)</code> is <code>-1</code> and <code>floorMod(-1, 16)</code> is <code>15</code>. A zero divisor throws <code>ArithmeticException</code>. Division retains Java's minimum-integer divided by −1 overflow behavior.</p>` + note('Validate floating-point ranges', '<p><code>DoubleRange</code> does not reject NaN or infinity. In 1.2.0, <code>new DoubleRange(0, 1).contains(Double.NaN)</code> returns <code>true</code> because both ordered comparisons are false. Validate finite bounds and values before using range membership as input validation.</p>', true) },
        { id: 'numeric-tolerances', title: 'Numeric tolerances', html:
          `<p><code>NumericTolerance.near(a, b, eps)</code> checks <code>|a - b| &lt;= eps</code>. <code>isZero(value, eps)</code> checks <code>|value| &lt;= eps</code>. Supply a finite, non-negative absolute tolerance in the same units as the values; invalid tolerances throw <code>IllegalArgumentException</code>. Comparisons involving NaN or infinity return false.</p><p><code>EPS</code> and <code>GEOMETRY_EPS</code> are <code>1e-12</code>; <code>INTERPOLATION_EPS</code> is <code>1e-9</code>. Their interpretation depends on the operation. A determinant threshold, angle cutoff, and position tolerance are different quantities.</p><p><code>MathUtil.near</code> performs the comparison directly and does not validate <code>eps</code>. Prefer <code>NumericTolerance.near</code> when you need that validation. For matrix composition and rotation, continue to <a href="#/transforms">Matrices and rotations</a>.</p>` }
      ]
    },
    {
      id: 'transforms', category: 'Math & geometry', title: 'Matrices and rotations',
      description: 'Compose affine transforms and quaternions while preserving point, direction, and angle conventions.',
      kind: 'guide', readingTime: 7,
      intro: `<p>A local position describes a point relative to an object. A transform maps that point into the coordinate system where you query the scene. Ashcore supplies matrix and quaternion arithmetic; your application owns the coordinate systems and their origins.</p>`,
      sections: [
        { id: 'matrix-convention', title: 'Matrix layout and multiplication order', html:
          `<p><code>Matrix2</code>, <code>Matrix3</code>, and <code>Matrix4</code> are immutable row-major records. They act on column vectors. In <code>a.mul(b)</code>, <code>b</code> acts first and <code>a</code> acts second.</p>` + table(['Operation', 'Meaning'], [
            ['<code>ofRows(...)</code>', 'Arguments follow successive matrix rows.'],
            ['<code>fromColumns(...)</code>', 'Arguments define columns. <code>Matrix3</code> accepts three <code>Vector3</code> basis columns.'],
            ['<code>Matrix3.mul(Vector3)</code>', 'Returns a new <code>Vector3</code>.'],
            ['<code>Matrix4.mul(x, y, z, w)</code>', 'Returns a new owned four-element <code>double[]</code>. No perspective division is performed.'],
            ['Affine point: <code>w = 1</code>', 'Translation affects the result.'],
            ['Affine direction: <code>w = 0</code>', 'Translation contributes zero; rotation, scale, and shear still apply.']
          ]) + `<p>The last column of an affine <code>Matrix4</code> stores translation. Its last row is <code>[0, 0, 0, 1]</code>. Use translation values in the same unit as positions. Scale factors and unit quaternion components are dimensionless.</p>` },
        { id: 'compose-a-transform', title: 'Rotate a local point, then translate it', html:
          `<p>Place a local point at <code>(2, 1, 1)</code> blocks. Rotate its frame 90 degrees about +Y, then move the frame origin two blocks along world X. The expected world position is <code>(3, 1, -2)</code>.</p>` +
          code('java', 'TransformExample.java', `import nsk.nu.ashcore.api.math.Matrix4;
import nsk.nu.ashcore.api.math.Quaternion;
import nsk.nu.ashcore.api.math.Vector3;

public class TransformExample {
    public static void main(String[] args) {
        Quaternion rotation = Quaternion.fromAxisAngle(
                new Vector3(0, 1, 0), Math.toRadians(90));
        Matrix4 translation = Matrix4.ofRows(
                1, 0, 0, 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        Matrix4 localToWorld = translation.mul(rotation.toMatrix4());

        double[] point = localToWorld.mul(2, 1, 1, 1);
        double[] direction = localToWorld.mul(0, 0, 1, 0);
        check(new Vector3(point[0], point[1], point[2]), new Vector3(3, 1, -2));
        check(new Vector3(direction[0], direction[1], direction[2]), new Vector3(1, 0, 0));

        double[] local = localToWorld.inverseAffine().mul(point[0], point[1], point[2], point[3]);
        check(new Vector3(local[0], local[1], local[2]), new Vector3(2, 1, 1));
        System.out.println(new Vector3(point[0], point[1], point[2]));
    }

    private static void check(Vector3 actual, Vector3 expected) {
        if (actual.distance(expected) > 1e-12) throw new AssertionError(actual);
    }
}`) + `<p><code>inverseAffine()</code> maps the world result back to the original local point. The direction becomes +X and receives no two-block translation.</p>` },
        { id: 'coordinate-illustration', title: 'Explore the same transform', html:
          `<p>This browser illustration shows a local point at <code>(2, 1, 1)</code>. It uses +Y upward, right-handed Y rotation, and one coordinate unit per block. Change <strong>Y rotation</strong> in degrees or <strong>Origin X offset</strong> in blocks, then compare the world and local readouts.</p><p>Set rotation to 90 degrees and origin X offset to 2 blocks to reproduce <code>(3, 1, -2)</code>. The fixed local readout remains <code>(2, 1, 1)</code>. This is a JavaScript illustration of the matrix example; it does not run the Java library or represent a coordinate-space service.</p><div data-diagram="coordinates"></div>` },
        { id: 'quaternion-contracts', title: 'Quaternion contracts', html:
          `<p><code>Quaternion</code> stores components in <code>(w, x, y, z)</code> order. <code>fromAxisAngle(axis, angle)</code> normalizes a finite non-zero axis and takes a finite angle in radians. Positive angles follow the right-hand rule. Invalid inputs throw <code>IllegalArgumentException</code>.</p>` + table(['Operation', 'Input and result'], [
            ['<code>rotate(v)</code>', 'Caller supplies a unit quaternion. The method does not normalize it first.'],
            ['<code>normalized()</code>', 'Finite non-zero components become unit length. Zero returns identity for compatibility. Non-finite components are rejected.'],
            ['<code>conjugate()</code>', 'Returns <code>(w, -x, -y, -z)</code>. This is the inverse rotation for a unit quaternion.'],
            ['<code>inverse()</code>', 'Algebraic inverse; preserves the effect of the original magnitude. Zero or an unrepresentable inverse throws <code>ArithmeticException</code>.'],
            ['<code>a.mul(b)</code>', 'Hamilton product. For rotations, apply <code>b</code> first, then <code>a</code>.'],
            ['<code>slerp(a, b, t)</code>', 'Shortest-arc interpolation. Caller supplies finite unit quaternions and <code>t</code> in <code>[0, 1]</code>; inputs are not validated or clamped.'],
            ['<code>toMatrix3()</code>, <code>toMatrix4()</code>', 'Normalize the quaternion first. Zero gives identity. The 4×4 result has zero translation.']
          ]) + `<p>For repeated rotation composition, normalize the product when you need a unit rotation. Do not treat the zero fallback in <code>normalized()</code> as evidence that a zero quaternion represents a valid supplied orientation: <code>OrientedBox</code> rejects it.</p>` },
        { id: 'matrix-to-quaternion', title: 'Convert a rotation matrix', html:
          `<p><code>Quaternion.fromMatrix3</code> accepts a proper rotation matrix. Column squared lengths must be within <code>1e-9</code> of one, pairwise column dot products within <code>1e-9</code> of zero, and determinant within <code>1e-9</code> of +1. These are absolute, dimensionless checks.</p><p>Scale, shear, reflection, or non-finite entries outside these checks cause <code>IllegalArgumentException</code>. Accepted rounding differences are normalized; this method does not find the nearest rotation for arbitrary matrices.</p><p><code>fromMatrix4</code> reads the upper-left 3×3 rotation and ignores finite translation. It requires an affine last row within <code>1e-9</code> of <code>[0, 0, 0, 1]</code>. A matrix round trip may change a quaternion's sign: <code>q</code> and <code>-q</code> rotate vectors identically.</p>` },
        { id: 'inversion-limits', title: 'Inversion limits', html:
          `<p><code>Matrix4.inverse()</code> rejects non-finite entries with <code>IllegalArgumentException</code>. A non-finite determinant or <code>|determinant| &lt; 1e-12</code> causes <code>ArithmeticException</code>. <code>inverseAffine()</code> applies that determinant check to the 3×3 linear block and requires the last row within <code>1e-12</code> of the affine row; accepted last-row deviations are discarded.</p><p><code>Matrix2.inverse()</code> and <code>Matrix3.inverse()</code> also use the absolute <code>1e-12</code> singularity guard, but lack <code>Matrix4</code>'s explicit finite-entry validation. Supply finite entries and representable intermediate arithmetic.</p><p>An absolute determinant cutoff depends on matrix scale. Passing the guard does not guarantee an accurate inverse for an ill-conditioned matrix. Verify round trips using a tolerance appropriate to your scene's units and scale.</p>` }
      ]
    },
    {
      id: 'geometry', category: 'Math & geometry', title: 'Geometry primitives',
      description: 'Define boxes, spheres, capsules, planes, and segments before choosing a spatial query.',
      kind: 'reference', readingTime: 7,
      intro: `<p>Geometry primitives describe shapes in a shared coordinate system. A closed shape includes its boundary. A supplied bounding box describes its own volume; overlap with that box does not establish overlap with a more detailed enclosed object.</p><p>All positions, radii, and half extents in one query must share a unit. The examples use blocks and assign +Y as the vertical axis.</p>`,
      sections: [
        { id: 'shape-reference', title: 'Choose a primitive', html:
          table(['Type', 'Definition', 'Boundary and degenerate cases'], [
            ['<code>AxisAlignedBox(min, max)</code>', '3D box with sides parallel to the coordinate axes.', 'Inclusive corners. Equal bounds on an axis are allowed; all equal bounds form a point.'],
            ['<code>AxisAlignedRect(min, max)</code>', '2D rectangle with axis-aligned sides.', 'Inclusive bounds and touching overlaps. Zero width or height is allowed.'],
            ['<code>OrientedBox(center, halfExtents, orientation)</code>', 'Local box rotated into the query coordinate system.', 'Each local component lies within <code>[-halfExtent, +halfExtent]</code>. Zero extents are allowed.'],
            ['<code>Sphere(center, radius)</code>', 'Points at most <code>radius</code> from the center.', 'Zero radius is a point.'],
            ['<code>Capsule(a, b, radius)</code>', 'Points at most <code>radius</code> from segment AB, including spherical ends.', 'Equal endpoints form a sphere. Zero radius forms a segment.'],
            ['<code>Plane(normal, d)</code>', 'Infinite plane <code>normal · point + d = 0</code>.', 'Constructor normalizes both normal and offset.'],
            ['<code>Segment3(a, b)</code>', 'Closed finite segment between two positions.', 'Equal endpoints form a point. <code>at(t)</code> does not clamp its parameter.'],
            ['<code>Ray(origin, direction)</code>', 'Forward half-line with a normalized direction.', 'Zero direction is rejected. See <a href="#/raycasting">Ray and segment queries</a>.']
          ]) },
        { id: 'construct-shapes', title: 'Construct shapes in block units', html:
          `<p>A two-block-tall box can extend from the block corner <code>(0, 0, 0)</code> to <code>(1, 2, 1)</code>. That origin choice belongs to this example. Ashcore does not automatically shift coordinates to block centers.</p>` +
          code('java', 'GeometryExample.java', `import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Boxes;
import nsk.nu.ashcore.api.geometry.Capsule;
import nsk.nu.ashcore.api.geometry.OrientedBox;
import nsk.nu.ashcore.api.geometry.Plane;
import nsk.nu.ashcore.api.math.Quaternion;
import nsk.nu.ashcore.api.math.Vector3;

public class GeometryExample {
    public static void main(String[] args) {
        AxisAlignedBox box = new AxisAlignedBox(Vector3.ZERO, new Vector3(1, 2, 1));
        AxisAlignedBox expanded = Boxes.expand(box, 0.25);
        OrientedBox rotated = new OrientedBox(
                new Vector3(0.5, 1, 0.5), new Vector3(0.5, 1, 0.5),
                Quaternion.fromAxisAngle(new Vector3(0, 1, 0), Math.toRadians(45)));
        Capsule capsule = new Capsule(new Vector3(0, 0, 0), new Vector3(0, 2, 0), 1);
        Plane ground = new Plane(new Vector3(0, 2, 0), -2);

        if (!box.contains(new Vector3(1, 2, 1))) throw new AssertionError("Closed boundary");
        if (!expanded.min().equals(new Vector3(-0.25, -0.25, -0.25))) {
            throw new AssertionError("Expansion");
        }
        if (capsule.distanceSqTo(new Vector3(1.5, 1, 0)) != 0.25) {
            throw new AssertionError("Squared distance to the solid capsule");
        }
        if (!ground.project(new Vector3(2, 3, 4)).equals(new Vector3(2, 1, 4))) {
            throw new AssertionError("Plane projection");
        }
        System.out.println(rotated);
    }
}`) + `<p><code>rotated</code> has full local dimensions <code>(1, 2, 1)</code> blocks; constructor extents are half those values. The capsule query returns <code>0.25</code> blocks² because the point is half a block outside its surface. The plane simplifies to <code>y = 1</code>.</p>` },
        { id: 'construction-validation', title: 'Construction and validation', html:
          `<p><code>Sphere</code> and <code>Capsule</code> require finite positions and a finite radius ≥ 0. <code>OrientedBox</code> requires a finite center, finite non-negative half extents, and a finite non-zero quaternion. It normalizes the quaternion during construction. Invalid numeric input throws <code>IllegalArgumentException</code>; null vectors fail with <code>NullPointerException</code>.</p><p><code>AxisAlignedBox</code> rejects null corners and reversed bounds. It retains legacy acceptance of NaN/infinite corners. <code>contains</code> returns false for a non-finite box or point; collision queries require finite bounds.</p><p><code>AxisAlignedRect</code>, <code>Segment3</code>, and helpers such as <code>Boxes.overlaps</code> do not provide uniform finite-input checks. Validate these values at your application boundary. Do not use their comparison results to validate NaN.</p>` },
        { id: 'box-and-distance-helpers', title: 'Box and distance helpers', html:
          table(['Method', 'Result'], [
            ['<code>Boxes.union(a, b)</code>', 'Smallest axis-aligned box containing both input boxes.'],
            ['<code>Boxes.expand(box, margin)</code>', 'Subtracts the margin from min and adds it to max on all axes. The margin uses position units.'],
            ['<code>Boxes.overlaps(a, b)</code>', 'Inclusive overlap; touching at a face, edge, or corner counts.'],
            ['<code>AxisAlignedRect.of(a, b)</code>', 'Orders two corners before construction. Rectangle methods include <code>union</code>, <code>expand</code>, <code>clamp</code>, <code>width</code>, <code>height</code>, <code>area</code>, and <code>center</code>.'],
            ['<code>CollisionUtils.closestPointOnBox(point, box)</code>', 'Clamps the point into the solid box. A point inside remains unchanged.'],
            ['<code>CollisionUtils.distanceSqPointBox(point, box)</code>', 'Squared distance to the solid box. Returns zero inside or on its boundary.'],
            ['<code>Capsule.distanceSqTo(point)</code>', 'Squared distance to the solid capsule, not to its central segment. Returns zero inside or on it.']
          ]) + `<p>Use a finite non-negative margin when expanding. A negative margin shrinks a box or rectangle and can reverse bounds, causing construction to fail. Distance squares use squared position units and may overflow or underflow.</p><p>Capsule queries require finite coordinate differences and representable axis length and projections. Unsupported arithmetic causes <code>IllegalArgumentException</code>. Very small features relative to scene coordinates can still be lost through rounding.</p>` },
        { id: 'planes-and-vector-geometry', title: 'Planes and vector geometry', html:
          `<p><code>Plane</code> divides its normal and <code>d</code> by the original normal's length. After construction, <code>distanceTo(point)</code> is a signed distance in position units: positive values lie toward the normal. <code>project(point)</code> returns the point projected onto the plane. A zero or non-finite normal, non-finite offset, or unrepresentable normalized offset is rejected.</p><p><code>GeometryUtils.reflect(v, n)</code> expects a unit normal. <code>project(v, n)</code> and <code>reject(v, n)</code> decompose a vector along and perpendicular to a non-zero vector <code>n</code>. These helpers do not validate the normal or protect all intermediate arithmetic from overflow.</p><p><code>GeometryUtils.closestPointOnSegment(a, b, point)</code> clamps the segment fraction to its endpoints. Equal endpoints return <code>a</code>. By contrast, <code>Segment3.at(t)</code> evaluates the supporting line for any supplied <code>t</code>; only <code>[0, 1]</code> lies on the segment.</p>` },
        { id: 'orient-a-local-sample', title: 'Orient a local sample to a normal', html:
          `<p><code>OrthonormalBasis.fromNormal</code> builds three perpendicular unit vectors from a finite, non-zero normal. Local X follows its tangent, local Y follows its normal, and local Z follows its bitangent. <code>toWorld</code> combines those axes without adding a position.</p>` +
          code('java', 'BasisExample.java', `import nsk.nu.ashcore.api.geometry.OrthonormalBasis;
import nsk.nu.ashcore.api.math.Vector3;

public class BasisExample {
    public static void main(String[] args) {
        Vector3 surfaceNormal = new Vector3(1, 1, 0).normalized();
        OrthonormalBasis basis = OrthonormalBasis.fromNormal(surfaceNormal);
        Vector3 localUp = new Vector3(0, 1, 0);
        Vector3 worldDirection = basis.toWorld(localUp);
        if (worldDirection.distance(surfaceNormal) > 1e-12) {
            throw new AssertionError("Local Y should follow the normal");
        }
        System.out.println(worldDirection);
    }
}`) + `<p>For a local position, add the desired world origin after transforming the offset. For sampled directions, continue to <a href="#/random">Random and sampling</a>.</p>` }
      ]
    },
    {
      id: 'raycasting', category: 'Math & geometry', title: 'Ray and segment queries',
      description: 'Find primitive intersections and interpret distances, segment fractions, normals, and misses.',
      kind: 'guide', readingTime: 8,
      intro: `<p>A ray query checks a supplied geometric shape along a direction. It does not load chunks, inspect blocks, traverse a voxel grid, or select Minecraft entities. Your code supplies the origin, shape, and any maximum reach.</p>`,
      sections: [
        { id: 'parameter-units', title: 'Distance and segment fraction', html:
          `<p><code>Ray</code> normalizes a finite non-zero direction during construction. Its parameter <code>t</code> measures distance from the origin in the position unit: <code>origin + direction * t</code>. A direction of <code>(10, 0, 0)</code> becomes <code>(1, 0, 0)</code>; it does not set a ten-block reach.</p><p><code>Segment3</code> stores two endpoints. Its parameter is a dimensionless fraction: <code>a + (b - a) * t</code>. The endpoints are <code>t = 0</code> and <code>t = 1</code>. Both <code>Ray.at</code> and <code>Segment3.at</code> evaluate their formulas without clamping.</p>` + table(['Result', 'Parameter unit', 'Miss'], [
            ['Ray <code>...T</code> method', 'Distance from ray origin', '<code>Double.POSITIVE_INFINITY</code>'],
            ['Segment <code>...T</code> method', 'Fraction in <code>[0, 1]</code>', '<code>Double.POSITIVE_INFINITY</code>'],
            ['<code>Hit</code> from <code>rayVsBoxHit</code>', 'Ray distance plus point and normal', 'Infinite <code>t</code>, null point and normal'],
            ['<code>IntersectionInterval</code>', 'Query-specific entry and exit parameters', '<code>(+infinity, -infinity)</code>']
          ]) },
        { id: 'ray-box-example', title: 'Cast a ray with a maximum reach', html:
          `<p>This example casts from <code>(0, 1, 0)</code> toward +X. The box begins at X = 3 blocks, so the first hit is three blocks away. Check <code>hit()</code> before reading the point or normal, then apply your reach limit.</p>` +
          code('java', 'RaycastExample.java', `import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.collision.Hit;
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;

public class RaycastExample {
    public static void main(String[] args) {
        Ray ray = new Ray(new Vector3(0, 1, 0), new Vector3(10, 0, 0));
        AxisAlignedBox target = new AxisAlignedBox(
                new Vector3(3, 0, -1), new Vector3(4, 2, 1));
        double maxReachBlocks = 5;
        Hit hit = CollisionTests.rayVsBoxHit(ray, target);

        if (!hit.hit() || hit.t() > maxReachBlocks) {
            throw new AssertionError("Expected a hit within reach");
        }
        if (hit.t() != 3 || !hit.point().equals(new Vector3(3, 1, 0))) {
            throw new AssertionError("Unexpected hit position");
        }
        if (!hit.normal().equals(new Vector3(-1, 0, 0))) {
            throw new AssertionError("Expected the outward X-min normal");
        }
        System.out.println(hit);
    }
}`) + `<p>For several shapes, query each supplied candidate and keep the smallest finite distance within reach. Define your own tie policy for equal-distance candidates; Ashcore does not maintain a scene index.</p>` },
        { id: 'supported-ray-queries', title: 'Supported ray queries', html:
          table(['Method', 'Hit behavior'], [
            ['<code>CollisionTests.rayVsBoxT(ray, box)</code>', 'First contact with a closed axis-aligned box. Returns zero inside/on the box.'],
            ['<code>CollisionTests.rayVsBoxHit(ray, box)</code>', 'Distance, point, and outward face normal; inside-origin rule described below.'],
            ['<code>CollisionTests.rayVsSphereT(ray, sphere)</code>', 'First contact; zero inside/on the sphere. Tangency and zero-radius points count.'],
            ['<code>CollisionTests.rayVsOrientedBoxT(ray, box)</code>', 'First contact with the rotated box; zero inside/on it.'],
            ['<code>CollisionTests.rayVsOrientedBoxInterval(ray, box)</code>', 'Supporting-line entry/exit distances, provided the interval reaches the forward ray.'],
            ['<code>capsule.rayIntersectT(ray)</code>', 'First contact with the finite cylindrical body and spherical ends; zero inside/on the capsule.'],
            ['<code>CollisionUtils.rayVsPlaneT(ray, plane)</code>', 'Distance to the plane; infinity if behind or treated as parallel. Coplanar rays are treated as parallel.']
          ]) + `<p>Box slab tests treat only exactly zero direction components as parallel. The plane helper instead uses <code>|normal · direction| &lt; 1e-12</code>, a dimensionless angular cutoff. That cutoff can omit distant intersections at shallow angles.</p>` },
        { id: 'inside-or-boundary', title: 'Starting inside or on a boundary', html:
          `<p>First-contact methods for solid shapes return zero when the origin is already inside or on the shape. For a sphere, the result is zero even when the ray would later leave its surface. It is not the exit distance.</p><p><code>rayVsBoxHit</code> returns <code>t = 0</code> and the origin as its point for an inside start, but chooses the exit face's outward normal. That normal does not mean the returned origin is on that face. Equal face parameters choose X before Y before Z.</p><p>Ray/OBB intervals retain a negative entry for inside starts. For a box centered at zero with half extents <code>(1, 1, 1)</code>, a ray from the center toward +X gives interval <code>[-1, 1]</code> and first-contact distance zero. An interval entirely behind the origin is a miss.</p>` },
        { id: 'segment-interval-example', title: 'Read a segment interval', html:
          `<p>Segment intervals clip entry and exit to <code>[0, 1]</code>. This segment travels six blocks from X = −3 to X = 3 through a two-block-wide box. It enters at one third and leaves at two thirds.</p>` +
          code('java', 'IntervalExample.java', `import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.collision.IntersectionInterval;
import nsk.nu.ashcore.api.geometry.OrientedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.geometry.Segment3;
import nsk.nu.ashcore.api.math.Quaternion;
import nsk.nu.ashcore.api.math.Vector3;

public class IntervalExample {
    public static void main(String[] args) {
        OrientedBox box = new OrientedBox(Vector3.ZERO,
                new Vector3(1, 1, 1), Quaternion.identity());
        Segment3 segment = new Segment3(new Vector3(-3, 0, 0), new Vector3(3, 0, 0));
        IntersectionInterval interval = CollisionTests.segmentVsOrientedBoxInterval(segment, box);
        if (!interval.hit()) throw new AssertionError("Expected intersection");
        if (Math.abs(interval.tEnter() - 1.0 / 3) > 1e-12
                || Math.abs(interval.tExit() - 2.0 / 3) > 1e-12) {
            throw new AssertionError(interval);
        }
        if (segment.at(interval.tEnter()).distance(new Vector3(-1, 0, 0)) > 1e-12) {
            throw new AssertionError("Expected entry at X = -1");
        }
        Ray inside = new Ray(Vector3.ZERO, new Vector3(1, 0, 0));
        IntersectionInterval rayInterval = CollisionTests.rayVsOrientedBoxInterval(inside, box);
        if (rayInterval.tEnter() != -1 || rayInterval.tExit() != 1) {
            throw new AssertionError(rayInterval);
        }
        System.out.println(interval);
    }
}`) + `<p><code>segmentVsOrientedBoxT</code> returns the entry fraction alone. <code>segmentVsBoxT</code> provides the corresponding first fraction for an axis-aligned box. Both include endpoint contacts.</p><p>A zero-length segment inside/on an OBB returns interval <code>[0, 1]</code>, because every fraction evaluates to the same contained point. Its first-contact fraction is zero. An outside stationary point returns a miss.</p>` },
        { id: 'query-numeric-limits', title: 'Query numeric limits', html:
          `<p>Supply finite positions and shape bounds. Coordinate differences, rotated coordinates, and relevant slab ratios must remain representable. OBB interval queries reject non-finite slab ratios with <code>IllegalArgumentException</code>; AABB queries have fewer explicit overflow checks.</p><p>The solid-shape queries add no contact tolerance. Double rounding can change tangency decisions or lose tiny gaps at large relative scales. A result describes the supplied primitive, and none of these queries computes a physical response. For overlaps, penetration witnesses, and translating boxes, see <a href="#/collisions">Collisions and contacts</a>.</p>` }
      ]
    },
    {
      id: 'collisions', category: 'Math & geometry', title: 'Collisions and contacts',
      description: 'Test closed-shape overlap, read contact witnesses, and find when translating boxes first touch.',
      kind: 'reference', readingTime: 10,
      intro: `<p>Choose an overlap test when you need a boolean. Choose a contact query when you need a penetration depth and points on the two supplied surfaces. Choose <code>SweptAABB</code> when you need the first contact time during straight translation.</p><p>These operations use the shapes you provide. They do not move entities, resolve velocities, apply forces, or build a contact manifold.</p>`,
      sections: [
        { id: 'overlap-tests', title: 'Static overlap tests', html:
          table(['Shapes', 'Method'], [
            ['AABB / AABB', '<code>Boxes.overlaps(a, b)</code>'],
            ['Rectangle / rectangle', '<code>a.overlaps(b)</code> on <code>AxisAlignedRect</code>'],
            ['Sphere / sphere', '<code>CollisionTests.sphereVsSphere(a, b)</code>'],
            ['Sphere / AABB', '<code>CollisionTests.sphereVsBox(sphere, box)</code>'],
            ['Sphere / OBB', '<code>CollisionTests.sphereVsOrientedBox(sphere, box)</code>'],
            ['AABB / OBB', '<code>CollisionTests.boxVsOrientedBox(a, b)</code>'],
            ['OBB / OBB', '<code>CollisionTests.orientedBoxVsOrientedBox(a, b)</code>']
          ]) + `<p>These tests use inclusive boundaries: face, edge, corner, and tangent contact count as overlap. Zero radii and zero extents remain valid closed shapes. Use finite geometry even for helpers that do not enforce it.</p><p>The OBB/OBB test checks six face axes and nine edge cross-product axes for separation. It skips exactly zero cross products and tests nearly parallel axes without an angular cutoff. It adds no shape inflation or contact epsilon; rounding still limits near-contact decisions.</p><p>Overlapping bounding boxes can enclose separated objects. An OBB can also miss another OBB even when their axis-aligned bounds overlap. Query the shape that represents the precision your application needs.</p>` },
        { id: 'contact-fields', title: 'Contact fields and units', html:
          `<p>Contact queries are available for sphere/sphere, sphere/AABB, and AABB/sphere. Argument order defines shapes A and B. Check <code>contact.hit()</code> before reading its vectors.</p>` + table(['Field or method', 'Meaning'], [
            ['<code>depth()</code>', 'Finite penetration depth in position units; zero means touching. A miss uses negative infinity.'],
            ['<code>normal()</code>', 'Dimensionless unit vector. For external contact, points from A toward B. Containment uses the query-specific rules below.'],
            ['<code>pointA()</code>, <code>pointB()</code>', 'Surface witnesses on A and B in the common coordinate system; null on a miss.'],
            ['<code>hit()</code>', 'True for touching or penetration.'],
            ['<code>touching()</code>', 'True only when depth is exactly zero.']
          ]) + `<p>The witnesses satisfy <code>pointA - pointB = normal * depth</code> within rounding. In exact arithmetic, translating A by <code>-normal * depth</code> brings supported shapes to tangency. That geometric relationship is not an instruction for a full physics solver.</p>` },
        { id: 'contact-example', title: 'Read sphere contact witnesses', html:
          `<p>Two spheres with radius one block and centers 1.5 blocks apart overlap by half a block. Their witnesses are X = 1 on A and X = 0.5 on B.</p>` +
          code('java', 'ContactExample.java', `import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.collision.Contact;
import nsk.nu.ashcore.api.geometry.Sphere;
import nsk.nu.ashcore.api.math.Vector3;

public class ContactExample {
    public static void main(String[] args) {
        Sphere a = new Sphere(Vector3.ZERO, 1);
        Sphere b = new Sphere(new Vector3(1.5, 0, 0), 1);
        Contact contact = CollisionTests.sphereVsSphereContact(a, b);
        if (!contact.hit() || contact.touching() || Math.abs(contact.depth() - 0.5) > 1e-12) {
            throw new AssertionError(contact);
        }
        Vector3 difference = contact.pointA().sub(contact.pointB());
        if (difference.distance(contact.normal().mul(contact.depth())) > 1e-12) {
            throw new AssertionError("Witness relation");
        }

        Sphere shiftedA = new Sphere(
                a.center().sub(contact.normal().mul(contact.depth())), a.radius());
        if (!CollisionTests.sphereVsSphereContact(shiftedA, b).touching()) {
            throw new AssertionError("Expected tangency after the example shift");
        }
        System.out.println(contact);
    }
}`) + `<p>The example creates a new sphere to demonstrate the witness relationship. The original sphere records remain unchanged.</p>` },
        { id: 'explore-contact', title: 'Explore contact and tangency', html:
          '<p>The figure starts with the same two unit spheres as <code>ContactExample</code>: A is at the origin and B is at <code>(1.5, 0, 0)</code>. Move B to compare penetration, tangency, and a miss. Positions and depth are in blocks; the normal has no unit.</p><div data-diagram="contacts"></div><p>Choose <strong>Touching</strong> to put B at X = 2: the depth becomes zero and both witnesses meet at X = 1. Choose <strong>Separated</strong> to remove the contact. <strong>Same center</strong> shows the documented +X normal for coincident centers.</p><p><strong>Show shift of A</strong> draws a dashed copy at <code>A − normal × depth</code>, keeping the queried spheres unchanged. The XY slice passes through both centers at Z = 0. Dragging the 3D view changes only the camera.</p>' },
        { id: 'containment-and-ties', title: 'Containment and tie rules', html:
          `<p><code>sphereVsSphereContact(a, b)</code> uses <code>radiusA + radiusB - centerDistance</code> as depth, including full containment. Distinct centers define the normal from A's center to B's. Coincident centers choose global +X; swapping those coincident arguments therefore does not negate the normal.</p><p><code>sphereVsBoxContact(sphere, box)</code> uses the closest box point when the center is outside. The normal points toward that point and depth is radius minus distance. For a center inside/on the box, it chooses the nearest face; depth is radius plus face clearance, and the normal opposes that face's outward direction.</p><p>Equal face clearances choose X-min, X-max, Y-min, Y-max, Z-min, then Z-max. <code>boxVsSphereContact</code> reverses the sphere/box result's normal and witnesses, including face ties. Global tie choices need not rotate with a scene.</p><p>A zero-radius point strictly inside another shape can have positive contact depth. Depth measures the displacement needed to reach tangency for these ordered shapes, not the volume of an overlap.</p>` },
        { id: 'swept-box-example', title: 'Find first contact during translation', html:
          `<p><code>SweptAABB.test(moving, velocity, target, tMax)</code> tests translation <code>velocity * t</code> over the closed interval <code>[0, tMax]</code>. If velocity is in blocks per tick, <code>t</code> and <code>tMax</code> are in ticks. This example moves at two blocks per tick and reaches the target after half a tick.</p>` +
          code('java', 'SweepExample.java', `import nsk.nu.ashcore.api.collision.SweptAABB;
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;

public class SweepExample {
    public static void main(String[] args) {
        AxisAlignedBox moving = new AxisAlignedBox(
                new Vector3(-2, 0, 0), new Vector3(-1, 1, 1));
        AxisAlignedBox target = new AxisAlignedBox(Vector3.ZERO, new Vector3(1, 1, 1));
        Vector3 velocityBlocksPerTick = new Vector3(2, 0, 0);
        double maximumTicks = 0.5;
        SweptAABB.Result result = SweptAABB.test(moving, velocityBlocksPerTick, target, maximumTicks);

        if (!result.hit() || result.t() != 0.5) throw new AssertionError(result);
        if (!result.normal().equals(new Vector3(-1, 0, 0))) {
            throw new AssertionError("Expected X-min contact normal");
        }
        Vector3 displacementBlocks = velocityBlocksPerTick.mul(result.t());
        AxisAlignedBox atContact = new AxisAlignedBox(
                moving.min().add(displacementBlocks), moving.max().add(displacementBlocks));
        if (atContact.max().x() != target.min().x()) {
            throw new AssertionError("Boxes should touch at X = 0");
        }
        System.out.println(result);
    }
}`) + `<p>To test one planned displacement instead, pass that displacement as <code>velocity</code> and use <code>tMax = 1</code>. The returned <code>t</code> then represents a dimensionless displacement fraction.</p>` },
        { id: 'swept-box-contract', title: 'Swept box boundaries and results', html:
          `<p>The query includes contact exactly at <code>tMax</code>. Starting overlap returns time zero. A moving overlap uses an exit-face normal, while a resting overlap has a null normal. Equal face times choose X before Y before Z. On a miss, <code>hit()</code> is false, <code>t()</code> is positive infinity, and <code>normal()</code> is null.</p><p><code>tMax</code> must be finite and non-negative, including at rest. Velocity components and bound differences must be finite; invalid values throw <code>IllegalArgumentException</code>. Rotation, changing scale, acceleration, and physical response are outside this query's model.</p>` },
        { id: 'contact-precision', title: 'Precision and ownership', html:
          `<p>Contact queries require finite and representable coordinate differences, depth, and surface witnesses. Unsupported arithmetic throws <code>IllegalArgumentException</code>. <code>Contact</code> construction checks finite fields and a normal length within an absolute <code>1e-12</code> of one.</p><p>No contact epsilon is added by sphere, OBB, or swept-box queries. Very small gaps and tangencies remain sensitive to double rounding. Each primitive query uses constant time and additional memory, but result records, vectors, and some internal fixed-size arrays can allocate.</p><p>All query inputs remain unchanged. Retain ownership of scene data, candidate selection, and any movement decision in your application. See <a href="#/geometry">Geometry primitives</a> for construction and <a href="#/raycasting">Ray and segment queries</a> for distance-based intersections.</p>` }
      ]
    }
  );
})();
