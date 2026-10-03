# Klient Android 1.42

Klient korzysta z webclienta pod `https://polanieonline.eu/client/polanieonline.html`.
Zmiany webclienta, w tym poprawki outfitów, wymagają opublikowania jego kompilacji 1.42 osobno.
Logowanie Google na stronie nie oznacza logowania Google bezpośrednio do gry.
Ta wersja nie przenosi integracji Steam z planowanej wersji 1.43.

## Nowe funkcje aplikacji

Ekran startowy używa zimowego tła z ośnieżonym lasem, górami i drewnianą wioską. W poziomie menu jest po prawej,
a małe karty aktualności i wydarzenia są na dole. W pionie menu znajduje się na dole.
Ornamenty używają tych samych ścieżek i kolorów co strona.
Menu i przyciski na ekranie startowym wykorzystują istniejącą teksturę `panel_wood`
oraz dotychczasowy styl `btn_wood`, bez generowania nowych grafik.
Nowe tło jest osobnym plikiem, a stare grafiki pozostają w zasobach. Środkowe kadrowanie
wypełnia ekran w pionie i poziomie bez rozciągania. Powiększone `logo2x2.png` z repozytorium strony
jest nakładane osobno i dopasowywane z zachowaniem proporcji. Czcionka `polskaonline.ttf`
pozostaje jako zasób wcześniejszego wariantu. Opis obu wygenerowanych teł znajduje się w `ARTWORK.md`.
Przyciski pomocnicze mają wizualnie 38dp wysokości i 48dp obszaru dotyku.

Publiczne aktualności i kalendarz są pobierane z `/api/v1/site/home` i
`/api/v1/site/calendar` na polanieonline.eu. Nie przesyłają danych konta ani cookies.
Po utracie dostępu do strony pozostaje ostatnia pobrana treść. Linki do strony otwierają
przeglądarkę, a kalendarz natywny jest osobną aktywnością, bez przeładowania gry.
Kalendarz używa tego samego zimowego tła, drewnianych przycisków, logo i ornamentów co menu.
W poziomie wydarzenia są obok panelu sterowania, a w pionie pod nim. Pełny opis jest dostępny w szczegółach.

Formularze logowania i rejestracji nie używają pełnoekranowego edytora klawiatury w poziomie.
Przycisk Dalej przechodzi do następnego pola, a Gotowe zamyka klawiaturę bez wysyłania formularza.
Całe okno, wraz z nagłówkiem i przyciskami, można przewijać przy małej ilości miejsca nad klawiaturą.

Panel zapisanych kont pokazuje tylko nazwy. Można usunąć pojedynczy zapis z telefonu,
bez usuwania konta gry. Lista profili Stage/Test jest dostępna tylko w kompilacji debug.

W czasie działania klienta status odczytuje stan istniejącego WebSocketu gry.
Sam dostęp do strony nie oznacza połączenia z grą. Nieznany interfejs transportu pozostaje
niepotwierdzony. Przełączenie sieci nie przeładowuje gry ani nie wysyła ponownie poleceń.
Przycisk ponownego połączenia wymaga potwierdzenia. Nie zmieniono kodu ani sterowania webclienta.

Po rozpoczęciu logowania pojawia się natywny ekran ładowania z przyciskiem Anuluj.
Po 45 sekundach informuje o dłuższym oczekiwaniu, ale nie ponawia logowania i nie przerywa sesji automatycznie.
Widoczny formularz logowania, wybór postaci albo gotowy widok gry kończą nakładkę.
Anulowanie zatrzymuje ładowanie, unieważnia oczekujące odpowiedzi logowania i wraca do menu.
Zmiana orientacji nie zeruje czasu oczekiwania.

Na Androidzie 8 i nowszym awaria procesu WebView usuwa wyłącznie uszkodzony widok.
Jeśli nie pozostał inny widok, powstaje nowy ekran startowy. Zapisane konta nie są usuwane,
a logowanie trzeba rozpocząć ponownie. Wspólny proces kilku widoków odtwarzany jest tylko raz.
Logi klienta nie zapisują pełnych adresów stron ani parametrów powrotu logowania.

### Przypomnienia

Użytkownik wybiera wydarzenia pojedynczo. Android 13 i nowszy prosi o zgodę dopiero wtedy.
Dla wydarzeń z godziną można wybrać początek, 30 minut albo godzinę wcześniej.
Dla wydarzenia całodniowego przypomnienie jest pierwszego dnia o 9:00 w czasie polskim.
Daty wielodniowe pokazują ostatni dzień włącznie.

WorkManager obsługuje przypomnienia i sprawdza zmiany wybranych wydarzeń co sześć godzin.
Zmiana terminu zastępuje poprzednią pracę, a odwołane wydarzenie nie jest przypominane.
Przy przeniesieniu wydarzenia sprawdzany jest dawny miesiąc i kolejne 12 miesięcy.
Wydarzenie niedostępne w tym okresie nie jest przypominane. Przy chwilowym braku sieci
wykorzystywany jest ostatni potwierdzony termin. Nie można wtedy wiedzieć o zmianach
wprowadzonych po ostatniej synchronizacji.

Nie są używane dokładne alarmy ani FCM. System może opóźnić wykonanie podczas oszczędzania
baterii. Przypomnienia pozostają tylko na urządzeniu, nie są przenoszone w backupie.
Usunięcie ostatniego przypomnienia wyłącza okresową synchronizację.

### Zgłoszenie problemu

