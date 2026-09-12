# Reference-page evidence for Ashcore 1.2.0

This authoring record supports `wiki/content/reference.js`. It is not part of the public site.

The public prose uses American English, matching `Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE/WIKI_DESIGN_TEMPLATE.md`. The project is a **library**, following the user's correction. Plugin-specific commands, permissions, configuration, and server installation instructions were not inferred.

## Version and API directory

- `pom.xml`: `dev.nasaka.blackframe:ashcore:1.2.0`, JAR packaging, Java release 21, JUnit only in test scope.
- `README.md`, “Supported API and compatibility”: all public `nsk.nu.ashcore.api` types/members plus public `implementation.random.SplitMix64Random` class/constructor; no serialization/wire guarantee.
- Every `src/main/java/**/*.java` public type was enumerated. The directory also includes nested `SweptAABB.Result`.
- Local `git diff v1.2.0 -- src/main/java pom.xml` has no changes. Type links therefore use the release-tag source, including its Javadoc comments. They do not depend on an unverified hosted Javadoc endpoint.
- The current `docs/MIGRATION.md` contains content absent from the release tag. Migration facts are reproduced inline and verified against the current source; no tagged link to that document is emitted.

## Claim sources

| Page / claims | Working sources | Validation context |
| --- | --- | --- |
| API index: type purpose and package | Class declarations, Javadoc and public methods in all eight API packages; `implementation/random/SplitMix64Random.java` | Directory covers current public types; grouped by package with stable tutorial links. |
| Migration: additions and compatibility | `docs/MIGRATION.md`; `README.md`; `docs/GEOMETRY.md`; `OrientedBox`, `IntersectionInterval`, `Contact`, `Quaternion` | 1.2 additions require no replacement of existing result types. |
| Normalization and invalid inputs | `Vector2/3/4`, `Quaternion`, `Ray`, `Plane`, `OrientedBox`; `NormalizationTest`, `VectorNormalizationExtensionTest`, `QuaternionConversionTest` | Zero normalization differs from a valid ray/axis/orientation; inverse has no zero fallback. |
| Ray results and sweep units | `CollisionTests`, `CollisionUtils`, `Hit`, `IntersectionInterval`, `SweptAABB`; `CollisionApiTest`, `CollisionBoundaryTest`, `OrientedBoxQueriesTest` | Miss sentinels, inside starts, plane parallel threshold, time/distance/fraction distinction. |
| Random call order and repeatability | `DeterministicRandom`, `SeedSequence`, `PerlinNoise`, `SplitMix64Random`; `DeterminismTest`, `BoundedRandomTest`; README repeatability and compatibility tables | 255 Perlin construction draws; bounded rejection draw count; exact seed tags; distinct tagged streams do not imply statistical independence. |
| Weighted sampling | `WeightedPicker`, `WeightedSampler`; `WeightedPickerBoundaryTest`, `WeightedSamplerBoundaryTest` | Picker requires finite weight sum; sampler builds its own tables; zero-weight entries are not selected. |
| Noise domains | `PerlinNoise`, `HashGridNoise`, `FractalNoise`; `NoiseBoundaryTest` | Different upper-exclusive coordinate domains; unnormalized octave sums; every octave must be valid. |
| Statistics and state | `RunningStats`, `P2Quantile`, `ReservoirSampler`, `HaltonSequence`; `StatsApiTest`, `StatsBoundaryTest`, `CounterBoundaryTest` | No-data NaN, counter limits, shallow snapshots, reset does not reset RNG. |
| Classpath and packaging | `pom.xml`; `PackagedArtifactIT`; `README.md` | Java 21 bytecode, artifact metadata, isolated loader checks, no builtin SPI registration. Class-source diagnostics use standard Java reflection metadata. |
| SPI discovery | `Identified`, `ServiceRegistry`; `IdentifiedApiTest`, `ServiceRegistryApiTest`, `PackagedArtifactIT` | Eager provider side effects, ID validation/absence, explicit-loader overload, immutable membership but possibly mutable providers. |

## Confirmed discrepancies retained in documentation

- `Angles.wrapDegrees180(-720)` computes `-360`, contradicting its declared interval. `deltaDegrees` delegates to it. The troubleshooting workaround first reduces finite angles through `wrapDegrees360`.
- `DoubleRange.contains(Double.NaN)` returns true and the constructor accepts NaN endpoints. The directory and troubleshooting page require caller validation instead of promising finite-value validation.
- `MathUtil.near` does not validate its tolerance. `NumericTolerance.near` does.
- `Matrix2` and `Matrix3` inverse methods have a legacy absolute determinant guard, without the explicit non-finite checks implemented by `Matrix4`.
- `WindowedMean`, `SlidingWindowMinMax`, and `ExponentialMovingAverage` do not reject non-finite samples. EMA's comparison-only constructor also accepts NaN alpha. `SlidingWindowMinMax` increments an unguarded integer index and has no reset method. Troubleshooting requires caller validation and replacement before index overflow.
- `ServiceRegistry` comments/README claim invalid provider IDs throw `IllegalArgumentException`, but loading calls `Identified.requireValidId`, which throws `IllegalStateException`; the tests confirm that method's behavior. Invalid lookup arguments do throw `IllegalArgumentException`. Public troubleshooting distinguishes these cases.

## Terminology

- **Library**: the Java dependency that supplies Ashcore API types.
- **Hit**: an intersection with the supplied primitive, not necessarily with an enclosed object's detailed geometry.
- **Ray distance**: the parameter of a normalized `Ray`, in position units.
- **Segment fraction**: the dimensionless segment parameter in `[0,1]`.
- **Sweep time**: the parameter in the velocity's time unit; it becomes a motion fraction when the caller supplies displacement and `tMax=1`.
- **Contact depth**: geometric penetration in position units, not a force or a movement instruction.
- **Repeatable output**: output reproduced under the stated algorithm/version, initial state, inputs, ordered calls, and environment.
- **Snapshot**: a copy of stored references; mutable referenced objects are not copied.

## Author checks

- `node --check wiki/content/reference.js` passed.
- A rendered-content audit found 61 source files represented by 62 source links, including `SweptAABB.Result`. No source file was missing and no link named a nonexistent file.
- The three pages have unique section IDs. Every internal destination matches the agreed complete-site page inventory.
- The integrating task confirmed compilation and execution of all 26 assembled Java examples using JDK 21.0.12.1; this includes the three standalone diagnostic examples in these pages.
- Website rendering, remote-link checks, and complete-site validation belong to the integrating task. The user requires preview approval before pushing or publishing.
