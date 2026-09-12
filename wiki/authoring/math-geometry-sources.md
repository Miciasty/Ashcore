# Math and geometry evidence for Ashcore 1.2.0

This authoring record supports `wiki/content/math-geometry.js`. It is not a public article.

Version: `pom.xml` declares `dev.nasaka.blackframe:ashcore:1.2.0` and Java release 21. The project is a library, as corrected by the user. The public prose uses American English to match `Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE/WIKI_DESIGN_TEMPLATE.md`. The associated `DOCUMENTATION_DESIGN_TEMPLATE.md` supplies the wording and contract rules.

## Source map

Paths below are relative to the repository. Production paths use `src/main/java/nsk/nu/ashcore/api/`; test paths use `src/test/java/nsk/nu/ashcore/api/`.

| Page and claims | Production sources | Test evidence inspected |
| --- | --- | --- |
| `math`: immutable vectors, operations, units, normalization, zero/non-finite behavior, integer conversions and overflow | `math/Vector2.java`, `Vector3.java`, `Vector4.java`, `Vector2i.java`, `Vector3i.java`, `Vector4i.java` | `math/MathScalarApiTest.java`, `NumericBoundaryTest.java`; vector arithmetic read directly from methods |
| `math`: angle conventions, wrappers and interpolation | `math/Angles.java` | `math/MathScalarApiTest.java` |
| `math`: range inclusivity, floor division, clamping, compensation, tolerances | `math/IntRange.java`, `DoubleRange.java`, `DivMod.java`, `MathUtil.java`, `KahanSummation.java`, `NumericTolerance.java` | `math/MathScalarApiTest.java`, `NumericBoundaryTest.java` |
| `transforms`: rows, columns, composition, point/direction W, owned arrays, singularity and affine checks | `math/Matrix2.java`, `Matrix3.java`, `Matrix4.java` | `math/NumericBoundaryTest.java` |
| `transforms`: quaternion axis/angle convention, inverse, sign, conversion guards, slerp requirements | `math/Quaternion.java` | `math/QuaternionConversionTest.java` |
| `geometry`: closed bounds, half extents, radii, constructor validation, plane normalization, capsule distance | All classes in `geometry/` | `geometry/GeometryApiTest.java`, `collision/OrientedBoxQueriesTest.java`, `math/NumericBoundaryTest.java` |
| `geometry`: box helpers, reflection, projection, rejection, TBN basis | `geometry/Boxes.java`, `GeometryUtils.java`, `OrthonormalBasis.java`; `collision/CollisionUtils.java` | `geometry/GeometryApiTest.java` |
| `raycasting`: normalized-ray distances, hit fields, inside starts, sphere contact, OBB intervals, segment fractions | `geometry/Ray.java`, `Segment3.java`, `Capsule.java`; `collision/CollisionTests.java`, `Hit.java`, `IntersectionInterval.java` | `geometry/GeometryApiTest.java`; `collision/PrimitiveIntersectionTest.java`, `OrientedBoxQueriesTest.java`, `CollisionBoundaryTest.java` |
| `raycasting`: plane cutoff and non-finite limits | `collision/CollisionUtils.java`, `CollisionTests.java` | `collision/CollisionBoundaryTest.java`, `OrientedBoxQueriesTest.java`; direct implementation inspection |
| `collisions`: inclusive overlap, 15 SAT axes and degeneracy, primitive-vs-enclosed-shape scope | `geometry/Boxes.java`, `AxisAlignedRect.java`; `collision/CollisionTests.java` | `collision/PrimitiveIntersectionTest.java`, `OrientedBoxQueriesTest.java` |
| `collisions`: contact fields, witness equation, containment, ordered shapes, deterministic ties | `collision/Contact.java`, `CollisionTests.java` | `collision/ContactQueriesTest.java` |
| `collisions`: sweep units, closed time bounds, stationary/moving overlap normals, limits | `collision/SweptAABB.java` | `collision/CollisionBoundaryTest.java` |

## Discrepancies documented beside the affected API

- `Angles.wrapDegrees180(-720)` evaluates to `-360`; its Javadoc claims `(-180, 180]`. `deltaDegrees` inherits this for sufficiently negative differences. The article names the 1.2.0 limitation and demonstrates `wrapDegrees180(wrapDegrees360(angle))` for finite input. For degree deltas, normalize both endpoints before calculating the difference.
- `DoubleRange.contains(Double.NaN)` returns true. The constructor accepts NaN/infinite endpoints. The article requires caller finite-value checks instead of repeating its blanket normalized-range guarantee.
- `MathUtil.near` does not validate epsilon although its comment says it must be finite and non-negative. The article distinguishes it from validating `NumericTolerance.near`.
- `MathUtil.lerp` is not clamped even though its brief comment refers to `t` in `[0,1]`; the article explains the actual formula.
- `Matrix2` and `Matrix3` inverse methods lack the finite checks implemented by `Matrix4`; no universal validation claim is made.
- `AxisAlignedBox` deliberately retains legacy non-finite constructor acceptance, but finite collision queries are required. `Boxes` and `AxisAlignedRect` checks must not be used as NaN validators.

## Examples and illustration

Nine standalone Java classes include imports, `public static void main`, and explicit assertion failures independent of Java's `-ea` flag: `VectorExample`, `AngleExample`, `TransformExample`, `GeometryExample`, `BasisExample`, `RaycastExample`, `IntervalExample`, `ContactExample`, and `SweepExample`.

The coordinate diagram uses the supplied `assets/diagrams.js` implementation. Its formula matches positive right-handed Y rotation: `x' = cos(a)x + sin(a)z + originX`, `z' = -sin(a)x + cos(a)z`. At `(2,1,1)`, 90 degrees, and origin X offset 2, the result is `(3,1,-2)`. The article identifies the browser implementation as an illustration of the Java matrix example, uses its literal control names, and states units. It does not invent a coordinate-space service.

The template's raycasting illustration is omitted because its inside-sphere behavior differs from Ashcore's first-contact-zero convention.

`node --check wiki/content/math-geometry.js` passed during authoring. On 2026-09-12, `npm.cmd run check:examples` compiled the current Ashcore production source with Java 21.0.12.1 and executed all 26 assembled documentation examples successfully, including the nine listed above. The contact example compares computed depth with an absolute `1e-12` tolerance after observing the representable result `0.4999999999999999`. The integrating task owns site checks and browser QA. The inspected JUnit test code is source evidence; this record does not claim a separate execution of the full JUnit suite.

## Glossary

- **Position unit:** the caller's chosen length unit; examples use blocks.
- **Direction:** a vector describing orientation; a ray normalizes it to unit length.
- **Closed shape:** a shape including its boundary.
- **Half extent:** distance from an oriented box's center to either local face on an axis.
- **Ray distance:** distance from a normalized ray's origin, in position units.
- **Segment fraction:** dimensionless parameter in `[0,1]` for the closed segment.
- **Contact witness:** a returned point on one supplied shape's surface.
- **Contact depth:** geometric penetration in position units, including the query's containment cases.
- **Sweep time:** parameter in the supplied velocity's time unit; a fraction when velocity is instead a displacement and `tMax=1`.
