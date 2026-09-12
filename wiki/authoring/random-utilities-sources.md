# Source map: sampling, noise, statistics, and utilities

Documentation target: **Ashcore 1.2.0**, from `pom.xml`. Java package names and contracts come from this checkout. These notes are authoring evidence; they are not required reading for WIKI users.

All paths below are repository-relative. Existing test source is evidence of intended assertions; reading a test does not mean it was executed. Root-task validation records cover execution of the website's Java examples and available project checks.

| Page / subject | Primary source | Existing coverage inspected |
| --- | --- | --- |
| `random`: SplitMix64 identity, default algorithm, stream stability and mutation | `src/main/java/nsk/nu/ashcore/api/random/DeterministicRandom.java`, `DeterministicRandoms.java`; `src/main/java/nsk/nu/ashcore/implementation/random/SplitMix64Random.java` | `src/test/java/nsk/nu/ashcore/api/random/RandomApiTest.java`, `BoundedRandomTest.java` |
| `random`: bound inclusion, rejected draws, invalid bounds consume no state, fixed example sequence | `src/main/java/nsk/nu/ashcore/api/random/DeterministicRandom.java` | `BoundedRandomTest`: fixed seed 0 yields 3, 850, -43 for the exact published call order; bounds, overflowing widths, JDK comparison and rejection cases |
| `random`: stateless tagged seeds | `src/main/java/nsk/nu/ashcore/api/random/SeedSequence.java`, `DeterministicRandoms.java`; `src/main/java/nsk/nu/ashcore/api/hash/Hash64.java` | `RandomApiTest`: repeated tags and argument validation |
| `random`: scan and alias selection, finite weights, input ownership, draw cost | `src/main/java/nsk/nu/ashcore/api/random/WeightedPicker.java`, `WeightedSampler.java` | `RandomApiTest`, `WeightedPickerBoundaryTest`, `WeightedSamplerBoundaryTest` |
| `random`: Halton initialization, bases, reset, cost and mappings | `src/main/java/nsk/nu/ashcore/api/random/HaltonSequence.java`, `Halton2DSequence.java`, `Halton3DSequence.java`, `LowDiscrepancy.java` | `RandomApiTest`: first base-2 values, reset, square/cube bounds, mapped domains |
| `random`: distributions and in-place shuffle | `src/main/java/nsk/nu/ashcore/api/random/Distributions.java`, `Permutation.java`; `src/main/java/nsk/nu/ashcore/api/math/NumericTolerance.java` | `RandomApiTest`: lambda zero guard, finite smoke cases, repeated shuffle and permutation content |
| `noise`: immutable Perlin table, 255 draws, domain, periodicity and 2D/3D distinction | `src/main/java/nsk/nu/ashcore/api/noise/PerlinNoise.java` | `NoiseApiTest`, `NoiseBoundaryTest` |
| `noise`: cell lookup vs interpolation and distinct coordinate limits | `src/main/java/nsk/nu/ashcore/api/noise/HashGridNoise.java` | `NoiseApiTest`: deterministic/bounded samples; `NoiseBoundaryTest`: integer lattice boundaries |
| `noise`: octave order, gain, lacunarity, unnormalized sums, FBM/turbulence/ridge formulas | `src/main/java/nsk/nu/ashcore/api/noise/FractalNoise.java`, `Noise2D.java`, `Noise3D.java` | `NoiseApiTest`: constant function sum 1.75 and ridge formula; `NoiseBoundaryTest`: callback count, zero octaves |
| `statistics`: Welford, sample vs population variance, empty values and finite guards | `src/main/java/nsk/nu/ashcore/api/stats/RunningStats.java` | `StatsApiTest`, `StatsBoundaryTest` |
| `statistics`: last-N window, initialization, EMA and half-life units | `src/main/java/nsk/nu/ashcore/api/stats/WindowedMean.java`, `SlidingWindowMinMax.java`, `ExponentialMovingAverage.java` | `StatsApiTest`: window saturation/eviction, extrema, first EMA sample and alpha 0.5 |
| `statistics`: P² bootstrap, approximation, counters and reset | `src/main/java/nsk/nu/ashcore/api/stats/P2Quantile.java` | `StatsApiTest`, `StatsBoundaryTest`: median range, descending input, constant samples, non-finite rejection |
| `statistics`: reservoir offers, RNG consumption, shallow snapshots, reset and limits | `src/main/java/nsk/nu/ashcore/api/stats/ReservoirSampler.java` | `StatsApiTest`: capacity, counters, immutability and reset |
| `utilities`: FNV-1a UTF-8 equivalence, stable 1.x output and noncryptographic scope | `src/main/java/nsk/nu/ashcore/api/hash/Hash64.java` | `HashApiTest` |
| `utilities`: biased coordinate bit interleaving and signed 21-bit bounds | `src/main/java/nsk/nu/ashcore/api/hash/Morton.java` | `HashApiTest`: 2D/3D round trips and rejection outside 3D bounds |
| `utilities`: inclusive endpoints, normalization, expansion, count vs width | `src/main/java/nsk/nu/ashcore/api/math/IntRange.java`, `DoubleRange.java`, `MathUtil.java` | Published example uses explicit assertions; this task did not inspect unrelated math test files |
| `utilities`: eager ServiceLoader loading, ID validation, immutable membership and mutable providers | `src/main/java/nsk/nu/ashcore/api/spi/Identified.java`, `ServiceRegistry.java`; inspected source/resource inventory | `ServiceRegistryApiTest` and test service descriptors under `src/test/resources/META-INF/services/` |

