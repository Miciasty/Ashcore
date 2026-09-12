# Ashcore — ISSUES

**Aktualne koordynaty wydania (2026-09-10): 1.2.0**, bez SNAPSHOT. Zależności uzgodniono dla całego zestawu bibliotek; 545 testów na Java 21 przeszło bez błędów. [Wersje, sumy artefaktów i dowody](../Ashnav/VERIFICATION.md#release-version-alignment). Bez operacji Git i publikacji. Wcześniejsze wpisy poniżej zachowują historyczne wersje i wyniki.

## Cel pliku

Ten plik powstał 2026-09-09 po przeglądzie wspólnych zasad Blackframe i przyjęciu [blackframe.md, rewizja 2.0](../blackframe.md). Służy do zaplanowania korekt tej biblioteki oraz przekazywania pracy między kolejnymi, niezależnymi sesjami. Nie trzeba znać historii rozmowy: poniżej są powód zadania, miejsca w kodzie, kryteria odbioru i powiązania z innymi projektami.

To lista prac i miejsce zapisu dowodów, a nie dokumentacja gotowych funkcji ani informacja, że błędy już naprawiono. Nie wszystkie pozycje są błędami wykonania: część wymaga doprecyzowania umowy z użytkownikiem lub sprawdzenia istniejących zabezpieczeń. Przegląd obejmował README, POM, workflow i wybrane źródła/testy; nie jest pełnym audytem całego kodu. Podczas przygotowania pliku nie zmieniano implementacji i nie uruchamiano testów bibliotek.

## Punkt odniesienia

- Rola projektu: Podstawowe obliczenia, geometria, losowanie i statystyki. Decyzje tutaj wpływają na wszystkie wyższe biblioteki.
- Wersja zadeklarowana w lokalnym POM podczas przygotowania planu: **1.0.1**. To nie jest potwierdzenie publikacji; aktualna wersja robocza jest w stanie przekazania poniżej.
- Stan źródeł podczas przygotowania: **4893f1d** na gałęzi docs/blackframe-contract-v2-20260909; commit zapisuje stan sprzed zmian dokumentacji.
- Zależności: Brak produkcyjnych zależności od innych bibliotek. JUnit 5.10.2 występuje tylko w testach.
- Dokument nadrzędny: rewizja **2.0 z 2026-09-09**. Numery sekcji w zadaniach odnoszą się do tej rewizji.

## Jak rozpocząć nową sesję

1. Przeczytaj lokalne AGENTS.md/instrukcje użytkownika, [kontrakt Blackframe](../blackframe.md) i cały ten plik. Jeśli kontraktu brakuje w osobnym klonie, uzyskaj właściwą rewizję przed rozstrzyganiem wspólnych zasad.
2. Sprawdź aktualny Git i różnice względem powyższego punktu odniesienia. W tej pracy obowiązywała instrukcja użytkownika: przed zmianami utworzyć nową gałąź i zacommitować obecną wersję. Zachowaj cudze zmiany; nie resetuj repozytorium. Dla katalogu bez Git nie wymyślaj istniejącego commita.
3. Zacznij od wskazanego P1, odtwórz obserwację i sprawdź istniejące testy. Ustal kontrakt przed korektą zachowania. Wpis INSPEKCJA nie zastępuje reprodukcji.
4. Naprawiaj zadania w granicach tego projektu. Zmianę wspólnego kontraktu prowadź u właściciela niższej warstwy, a potrzebną pracę w innym repozytorium zapisz pod jego ID. Rutynowa poprawka nie wymaga edycji blackframe.md.
5. Po zmianach uruchom odpowiednie testy i końcowe clean verify. Aktualizuj statusy i dziennik poniżej: co zmieniono, rzeczywisty wynik kontroli, decyzje zgodności, pozostałe zależności i następny krok. Nie publikuj artefaktów tylko po to, aby sprawdzić kod.

Zalecana kolejność ustaleń wspólnych: Ashcore → Ashgrid → Ashspace, następnie Ashtrace i Ashnav zgodnie z ich zależnościami. Ashnav nie musi czekać na Ashtrace; niezależne zadania lokalne można podejmować wcześniej. Ashtemplate można poprawiać osobno. Ashmesh nie ma obecnie lokalnego katalogu, więc ten backlog nie zleca jego implementacji.

Maven używa zależności rozstrzygniętych z POM i repozytoriów artefaktów. Zmiana pliku w sąsiednim checkout nie podmienia ich automatycznie. Przy integracji zapisz konkretne wersje, commity i wynik rozstrzygnięcia zależności. Dla próbnego builda dolnej warstwy użyj odróżnialnej wersji roboczej lub izolowanego repozytorium testowego; nie nadpisuj istniejącego wydania inną zawartością.

## Oznaczenia

- **P1** — poprawność, publiczne gwarancje lub wymagana weryfikacja; rozstrzygnąć przed deklaracją zgodności z rewizją 2.0 i następnym wydaniem objętego zakresu.
- **P2** — porządkowanie lub pogłębiona kontrola po pilnych korektach; nie pomijać bez zapisanej decyzji.
- **INSPEKCJA** — potwierdzony zapis lub mechanizm w źródle; podany skutek może wymagać jeszcze testu wykonania.
- **AUDYT** — zakres do sprawdzenia, bez twierdzenia, że wszystkie wymienione miejsca są błędne.
- **DECYZJA** — trzeba wybrać i udokumentować wspierany kontrakt lub migrację.
- Statusy: **OTWARTE**, **W TOKU**, **ZABLOKOWANE** (z konkretną zależnością), **GOTOWE** (z dowodem spełnienia kryteriów), **NIE DOTYCZY** (z uzasadnieniem). Zachowuj identyfikatory po zamknięciu.

## Kolejka

| ID | Priorytet | Typ | Zadanie |
| --- | --- | --- | --- |
| [CORE-001](#core-001) | P1 | INSPEKCJA | Domknąć gwarancje normalizacji promieni i obrotów |
| [CORE-002](#core-002) | P1 | AUDYT | Określić zakres determinizmu RNG, szumu i obliczeń |
| [CORE-003](#core-003) | P1 | INSPEKCJA | Ustalić kolejność i efekty ładowania providerów SPI |
| [CORE-004](#core-004) | P1 | AUDYT | Opisać jednostki, tolerancje i założenia geometrii |
| [CORE-005](#core-005) | P2 | AUDYT | Zweryfikować deklaracje algorytmów i kosztów |
| [CORE-006](#core-006) | P1 | DECYZJA | Wyznaczyć wspierane API i zasady migracji |
| [CORE-007](#core-007) | P1 | INSPEKCJA | Dostosować CI, pakowanie i dowody wydania |
| [CORE-008](#core-008) | P1 | INSPEKCJA / DECYZJA | Uzupełnić przecięcia prymitywów i poprawić kapsułę |
| [CORE-009](#core-009) | P1 | INSPEKCJA / DECYZJA | Ujednolicić wektory i uzupełnić operacje kwaternionów |
| [CORE-010](#core-010) | P1 | INSPEKCJA / DECYZJA | Dodać losowanie całkowite w zakresie i poprawić zerowe wagi |
| [CORE-011](#core-011) | P2 | DECYZJA | Określić prymityw OBB i testy jego przecięć |
| [CORE-012](#core-012) | P2 | DECYZJA | Zdefiniować geometryczny wynik kontaktu prymitywów |
| [CORE-013](#core-013) | P2 | DECYZJA | Ocenić zapytania prymitywów podczas zadanego obrotu |

<a id="core-001"></a>

## CORE-001 — Domknąć gwarancje normalizacji promieni i obrotów

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** INSPEKCJA  
**Kontrakt:** sekcje 3.1, 4.2, 4.3, 4.5

**Gdzie:** [Ray.java](src/main/java/nsk/nu/ashcore/api/geometry/Ray.java), [Vector3.java](src/main/java/nsk/nu/ashcore/api/math/Vector3.java), [Quaternion.java](src/main/java/nsk/nu/ashcore/api/math/Quaternion.java), [GeometryApiTest.java](src/test/java/nsk/nu/ashcore/api/geometry/GeometryApiTest.java).

**Stan podczas przeglądu:** Ray deklaruje niezerowy, znormalizowany kierunek, ale sprawdza tylko direction.length() == 0, a potem normalizuje. Długość Vector3 jest liczona przez sumę kwadratów; skończony wektor (1e308, 0, 0) daje nieskończoną długość i po normalizacji może dać kierunek zerowy. Quaternion.normalized analogicznie sumuje kwadraty; dla zera zwraca obrót jednostkowy. To wnioski z kodu, bez uruchomionego testu.

**Znaczenie:** Kierunek promienia ma wskazywać stronę i mieć długość 1. Jeżeli staje się zerowy albo niepoprawny, błędy przechodzą do wykrywania trafień, siatki i transformacji.

**Praca do wykonania:** Odtwórz przypadki w testach. Określ obsługę NaN, nieskończoności, bardzo małych/dużych wartości, zerowej osi obrotu i kwaternionu zerowego. Popraw normalizację lub jawnie odrzucaj dane poza uzasadnionym zakresem. Nie zmieniaj po cichu dotychczasowej obsługi zera.

**Warunki zamknięcia:**

- [x] Dla (1e308,0,0), bardzo małego kierunku, zera i danych niefinitych konstruktor zwraca obiekt spełniający kontrakt albo udokumentowany błąd; nigdy zaakceptowany promień z zerowym kierunkiem.
- [x] Testy sprawdzają długość i kierunek oraz geometrię Ray.at; obejmują zero/duże składowe Quaternion.
- [x] Zapisano skutki zgodności dla Ashgrid, Ashspace i Ashtrace.

**Wynik korekty 2026-09-09:** Skalowana normalizacja Vector3, Quaternion i Plane; Ray odrzuca zerowy kierunek oraz niefinity origin/direction. Zero Vector3 pozostaje zerem, zero Quaternion daje identity; zerowa oś obrotu jest jawnie odrzucana. NormalizationTest obejmuje 1e308, Double.MAX_VALUE, 1e-300, Double.MIN_VALUE, kierunki diagonalne, Ray.at, obroty i offset płaszczyzny. Przypadki odtworzono przed poprawką. Testy konsumentów przeszły; zob. docs/RELEASE.md.

**Powiązania:** Przekaż wynik do [GRID-001](../Ashgrid/ISSUES.md#grid-001), [SPACE-002](../Ashspace/ISSUES.md#space-002) i [TRACE-002](../Ashtrace/ISSUES.md#trace-002); nie naprawiaj tego przez duplikowanie normalizacji w każdej bibliotece.

<a id="core-002"></a>

## CORE-002 — Określić zakres determinizmu RNG, szumu i obliczeń

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** AUDYT  
**Kontrakt:** sekcje 4.1, 5

**Gdzie:** [README.md](README.md), [DeterministicRandoms.java](src/main/java/nsk/nu/ashcore/api/random/DeterministicRandoms.java), [SeedSequence.java](src/main/java/nsk/nu/ashcore/api/random/SeedSequence.java), [PerlinNoise.java](src/main/java/nsk/nu/ashcore/api/noise/PerlinNoise.java), [RunningStats.java](src/main/java/nsk/nu/ashcore/api/stats/RunningStats.java).

**Stan podczas przeglądu:** README mówi o tych samych danych i ziarnie, a defaultGenerator wybiera SplitMix64 w bieżącym wydaniu. Nie wynika z tego automatycznie zgodność wszystkich wyników między wersjami/JVM ani niezależność statystyki strumieniowej od kolejności próbek.

**Znaczenie:** Ten sam seed nie jest obietnicą identycznego terenu po dowolnej aktualizacji, jeśli zmieni się generator, liczba losowań lub obliczenia.

**Praca do wykonania:** Spisz gwarancje per rodzina API: generator i sposób użycia stanu, kolejność próbek, wersje, callbacki i obsługiwane środowiska. Ustal znaczenie generatora domyślnego i wersjonowania strumienia. Przejrzyj użyte Math/StrictMath bez mechanicznej zamiany wszystkich wywołań.

**Warunki zamknięcia:**

- [x] Istnieje jawna, ograniczona do dowodów deklaracja powtarzalności i zasad zmian wyników pomiędzy wydaniami.
- [x] Dla obiecanego stabilnego RNG/hash są utrwalone wektory wyników z nazwanym algorytmem; inne obietnice mają adekwatne testy.
- [x] README wyjaśnia rolę seeda, stanu i kolejności bez obietnicy dokładności wynikającej z samego determinizmu.

**Wynik korekty 2026-09-09:** README i Javadoc rozdzielają stałe strumienie SplitMix64/FNV/mix64/SeedSequence w 1.x od powtarzalności obliczeń zmiennoprzecinkowych w jednej wersji i środowisku. DeterminismTest utrwala wektory wyników, porównuje SplitMix64 z JDK i sprawdza zużycie 255 losowań podczas konstrukcji Perlin. Dopisano kolejność próbek, callbacki, zakresy szumu, reset RNG oraz brak gwarancji niezależności seedów. Przejrzano Math/StrictMath bez mechanicznej zamiany.

**Powiązania:** Konsumenci muszą znać ewentualne zmiany wyników; szczególnie [SPACE-003](../Ashspace/ISSUES.md#space-003) i [TRACE-003](../Ashtrace/ISSUES.md#trace-003).

<a id="core-003"></a>

## CORE-003 — Ustalić kolejność i efekty ładowania providerów SPI

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** INSPEKCJA  
**Kontrakt:** sekcje 3.1, 4.1, 4.2, 4.5

**Gdzie:** [ServiceRegistry.java](src/main/java/nsk/nu/ashcore/api/spi/ServiceRegistry.java), [ServiceRegistryApiTest.java](src/test/java/nsk/nu/ashcore/api/spi/ServiceRegistryApiTest.java).

**Stan podczas przeglądu:** Rejestr ładuje providery eagerly przez ServiceLoader, wybiera je po ID, odrzuca duplikaty i przechowuje wynik w Map.copyOf. ids()/all() nie definiują kolejności. Samo LinkedHashMap użyte przed kopiowaniem nie stanowi publicznej gwarancji kolejności.

**Znaczenie:** Jeśli klient wybierze pierwszy provider albo kolejność inicjalizacji ma znaczenie, niejawne założenie może zmienić wynik.

**Praca do wykonania:** Zdecyduj, czy enumeracja ma stabilny porządek, czy klienci muszą wybierać wyłącznie po ID. Oddziel kolejność zwracanych kolekcji od kolejności uruchamiania konstruktorów providerów. Udokumentuj classloader i efekty inicjalizacji.

**Warunki zamknięcia:**

- [x] Javadoc opisuje wybór po ID, brak/duplikat ID, ładowanie i zakres obietnicy kolejności.
- [x] Jeśli porządek jest gwarantowany, testy sprawdzają go dla różnych układów providerów; jeśli nie, przykłady nie polegają na pierwszym elemencie.
- [x] Sprawdzono działanie z zasobami SPI zbudowanego JAR, nie tylko z katalogu klas testowych.

**Wynik korekty 2026-09-09:** Zachowano nieokreślony porządek ids()/all() i inicjalizacji providerów; wybór odbywa się po ID. Javadoc wyjaśnia classloader, eager loading, mutowalność providerów, duplikaty, brak i propagację błędów. PackagedArtifactIT ładuje rejestr z głównego JAR i providery z osobnego JAR z META-INF/services, sprawdzając wybór, brak i duplikaty. Ashcore nie rejestruje własnych providerów.

**Powiązania:** [GRID-005](../Ashgrid/ISSUES.md#grid-005) korzysta z ServiceRegistry; uzgodnij kontrakt przed dostosowaniem testów Ashgrid.

<a id="core-004"></a>

## CORE-004 — Opisać jednostki, tolerancje i założenia geometrii

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** AUDYT  
**Kontrakt:** sekcje 3.1, 4.2, 4.3

**Gdzie:** [NumericTolerance.java](src/main/java/nsk/nu/ashcore/api/math/NumericTolerance.java), [Matrix4.java](src/main/java/nsk/nu/ashcore/api/math/Matrix4.java), [Quaternion.java](src/main/java/nsk/nu/ashcore/api/math/Quaternion.java), [CollisionTests.java](src/main/java/nsk/nu/ashcore/api/collision/CollisionTests.java), [SweptAABB.java](src/main/java/nsk/nu/ashcore/api/collision/SweptAABB.java).

**Stan podczas przeglądu:** NumericTolerance zawiera stałe absolutne i helpers przyjmujące eps. Quaternion.fromAxisAngle opisuje radiany, rotate zakłada kwaternion jednostkowy. Potrzebna jest kontrola pozostałych kontraktów, a nie założenie, że cała dokumentacja jednostek jest nieobecna.

**Znaczenie:** Jedna liczba może oznaczać odległość, kąt albo część czasu ruchu. Mieszanie tych znaczeń daje pozornie wiarygodny wynik dla niewłaściwego pytania.

**Praca do wykonania:** Przejrzyj warunki wejścia, osobliwość macierzy, granice i stykanie brył, parametr promienia/sweep, normalizację i jednostki tolerancji. Sprawdź efekty ujemnego/eps równego NaN/nieskończoności. Wyjaśnij, że przecięcie nie oblicza sił ani odbicia.

**Warunki zamknięcia:**

- [x] Każdy badany kontrakt ma jednostki, znaczenie wyniku i przypadki szczególne; istnieją odnośniki do odpowiednich testów.
- [x] Tolerancje są uzasadnione dla konkretnych wielkości; nie wprowadzono jednej globalnej wartości dla każdej operacji.
- [x] Testy przypadków stycznych, równoległych, zerowych i skrajnych odpowiadają wybranemu modelowi.

**Wynik korekty 2026-09-09:** README/Javadoc opisują jednostki i granice geometrii, kolejność macierzy, jednostkowe kwaterniony, legacy progi wyznaczników, angular cutoff ray/plane i eps absolutne. CollisionBoundaryTest, NumericBoundaryTest, NormalizationTest oraz istniejące testy sprawdzają stykanie, równoległość, zero, limity i ekstremalne dane. Ray/box zachowuje małe niezerowe składowe; sweep liczy bezpośrednio przedziały czasu. Konstruktor AABB zachowuje niefinity bounds dla zgodności z walidującymi adapterami Ashgrid; collision je odrzuca.

**Powiązania:** Ustalenia o Ray i transformacjach przekaż do [SPACE-002](../Ashspace/ISSUES.md#space-002) oraz [TRACE-002](../Ashtrace/ISSUES.md#trace-002).

<a id="core-005"></a>

## CORE-005 — Zweryfikować deklaracje algorytmów i kosztów

**Status:** GOTOWE

**Priorytet:** P2  
**Dowód:** AUDYT  
**Kontrakt:** sekcje 4.2, 4.4, 4.5

**Gdzie:** [README.md](README.md), [HaltonSequence.java](src/main/java/nsk/nu/ashcore/api/random/HaltonSequence.java), [WeightedSampler.java](src/main/java/nsk/nu/ashcore/api/random/WeightedSampler.java), [P2Quantile.java](src/main/java/nsk/nu/ashcore/api/stats/P2Quantile.java), [RunningStats.java](src/main/java/nsk/nu/ashcore/api/stats/RunningStats.java).

**Stan podczas przeglądu:** README podaje m.in. zamortyzowane O(1) Haltona i O(1) aktualizacji statystyk. Każda taka obietnica wymaga kontekstu: rozmiaru stanu, zakresu licznika, sposobu inicjalizacji i modelu aproksymacji.

**Znaczenie:** Szacowana kwantyla nie jest dokładnym wynikiem sortowania całej historii, a Big-O nie podaje czasu w milisekundach.

**Praca do wykonania:** Zweryfikuj opis funkcji, założenia próbkowania/ważenia, puste dane, przepełnienie liczników i ograniczenia aproksymacji. Dopisz pamięć i praktyczne znaczenie kosztów. Nie zmieniaj algorytmu tylko dlatego, że jest przybliżony.

**Warunki zamknięcia:**

- [x] Tabela kosztów jest zgodna z kodem i wyjaśnia symbole, pamięć, inicjalizację oraz koszt amortyzowany.
- [x] Przybliżenia i przypadki bez danych są jawne; testy opierają się na odpowiednim wzorcu/warunkach, nie na kopii kodu.
- [x] Benchmarki, jeśli potrzebne do twierdzeń o szybkości, mają opis danych i środowiska.

**Wynik korekty 2026-09-09:** Poprawiono brak znaku ruchu w formule parabolicznej P² (mediana danych malejących przed poprawką ~9999.998, wzorzec 5000.5). Skalowanie wag chroni przed przepełnieniem sumy/iloczynu. Dodano kontrolę liczników i danych niefinitych w badanych statystykach, testy resetu, danych stałych i granic. Tabela kosztów definiuje czas/pamięć, callbacki i amortyzację; poprawiono ReservoirSampler.add na rzeczywiste offer, opisano przybliżenie prawdopodobieństw reservoir oraz brak ogólnego błędu kwantyli. Nie ma twierdzeń benchmarkowych.

**Powiązania:** Brak wymaganej zmiany innych bibliotek.

<a id="core-006"></a>

## CORE-006 — Wyznaczyć wspierane API i zasady migracji

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** DECYZJA  
**Kontrakt:** sekcje 5, 5.1, 8

**Gdzie:** [README.md](README.md), [DeterministicRandoms.java](src/main/java/nsk/nu/ashcore/api/random/DeterministicRandoms.java), [SplitMix64Random.java](src/main/java/nsk/nu/ashcore/implementation/random/SplitMix64Random.java).

**Stan podczas przeglądu:** Publiczne fabryki RNG już ukrywają implementację, ale samo rozdzielenie api/implementation nie definiuje całej zgodności. Inne repozytoria używają typów Ashcore w swoich publicznych sygnaturach.

**Znaczenie:** Zmiana konstruktora, obsługi zera lub kolejności wyników może wpłynąć na użytkownika nawet bez zmiany nazwy metody.

**Praca do wykonania:** Zdefiniuj wspierane typy i semantykę. Oceń zgodność poprawek [CORE-001](../Ashcore/ISSUES.md#core-001)–[CORE-005](../Ashcore/ISSUES.md#core-005), wersjonowanie i ewentualne deprecations. Uzupełnij README o proste przykłady i ograniczenia bez przenoszenia całego backlogu.

**Warunki zamknięcia:**

- [x] Lista/zasada wspieranego API obejmuje oficjalne przykłady; nie usunięto opublikowanych konstruktorów bez planu migracji.
- [x] Wybrano wersję odpowiednią do skutków, zachowując opublikowane artefakty bez nadpisania.
- [x] Quick start kompiluje się i pokazuje rzeczywiście wspierane zachowanie.

**Wynik korekty 2026-09-09:** Wsparcie obejmuje publiczne api oraz istniejący SplitMix64Random/constructor. Sygnatury zachowane; wersja robocza 1.0.2-SNAPSHOT, bez nadpisania 1.0.1. README kompiluje się z gotowym JAR podczas verify. Dokument migracji wyjaśnia zmiany wyników, odrzucanie błędnych danych i zachowanie zera. Izolowane clean verify: Ashgrid 53, Ashspace 31, Ashtrace 43, Ashnav 29 testów; dokładne wersje i SHA-256 JAR w docs/RELEASE.md.

**Powiązania:** Konsumenci: Ashgrid, Ashspace, Ashtrace i Ashnav; wymagane sprawdzenie integracji dla zmienionych kontraktów.

<a id="core-007"></a>

## CORE-007 — Dostosować CI, pakowanie i dowody wydania

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** INSPEKCJA  
**Kontrakt:** sekcje 2, 4.5, 6

**Gdzie:** [pom.xml](pom.xml), [.github/workflows/maven.yml](.github/workflows/maven.yml), [.github/workflows/publish.yml](.github/workflows/publish.yml), [README.md](README.md).

**Stan podczas przeglądu:** CI uruchamia mvn -B package, a kontrakt wymaga clean verify. POM ustawia source/target 21 bez jawnego przypięcia maven-compiler-plugin; Javadoc ma doclint=none i failOnError=false. Profil central istnieje, lecz pokazany workflow deploy nie aktywuje go i publikuje do GitHub Packages. Początkowa gałąź: master; CI filtruje master. Workflow wybiera pierwszy JAR bez wykluczenia sources/javadoc.

**Znaczenie:** Zielony wynik obecnego CI nie jest dowodem wykonania całej bramki jakości ani obecności artefaktu w Maven Central. Brak automatyzacji Central nie dowodzi braku publikacji ręcznej.

**Praca do wykonania:** Ustaw rzeczywistą bramkę clean verify, dobierz przypięty compiler plugin i release 21, sprawdź generowanie dokumentacji oraz jednoznaczną identyfikację artefaktów. Potwierdź utrzymywane gałęzie, docelowe wersje zależności i sposób publikacji do każdej używanej destynacji. JUnit pozostaw w test scope; nie usuwaj go w imię niezależności produkcyjnej.

**Warunki zamknięcia:**

- [x] Zapisano wynik mvn -B clean verify z wymaganymi testami oraz wersje JDK/Maven; CI obejmuje faktycznie utrzymywane gałęzie i PR-y.
- [x] Główny JAR, sources, Javadoc i wymagane zasoby są sprawdzone. Błędny Javadoc nie jest po cichu uznawany za poprawny; nie trzeba przy tym mechanicznie włączać każdej reguły stylistycznej doclint.
- [x] Wskazano używane cele publikacji, tag/wersję i dowody dostępności albo jawnie pozostawiono publikację jako niezweryfikowaną. Sam deploy nie służy jako test poprawek.
- [x] Sprawdzono efektywne zależności i ich scope; test integracyjny korzysta z zamierzonej wersji dolnej warstwy, a nie przypadkowej starej kopii z lokalnego Maven.

**Wynik korekty 2026-09-09:** mvn -B clean verify: PASS, 113 testów jednostkowych + 3 testy gotowych artefaktów, zero pominiętych; OpenJDK 25.0.2, Maven 3.9.16, release 21. Compiler/resources/clean/jar/install/deploy/surefire/failsafe/source/javadoc przypięte. Doclint all,-missing i failOnError=true; naprawiono 6 uprzednio ignorowanych błędów Javadoc. CI obejmuje wszystkie push branches i PR-y, Java 21/25; nazwy trzech JAR wynikają z finalName. JUnit wyłącznie test scope. Cele publikacji, gate tag/version i niezweryfikowany stan zdalny zapisano jawnie w docs/RELEASE.md; nie uruchomiono deploy.

**Powiązania:** Wspólny wzorzec: [TEMPLATE-001](../Ashtemplate/ISSUES.md#template-001) i [TEMPLATE-002](../Ashtemplate/ISSUES.md#template-002). Tę korektę można wykonać niezależnie od napraw algorytmów. Istniejącego numeru wydania nie nadpisuj innym artefaktem.

<a id="core-008"></a>

## CORE-008 — Przecięcia prymitywów i poprawność kapsuły

**Status:** GOTOWE

**Priorytet:** P1. **Dowód:** odtworzone błędy kapsuły w JAR 1.0.2-SNAPSHOT; nowe API zatwierdzone przez użytkownika.
**Kontrakt:** 3.1, 4.2–4.5, 5, 7. **Baseline:** 7ffe561; snapshot tej sesji 2f5fbe5.

**Gdzie:** CollisionTests, Sphere, Capsule, GeometryApiTest i nowe testy przecięć.
Kapsuła zwracała 1.25 zamiast kwadratu odległości 0.25 i pomijała trafienie promieniem wzdłuż osi.
Brakowało bezpośrednich testów promień–sfera, sfera–sfera, sfera–AABB i odcinek–AABB.
Dodajemy te operacje w Ashcore, bez zależności od wyższych warstw i bez fizycznej reakcji na kontakt.
Granice są domknięte, promień zwraca odległość, odcinek parametr w [0,1], start wewnątrz daje zero.

- [x] Testy trafień, styczności, braku trafienia, zerowych promieni sfer/odcinków, wnętrza i danych niepoprawnych.
- [x] Poprawna odległość kapsuły i zakończenia półkuliste, także dla promieni osiowych i zdegenerowanej kapsuły.
- [x] Dokumentacja zakresów liczbowych, zgodności i kosztów; testy konsumentów ze wskazanym nowym artefaktem.

**Wynik 2026-09-09:** Dodano cztery metody w CollisionTests. Sphere/Capsule odrzucają niefinityczne pozycje i ujemny/niefinityczny promień; zero pozostaje poprawne. Kapsuła liczy rzeczywisty kwadrat odległości od bryły, uwzględnia zakończenia dla promieni osiowych i nie zwraca trafienia za promieniem. PrimitiveIntersectionTest i CapsuleQueriesTest: 13 testów PASS, w tym skale, odwrócenie końców oraz obrót/przesunięcie sceny. README/Javadoc opisują domknięte granice, jednostki, zakresy liczbowe i O(1). Czterej konsumenci przeszli 156 testów z dokładnym JAR 1.1.0-SNAPSHOT; wersje i SHA-256 w [docs/RELEASE.md](docs/RELEASE.md).

**Powiązania:** SPACE-002, TRACE-002; nowe metody dostępne do późniejszej migracji konsumentów, bez edycji ich źródeł w tej sesji.

<a id="core-009"></a>

## CORE-009 — Spójność wektorów i operacje kwaternionów

**Status:** GOTOWE

**Priorytet:** P1. **Dowód:** Vector2/Vector4 z 1e308 normalizują się do zera; braki API potwierdzone inspekcją.
**Kontrakt:** 3.1, 4.2–4.5, 5, 7. **Baseline:** 7ffe561; snapshot 2f5fbe5.

**Gdzie:** Vector2/3/4, Quaternion, testy matematyki.
Dodajemy brakujące operacje odległości/interpolacji i operacje składowych Vector3, odporną normalizację Vector2/4,
sprzężenie i odwrotność kwaternionu oraz konwersje Matrix3/Matrix4 ↔ Quaternion.
Konwersje dotyczą obrotów; macierze ze skalą, ścinaniem lub odbiciem będą odrzucane. Zero normalized zachowuje obecną semantykę.

- [x] Zgodne zachowanie wektorów dla zera, skrajnych skończonych i niefinitych składowych.
- [x] Testy znanych obrotów, kompozycji, odwrotności i round-trip macierz–kwaternion, w tym 180°.
- [x] Jawne zasady odrzucania macierzy, tolerancje i zgodność źródłowa/binarnych sygnatur; minor 1.1.0-SNAPSHOT.

**Wynik 2026-09-09:** Vector3 otrzymał 10 metod, Vector4 dwie metody odległości, Quaternion sześć metod sprzężenia/odwrotności/konwersji. Normalizacja Vector2/4 używa skalowania; zero zachowano. Konwersje macierzy sprawdzają obrót właściwy z bezwymiarową tolerancją absolutną 1e-9; Matrix4 wymaga części afinicznej i pomija skończoną translację. Odwrotność kwaternionu zerowego rzuca wyjątek, normalizacja zera nadal daje identity. Trzy nowe klasy testów matematyki: 9 testów PASS. Porównanie javap obu JAR: łącznie dla rozszerzenia 26 dodanych metod publicznych, zero usuniętych sygnatur, identyczny zbiór 61 plików klas. Wersja 1.1.0-SNAPSHOT, skompilowany przykład README oraz buildy konsumentów PASS.

**Powiązania:** SPACE-002; konwersje i odwrotność są operacjami matematycznymi Ashcore, bez grafów ramek.

<a id="core-010"></a>

## CORE-010 — Losowanie całkowite w zakresie i zerowe wagi

**Status:** GOTOWE

**Priorytet:** P1. **Dowód:** WeightedPicker wybiera indeks 0 dla wag [0,1] przy losowaniu 0; brak metod zakresowych w interfejsie.
**Kontrakt:** 3.1, 4.1, 4.2, 4.4, 4.5, 5. **Baseline:** 7ffe561; snapshot 2f5fbe5.

**Gdzie:** DeterministicRandom, WeightedPicker i testy RNG.
Dodajemy domyślne nextInt(bound), nextInt(origin,bound), nextLong(bound), nextLong(origin,bound).
Przedziały są lewostronnie domknięte; redukcja przez odrzucanie eliminuje obciążenie modulo przy równomiernych bitach źródłowych.
Stan może zużyć więcej niż jedno losowanie. Dotychczasowe bezargumentowe strumienie pozostają bez zmian.

- [x] Testy odrzucania, zakresów o przepełniającej się szerokości i skrajnych granic signed, potęg dwójki, zakresów jednostkowych i niepoprawnych granic.
- [x] Powtarzalność, określone zużycie stanu i utrwalone wyniki; dokumentacja oczekiwanego kosztu i warunków zakończenia.
- [x] WeightedPicker nigdy nie wybiera zerowej wagi, w tym na granicach przedziałów i przy zaokrągleniu.

**Wynik 2026-09-09:** Cztery metody default stosują redukcję z odrzucaniem. Niepoprawny zakres nie zużywa stanu; poprawny zużywa co najmniej jedno losowanie, nawet gdy zawiera jedną liczbę. Oczekiwany koszt O(1), pamięć O(1); nie ma skończonego limitu prób dla patologicznego generatora. Nowe wyniki utrwalono od 1.1 na resztę 1.x, bez zmiany strumieni bezargumentowych. BoundedRandomTest porównuje wyniki z JDK RandomGenerator przy identycznych bitach i zawiera stałe wektory oraz testy zużycia stanu. WeightedPicker pomija zerowe wagi, używa granicy wyłącznej i ostatniej dodatniej wagi jako zabezpieczenia zaokrąglenia. Obie nowe klasy RNG: 9 testów PASS. Własne implementacje starego interfejsu nadal się kompilują; nie dodano abstrakcyjnych metod.

**Powiązania:** istniejące interfejsy RNG konsumentów muszą kompilować się bez nowych implementacji metod.

## Uwagi o zakresie kolizji — 2026-09-10

Historyczny stan przy dopisaniu backlogu: poniższe pozycje były propozycjami rozwoju w granicach rewizji 2.0, nie odtworzonymi błędami ani warunkami wydania ówczesnego API. Użytkownik zlecił wtedy uzupełnienie backlogu; nie implementację nowych funkcji. Punkt odniesienia: lokalne źródła odczytane 2026-09-10. W tamtej sesji, zgodnie z jej instrukcją, nie wykonywano operacji Git ani nowego checkpointu. Późniejsze wykonanie korekty opisano w wynikach i dzienniku poniżej.

Ashcore może obliczać przecięcia i parametry geometryczne dla podanych kształtów oraz jawnego modelu ruchu. Siły, masa, impulsy, tarcie, wyporność, zmiana stanu sceny w reakcji na kontakt, podparcie postaci i adapter Minecraft pozostają poza zakresem. Nowe typy nie mogą zależeć od Ashspace ani Ashtrace. Odrzucenie propozycji wymaga zapisanej decyzji i statusu NIE DOTYCZY; samo dopisanie planu nie oznacza GOTOWE.

<a id="core-011"></a>

## CORE-011 — Określić prymityw OBB i testy jego przecięć

**Status:** GOTOWE

**Priorytet:** P2  
**Dowód:** DECYZJA, oparta na inspekcji API; bez reprodukcji błędu  
**Kontrakt:** sekcje 3.1, 4.1–4.5, 5, 7

**Gdzie:** [geometria](src/main/java/nsk/nu/ashcore/api/geometry), [CollisionTests.java](src/main/java/nsk/nu/ashcore/api/collision/CollisionTests.java), [Quaternion.java](src/main/java/nsk/nu/ashcore/api/math/Quaternion.java), testy przecięć i [README.md](README.md).

**Stan i znaczenie:** Ashcore ma AABB, sferę, kapsułę i wybrane testy przecięć, lecz nie ma publicznego OBB, czyli prostopadłościanu z własną orientacją. Osiowa obwiednia obróconej bryły obejmuje dodatkową przestrzeń. Przecięcie obwiedni nie dowodzi przecięcia samej bryły; obecne AABB działa zgodnie ze swoim kontraktem.

**Praca do wykonania:** Ustalić małą, niezmienną reprezentację OBB z użyciem typów Ashcore, np. środek, półrozmiary i właściwy obrót. Wybrać i uzasadnić wspierane pary: promień/odcinek–OBB, sfera–OBB, AABB–OBB i OBB–OBB. Zakres dotyczy kształtów nieruchomych w chwili zapytania. Zdefiniować styczność, zerowe rozmiary, orientację, skończoność danych, tolerancje oraz granice dokładności.

Dla promienia rozdzielić pierwsze trafienie od pełnego przedziału wejścia/wyjścia. Jeżeli wynik ma zasilać Ashtrace, ustalić odpowiedni kontrakt u właściciela geometrii, bez zależności od RayIntersection3 z wyższej warstwy. Start wewnątrz nie może usuwać potrzebnej informacji o wejściu przed początkiem promienia. Dotychczasowe metody zwracające t=0 zachowują znaczenie.

**Warunki zamknięcia:**

- [x] Zapisano reprezentację, wspierane pary i przypadki odrzucone. Brak wsparcia nie jest zwracany jako pewny brak przecięcia.
- [x] Wybrane operacje mają testy styczności, rozdzielenia, zawierania, degeneracji i niemal równoległych osi, w tym rozłączne OBB o przecinających się obwiedniach AABB.
- [x] Sprawdzono znane wyniki, zgodność z istniejącymi prymitywami przy zerowym obrocie oraz wspólne sztywne przekształcenie w uzasadnionej tolerancji. Wzorce nie kopiują badanego algorytmu.
- [x] Przyjęte zapytania promieniowe/odcinkowe mają jawne jednostki i testy pełnych przedziałów, startu wewnątrz, styczności oraz odcinka zerowego.
- [x] Oceniono zgodność API, koszty i ograniczenia numeryczne; zaimplementowany zakres przechodzi testy i clean verify bez nowych zależności produkcyjnych.

**Wynik korekty 2026-09-10:** `OrientedBox` przechowuje środek, półrozmiary i normalizowaną orientację; odrzuca zero kwaternionu i błędne dane, dopuszcza zerowe rozmiary. Dodano siedem metod dla promienia/odcinka, sfery, AABB i OBB. `IntersectionInterval` zachowuje ujemne wejście promienia wewnątrz bryły, a przedział odcinka przycina do [0,1]. `OrientedBoxQueriesTest`: 11 testów PASS; 600 par porównano z niezależnym wzorcem opartym o wierzchołki i przycinanie krawędzi, w tym pary wymagające osi iloczynów wektorowych. Znane wyniki, obrót/przesunięcie, skale 1e-140–1e300, degeneracje i granice są objęte kontrolą. Kontrakty, ograniczenia zaokrągleń i koszt O(1): [docs/GEOMETRY.md](docs/GEOMETRY.md). Pełne clean verify: 165 + 3 PASS; wersja 1.2.0-SNAPSHOT, brak nowych zależności produkcyjnych.

**Powiązania:** Uzupełnia zamknięte [CORE-008](#core-008). Konwersje należą do [SPACE-012](../Ashspace/ISSUES.md#space-012), użycie indeksów do [TRACE-012](../Ashtrace/ISSUES.md#trace-012). Grafy ramek i fizyka nie należą do tego zadania.

<a id="core-012"></a>

## CORE-012 — Zdefiniować geometryczny wynik kontaktu prymitywów

**Status:** GOTOWE

**Priorytet:** P2  
**Dowód:** DECYZJA, oparta na inspekcji znaczenia istniejących wyników  
**Kontrakt:** sekcje 3.1, 4.1–4.5, 5, 7

**Gdzie:** [CollisionTests.java](src/main/java/nsk/nu/ashcore/api/collision/CollisionTests.java), [Hit.java](src/main/java/nsk/nu/ashcore/api/collision/Hit.java), [SweptAABB.java](src/main/java/nsk/nu/ashcore/api/collision/SweptAABB.java), testy przecięć i [README.md](README.md).

**Stan i znaczenie:** sphereVsSphere i sphereVsBox zwracają boolean. Hit opisuje trafienie promieniem, a SweptAABB.Result wynik określonego modelu translacji. Nie są ogólnym wynikiem kontaktu dwóch przenikających się brył; istniejącym normalnym nie należy po cichu przypisywać innego znaczenia.

**Praca do wykonania:** Określić potrzebę i minimalny wynik geometryczny dla wybranych par, zaczynając od sfera–sfera i sfera–AABB. Rozdzielić brak kontaktu, styczność i przenikanie. Zdefiniować kierunek normalnej względem argumentów, punkty na powierzchniach i znaczenie głębokości, jeśli jest zwracana. Dla wspólnych środków, pełnego zawierania i remisów jawnie oznaczać niejednoznaczność albo dokumentować deterministyczny wybór. Nie obiecywać jednego unikalnego punktu dla kontaktu całej krawędzi/powierzchni.

Wynik opisuje geometrię, nie polecenie przesunięcia ciała. Zakres nie obejmuje solvera impulsów, tarcia, mas, podparcia ani utrzymywania kontaktów między klatkami. Pary OBB są opcjonalnym następnym zakresem po CORE-011.

**Warunki zamknięcia:**

- [x] Każde pole ma określone jednostki, układ, dostępność i znaczenie. Głębokość nie jest mylona z odległością promienia ani czasem sweep.
- [x] Testy obejmują styczność, rozdzielenie, częściowe/pełne zawieranie, wspólne środki i degeneracje; punkty leżą na zadeklarowanych powierzchniach w uzasadnionej tolerancji.
- [x] Zamiana argumentów i wspólne sztywne przekształcenie zachowują zadeklarowane relacje; wyjątki dla niejednoznacznych przypadków mają jawne reguły.
- [x] Istniejące metody boolowskie, Hit i SweptAABB.Result zachowują kontrakty. Opisano wspierane pary, koszt i migrację; implementacja przechodzi testy oraz clean verify.

**Wynik korekty 2026-09-10:** Dodano `Contact` i trzy metody kontaktu sfera–sfera, sfera–AABB oraz odwrotnej kolejności AABB–sfera. Brak kontaktu ma depth=-infinity i wektory null; zero oznacza styczność, dodatnia wartość głębokość geometryczną. Punkty na powierzchniach spełniają pointA-pointB=normal*depth w granicach zaokrągleń. Wspólne środki sfer wybierają globalne +X; środek sfery wewnątrz AABB wybiera najbliższą ścianę, remisy X-min/X-max/Y-min/Y-max/Z-min/Z-max. `ContactQueriesTest`: 10 testów PASS, w tym zawieranie, degeneracje, zamiana argumentów, przekształcenia i odrzucanie niereprezentowalnych wyników. Dotychczasowe API zachowane; porównanie javap: zero usuniętych sygnatur. Pełne clean verify i opis jednostek, kosztu O(1), wyjątków oraz migracji: [docs/RELEASE.md](docs/RELEASE.md), [docs/GEOMETRY.md](docs/GEOMETRY.md).

**Powiązania:** [CORE-004](#core-004), [CORE-008](#core-008), opcjonalnie [CORE-011](#core-011). Nie zleca zmian w Ashnav ani adapterach silników.

<a id="core-013"></a>

## CORE-013 — Ocenić zapytania prymitywów podczas zadanego obrotu

**Status:** GOTOWE — zakończona ocena; implementacja odroczona

**Priorytet:** P2  
**Dowód:** DECYZJA; ograniczenie obecnego sweep jest jawnie udokumentowane  
**Kontrakt:** sekcje 1, 3.1, 4.2–4.5, 7, 8

**Gdzie:** [SweptAABB.java](src/main/java/nsk/nu/ashcore/api/collision/SweptAABB.java), [Quaternion.java](src/main/java/nsk/nu/ashcore/api/math/Quaternion.java), [README.md](README.md), przyszłe testy/prototypy geometryczne.

**Stan i znaczenie:** SweptAABB bada translację AABB względem nieruchomego AABB, bez obrotu i przyspieszenia. Rozłączność na początku i końcu kroku nie dowodzi braku kontaktu pomiędzy nimi. To ograniczenie zakresu, nie reprodukcja błędu tego algorytmu.

**Praca do wykonania:** Ocenić potrzebę i wykonalność ograniczonego zapytania geometrycznego, bez zobowiązania do budowy pełnego systemu ciągłych kolizji. Wybrać konkretną parę prymitywów i jawny model toru, np. zadany obrót wokół stałej osi/punktu oraz translację w skończonym przedziale czasu. Dwa końcowe kwaterniony nie określają liczby obrotów ani całej drogi. Dowolny callback ruchu bez ograniczeń nie wystarcza do gwarancji wykrycia kontaktu.

Porównać wynik dla zadeklarowanego modelu w granicach arytmetyki, konserwatywny przedział możliwego kontaktu i próbkowanie bez pełnej gwarancji. Stała liczba podkroków oraz obwiednia obejmująca tylko początkowe i końcowe ustawienie nie gwarantują pokrycia łuku. Wyczerpanie budżetu nie może udawać pewnego braku kontaktu.

**Warunki zamknięcia decyzji:**

- [x] Zapisano parę prymitywów, model ruchu, jednostki, punkt i oś obrotu, obsługę pełnych i wielokrotnych obrotów oraz ograniczenia numeryczne.
- [x] Przeanalizowano kontakt tylko pomiędzy końcami kroku, pełny obrót z identyczną orientacją końcową, styczność i zerowy ruch. Gęste próbkowanie nie jest jedynym dowodem gwarancji ciągłości.
- [x] Zapisano znaczenie wyniku, brakujące dowody, koszty i plan niezależnej weryfikacji, bez niezmierzonych obietnic czasu wykonania.
- [x] Wybrano osobne, ograniczone zadanie implementacyjne z kryteriami odbioru albo odroczenie z uzasadnieniem. GOTOWE dla tej oceny oznacza zakończenie decyzji, nie dostępność nowego API.
- [x] Nie zmieniono kontraktu SweptAABB, nie dodano integracji sił, reakcji na kontakt, stanu świata ani zależności od Ashspace. Ewentualna zmiana wspólnej architektury wymaga osobnego rozstrzygnięcia według sekcji 8.

**Decyzja 2026-09-10:** Oceniono ruch OBB względem nieruchomej sfery: stała oś, prędkość kątowa ze znakiem, początkowy punkt obrotu i translacja w [0,T]. [docs/GEOMETRY.md](docs/GEOMETRY.md#rotation-assessment--core-013) zawiera analityczne przykłady kontaktu między końcami, pełnych/wielokrotnych obrotów, izolowanej styczności i zerowego ruchu. Porównano izolację pierwiastków, konserwatywny podział przedziału i próbkowanie. Wyprowadzono ograniczenie przemieszczenia z prędkości punktów oraz wskazano brak certyfikowanego błędu odległości i zaokrągleń na zewnątrz. Publiczne API odroczono do uzyskania tych dowodów oraz wymagań dokładności/budżetu od konsumenta. Zapisano koszty i niezależny plan weryfikacji; nie zaimplementowano ani nie deklarowano ciągłego wykrywania kolizji. SweptAABB pozostaje bez zmian.

**Powiązania:** [CORE-011](#core-011), [TRACE-012](../Ashtrace/ISSUES.md#trace-012). Ocena nie blokuje statycznych testów ani obecnego wydania.

## Stan przekazania i dziennik sesji

**Stan historyczny przed korektą, 2026-09-09:** wszystkie zadania pozostawały OTWARTE. Utworzono dokumentację; nie wprowadzono korekt kodu, nie wykonano buildów bibliotek ani publikacji. Nie uznawaj samego dodania ISSUES.md za realizację żadnego zadania.

**Historyczny stan po pierwszej korekcie 2026-09-09:** CORE-001–CORE-007 GOTOWE w opisanym zakresie. Gałąź fix/ashcore-contract-v2-20260909, snapshot wejściowy 7649935, commit korekty 7ffe561, wersja robocza 1.0.2-SNAPSHOT.

**Historyczny stan po rozszerzeniu 2026-09-09:** CORE-008–CORE-010 również GOTOWE. Gałąź feat/ashcore-primitives-math-random-20260909, snapshot 2f5fbe5, wersja robocza 1.1.0-SNAPSHOT. Dodano 26 metod publicznych w istniejących typach i 7 klas testowych; brak nowych klas lub zależności produkcyjnych. clean verify: 144 testy jednostkowe + 3 testy artefaktów PASS, IntelliJ build PASS; 156 testów konsumentów PASS. Szczegółowe dowody, migracja i integracja: [docs/RELEASE.md](docs/RELEASE.md). Zamknięcie tych zadań nie oznacza pełnego audytu każdego publicznego API ani potwierdzenia publikacji.

**Następny krok:** uruchomić zdalne CI na Java 21/25 i przed rzeczywistym wydaniem sprawdzić dostępność nowego numeru oraz konfigurację destynacji. W repozytoriach konsumentów kontynuować GRID-001/GRID-005, SPACE-002/SPACE-003 i TRACE-002/TRACE-003 według ich backlogów; zmiana lokalnego Ashcore nie podmienia opublikowanych zależności.

Po kolejnej sesji dopisz wiersz i uzupełnij statusy odpowiednich zadań. Zapisz także nieudane próby i ograniczenia środowiska; nie opisuj kontroli niewykonanej jako zaliczonej.

| Data / commit | ID i decyzja | Zmiana | Polecenie / test i rzeczywisty wynik | Pozostałe zależności / następny krok |
| --- | --- | --- | --- | --- |
| 2026-09-09 / punkt odniesienia powyżej | Wszystkie: OTWARTE | Utworzenie planu korekt | Inspekcja statyczna; testów bibliotek nie uruchomiono | Rozpocząć od wskazanego P1 |
| 2026-09-09 / 7ffe561; snapshot 7649935 | CORE-001–CORE-007: GOTOWE | Korekty normalizacji, geometrii, P², wag, walidacji, dokumentacji, CI i pakowania; zachowana zgodność AABB po teście Ashgrid | Bazowe 88 testów PASS; pierwsze 13 regresji: 10 FAIL; końcowe clean verify: 113 + 3 PASS. Cztery izolowane buildy konsumentów: 156 testów PASS. IntelliJ build PASS. JDK/Maven, wersje artefaktów, SHA i polecenia: docs/RELEASE.md | Publikacja i zdalne CI niewykonane; nie nadpisano wydanych artefaktów. Przekazać ustalenia dolnej warstwy do backlogów konsumentów |
| 2026-09-09 / commit dodający ten wpis; snapshot 2f5fbe5 | CORE-008–CORE-010: GOTOWE | Przecięcia prymitywów, kapsuła, spójność wektorów, konwersje/odwrotność kwaternionu, RNG zakresowe i zerowe wagi; minor 1.1.0-SNAPSHOT | Pierwsze 6 regresji: 6 FAIL; końcowe clean verify: 144 + 3 PASS. IntelliJ build PASS. Cztery izolowane buildy: 156 testów PASS. javap: 26 metod dodanych, zero sygnatur usuniętych. JDK 25.0.2, Maven 3.9.16, release 21; szczegóły i SHA w docs/RELEASE.md | Publikacja i zdalne CI niewykonane; lokalnie nie testowano runtime Java 21. Źródła/POM konsumentów pozostawiono bez zmian |

### Przegląd zakresu kolizji 2026-09-10

**Historyczny stan przeglądu backlogu:** CORE-001–CORE-010 zachowują dotychczasowe statusy. CORE-011–CORE-013 są otwartymi propozycjami P2; nie stanowią dowodu błędu ani warunku wydania obecnego zakresu. Historyczne zalecenia dla konsumentów należy zestawić z ich bieżącymi ISSUES.md.

| Data / commit | ID i decyzja | Zmiana | Polecenie / test i rzeczywisty wynik | Pozostałe zależności / następny krok |
| --- | --- | --- | --- | --- |
| 2026-09-10 / bez operacji Git, zgodnie z instrukcją użytkownika | CORE-011–CORE-013: OTWARTE, P2 / DECYZJA | Geometria OBB, wyniki kontaktu i ocena zapytań obrotowych; wyłącznie backlog | Inspekcja źródeł; kontrola struktury, odnośników i zachowania wcześniejszej treści. Testów bibliotek i buildów nie uruchamiano | Najpierw rozstrzygnąć CORE-011 i CORE-012; CORE-013 pozostaje oceną, bez zobowiązania do implementacji. |

### Korekta geometrii 2026-09-10

**Stan aktualny:** CORE-001–CORE-012 GOTOWE w opisanych zakresach. CORE-013 GOTOWE jako ocena z odroczeniem implementacji, bez nowego API ciągłego obrotu. Gałąź `fix/ashcore-geometry-contracts-20260910`, snapshot `c577243`, wersja robocza `1.2.0-SNAPSHOT`. Zachowano wcześniejsze publiczne sygnatury i kontrakty; dodano trzy typy i dziesięć metod zapytań. Wszystkie zmiany i próby wykonano w katalogu Ashcore.

| Data / commit | ID i decyzja | Zmiana | Polecenie / test i rzeczywisty wynik | Pozostałe zależności / następny krok |
| --- | --- | --- | --- | --- |
| 2026-09-10 / commit dodający ten wpis; snapshot c577243 | CORE-011/CORE-012 GOTOWE; CORE-013 GOTOWE jako ocena, implementacja odroczona | OrientedBox, przedziały przecięć, kontakt sfer i AABB, decyzje geometryczne, README i pakowanie; 1.2.0-SNAPSHOT | Baseline 144 PASS; nowe 21 PASS; clean verify 165 + 3 PASS; javap: zero usuniętych sygnatur. 156 testów zapisanych kopii konsumentów PASS z dokładnym JAR 1.2.0-SNAPSHOT. Pierwsza próba uruchomienia Maven w sandboxie niedostępna; offline brak metadanych Failsafe; późniejszy build PASS. JDK 25.0.2, Maven 3.9.16, release 21; polecenia i SHA w docs/RELEASE.md | Publikacja, zdalne CI oraz bieżące checkouty konsumentów nieweryfikowane; integracja korzysta z historycznych kopii wewnątrz Ashcore. Adoptować nowe kontrakty w SPACE-012/TRACE-012 w osobnych sesjach. Przed powrotem do CORE-013 ustalić budżet/dokładność i certyfikację numeryczną. |

**Następny krok:** wykonać zdalne CI na Java 21/25 oraz kontrole wydania opisane w [docs/RELEASE.md](docs/RELEASE.md). Przy integracji nowych metod w wyższych bibliotekach użyć jawnie wskazanego nowego artefaktu; nie traktować obecnych wyników kopii konsumentów jako testu ich aktualnych źródeł.
