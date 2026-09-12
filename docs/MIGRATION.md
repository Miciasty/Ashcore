# Upgrading to Ashcore 1.2.0

Version 1.2.0 retains the supported public types, methods and constructors from 1.0.x and 1.1.x.
It adds `OrientedBox`, `IntersectionInterval`, `Contact` and optional geometry queries.
Existing callers can continue using boolean queries, `Hit` and `SweptAABB.Result`.
See the [geometry contracts](GEOMETRY.md) for the new result types and their limits.

The bounded RNG methods added in 1.1 are default interface methods, so existing
`DeterministicRandom` implementations do not need new methods. Existing unbounded streams are unchanged.

Numeric corrections and input validation can change results when upgrading from earlier versions:

| Area | What callers need to account for |
| --- | --- |
| Normalization | Finite non-zero vectors, quaternions and plane normals use scaled normalization. Extreme values now normalize correctly; ordinary results can differ in the last bits. Zero vectors stay zero and zero quaternions normalize to identity. Zero rotation axes and non-finite normalization inputs are rejected. |
| Rays and boxes | Rays require finite origins and finite non-zero directions. Ray/box queries treat only exactly zero direction components as parallel, so shallow-angle rays can produce distant hits. Legacy non-finite AABBs remain constructible, but containment returns false and ray collision rejects them. |
| Swept boxes | Time intervals are computed directly. The returned parameter is time; moving initial overlap retains the exit-face normal. Use finite non-negative `tMax`, finite velocity and representable bound differences. |
| Spheres and capsules | Constructors reject non-finite data and negative radii. Capsule distance and intersection results include spherical ends and degenerate point/segment cases. |
| Weighted sampling | Large finite weights preserve their relative probabilities in the sampler tables. Zero-weight entries are never selected by `WeightedPicker`. Draws may differ from earlier implementations, and tiny relative weights can still underflow. |
| Statistics | P² quantile estimates change, particularly for descending samples. Estimates remain approximate. Statistics reject non-finite samples and exhausted counters where specified. |
| Numeric limits | Invalid tolerances, non-finite inverse inputs/determinants and out-of-domain noise coordinates are rejected. Scaling in normalization does not remove the documented arithmetic limits of other operations. |
| Service discovery | Choose providers by ID. Enumeration and construction order are unspecified; loading eagerly executes provider code. |

The documented SplitMix64, hash and seed streams are stable within 1.x. Other floating-point
and approximate results can change with documented fixes. Pin the library version when saved
terrain or procedural output depends on those results, and revalidate outputs before upgrading.

See the [README](../README.md) for installation, examples and the supported API.