Raport zawiera wersję aplikacji, Androida i WebView, model telefonu, rodzaj sieci
oraz ogólny stan połączenia. Nie zawiera logów, adresów sesji, haseł, loginów ani czatu.
Użytkownik widzi treść, może ją skopiować albo przygotować wiadomość do
support@polanieonline.eu. Aplikacja nie wysyła wiadomości automatycznie.

## Kompilacja i testy

Wymagane: JDK 17 lub 21, Android SDK 36, Build Tools 36.1.0 oraz Node.js 22 do testów formularzy.
Ustaw `JAVA_HOME` na katalog JDK. Katalog SDK wskaż przez `-Dandroid.home` albo lokalny plik
`local.properties` z `sdk.dir`. Lokalnej konfiguracji nie dodawaj do repozytorium.

Z katalogu `app/android`:

```sh
./gradlew --no-daemon -Dandroid.home=/path/to/android-sdk :client:assembleRelease :client:testReleaseUnitTest :client:lintRelease
node --test tests/native-forms.test.cjs
```

Na Windows użyj `gradlew.bat`. Raporty testów i APK znajdują się pod
`build/build_android_client` w katalogu głównym repozytorium.

Bez `keystore.properties` pakiet release pozostaje niepodpisany. Publikowany APK musi być podpisany
dotychczasowym kluczem aplikacji, inaczej nie zaktualizuje zainstalowanej wersji.
Nie używaj `-Pgenerate-debug-key` do publikacji. CI sprawdza niepodpisany release i nie potrzebuje klucza.

Do odizolowanego testu na emulatorze można dodać `-Pqa-package` przy `:client:assembleDebug`.
Tylko debug otrzyma wtedy identyfikator `eu.polanieonline.client.debug.qa`.
Nie wpływa to na aplikację release. Testowego APK podpisanego lokalnym kluczem nie publikuj.

## Zapis plików i kopie danych

Android 10 i nowszy zapisuje zrzuty do `Pictures/PolanieOnLine`, a eksport rozmów do
`Download/PolanieOnLine` przez MediaStore. Nie wymaga dostępu do wszystkich plików telefonu.
Android 5 do 9 zapisuje je w zewnętrznym katalogu aplikacji. Te pliki są usuwane podczas odinstalowania aplikacji.
Nie nadpisujemy istniejących plików w tym katalogu. Nieudane wpisy MediaStore są usuwane.

Kopia i przenoszenie danych obejmują tylko zwykłe ustawienia aplikacji. Szyfrowane hasła i dane sesji
WebView nie trafiają do kopii, ponieważ klucz Android Keystore nie jest przenoszony razem z nimi.
Po odtworzeniu aplikacji trzeba zalogować się ponownie.

## Sprawdzenie przed publikacją

Testy automatyczne sprawdzają adresy zaufanych serwerów, późno pojawiające się formularze,
pojedyncze wysłanie danych, timeout, reguły backupu, dekodowanie eksportów, zapis MediaStore
oraz usuwanie niekompletnych plików. Sprawdzają również limit oczekiwania bez automatycznego
ponowienia, anulowanie, zachowanie czasu po przebudowie panelu i odzyskiwanie widoków po awarii.
Nie zastępują sprawdzenia na urządzeniu.

Drewniane przyciski w parach mają równe krawędzie niezależnie od liczby wierszy napisu.
Ich wysokość uwzględnia dwa wiersze oraz rozmiar czcionki ustawiony w telefonie.
Wyrównanie i brak obcinania napisów sprawdzają również testy z rzeczywistymi metrykami czcionki.

Na odizolowanym emulatorze Androida 16.1 sprawdzono komunikat po 45 sekundach,
powrót do menu, celowo wywołaną awarię renderera oraz znikanie panelu ładowania
po pojawieniu się formularza logowania. Test nie obejmował logowania na konto gracza.

Przed wydaniem podpisanego APK sprawdź na telefonie:

1. Aktualizację istniejącej aplikacji bez utraty zapisanych kont.
2. Logowanie, wylogowanie i rejestrację nowego konta, także przy wolnym połączeniu.
3. Brak ponownego wysłania rejestracji po przeładowaniu strony.
4. Przekierowania ze starej domeny oraz otwieranie linków poza klientem.
5. Zrzut ekranu i eksport rozmowy, również przy braku miejsca.
6. Przycisk i gest Wstecz, pion, poziom, klawiaturę oraz wycięcie ekranu na Androidzie 15 i 16.
7. Wyłączenie sieci i ponowne połączenie.
8. Ustawienia i konieczność ponownego logowania po odtworzeniu kopii na innym urządzeniu.
9. Usunięcie jednego zapisanego loginu bez naruszenia pozostałych.
10. Odmowę i udzielenie zgody na powiadomienia, dodanie i usunięcie przypomnienia.
11. Zmieniony termin i odwołane wydarzenie, opóźnienie systemowe oraz brak sieci.
12. Podgląd raportu, kopiowanie i telefon bez skonfigurowanej aplikacji poczty.
13. Anulowanie ładowania, komunikat po 45 sekundach i brak ponownego wysłania danych.
14. Awarię procesu WebView i bezpieczny powrót do menu bez utraty zapisanych kont.

Obsługa gry nadal wymaga serwera i webclienta 1.42. Limit ośmiu postaci jest egzekwowany na serwerze.

## Dokumentacja platformy

[AGP 8.10 i zgodne wersje narzędzi](https://developer.android.com/build/releases/agp-8-10-0-release-notes)

[Zapis plików MediaStore](https://developer.android.com/training/data-storage/shared/media)

[Wyłączenie szyfrowanych ustawień z backupu](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)

[Zmiany zachowania Androida 16](https://developer.android.com/about/versions/16/behavior-changes-16)
