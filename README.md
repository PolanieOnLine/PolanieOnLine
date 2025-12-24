# PolanieOnLine
Projekt wykorzystujący zmodyfikowaną wersję oryginalnych plików gry Stendhal, przez ekipę nowego PolanieOnLine. Pliki do stworzenia aktualnego projektu zastosowano stary, nieaktywny już projekt PolskaOnLine oraz PolskaGRA.
PolanieOnLine działa w sferze open-source, gdzie wszyscy mają dostęp do plików oraz możliwość modyfikowania ich na własną rękę.
Przestarzałe już pliki serwerowe PolskaOnLine wraz z PolskaGRA, które wchodzą w skład naszego projektu PolanieOnLine zostały zintegrowane do najnowszych źródeł Stendhal'a. Gra "nowej generacji POL" będzie otrzymywała ciągłe wsparcie rozwojowe (mniejsze bądź większe), staramy się słuchać propozycji naszych graczy oraz próbujemy wprowadzać je w kolejnych wersjach gry.

## Czym to jest?
W skrócie, jest to gra przygodowa multiplayer (MMORPG) z kamerą 2D z bardzo przyjazną grafiką dla oka,
minimalnym oraz przyjaznym również interfejsem gracza. Wszelkie wykorzystywane materiały w "naszej" grze podlegają publicznej licencji GNU GPL.

## Instalacja
Aby móc zainstalować naszą grę wystarczy pobrać odpowiedni plik instalacyjny z naszej witryny https://s1.polanieonline.eu/ lub z naszego sourceforge'a https://sourceforge.net/projects/game-polanieonline/

## Logi klienta Android
Klient Android wykorzystuje Log4j 2 do zapisywania logów uruchomienia i błędów. Domyślnie pliki trafiają do `~/PolanieOnline/logs/client.log` z rotacją do pięciu plików `.gz` po 5 MB każdy (wzorzec `%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger{36} - %msg`). Jeśli zapis w katalogu domowym jest niemożliwy, aplikacja używa katalogów `getFilesDir()/logs` lub `getExternalFilesDir(null)/logs`, dzięki czemu nie są wymagane dodatkowe uprawnienia do pamięci zewnętrznej (scoped storage). W wersjach debug logi są dodatkowo kierowane na konsolę/logcat.
