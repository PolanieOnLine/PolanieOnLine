# Zadania Halloween Mine Town w wersji 1.42

## Zadania i nagrody

| Zadanie | NPC | Wymagania | Nagroda | Powtórzenie |
| --- | --- | --- | --- | --- |
| Przygotowania do Mine Town | Boguchwał | 100 mięsa, 50 szynki, 100 sera, 40 steków | skrzynka | po 6 godzinach od oddania |
| Zagubione latarenki | Wolrad | 5 eventowych latarenek z bieżącej edycji | srebrna skrzynia | po 12 godzinach od oddania |
| Rytuał Guślarza | Guślarz | ukończenie obu powyższych zadań przynajmniej raz oraz oddanie 20 strasznych dyń | złota skrzynia | raz na postać w corocznej edycji |

Boguchwał i Wolrad pojawiają się obok Katii na południu Zakopanego (`0_zakopane_s`). Przywitaj się, zapytaj o `zadanie` i potwierdź. Po zebraniu przedmiotów ponownie zapytaj o `zadanie` i potwierdź oddanie. Przedmioty trzeba mieć przy sobie; bank nie jest uwzględniany. Zapasy oddaje się w całości. Skrzynie są przypisane do postaci. Przy pełnym plecaku nagroda zostanie położona pod graczem zgodnie ze standardową obsługą nagród gry.

## Latarenki

Latarenki pojawiają się automatycznie podczas Mine Town, bez wymagania przyjęcia zadania. Publiczne mapy o rozmiarze co najmniej 16 na 16 pól są grupowane po trzy według nazw stref. W każdej grupie losowane są jedna lub dwie mapy i po jednej latarence na mapę. Mapy prywatne, administracyjne, więzienia, areny i samouczki są wyłączone. Mapy bez dostępnego miejsca nie otrzymują latarenki; mechanizm próbuje pozostałych map grupy.

Miejsce na wybranej mapie jest losowane ponownie przy odnowieniu. Latarenka nie pojawia się na przeszkodzie, na portalu, na zajętym polu, w obszarze sekretnym ani w obszarze bez dojścia do brzegu mapy lub portalu. Po podniesieniu odnowienie następuje po 30 minutach. Istniejąca, niepodniesiona latarenka nie jest dublowana. Wyłączenie eventu usuwa spawny, pozostałe latarenki na ziemi i ich timery, ale nie usuwa zebranych przedmiotów z postaci.

Wykorzystany jest istniejący przedmiot `latarenka` i jego grafika. Instancje eventowe mają oznaczenie edycji w `itemdata` oraz opis wskazujący Wolrada. Latarenki sklepowe i z poprzednich edycji nie są przyjmowane w tym zadaniu.

## Dynie i finał

Podczas aktywnego Mine Town zwykła ścieżka dropu potworów ma dodatkowe, pojedyncze losowanie z szansą 5% na jedną straszną dynię. Nie trzeba zmieniać XML potworów. Stare wpisy dropu dyni są pomijane, żeby nie dublować szansy. Po wyłączeniu eventu losowanie przestaje działać. Nie zmienia to osobnego mechanizmu dropu zwierząt hodowlanych.

Obecne zadanie Katii i prezent za samo spotkanie Guślarza pozostają osobnymi nagrodami. Rytuał nie zużywa wcześniejszych skrzyń. Rozpoczęcie ponownego zbierania jedzenia lub latarenek nie odbiera zaliczenia potrzebnego do finału. Postęp i czasy ukończenia są zapisywane w danych postaci. Wyłączenie, ponowne włączenie lub restart serwera nie odblokowują kolejnej złotej skrzyni w tym samym roku. Kolejna coroczna edycja ma osobne wpisy zadań.

## Uruchomienie na serwerze

Po pobraniu zmian z `VERSION_1_RELEASE_42` zbuduj pakiet serwera (`dist_server_binary`), wgraj jego aktualne JAR-y, konfigurację i skrypty oraz uruchom serwer ponownie. Samo wgranie XML nie wystarczy. Klienci Java, Android i web nie wymagają nowych grafik ani zmian protokołu.

Mine Town włącza standardowe polecenie administratora:

```text
/script MineTown.class true
```

Guślarz jest sterowany osobno:

```text
/script Guslarz.class true
```

Wyłączenie odbywa się tymi samymi poleceniami z argumentem `false`. Mine Town może również działać od startu serwera z `-Dstendhal.minetown=true`, a Guślarz z `-Dstendhal.guslarz=true`. Limit złotej skrzyni nie jest resetowany tymi przełącznikami.
