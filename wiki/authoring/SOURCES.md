# Podstawa dokumentacji Ashcore

Dokumentowana wersja: **1.2.0**. Java: **21+**. Data opracowania: **2026-09-12**.
Lokalny tag `v1.2.0`: `42b7e6af922f48a8bf3b20fd11db3d1aee9f8129`.
Porównanie `src/main/java` i `pom.xml` z tym tagiem nie wykazało różnic.
Publiczne API GitHuba potwierdziło ten sam identyfikator tagu w repozytorium zdalnym.

## Zastosowanie szablonów

Zasady językowe pochodzą z `Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE.md`.
Implementacja, komponenty i wygląd pochodzą z katalogu
`Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE` i jego `WIKI_DESIGN_TEMPLATE.md`.
Katalog zawiera gotowy szablon mimo wcześniejszej informacji w dokumencie językowym,
że miejsce na szablon pozostaje puste. Zgodnie ze zleceniem wykorzystano istniejące pliki.

Proza publiczna jest angielska, zgodnie z instrukcją i interfejsem szablonu WIKI.
Materiał roboczy pozostaje polski. Nazwy API zachowują zapis źródłowy.
Termin „plugin” zastąpiono „library” w opisach Ashcore zgodnie z korektą użytkownika;
plugin występuje tylko jako przykład aplikacji korzystającej z biblioteki.

## Mapa źródeł

| Obszar | Podstawa | Sposób kontroli |
| --- | --- | --- |
| Wersja, Java, współrzędne Maven, zależności | `pom.xml`, `README.md` | Porównanie POM z treścią i lokalnym tagiem |
| Pierwszy przykład | `Ray`, `AxisAlignedBox`, `CollisionTests`, `DeterministicRandoms`, `RunningStats` | Kompilacja i wykonanie kodu z treści strony |
| Matematyka i geometria | `src/main/java/nsk/nu/ashcore/api/math`, `geometry`, `collision`, testy i `docs/GEOMETRY.md` | Sprawdzenie kontraktów, jednostek i przypadków brzegowych |
| Generowanie i pomiary | Pakiety `random`, `noise`, `stats` i ich testy | Sprawdzenie stanu, zakresów i walidacji |
| Narzędzia i SPI | Pakiety `hash`, `util`, `spi`, typy zakresów w `math` | Kontrola rzeczywistych identyfikatorów i wyjątków |
| Migracja | `docs/MIGRATION.md`, obecna implementacja | Rozdzielenie zmian zgodnych źródłowo i zmian zachowania |
| Indeks API | Publiczne typy w `api` i `implementation/random/SplitMix64Random` | Odnośniki do wersjonowanego źródła |
| Publikacja strony | Dokumentacja GitHub Pages | Oddzielny build i deploy; artefakt zawiera tylko pliki strony |

Szczegółowe notatki tematyczne, jeśli potrzebne, znajdują się obok tego pliku.
Nie są częścią publikowanego WIKI. Czytelnik otrzymuje istotne warunki w artykułach.

## Źródła integracji

- [PrismJS 1.30.0](https://github.com/PrismJS/prism/tree/v1.30.0): lokalny rdzeń i gramatyki C-like/Java, z dołączoną licencją MIT. Kolory są wspólne z paletą Java szablonu WIKI.
- [Maven Shade — przenoszenie klas](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html).
- [Gradle — toolchains](https://docs.gradle.org/current/userguide/toolchains.html).
- [Gradle — deklarowanie zależności](https://docs.gradle.org/current/userguide/declaring_dependencies_basics.html).
- [GitHub Pages — własny workflow](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).

Przykłady konfiguracji konsumenta są przykładami integracji, nie konfiguracją samego
Ashcore. Sprawdzenie przykładów Java nie oznacza próby uruchomienia pluginu na serwerze.

## Wizualizacje — 2026-09-13

Ponownie przeczytano `DOCUMENTATION_DESIGN_TEMPLATE.md` i
`DOCUMENTATION_DESIGN_TEMPLATE/WIKI_DESIGN_TEMPLATE.md`. Punktem odniesienia dla
interakcji i rozdzielenia kamery od obliczeń było lokalne Ashspace WIKI:
`assets/aabb-3d.js`, `assets/frame-chain-3d.js`, `assets/diagrams.js` i `src/diagrams.css`.
Nowe figury Ashcore używają własnego SVG, wspólnej palety, opisanych jednostek,
kontrolek klawiatury i osobnego resetu przykładu. Nie wymagają usług zewnętrznych.

| Figura | Rzeczywiste źródło obliczeń | Zakres i uproszczenie |
| --- | --- | --- |
| Kontakty | `CollisionTests.sphereVsSphereContact`, `Sphere`, `Vector3` | Dwie kule o promieniu 1; A w początku, B w płaszczyźnie Z = 0. Rzut ortograficzny 3D lub przekrój XY. Przesunięta kopia ilustruje styczność, bez symulacji fizyki. |
| Próbkowanie | `SplitMix64Random`, `DeterministicRandom.nextUnitDouble`, `LowDiscrepancy.halton`, `mapToConcentricDisk` | Po dwa losowania na punkt; bezpośredni Halton dla indeksów 1…N i baz 2/3. Do 256 punktów, kwadrat lub dysk; bez obietnicy minimalnego odstępu. |
| Szum | `PerlinNoise.sample(double,double)`, `FractalNoise.fbm`, przykład `TerrainNoise` | Ta sama funkcja 2D zasila mapę i powierzchnię 3D; obszar X = 96…160, Z = 32…96. Siatka 65 × 65, wysokość floor(64 + raw × 12), lacunarity 2. Kolory ograniczone do skali −2…2; wartości i wysokości zachowują surową sumę. |

`check-diagrams.mjs` porównuje modele JS z wynikami wywołań biblioteki w
`DiagramFixtures.java`. Test nie używa przepisanych wzorów po stronie odniesienia.
Zgodność dotyczy sprawdzonych, ograniczonych ustawień figur i wersji 1.2.0;
nie rozszerza gwarancji API o bitową zgodność szumu między platformami.
