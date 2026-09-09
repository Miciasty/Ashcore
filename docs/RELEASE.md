# Development verification and release procedure

## 1.1.0-SNAPSHOT extensions — 2026-09-09

This development version implements CORE-008 through CORE-010 under Blackframe contract revision 2.0.
The branch is `feat/ashcore-primitives-math-random-20260909`, starting snapshot `2f5fbe5`, source baseline `7ffe561`.
The extension commit is the commit adding this section. No tag, remote CI run or publication was made for this work.

### API and migration

The version advances to `1.1.0-SNAPSHOT` for additive public API. Comparison of the old and new main JARs with
`javap -public` found **26 added public methods and no removed public signatures**; both contain the same 61 class entries.
No production class or external production dependency was added. Existing coding conventions and package names are retained.

| Area | Added API and behavior |
| --- | --- |
| Primitive queries | `CollisionTests.rayVsSphereT`, `sphereVsSphere`, `sphereVsBox`, `segmentVsBoxT`. Closed contact includes tangency and degenerate shapes. Ray results are distances; segment results are fractions in `[0,1]`. Starting inside gives zero; missed parameter queries give positive infinity. |
| Vector3 | `div`, `lengthSq`, `distance`, `distanceSq`, `lerp`, `min`, `max`, `withX`, `withY`, `withZ`. Interpolation is unclamped; division by zero throws. Ordinary arithmetic still requires representable intermediates. |
| Vector4 | `distance`, `distanceSq`. Vector2/4 normalization now uses scaling, preserves zero and rejects non-finite components, matching Vector3. Length/distance use `hypot`; squared results can overflow or underflow. |
| Quaternion | `conjugate`, `inverse`, `toMatrix3`, `toMatrix4`, `fromMatrix3`, `fromMatrix4`. The inverse preserves algebraic magnitude; zero or overflow throws. Matrix output normalizes first, retaining the zero-to-identity convention. |
| Bounded RNG | Default `nextInt(bound)`, `nextInt(origin,bound)`, `nextLong(bound)`, `nextLong(origin,bound)`. Half-open intervals support widths that overflow the signed type. Existing implementations need no additional method bodies. |

Matrix conversion accepts proper rotations within the documented absolute dimensionless `1e-9` checks on column
squared lengths, dot products and determinant. Matrix4 also checks the last row against `[0,0,0,1]` and ignores finite
translation. Scale, shear, reflection and perspective outside those limits are rejected; conversion does not decompose
general transforms or find a nearest rotation. Quaternion sign can change on a matrix round trip.

Sphere/Capsule constructors now reject non-finite positions and negative/non-finite radii. A zero radius is valid.
Capsule squared distance now uses `max(0,distanceToAxis-radius)^2`: the regression example returns `0.25`, formerly `1.25`.
Ray/capsule queries include the spherical ends for axial rays and reject hits behind a degenerate capsule.
The legacy AABB constructor contract from 1.0.2 remains. Geometry documents finite differences, representable axis lengths,
projections/slab ratios/hit times and rounding limits; the new queries do not add a contact tolerance.

Bounded draws reject incomplete buckets to avoid modulo bias under uniform source bits. Invalid bounds consume no state;
valid intervals consume at least one draw, including singleton intervals. Expected time is O(1), memory O(1), with no finite
worst-case draw count. The default reduction is stable from 1.1 through the rest of 1.x. Existing unbounded RNG streams are
unchanged. WeightedPicker now uses half-open intervals and never chooses a zero-weight entry, including at a rounding
boundary; fixed-seed weighted choices can change. It still requires a finite positive total weight and consumes one double draw.

### Verification evidence

Executed on Windows 11 amd64 with Oracle OpenJDK **25.0.2+10-69**, Maven **3.9.16**, compiler **release 21**:

```sh
mvn -B clean verify
mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:tree
```

