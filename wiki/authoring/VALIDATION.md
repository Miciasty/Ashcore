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
