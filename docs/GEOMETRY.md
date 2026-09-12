# Geometry contracts

This guide describes oriented boxes and contact results in Ashcore 1.2.0.
See the [README](../README.md) for installation and examples.

## Oriented boxes

`OrientedBox(center, halfExtents, orientation)` stores immutable values. The quaternion maps box-local
coordinates to the query coordinate system and is normalized at construction. Zero quaternion, negative
extents and non-finite data are rejected; zero extents are valid closed lower-dimensional shapes.
Angles used to construct the quaternion are radians. Scale, shear, frame graphs and time are absent.

The selected pairs are ray/segment–OBB, sphere–OBB, AABB–OBB and OBB–OBB. They cover picking,
point/volume queries and distinguishing a rotated box from its enclosing AABB. Unsupported capsule,
mesh and OBB contact-manifold queries have no API; they are not represented as misses.

Ray and segment tests transform the query into box coordinates without renormalizing its direction,
then intersect three closed slabs. `rayVsOrientedBoxInterval` returns the supporting line's full
entry/exit interval when it intersects the forward ray. Thus an inside start retains negative entry;
an interval entirely behind the ray is a miss. Parameters are distances in the input ray's units.
`rayVsOrientedBoxT` instead returns max(0, entry), preserving the existing first-contact convention.
The segment interval is clipped to [0,1], with dimensionless parameters in the original segment;
a stationary point inside/on the box yields [0,1]. Both parameter helpers return +infinity on misses.
`IntersectionInterval` represents a miss by (+infinity,-infinity), with `hit()` false.

Sphere tests use the closest point in local box coordinates. Box pairs test six face axes and nine
edge cross-product axes; a separating projection proves a miss. Only exactly zero cross products are
skipped, so nearly parallel axes remain tested. This is the static separating-axis construction described
by [David Eberly, sections 2.1–2.2.1](https://www.geometrictools.com/Documentation/DynamicCollisionDetection.pdf).
The implementation uses direct projections with scaled distances/extents, rather than an inflated matrix.
All selected queries take O(1) time and additional memory; arrays, vectors and results can allocate.

Queries include touching in the mathematical model, but use rounded double arithmetic without an added
contact tolerance or an exact-predicate guarantee. Tiny gaps, exact tangency and tiny extents relative to
large offsets can be misclassified by rounding. Scaling helps avoid projection overflow, not loss of
relative precision. Local coordinate differences, rotated coordinates and all nonparallel slab ratios must
be finite and representable; violations throw IllegalArgumentException, never an unsupported-result miss.
AABB conversion uses finite representable widths. Query-specific restrictions are stricter than construction.
Repeatability covers identical ordered values, library version and environment; no cross-platform bitwise promise.

## Contact witnesses

`sphereVsSphereContact`, `sphereVsBoxContact` and `boxVsSphereContact` produce a `Contact`.
Its depth is -infinity for absence, zero for touching, or positive for penetration; absence has null vectors.
For present contacts, pointA/pointB are on the respective surfaces. The normal is a unit direction and
`pointA - pointB = normal * depth` within rounding. Positions and depth share shape units.
Positive depth also describes a point contained in a solid; it does not imply positive intersection volume.
The witnesses need not coincide during penetration and do not claim a complete or unique contact manifold.

For distinct sphere centers, the normal goes from A's center to B's. Depth is the sum of radii minus
center distance, including complete containment. Coincident centers choose global +X regardless of radius
or argument order. Swapping distinct centers swaps witnesses and negates the normal; coincident centers
are an explicit exception. Common rigid transformations preserve the relation for unique normals, within
rounding. Global tie choices intentionally do not commute with arbitrary rotations.

For a sphere center outside an AABB, the normal points toward the closest box point and depth is radius
minus that distance. For a center inside/on the box, choose the nearest face in the fixed tie order
X-min, X-max, Y-min, Y-max, Z-min, Z-max. The normal points opposite that face's outward direction;
pointA is on that side of the sphere and pointB on the chosen box face. Depth is radius plus face clearance.
This describes the geometric translation to tangency, including full containment, rather than just the
distance between nearest surfaces. `boxVsSphereContact` reverses witnesses and normal, including ties.
AABB transform comparisons apply only to rigid transforms preserving axis alignment; general rotations
require an OBB and there is no OBB contact API in this scope.

Contact tests add no contact epsilon. The 1e-12 normal-length acceptance check in the result constructor is
dimensionless and does not inflate shapes. Finite representable differences, depth and witness coordinates
are required; overflow throws IllegalArgumentException. Extreme inputs accepted by a boolean overlap query
can therefore be outside the contact-result range. Cost is O(1) time and memory, with immutable allocations.
`Hit`, boolean operations and `SweptAABB.Result` retain their previous meanings. Callers opt into the new
methods; no consumer migration is required for existing usage.

## Rotation and motion limits

Ashcore 1.2.0 has no continuous-rotation query. Static OBB queries describe one pose,
while `SweptAABB` handles translation without rotation. Testing the start and end poses
cannot establish separation throughout a rotation.

For example, a thin box with half extents `(2, 0.1, 0.1)`, centered at zero, rotates around Z
from 0 to pi. A sphere of radius `0.1` at `(0, 1.5, 0)` misses both endpoints but intersects
at pi/2. A full turn returns to the starting orientation while passing through the same contact.
Fixed sampling can also miss brief contact or an isolated tangency. Treat sampled hits as
observations, not proof that the full motion is collision-free.