Both commands passed. There were **144 unit tests and 3 packaged-artifact integration tests**, with no failures,
errors or skipped tests. IntelliJ's project build passed. JUnit 5.10.2 and its transitive dependencies are test-only.
Java 21 bytecode, main/sources/Javadoc JARs, SPI fixture loading and the extended README example were checked by
PackagedArtifactIT. The example reports sphere distance `1`, segment fraction `0.5` and bounded roll `6`.
No local execution on Java 21 or Linux is claimed; configured remote CI on Temurin 21/25 remains unexecuted.

The six initial regression tests failed against the old implementation: capsule distance, axial/behind-ray hits,
Vector2/4 extreme normalization and zero-weight selection. Seven new test classes now cover 31 additional test methods.
Primitive tests use known contacts/misses, tangency, zero-radius/zero-length shapes, reversed segments and scaled scenes;
capsule tests also check rotation/translation invariance. Quaternion tests exercise half-turn branches, composition,
inverse scales from `1e-300` to `1e300`, matrix round trips and invalid transforms. RNG tests exercise rejection/state
consumption, overflow-width intervals, fixed vectors and agreement with JDK RandomGenerator using identical input bits.
These checks support the stated contracts, not arbitrary-precision geometry or cross-platform floating-point identity.

### Consumer integration

Source/test copies were built in `.verification/consumers-extensions`, using the separate Maven repository
`.verification/extensions-repository`. Original consumer sources and POMs were unchanged. Only copied project/dependency
versions were adjusted, then `clean verify` and dependency trees were run in dependency order:

| Consumer source commit | Copied build version | Tests | Result |
| --- | --- | --- | --- |
| Ashgrid `04404cf` | `1.2.0-ashcore11check-SNAPSHOT` | 53 | passed |
| Ashspace `3f1b910` | `1.0.0-ashcore11check-SNAPSHOT` | 31 | passed |
| Ashtrace `5c516fc` | `1.0.0-ashcore11check-SNAPSHOT` | 43 | passed |
| Ashnav `08e7d26` | `1.0.0-ashcore11check-SNAPSHOT` | 29 | passed |

All **156 consumer tests** passed without source changes or exclusions. Each dependency tree resolved
`dev.nasaka.blackframe:ashcore:jar:1.1.0-SNAPSHOT:compile`, with the copied Ashgrid/Ashspace versions where used.
The exact tested main JAR and its isolated installed copy both had SHA-256
`9b7758dee82a8fa7338afcc7b7bf22fe02682aaff670c10dd4971be9390c845f`.
This identifies this integration run; archive timestamps may change the checksum on rebuild.

The command for each copied consumer was the following, using absolute paths in actual invocations:

```sh
mvn -B -s .verification/extensions-settings.xml -Dmaven.repo.local=.verification/extensions-repository \
  -f <copied-consumer>/pom.xml clean verify org.apache.maven.plugins:maven-dependency-plugin:3.8.1:tree
```

Exact JAR/POM pairs were installed there with `maven-install-plugin:3.1.3:install-file`. The normal Maven cache was used
as a read-only file-repository source, with Central as fallback. Execution script, logs and API listings are retained in
ignored `.verification/` (`verify-consumers-extensions.ps1`, `extensions-*.log`, `<consumer>-extensions-verify.log`).
Existing consumer tests passed; adoption of new helpers and the consumers' separate backlogs remain their own work.

## Historical 1.0.2-SNAPSHOT corrections

This record covers the 1.0.2-SNAPSHOT corrections to Blackframe contract revision 2.0, dated 2026-09-09.
The starting snapshot is `7649935` on `fix/ashcore-contract-v2-20260909` (previous source baseline `4893f1d`).
The correction commit is `7ffe561`; there was no release tag or publication for that work.

### Verification evidence

Executed on Windows 11 amd64 with Oracle OpenJDK **25.0.2+10-69** and Maven **3.9.16**.
Compilation uses `maven-compiler-plugin:3.13.0` with `--release 21`; packaged class version 65 is checked.
No local execution on a Java 21 runtime or Linux is claimed. CI is configured for Temurin 21 and 25 on Ubuntu,
on every pushed branch and every pull request; its remote runs have not been executed in this session.

