# Ashcore — ISSUES

## Cel pliku

Ten plik powstał 2026-09-09 po przeglądzie wspólnych zasad Blackframe i przyjęciu [blackframe.md, rewizja 2.0](../blackframe.md). Służy do zaplanowania korekt tej biblioteki oraz przekazywania pracy między kolejnymi, niezależnymi sesjami. Nie trzeba znać historii rozmowy: poniżej są powód zadania, miejsca w kodzie, kryteria odbioru i powiązania z innymi projektami.

To lista prac i miejsce zapisu dowodów, a nie dokumentacja gotowych funkcji ani informacja, że błędy już naprawiono. Nie wszystkie pozycje są błędami wykonania: część wymaga doprecyzowania umowy z użytkownikiem lub sprawdzenia istniejących zabezpieczeń. Przegląd obejmował README, POM, workflow i wybrane źródła/testy; nie jest pełnym audytem całego kodu. Podczas przygotowania pliku nie zmieniano implementacji i nie uruchamiano testów bibliotek.

## Punkt odniesienia

- Rola projektu: Podstawowe obliczenia, geometria, losowanie i statystyki. Decyzje tutaj wpływają na wszystkie wyższe biblioteki.
- Wersja zadeklarowana w lokalnym POM: **1.0.1**. To nie jest potwierdzenie publikacji.
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

<a id="core-001"></a>

## CORE-001 — Domknąć gwarancje normalizacji promieni i obrotów

**Status:** OTWARTE  
**Priorytet:** P1  
**Dowód:** INSPEKCJA  
**Kontrakt:** sekcje 3.1, 4.2, 4.3, 4.5

**Gdzie:** [Ray.java](src/main/java/nsk/nu/ashcore/api/geometry/Ray.java), [Vector3.java](src/main/java/nsk/nu/ashcore/api/math/Vector3.java), [Quaternion.java](src/main/java/nsk/nu/ashcore/api/math/Quaternion.java), [GeometryApiTest.java](src/test/java/nsk/nu/ashcore/api/geometry/GeometryApiTest.java).

**Stan podczas przeglądu:** Ray deklaruje niezerowy, znormalizowany kierunek, ale sprawdza tylko direction.length() == 0, a potem normalizuje. Długość Vector3 jest liczona przez sumę kwadratów; skończony wektor (1e308, 0, 0) daje nieskończoną długość i po normalizacji może dać kierunek zerowy. Quaternion.normalized analogicznie sumuje kwadraty; dla zera zwraca obrót jednostkowy. To wnioski z kodu, bez uruchomionego testu.

**Znaczenie:** Kierunek promienia ma wskazywać stronę i mieć długość 1. Jeżeli staje się zerowy albo niepoprawny, błędy przechodzą do wykrywania trafień, siatki i transformacji.

**Praca do wykonania:** Odtwórz przypadki w testach. Określ obsługę NaN, nieskończoności, bardzo małych/dużych wartości, zerowej osi obrotu i kwaternionu zerowego. Popraw normalizację lub jawnie odrzucaj dane poza uzasadnionym zakresem. Nie zmieniaj po cichu dotychczasowej obsługi zera.

**Warunki zamknięcia:**

- [ ] Dla (1e308,0,0), bardzo małego kierunku, zera i danych niefinitych konstruktor zwraca obiekt spełniający kontrakt albo udokumentowany błąd; nigdy zaakceptowany promień z zerowym kierunkiem.
- [ ] Testy sprawdzają długość i kierunek oraz geometrię Ray.at; obejmują zero/duże składowe Quaternion.
- [ ] Zapisano skutki zgodności dla Ashgrid, Ashspace i Ashtrace.

