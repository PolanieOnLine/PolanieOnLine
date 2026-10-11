# Łatka 1.42.1: arena, skup i sakwa

Zmiany są przeznaczone dla `VERSION_1_RELEASE_42_REVISION_01` i `master`.
Pierwotny branch `VERSION_1_RELEASE_42` pozostaje bez zmian.

## Arena Wyzwań

Potwory zachowują własne tabele łupów. Sama szansa wypadnięcia przedmiotu nie jest
zwiększana. Po zwycięstwie gracz ma 60 sekund na zebranie łupów; rezerwacja areny
pozostaje aktywna. Wcześniejsze wyjście przez Mistrza Wyzwań nie odbiera nagrody.
Zakończenie sesji usuwa pozostałe potwory i ich ciała, ale nie usuwa rzeczy graczy.

Kupiec Areny stoi w lobby na wolnym polu `40,47`. Przykład rozmowy:
`cześć`, `sprzedaj kolczuga`, `tak`.
Katalog powstaje automatycznie z definicji przedmiotów. Skup wymaga dodatniej
wartości i dopuszczenia do handlu; odrzuca monety, klucze, rzeczy przypisane do
postaci oraz własność innych postaci. Maksymalnie 1000 sztuk na transakcję.
Cena uwzględnia konkretne egzemplarze i ich zapisany mnożnik wartości rzadkości,
a nie tylko nazwę przedmiotu. Przekroczenie limitu stosu monet blokuje sprzedaż.

## Rzadkość łupów

Procenty dotyczą wyłącznie wyposażenia kwalifikującego się do rzadkości,
**pod warunkiem, że przedmiot w ogóle wypadnie**.
Monety, materiały i przedmioty wyłączone z rzadkości zachowują swoje zasady.

| Źródło | Common | Rare | Epic | Legendary |
| --- | ---: | ---: | ---: | ---: |
| Zwykły potwór | 70% | 22% | 6% | 2% |
| Boss (`boss` w profilu AI) | 0% | 85% | 13% | 2% |
| Końcowy czempion próby LEGEND | 0% | 85% | 13% | 2% |
| Wyposażenie ze skrzyni LEGEND | 0% | ok. 71,64% | ok. 20,60% | ok. 7,76% |

Skrzynia LEGEND zachowuje najlepszy z czterech standardowych rzutów, z dolną
granicą Rare. Pozostałe próby oraz dwurzutowy profil zwykłej elity nie są zmieniane.
Gwarancja nie podnosi rzadkości wcześniej zdobytych przedmiotów.

## Wyceny

`tools/rebalance-item-values.ps1` jest narzędziem tylko do odczytu:

```powershell
# PowerShell 7, raport JSON:
./tools/rebalance-item-values.ps1
# Propozycja poprawki; narzędzie samo nie zapisuje plików:
./tools/rebalance-item-values.ps1 -Patch
```

Punktem odniesienia jest średnia cen **skupu** z `buy_items.xml`, po zastosowaniu
mnożników poszczególnych kupców. Niewyceniony sprzęt bojowy jest szacowany na
podstawie najbliższego sprzętu tej samej klasy: siły ataku względem szybkości lub
DEF i wymaganego poziomu. Stosowana jest kwadratowa proporcja siły. Przy braku
punktów odniesienia danej klasy używany jest sprzęt pancerza.
Pozostałe klasy z dodatnimi wartościami mogą użyć mediany relacji cena/value.
Pozycje bez podstaw do wyceny zachowują dotychczasową wartość — nie wymyślamy cen
przedmiotów fabularnych. Pierścienie i waluta są wyłączone z przeliczenia.

Skup nie może przekroczyć najniższej znanej ceny sprzedaży u NPC minus jedna
moneta, aby nie opłacało się kupowanie i natychmiastowe odsprzedawanie.
Ograniczenie dotyczy normalnego sprzętu kupowanego w sklepach; rzadkie łupy
zachowują premię wartości. Istniejące cenniki innych kupców pozostają bez zmian.

| Przykład zwykłego przedmiotu | Nowe value |
| --- | ---: |
| Kolczuga | 29 |
| Czarna zbroja | 60000 |
| Zbroja z mithrilu | 68878 |
| Miecz z mithrilu | 63725 |

To wycena startowa do obserwacji gospodarki, nie gwarancja idealnego balansu.
Warto monitorować dochód z jednej próby areny, ilość sprzedanych rzeczy i
różnicę między wyceną arena/value a konkretnymi sklepami.
Przy odtwarzaniu zapisanych przedmiotów i przed skupem odświeżana jest cena,
bez losowania nowych statystyk, rzadkości, ulepszeń ani właściwości.

## Migracja sakwy