With the selected JDK/Maven on PATH, run:

```sh
mvn -version
mvn -B clean verify
mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:tree
```

`clean verify` passed **113 unit tests and 3 packaged-artifact integration tests**, with no failures, errors or skipped tests.
IntelliJ's project build also passed. JUnit 5.10.2 and its transitive libraries occur only in test scope;
there are no external production dependencies.

`PackagedArtifactIT` opens the exact main, sources and Javadoc JAR paths from Maven's finalName. It checks
Java 21 bytecode, Maven coordinates, representative source/API documentation entries and absence of test/JUnit classes.
It compiles the README Java block against the main JAR alone and executes it through an isolated classloader.
The example returns ray distance 2.0. The locale affects printed decimal separators, not that distance.
It also packages SPI test fixtures and service descriptors into a separate JAR, then tests provider selection,
missing IDs and duplicate IDs with the production registry loaded from the main JAR. Ashcore itself has no built-in SPI registrations.

The existing 88 unit tests passed before the fixes. The first 13 new regression tests produced 10 failures,
including an infinite ray distance instead of `1e13`, a zero normalized direction and a P² descending-stream
median near `9999.998` instead of `5000.5`. The first stricter Javadoc build failed on six markup errors;
those errors were corrected, with doclint `all,-missing` and `failOnError=true` retained.
An initial offline build lacked Maven plugin dependencies; the online build resolved them and passed.

Fixed vectors cover SplitMix64 seed 0, its mixed draw conversions, FNV-1a-64, mix64 and seed derivation.
The SplitMix64 vectors also match Java's `SplittableRandom.nextLong` for the stated seed.
Math/StrictMath use was inspected: transcendental calls remain in angle, quaternion, distribution and sampling code.
They were not mechanically replaced; cross-platform floating-point identity is not promised.

### Migration and consumer checks

Public class names, method signatures and constructors remain available. The new development version is
`1.0.2-SNAPSHOT`; no artifact named `1.0.1` was replaced. These are corrections to the intended mathematical
behavior and explicit invalid-input contracts, with the following observable effects:

| Change | Consumer implication |
| --- | --- |
| Scaled Vector3, Quaternion and Plane normalization | Extreme finite directions now stay unit-length; ordinary results can differ in the last bits. Zero vector remains zero and zero quaternion remains identity. Zero axis and non-finite normalization inputs are rejected. |
| Ray validates finite origin/direction | Ashgrid, Ashspace and Ashtrace may rely on the constructor's direction guarantee; no duplicate normalization is needed. |
| AABB constructor retains legacy non-finite bounds | Adapters can still construct such a value and reject it in their own API. `contains` returns false for non-finite boxes/points, and ray collision rejects non-finite bounds. |
| Ray/box uses exact zero for parallel axes | Small non-zero components can now produce distant hits. The ray/plane helper retains its documented angular cutoff. |
| Sweep solves time intervals directly | Avoids overflow from velocity length and box-center expansion. t stays time; moving initial overlap still uses an exit-face normal. Finite non-negative tMax and representable bound differences are required. |
| P² descending marker correction | Quantile estimates change, especially on falling samples. The estimator remains approximate, with five-marker state and an int-sized sample limit. |
| Scaled weighted tables | Very large finite weights preserve their relative probabilities; draw outputs may differ from old tables. Tiny relative weights may still underflow. |
| Explicit bounds/validation | NumericTolerance rejects invalid eps; inverse operations reject non-finite matrices/determinants; Perlin/hash-grid reject out-of-domain coordinates; audited statistics reject non-finite samples and exhausted counters. |
| SPI contract | Select by ID. Enumeration and construction order remain unspecified; provider effects are eager and not rolled back. |

Source/test copies of the consumers were built inside `.verification/consumers-verified`, leaving their original
checkouts unchanged. Only copied POM project/dependency versions were changed. Maven used a separate
`.verification/repository`; it did not replace release artifacts in the user's normal local repository.

