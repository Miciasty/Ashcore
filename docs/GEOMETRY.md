# Static geometry decisions — CORE-011 and CORE-012

Decision date: 2026-09-10. Blackframe contract revision 2.0; source snapshot `c577243`.
These additions belong to Ashcore and use only its values and the Java standard library.
The release version is 1.2.0. Existing public signatures and query behavior are retained.

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
methods; no consumer migration is required for existing usage. Adoption belongs to the consumer repository.

# Rotation assessment — CORE-013

Decision: defer a public continuous-rotation API. This closes the requested assessment, not implementation.
There is no continuous-rotation query in 1.2.0. Static OBB queries and `SweptAABB` remain independent.

The bounded candidate is one moving OBB against one stationary sphere. With time t in [0,T], a fixed unit
axis u, pivot p, initial center c0/orientation q0, constant translation velocity v and signed angular speed
omega, define c(t) = p + v*t + R(u,omega*t)*(c0-p) and q(t) = q(u,omega*t)*q0. The pivot's translation is
p+v*t; the axis direction stays fixed in the query coordinates. Time units are caller-selected, v uses
position/time and omega radians/time. Retain signed omega*T, including full or multiple turns; two endpoint
quaternions cannot replace it. Inputs, T >= 0 and all intermediate positions/angles must remain finite.
Argument reduction at huge angles and accumulated rounding need explicit supported limits before release.

An analytic counterexample needs no sampling assumption: a thin box with half extents (2,0.1,0.1), centered
at zero, rotates around Z from angle 0 to pi. A sphere of radius 0.1 at (0,1.5,0) misses both endpoints but
intersects at pi/2. At 2*pi, the end orientation is again the start despite the same intermediate contact;
at 4*pi it repeats. A zero-radius point at (0,sqrt(4.01),0) touches the swept corner at the angle rotating
(2,0.1,0) onto +Y. Such isolated tangency can escape a fixed sample schedule. Zero angular/linear motion
reduces to one static query; a stationary tangent is contact throughout the interval.

Three possible result contracts were compared:

| Approach | Result and evidence needed | Cost |
| --- | --- | --- |
| Certified root isolation for point-to-box distance minus sphere radius | First contact time bracket, with proof of no earlier root, including tangencies and changing closest features. No implementation or numerical proof yet. | Data-dependent; no latency bound established. |
| Conservative subdivision | Intervals in which contact remains possible, allowing false positives. A negative result needs certified distance lower bounds and outward rounding. Exhausted budget must return unresolved intervals. | O(N) static/bound evaluations for N visited intervals; depth-first working memory O(D) plus output K, where D is subdivision depth. |
| Fixed sampling | Observed static hits only. No observed hit cannot establish continuous separation. | O(S) static tests for S samples; O(1) working memory excluding output. |

A possible subdivision bound follows from motion: each material point has speed at most
L = |v| + |omega|*(|c0-p| + |halfExtents|). Over a time offset h, displacement is at most L*h by integrating
this speed bound. Distance from a fixed sphere center to the moving solid is therefore Lipschitz with the
same bound. A certified positive distance margin greater than L*h can exclude a whole interval around a
sample; this argument covers the arc, unlike the union of endpoint AABBs. However, the current double
distance computation supplies no outward-rounded error bound. It cannot by itself certify that margin.
L, its products and computed bounds also need overflow handling; a loose bound must not become a false miss.

Deferral is justified by the missing certified static-distance error bound, tangent/root handling and a
concrete consumer accuracy/budget requirement. Independent verification for a future proposal must include
analytic circle/corner cases above, interval-arithmetic or high-precision references, full/multiple turns,
zero motion, extreme scales and deliberate budget exhaustion. Dense samples may supplement these checks
but cannot be the only proof. Performance measurement must name N/S, tolerance, geometry and environment.
No forces, physical response, world state or upper-layer dependencies are required or introduced.