## Limitations and discrepancies preserved in public text

- `ServiceRegistry` class-level Javadoc says invalid provider IDs throw `IllegalArgumentException`. Its constructor actually calls `Identified.requireValidId`, which throws `IllegalStateException`. Lookup validation separately throws `IllegalArgumentException`. Public documentation follows executable behavior.
- `DoubleRange` does not reject NaN. `contains(Double.NaN)` returns true because both rejection comparisons evaluate false. This is a limitation, not an intended useful numeric contract. Public text instructs callers to validate finite values.
- `ExponentialMovingAverage` accepts NaN alpha; it and both window classes accept non-finite samples. Public documentation asks for finite alpha/samples instead of claiming universal validation.
- `SlidingWindowMinMax` increments an int position without a guard or reset. Recreating before `Integer.MAX_VALUE` additions avoids overflowing that position. Public text does not claim an unbounded stream lifetime.
- `P2Quantile` initially uses the sorted middle of the first five samples for every requested quantile. Public text identifies the bootstrap limitation instead of promising a calibrated 95th percentile after only five measurements.
- `Distributions` checks only `lambda <= 0`. NaN and infinity are not consistently rejected. Gaussian/exponential logarithm inputs are clamped to `NumericTolerance.EPS` (1e-12), which limits the tails. The Poisson implementation is documented for small finite lambda.
- `ReservoirSampler` uses scaled uniform doubles, not the bounded-long rejection sampler; stream inclusion probabilities are approximate at finite resolution. Reset clears reservoir data without resetting RNG state.
- No `src/main/resources` directory or production service descriptors are present. `SplitMix64Random` needs a seed constructor and is factory-created; its `Identified.id()` does not register it with Java ServiceLoader.

## Editorial decisions

- The WIKI describes a library, as corrected by the user. It adds no Minecraft command, permission, configuration key, server-version promise, or runtime plugin installation procedure.
- American English follows `WIKI_DESIGN_TEMPLATE.md`. Product examples such as terrain scaling and resource-choice labels are explicitly application examples, not new library defaults.
- Morton bit interleaving covers coordinate packing; no general bit-packing class was invented.
- Stable generator/hash contracts are distinguished from environment/version-scoped floating-point noise and statistics.
- Complete Java examples include imports, a public final class, `main`, expected behavior, and assertions. They need only Ashcore and the JDK. The terrain example intentionally reports a computed height without promising a cross-environment noise bit pattern.