Nowe postacie nie otrzymują `pouch_set`, a oba klienty nie pokazują tego slotu.
Przełączanie zestawów nie obejmuje głównej sakwy. Przy logowaniu stare postacie
przechodzą migrację: monety ze schowka są przenoszone do `pouch` albo sumowane.
Po udanej migracji stary slot jest usuwany. Definicja legacy w RPClass jest
celowo zachowana, aby móc odczytać jeszcze niezmigrowane postacie.

Przy uszkodzonych danych, nietypowej zawartości lub sumie ponad `2147483647`
monet obie oryginalne zawartości są zachowane, nowe wpłaty do schowka zablokowane,
a gracz dostaje komunikat. Log `Reserve pouch migration needs manual attention`
wskazuje postać wymagającą indywidualnej interwencji administratora.
Nie należy kasować zawartości tego slotu ręcznie bez sprawdzenia monet.

## Przywoływanie dla administratora

Wymagany poziom administracyjny co najmniej 35 i uprawnienia do `/script`:

```text
/script SummonMistCreature.class kamienny golem
/script SummonMistCreature.class kamienny golem elite=true
/script SummonMistCreature.class dwugłowy smok elite=true factor=0.1
```

`factor=0.1` to dodatkowe 10% siły wersji mgielnej według istniejącej transformacji
map; domyślnie 0.1, dozwolone 0..10. Elita nakładana jest dopiero po przemianie
mgielnej. Obowiązują dotychczasowe ograniczenia elity, w tym wykluczenie bossów.
Skrypt tworzy jeden potwór obok administratora i zapisuje zdarzenie w logu.
Nie zmienia szablonu zwykłego potwora ani jego wspólnej tabeli łupów.

## Wdrożenie

1. Wykonać kopię bazy postaci i działających plików serwera.
2. Zbudować serwer oraz klienty z właściwego brancha. Sama podmiana XML nie
   uruchomi nowych zachowań skupu, migracji i rzadkości.
3. Podmienić build serwera wraz z odpowiadającymi mu `data/conf/items/*.xml`,
   następnie uruchomić serwer ponownie.
4. Podmienić klienta Java i zbudowany webklient (HTML, CSS, kod JavaScript),
   aby schowek nie pokazywał starej sakwy. Nie zmienia się kodu aplikacji Android.
5. Sprawdzić logowanie postaci z dawną sakwą, przełączanie zestawów, rozmowę ze
   skupującym, drop na arenie i możliwość zebrania łupów po zwycięstwie.

Migracja odbywa się przy logowaniu; nie wymaga masowego SQL na produkcyjnej bazie.
Pierścieni ani ich zasad rzadkości nie zmieniono w tej łatce.

## Pierścienie — analiza, nie wdrożona zmiana

Pierścienie funkcjonalne (powrotu, niewidzialności, szmaragdowy, ślubne) oraz
same odporności na statusy nie mają statystyk wymaganych przez automatyczną
kwalifikację rzadkości. Pierścienie bojowe, czystości i zdrowia kwalifikują się.
Rzadkość nie skaluje bezpośrednio XML-owych podatności na żywioły ani bazowych
odporności `statusresist`.

Przy bazowym ATK/DEF równym 1 mnożniki Rare, Epic i Legendary nadal dają 1 po
zaokrągleniu. ATK 2 może dać 3 dopiero przy odpowiednio wysokim mnożniku
Legendary. Największe różnice pochodzą z dodatkowych właściwości:
Rare ma jeden regularny bonus, Epic dwa, Legendary trzy i dodatkową sygnaturę.
Regularny bonus ATK biżuterii to +1..3, a legendarna Moc reliktu to +4..7 ATK.
Przy dwóch pierścieniach sama podwójna Moc reliktu daje łącznie +8..14 ATK.

Bazowa kradzież życia pierścienia zdrowia pozostaje także na Common — usuwanie
wrodzonego lifesteal dla Common dotyczy broni, nie pierścieni. Jednak
zaokrąglanie skalowanego ułamka do dwóch miejsc powoduje, że bazowe 1% pozostaje
1% również na wyższych rzadkościach. Wzmocnione 2,5% zwykle staje się 3%.

Propozycja do osobnej decyzji: pozostawić funkcjonalne pierścienie bez losowej
rzadkości, zachować bazową tożsamość pierścieni bojowych i zdrowia, a różnice
Rare/Epic/Legendary oprzeć na wyspecjalizowanych bonusach biżuterii. Rozważyć
obniżenie Mocy reliktu tylko dla pierścieni do +2..3 ATK (bez zmiany naszyjników)
oraz świadomy limit sumowania najmocniejszych efektów dwóch pierścieni.
Nie podnosić globalnych mnożników całego wyposażenia tylko po to, żeby ATK 1
zaczął rosnąć. Te propozycje nie są częścią kodu łatki.
