# Rozszerzenie słowiańskich glifów

Zachowano 11 dotychczasowych glifów. Dziesięć nowych daje po trzy glify w każdej z siedmiu kategorii. Wszystkie receptury Zoryka mają 100% szansy powodzenia; wszystkie składniki są zużywane, w tym glif Strzyboga przy ulepszeniu do wichru lub wytrwałości.

| Glif | Gniazdo | Efekt | Składniki |
|---|---|---|---|
| glif świętego dębu | defensive_rune | Zmniejsza dodatkową część obrażeń trafienia krytycznego o 25%. | fragment glifu ×3, magia ziemi ×800, bryłka mithrilu ×20, szafir ×12 |
| glif żywej wody | healing_rune | Po 10 sekundach bez zadawania i otrzymywania obrażeń odnawia co 5 sekund 1% maksymalnego zdrowia, nie więcej niż 20 punktów. | fragment glifu ×4, magia deszczu ×800, serce olbrzyma ×40, cudowna krew ×3 |
| glif Rodzanic | healing_rune | Zwiększa leczenie z jedzenia, napojów i mikstur o 20%. | fragment glifu ×4, serce olbrzyma ×60, smocza krew ×24, bursztyn ×20 |
| glif wichru | utility_rune | Zmniejsza wagę wolniejszych broni o 1, do minimum 2, oraz daje 5% szansy uniku zwykłego, niekrytycznego fizycznego ciosu. | glif Strzyboga ×1, fragment glifu ×2, magia wiatru ×600, ametyst ×16, pióro anioła ×20 |
| glif wytrwałości | utility_rune | Zmniejsza wagę wolniejszych broni o 1, do minimum 2; poniżej 35% zdrowia redukuje otrzymywane obrażenia o 10%. | glif Strzyboga ×1, fragment glifu ×2, serce olbrzyma ×40, ametyst ×16, cudowna krew ×2 |
| glif ogniska | resistance_rune | Zmniejsza otrzymywane obrażenia od ognia o 35%. | fragment glifu ×3, magia płomieni ×1000, bursztyn ×30, kryształ obsydianu ×8 |
| glif korzeni | resistance_rune | Zmniejsza otrzymywane obrażenia od ziemi i wody o 25%. | fragment glifu ×3, magia ziemi ×800, magia deszczu ×800, kryształ obsydianu ×8 |
| glif zmory | control_rune | Daje 10% szansy na zmniejszenie obrażeń zadawanych przez trafioną istotę o 10% przez 6 sekund; czas odnowienia wynosi 12 sekund dla źródła i efekt nie kumuluje się. | fragment glifu ×3, magia mroku ×800, wampirza krew ×24, truchło nietoperza ×12, obsydian ×16 |
| glif Świętowita | special_rune | Dodaje 3 punkty ataku, 5 punktów obrony i 5 punktów procentowych szansy na trafienie krytyczne. | fragment glifu ×5, glif siły ×1, glif tarczy ×1, diament ×8, rubin ×12, szafir ×12 |
| glif przodków | special_rune | Dodaje 250 punktów zdrowia i 8 punktów obrony oraz zmniejsza obrażenia od wszystkich sześciu żywiołów o 5%. | fragment glifu ×5, glif tarczy ×1, serce olbrzyma ×50, cudowna krew ×4, magia ziemi ×400, magia światła ×400 |

Wartości nowych atrybutów procentowych są zapisane jako FLOAT. Odporności są mnożnikami podatności (np. 0,65 oznacza 35% redukcji). Żywa woda wymaga 10 sekund bez zadawania i otrzymywania obrażeń i odnawia najwyżej 20 HP na impuls co 5 sekund. Wytrwałość działa ściśle poniżej 35% maksymalnego HP.

Grafiki: 15 zatwierdzonych zamienników z `outputs/glyph-slowianskie`, 10 nowych z `outputs/nowe-glify-v2`; nazwy podklas odpowiadają plikom PNG. Nie zmieniono tłumaczeń klienta.

Weryfikacja kontraktu: `python docs/test_glyph_expansion.py`.
