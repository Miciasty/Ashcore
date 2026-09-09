# Development verification and release procedure

This record covers the 1.0.2-SNAPSHOT corrections to Blackframe contract revision 2.0, dated 2026-09-09.
The starting snapshot is `7649935` on `fix/ashcore-contract-v2-20260909` (previous source baseline `4893f1d`).
The correction commit is the commit containing this record; there is no release tag or publication for this work.

## Verification evidence

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

## Migration and consumer checks

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
| Maven Central | Existing manual `central` profile with GPG signing and Central Publishing plugin | Retained; not executed. No claim of Central publication for 1.0.1 or 1.0.2-SNAPSHOT. |

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