| Consumer source commit | Copied build version | Tests | Result |
| --- | --- | --- | --- |
| Ashgrid `04404cf` | `1.2.0-ashcorecheck-SNAPSHOT` | 53 | clean verify passed |
| Ashspace `3f1b910` | `1.0.0-ashcorecheck-SNAPSHOT` | 31 | clean verify passed |
| Ashtrace `5c516fc` | `1.0.0-ashcorecheck-SNAPSHOT` | 43 | clean verify passed |
| Ashnav `08e7d26` | `1.0.0-ashcorecheck-SNAPSHOT` | 29 | clean verify passed |

Every dependency tree resolved `dev.nasaka.blackframe:ashcore:1.0.2-SNAPSHOT:compile`.
Where used, Ashgrid and Ashspace resolved the copied versions above, including their transitive dependencies.
The tested Ashcore JAR had SHA-256
`3aa66c4a5846d6f558e716124415494d6a3862760c5df26a470eaa4fabfbfeb8`, identical to its installed copy in the isolated repository.
This identifies that integration run's artifact; ZIP timestamps can change the checksum on a subsequent rebuild.
Consumer tests required no source changes. The initial Ashgrid attempt exposed the AABB constructor compatibility
problem described above; after correcting Ashcore, all consumer tests passed without exclusions.

The consumer command, applied to each copied POM in dependency order, was:

```sh
mvn -B -s .verification/settings.xml -Dmaven.repo.local=.verification/repository \
  -f <copied-consumer>/pom.xml clean verify org.apache.maven.plugins:maven-dependency-plugin:3.8.1:tree
```

Actual invocations used absolute paths for the isolated repository and copied POMs. Dependencies were installed
there using `maven-install-plugin:3.1.3:install-file` with the exact JAR and corresponding POM.
An existing Maven cache was a read-only file-repository source for build/test dependencies, with Central as fallback.
Local logs, copied sources/POMs and the execution script remain in ignored `.verification/`.
This checks existing consumer tests, not all future extreme-coordinate usage or the separate consumer backlogs.

## Publication destinations

| Destination | Route | Evidence/status for this correction |
| --- | --- | --- |
| GitHub Packages | Release workflow, repository ID `github`, `mvn -B deploy` after clean verification | Configured; not executed. Package availability is unverified. |
| GitHub Release assets | Same workflow uploads the exact main, sources and Javadoc JAR names using `gh release upload` | Configured; not executed. No release was created. |
| Maven Central | Existing manual `central` profile with GPG signing and Central Publishing plugin | Retained; not executed. No claim of Central publication for 1.0.1, 1.0.2-SNAPSHOT or 1.1.0-SNAPSHOT. |

Verification is separate from deployment. The default `clean verify` does not sign, upload or publish.
The `central` profile is not activated by CI or the GitHub publishing workflow. It requires owner-managed
Central credentials under server ID `central` and GPG configuration; credentials do not belong in this repository.
The manual Central deployment command is `mvn -B -Pcentral clean deploy` after local verification and release review.
Its `autoPublish=true` setting publishes when validation succeeds, so it must not be used as a build test.

For an actual release, select a new non-SNAPSHOT version, confirm that version is unused at the target destinations,
run the required verification, and tag that exact commit as `v<version>`. The GitHub workflow checks tag/POM agreement
and rejects SNAPSHOT versions. It does not overwrite attached assets (`--clobber` is not used).
Before rerunning a partially successful publication, inspect which packages/assets already exist instead of replacing a release.
Record the release tag/commit, destination URLs, date and successful workflow or Central deployment evidence here.
Remote credentials and availability remain release-time checks; a local passing build does not prove publication.

The integration-test lifecycle follows the [Maven Failsafe contract](https://maven.apache.org/surefire/maven-failsafe-plugin/):
tests run after packaging and their outcome is checked in `verify`.
