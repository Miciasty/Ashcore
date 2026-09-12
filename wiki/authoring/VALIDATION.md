# Sprawdzenie WIKI — 2026-09-12

Wersja biblioteki: 1.2.0. Testy strony: Node.js 24.14.0, Tailwind CSS 4.3.3,
JDK 21.0.12.1+1-LTS i przeglądarka wbudowana w Codex na Windows.

| Kontrola | Wynik | Zakres |
| --- | --- | --- |
| `npm run build` | PASS | Kompilacja CSS; 15 stron, 89 sekcji, 122 odnośniki w treści; nawigacja, zasoby, zgodność wersji z POM |
| `npm run check:examples` | PASS | Kompilacja bieżącego źródła Ashcore i 26 pełnych przykładów Java z artykułów; wykonanie z włączonymi asercjami |
| Usunięte pliki wyniku | PASS | Ponowne budowanie usuwa celowo dodany plik próbny z podkatalogu `_site/content` |
| Widok komputerowy | PASS | Wszystkie 15 stron renderuje nagłówek i sekcje; brak poziomego przepełnienia; spis treści widoczny przy 1440 px |
| Widok 390 × 844 | PASS | Wszystkie 15 stron bez poziomego przepełnienia dokumentu; menu otwiera się i zamyka po nawigacji |
| Wyszukiwanie | PASS | Zapytanie `rayVsBoxT`, wybór klawiaturą i przejście do sekcji artykułu |
| Schowek | PASS | Przycisk kopiuje treść `AshcoreQuickStart.java` |
| Motyw | PASS | Zmiana na jasny, zachowanie po przeładowaniu, powrót do ciemnego |
| Diagram | PASS | Obrót Y do 180° zmienia wynik, Reset przywraca 30°; wartości i etykiety dostępne w interfejsie |
| Podkatalog publikacji | PASS | Gotowy pakiet otwarty pod `/_site/`; odświeżenie bezpośredniego adresu sekcji zachowuje stronę i style |
| Konsola przeglądarki | PASS | Brak zarejestrowanych błędów i ostrzeżeń podczas kontroli |
| Źródło zdalne | PASS | Publiczny tag `v1.2.0` ma ten sam identyfikator co lokalny tag; domyślna gałąź repozytorium to `master` |

Pierwsza próba wykonania przykładów wykryła zbyt ścisłe porównanie głębokości
kontaktu z `0.5`. Rzeczywisty wynik wyniósł `0.4999999999999999`; asercja przykładu
uwzględnia teraz tolerancję `1e-12`. Po poprawce cały zestaw przeszedł.

Kontrola dotyczy strony i przykładów biblioteki. Nie uruchamiano serwera Minecraft,
pakowania konkretnego pluginu ani workflow na GitHubie. Kod biblioteki nie był zmieniany.
Publikacja wymaga osobnego uruchomienia po zaakceptowaniu lokalnego podglądu.

## Kolorowanie Java — aktualizacja

- PASS: lokalny PrismJS 1.30.0 oznaczył 4497 tokenów w 26 blokach Java; po usunięciu znaczników źródło pozostaje identyczne. Sprawdzono też escapowanie znaczników HTML wewnątrz napisów Java.
- PASS: oba motywy pokazują osobne kolory słów kluczowych, typów, metod, napisów, liczb, adnotacji i komentarzy. Najniższy kontrast palety wynosi 4,95:1 na jasnym tle i 6,37:1 na ciemnym.
- PASS: rzeczywiste skopiowanie i wklejenie przykładu z palety zachowuje kod; jednowierszowe pole kontrolne usuwa jedynie znaki końca linii.
- PASS: przykład `JavaPalette.java` ze wspólnego szablonu skompilowano z `--release 11` i uruchomiono na JDK 21; wynik to `SPAWN, CAMP / 2.5`.
- PASS: nowa zakładka szablonu i kolorowany blok Ashcore nie powodują poziomego przepełnienia przy szerokości 390 px. Konsola przeglądarki nie zgłasza błędów.
- PASS: budowanie Ashcore i szablonu oraz kontrola odnośników. Szablon zawiera teraz 10 stron i 34 sekcje, w tym `java-palette`.

## Pozostałe palety kodu — aktualizacja

- PASS: 37 bloków Ashcore i 25 bloków szablonu zachowuje źródło po kolorowaniu, włącznie z wcięciami, znakami Unicode i escapowanym HTML. Zwykły tekst nie dostaje tokenów.
- PASS: XML/HTML, Kotlin/Groovy, Bash, PowerShell, YAML/JSON i komendy Minecraft korzystają z jawnie wybranych lokalnych gramatyk. Sprawdzono aliasy, parametry `-cp` i `-NoEnumerate`, operatory PowerShell, `$null`, zmienne środowiskowe oraz polecenia Maven/Java.
- PASS: wszystkie cztery nowe strony palet wyświetlają kolory w obu motywach; przy 390 px próbki układają się w jednej kolumnie bez poziomego przepełnienia dokumentu.
- PASS: rzeczywiste skopiowanie i wklejenie bloków XML, Bash i PowerShell zachowuje źródło; jednowierszowe pole kontrolne usuwa jedynie znaki końca linii. Konsola podglądu nie zgłasza błędów.
- PASS: budowanie i kontrole obu WIKI. Ashcore: 15 stron, 89 sekcji, 122 odnośniki. Szablon: 14 stron, 47 sekcji, 15 odnośników, w tym nowe strony `xml-palette`, `gradle-palette`, `terminal-palette` i `config-palette`.
- Paleta Java i źródło 26 przykładów Java pozostają bez zmian. Nie wykonano pusha ani publikacji.

## Paleta wyników — aktualizacja

- PASS: blok `Expected output` używa formatu `output`: nazwy pól są turkusowe, liczby bursztynowe, wartości logiczne fioletowe; separatory pozostają neutralne.
- PASS: sprawdzono kolory w jasnym i ciemnym motywie oraz rzeczywiste kopiowanie wyniku. Tekst dwóch wierszy i wszystkie wartości pozostają bez zmian.
- PASS: kontrola zachowania źródła w 37 blokach Ashcore i budowanie WIKI. Szablon zawiera również osobną stronę `output-palette` z przykładami i wartościami kolorów.
