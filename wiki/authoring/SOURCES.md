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

- [Maven Shade — przenoszenie klas](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html).
- [Gradle — toolchains](https://docs.gradle.org/current/userguide/toolchains.html).
- [Gradle — deklarowanie zależności](https://docs.gradle.org/current/userguide/declaring_dependencies_basics.html).
- [GitHub Pages — własny workflow](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).

Przykłady konfiguracji konsumenta są przykładami integracji, nie konfiguracją samego
Ashcore. Sprawdzenie przykładów Java nie oznacza próby uruchomienia pluginu na serwerze.