**Powiązania:** Przekaż wynik do [GRID-001](../Ashgrid/ISSUES.md#grid-001), [SPACE-002](../Ashspace/ISSUES.md#space-002) i [TRACE-002](../Ashtrace/ISSUES.md#trace-002); nie naprawiaj tego przez duplikowanie normalizacji w każdej bibliotece.

<a id="core-002"></a>

## CORE-002 — Określić zakres determinizmu RNG, szumu i obliczeń

**Status:** OTWARTE  
**Priorytet:** P1  
**Dowód:** AUDYT  
**Kontrakt:** sekcje 4.1, 5

**Gdzie:** [README.md](README.md), [DeterministicRandoms.java](src/main/java/nsk/nu/ashcore/api/random/DeterministicRandoms.java), [SeedSequence.java](src/main/java/nsk/nu/ashcore/api/random/SeedSequence.java), [PerlinNoise.java](src/main/java/nsk/nu/ashcore/api/noise/PerlinNoise.java), [RunningStats.java](src/main/java/nsk/nu/ashcore/api/stats/RunningStats.java).

**Stan podczas przeglądu:** README mówi o tych samych danych i ziarnie, a defaultGenerator wybiera SplitMix64 w bieżącym wydaniu. Nie wynika z tego automatycznie zgodność wszystkich wyników między wersjami/JVM ani niezależność statystyki strumieniowej od kolejności próbek.

**Znaczenie:** Ten sam seed nie jest obietnicą identycznego terenu po dowolnej aktualizacji, jeśli zmieni się generator, liczba losowań lub obliczenia.

**Praca do wykonania:** Spisz gwarancje per rodzina API: generator i sposób użycia stanu, kolejność próbek, wersje, callbacki i obsługiwane środowiska. Ustal znaczenie generatora domyślnego i wersjonowania strumienia. Przejrzyj użyte Math/StrictMath bez mechanicznej zamiany wszystkich wywołań.

**Warunki zamknięcia:**

- [ ] Istnieje jawna, ograniczona do dowodów deklaracja powtarzalności i zasad zmian wyników pomiędzy wydaniami.
- [ ] Dla obiecanego stabilnego RNG/hash są utrwalone wektory wyników z nazwanym algorytmem; inne obietnice mają adekwatne testy.
- [ ] README wyjaśnia rolę seeda, stanu i kolejności bez obietnicy dokładności wynikającej z samego determinizmu.

**Powiązania:** Konsumenci muszą znać ewentualne zmiany wyników; szczególnie [SPACE-003](../Ashspace/ISSUES.md#space-003) i [TRACE-003](../Ashtrace/ISSUES.md#trace-003).

<a id="core-003"></a>

## CORE-003 — Ustalić kolejność i efekty ładowania providerów SPI

**Status:** OTWARTE  
**Priorytet:** P1  
**Dowód:** INSPEKCJA  
**Kontrakt:** sekcje 3.1, 4.1, 4.2, 4.5

**Gdzie:** [ServiceRegistry.java](src/main/java/nsk/nu/ashcore/api/spi/ServiceRegistry.java), [ServiceRegistryApiTest.java](src/test/java/nsk/nu/ashcore/api/spi/ServiceRegistryApiTest.java).

**Stan podczas przeglądu:** Rejestr ładuje providery eagerly przez ServiceLoader, wybiera je po ID, odrzuca duplikaty i przechowuje wynik w Map.copyOf. ids()/all() nie definiują kolejności. Samo LinkedHashMap użyte przed kopiowaniem nie stanowi publicznej gwarancji kolejności.

**Znaczenie:** Jeśli klient wybierze pierwszy provider albo kolejność inicjalizacji ma znaczenie, niejawne założenie może zmienić wynik.

**Praca do wykonania:** Zdecyduj, czy enumeracja ma stabilny porządek, czy klienci muszą wybierać wyłącznie po ID. Oddziel kolejność zwracanych kolekcji od kolejności uruchamiania konstruktorów providerów. Udokumentuj classloader i efekty inicjalizacji.

**Warunki zamknięcia:**

- [ ] Javadoc opisuje wybór po ID, brak/duplikat ID, ładowanie i zakres obietnicy kolejności.
- [ ] Jeśli porządek jest gwarantowany, testy sprawdzają go dla różnych układów providerów; jeśli nie, przykłady nie polegają na pierwszym elemencie.
- [ ] Sprawdzono działanie z zasobami SPI zbudowanego JAR, nie tylko z katalogu klas testowych.

**Powiązania:** [GRID-005](../Ashgrid/ISSUES.md#grid-005) korzysta z ServiceRegistry; uzgodnij kontrakt przed dostosowaniem testów Ashgrid.

<a id="core-004"></a>

## CORE-004 — Opisać jednostki, tolerancje i założenia geometrii

**Status:** OTWARTE  
**Priorytet:** P1  
**Dowód:** AUDYT  
**Kontrakt:** sekcje 3.1, 4.2, 4.3

**Gdzie:** [NumericTolerance.java](src/main/java/nsk/nu/ashcore/api/math/NumericTolerance.java), [Matrix4.java](src/main/java/nsk/nu/ashcore/api/math/Matrix4.java), [Quaternion.java](src/main/java/nsk/nu/ashcore/api/math/Quaternion.java), [CollisionTests.java](src/main/java/nsk/nu/ashcore/api/collision/CollisionTests.java), [SweptAABB.java](src/main/java/nsk/nu/ashcore/api/collision/SweptAABB.java).

**Stan podczas przeglądu:** NumericTolerance zawiera stałe absolutne i helpers przyjmujące eps. Quaternion.fromAxisAngle opisuje radiany, rotate zakłada kwaternion jednostkowy. Potrzebna jest kontrola pozostałych kontraktów, a nie założenie, że cała dokumentacja jednostek jest nieobecna.

**Znaczenie:** Jedna liczba może oznaczać odległość, kąt albo część czasu ruchu. Mieszanie tych znaczeń daje pozornie wiarygodny wynik dla niewłaściwego pytania.

**Praca do wykonania:** Przejrzyj warunki wejścia, osobliwość macierzy, granice i stykanie brył, parametr promienia/sweep, normalizację i jednostki tolerancji. Sprawdź efekty ujemnego/eps równego NaN/nieskończoności. Wyjaśnij, że przecięcie nie oblicza sił ani odbicia.

**Warunki zamknięcia:**

- [ ] Każdy badany kontrakt ma jednostki, znaczenie wyniku i przypadki szczególne; istnieją odnośniki do odpowiednich testów.
- [ ] Tolerancje są uzasadnione dla konkretnych wielkości; nie wprowadzono jednej globalnej wartości dla każdej operacji.
- [ ] Testy przypadków stycznych, równoległych, zerowych i skrajnych odpowiadają wybranemu modelowi.

**Powiązania:** Ustalenia o Ray i transformacjach przekaż do [SPACE-002](../Ashspace/ISSUES.md#space-002) oraz [TRACE-002](../Ashtrace/ISSUES.md#trace-002).

<a id="core-005"></a>

## CORE-005 — Zweryfikować deklaracje algorytmów i kosztów

**Status:** OTWARTE  
**Priorytet:** P2  
**Dowód:** AUDYT  
**Kontrakt:** sekcje 4.2, 4.4, 4.5

**Gdzie:** [README.md](README.md), [HaltonSequence.java](src/main/java/nsk/nu/ashcore/api/random/HaltonSequence.java), [WeightedSampler.java](src/main/java/nsk/nu/ashcore/api/random/WeightedSampler.java), [P2Quantile.java](src/main/java/nsk/nu/ashcore/api/stats/P2Quantile.java), [RunningStats.java](src/main/java/nsk/nu/ashcore/api/stats/RunningStats.java).

**Stan podczas przeglądu:** README podaje m.in. zamortyzowane O(1) Haltona i O(1) aktualizacji statystyk. Każda taka obietnica wymaga kontekstu: rozmiaru stanu, zakresu licznika, sposobu inicjalizacji i modelu aproksymacji.

**Znaczenie:** Szacowana kwantyla nie jest dokładnym wynikiem sortowania całej historii, a Big-O nie podaje czasu w milisekundach.

**Praca do wykonania:** Zweryfikuj opis funkcji, założenia próbkowania/ważenia, puste dane, przepełnienie liczników i ograniczenia aproksymacji. Dopisz pamięć i praktyczne znaczenie kosztów. Nie zmieniaj algorytmu tylko dlatego, że jest przybliżony.

**Warunki zamknięcia:**

- [ ] Tabela kosztów jest zgodna z kodem i wyjaśnia symbole, pamięć, inicjalizację oraz koszt amortyzowany.
- [ ] Przybliżenia i przypadki bez danych są jawne; testy opierają się na odpowiednim wzorcu/warunkach, nie na kopii kodu.
- [ ] Benchmarki, jeśli potrzebne do twierdzeń o szybkości, mają opis danych i środowiska.

**Powiązania:** Brak wymaganej zmiany innych bibliotek.

<a id="core-006"></a>

## CORE-006 — Wyznaczyć wspierane API i zasady migracji

**Status:** OTWARTE  
**Priorytet:** P1  
**Dowód:** DECYZJA  
**Kontrakt:** sekcje 5, 5.1, 8

**Gdzie:** [README.md](README.md), [DeterministicRandoms.java](src/main/java/nsk/nu/ashcore/api/random/DeterministicRandoms.java), [SplitMix64Random.java](src/main/java/nsk/nu/ashcore/implementation/random/SplitMix64Random.java).

**Stan podczas przeglądu:** Publiczne fabryki RNG już ukrywają implementację, ale samo rozdzielenie api/implementation nie definiuje całej zgodności. Inne repozytoria używają typów Ashcore w swoich publicznych sygnaturach.

**Znaczenie:** Zmiana konstruktora, obsługi zera lub kolejności wyników może wpłynąć na użytkownika nawet bez zmiany nazwy metody.

**Praca do wykonania:** Zdefiniuj wspierane typy i semantykę. Oceń zgodność poprawek [CORE-001](../Ashcore/ISSUES.md#core-001)–[CORE-005](../Ashcore/ISSUES.md#core-005), wersjonowanie i ewentualne deprecations. Uzupełnij README o proste przykłady i ograniczenia bez przenoszenia całego backlogu.

**Warunki zamknięcia:**

- [ ] Lista/zasada wspieranego API obejmuje oficjalne przykłady; nie usunięto opublikowanych konstruktorów bez planu migracji.
- [ ] Wybrano wersję odpowiednią do skutków, zachowując opublikowane artefakty bez nadpisania.
- [ ] Quick start kompiluje się i pokazuje rzeczywiście wspierane zachowanie.

**Powiązania:** Konsumenci: Ashgrid, Ashspace, Ashtrace i Ashnav; wymagane sprawdzenie integracji dla zmienionych kontraktów.

<a id="core-007"></a>

## CORE-007 — Dostosować CI, pakowanie i dowody wydania

**Status:** OTWARTE  
**Priorytet:** P1  
**Dowód:** INSPEKCJA  
**Kontrakt:** sekcje 2, 4.5, 6

**Gdzie:** [pom.xml](pom.xml), [.github/workflows/maven.yml](.github/workflows/maven.yml), [.github/workflows/publish.yml](.github/workflows/publish.yml), [README.md](README.md).

**Stan podczas przeglądu:** CI uruchamia mvn -B package, a kontrakt wymaga clean verify. POM ustawia source/target 21 bez jawnego przypięcia maven-compiler-plugin; Javadoc ma doclint=none i failOnError=false. Profil central istnieje, lecz pokazany workflow deploy nie aktywuje go i publikuje do GitHub Packages. Początkowa gałąź: master; CI filtruje master. Workflow wybiera pierwszy JAR bez wykluczenia sources/javadoc.

**Znaczenie:** Zielony wynik obecnego CI nie jest dowodem wykonania całej bramki jakości ani obecności artefaktu w Maven Central. Brak automatyzacji Central nie dowodzi braku publikacji ręcznej.

**Praca do wykonania:** Ustaw rzeczywistą bramkę clean verify, dobierz przypięty compiler plugin i release 21, sprawdź generowanie dokumentacji oraz jednoznaczną identyfikację artefaktów. Potwierdź utrzymywane gałęzie, docelowe wersje zależności i sposób publikacji do każdej używanej destynacji. JUnit pozostaw w test scope; nie usuwaj go w imię niezależności produkcyjnej.

**Warunki zamknięcia:**

- [ ] Zapisano wynik mvn -B clean verify z wymaganymi testami oraz wersje JDK/Maven; CI obejmuje faktycznie utrzymywane gałęzie i PR-y.
- [ ] Główny JAR, sources, Javadoc i wymagane zasoby są sprawdzone. Błędny Javadoc nie jest po cichu uznawany za poprawny; nie trzeba przy tym mechanicznie włączać każdej reguły stylistycznej doclint.
- [ ] Wskazano używane cele publikacji, tag/wersję i dowody dostępności albo jawnie pozostawiono publikację jako niezweryfikowaną. Sam deploy nie służy jako test poprawek.
- [ ] Sprawdzono efektywne zależności i ich scope; test integracyjny korzysta z zamierzonej wersji dolnej warstwy, a nie przypadkowej starej kopii z lokalnego Maven.

**Powiązania:** Wspólny wzorzec: [TEMPLATE-001](../Ashtemplate/ISSUES.md#template-001) i [TEMPLATE-002](../Ashtemplate/ISSUES.md#template-002). Tę korektę można wykonać niezależnie od napraw algorytmów. Istniejącego numeru wydania nie nadpisuj innym artefaktem.

## Stan przekazania i dziennik sesji

**Na 2026-09-09:** wszystkie zadania pozostają OTWARTE. Utworzono dokumentację; nie wprowadzono korekt kodu, nie wykonano buildów bibliotek ani publikacji. Nie uznawaj samego dodania ISSUES.md za realizację żadnego zadania.

**Sugerowany start:** [CORE-001](../Ashcore/ISSUES.md#core-001); następnie [CORE-002](../Ashcore/ISSUES.md#core-002) i [CORE-003](../Ashcore/ISSUES.md#core-003).

Po kolejnej sesji dopisz wiersz i uzupełnij statusy odpowiednich zadań. Zapisz także nieudane próby i ograniczenia środowiska; nie opisuj kontroli niewykonanej jako zaliczonej.

| Data / commit | ID i decyzja | Zmiana | Polecenie / test i rzeczywisty wynik | Pozostałe zależności / następny krok |
| --- | --- | --- | --- | --- |
| 2026-09-09 / punkt odniesienia powyżej | Wszystkie: OTWARTE | Utworzenie planu korekt | Inspekcja statyczna; testów bibliotek nie uruchomiono | Rozpocząć od wskazanego P1 |
