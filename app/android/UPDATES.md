# Aktualizacje aplikacji Android

Wydanie `release` pobierane ze strony sprawdza `https://polanieonline.eu/client/android/update.json`.
Przy wejściu do menu sprawdza dostępność nie częściej niż co 6 godzin. Błędy sieci nie przerywają gry.
Ręczne sprawdzanie jest dostępne pod numerem wersji w menu oraz w ustawieniach.

Pobieranie wymaga wybrania przycisku przez gracza. APK trafia do prywatnej pamięci podręcznej.
Sprawdzane są rozmiar, SHA-256, nazwa pakietu, numer wersji, wymagany Android oraz zgodność certyfikatu z zainstalowaną aplikacją.
Pliki debug, starsze wersje, obce pakiety i podpisy są odrzucane.
Instalację oraz ostateczną weryfikację podpisu przeprowadza instalator Androida po potwierdzeniu przez użytkownika.
Nie ma instalacji w tle. Na Androidzie 8 i nowszym potrzebna jest zgoda na instalację z tej aplikacji.
Pobieranie i instalacja są blokowane, jeśli któraś sesja gry jest aktywna.
Aktualizacja zachowuje dane aplikacji, w tym zapisane konta. Nie odinstalowuj poprzedniej wersji.

## Pierwsze wydanie z updaterem

Pierwsze APK z tą funkcją trzeba zainstalować normalnie, gdyż wcześniejsze wersje nie mają updatera.
Bieżący kod aplikacji to `1042002`, a nazwa wersji gry pozostaje `1.42`.
Każda kolejna paczka do aktualizacji musi mieć wyższy `versionCode` w `client/build.gradle`.
Wszystkie aktualizacje muszą być podpisane tym samym kluczem produkcyjnym. Updater nie obsługuje rotacji klucza.
Przechowuj klucz poza repozytorium. Skrypt przygotowujący publikację nie wymaga podania hasła klucza.

## Przygotowanie publikacji w Windows

Zbuduj podpisany wariant `release` przez Eclipse/Ant lub Gradle, tak jak dotychczas.
Uruchom skrypt, wskazując gotowy APK i katalog narzędzi Android SDK:

```powershell
powershell -File app/android/ops/prepare-update.ps1 -Apk "build/build_android_client/outputs/apk/release/eu.polanieonline.client-1042002.apk" -BuildTools "C:/Users/Kamil Lewicki/AppData/Local/Android/Sdk/build-tools/36.1.0" -Notes "Opis zmian w tym wydaniu"
```

Domyślnie wynik trafia do `build/android-update-1042002`. Skrypt sprawdza podpis, odrzuca paczki debug i nie nadpisuje istniejącego katalogu.
Opcjonalny `-OutputDirectory` pozwala wskazać inny, nowy katalog.

1. Przetestuj APK na telefonie, również aktualizację bez odinstalowania wcześniejszej aplikacji.
2. Wgraj wygenerowany APK do publicznego katalogu strony obsługiwanego jako `/client/android/`.
3. Sprawdź, czy dokładny adres APK działa przez HTTPS bez przekierowania i zgadza się SHA-256.
4. Wgraj `update.json` jako ostatni plik. Zamień plik atomowo, aby aplikacja nie odczytała częściowego JSON.
5. Dla `update.json` ustaw `Cache-Control: no-cache`; plików APK o danym numerze wersji nie zastępuj innymi paczkami.

Nie twórz prawdziwego wpisu aktualizacji, zanim wydanie będzie gotowe do udostępnienia. Brak pliku (HTTP 404) nie wywołuje automatycznych komunikatów.
Aby wyłączyć publikację, wystarczy manifest `{"enabled":false}`. Nie wymuszaj aktualizacji przez zmianę hash istniejącego APK.
Publikacja nie korzysta z kont ani tokenów i nie wymaga zmiany webklienta.

## Google Play

Do Google Play buduj `:client:bundlePlayRelease` (`android.build=bundlePlayRelease` przy budowie Ant).
Ten wariant ma wyłączony updater APK, usuwa uprawnienie `REQUEST_INSTALL_PACKAGES` i dostawcę plików instalacyjnych.
Ręczna pozycja aktualizacji prowadzi do Google Play. Aktualizacje tego wydania dostarcza sklep.
Nie wysyłaj do Google Play zwykłego wariantu `release` z instalatorem APK.
Wariant sklepowy i strona powinny używać zgodnych kluczy podpisu, jeśli mają się wzajemnie aktualizować.
Jeśli Play App Signing używa innego klucza niż strona, nie traktuj tych paczek jako wymiennych.

## Test przed publikacją

Sprawdź brak sieci, brak manifestu, aktualną wersję, nowszą wersję, anulowanie pobierania, odmowę uprawnienia instalacji,
odmowę instalacji, błędną sumę kontrolną, obcy podpis i zachowanie kont po udanej aktualizacji.
Pełny test instalatora wymaga dwóch produkcyjnych APK o rosnących kodach i podpisanych tym samym kluczem.
