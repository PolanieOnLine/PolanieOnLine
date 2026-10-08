# Lokalna diagnostyka webklienta

Panel: **Ustawienia → Diagnostyka (admin)**. Jest dostępny dla postaci z uprawnieniami administratora (`adminlevel > 600`, tak jak `Player.isAdmin()`).

Domyślnie wyłączony. Testy dotyczą tylko bieżącej sesji przeglądarki/webklienta; nie wysyłają akcji do serwera ani nie zapisują preferencji. Odświeżenie, wylogowanie, zmiana postaci lub utrata uprawnień wyłącza testy. Zamknięcie okna ustawień pozwala kontynuować pomiar w grze.

## Porównanie na telefonie

1. Włącz diagnostykę. Pozostaw wszystkie trzy eksperymenty wyłączone.
2. Zamknij ustawienia i przez 10–15 sekund poruszaj się po tej samej mapie z zamkniętym plecakiem.
3. Otwórz ustawienia, opisz próbkę i zapisz ją do raportu. Zeruj pomiar.
4. Powtórz z otwartym, pełnym plecakiem.
5. Powtórz z otwartym plecakiem, zmieniając tylko jeden eksperyment naraz:
   - zatrzymanie animacji ikon (animacje świata pozostają bez zmian),
   - uproszczenie slotów (bez masek, gradientowych ramek i cieni),
   - ukrycie obwódek rzadkości w ekwipunku (poświata przedmiotów na ziemi pozostaje bez zmian).
6. Naciśnij „Kopiuj raport”. Po potwierdzeniu skopiowania wklej tekst do wiadomości; dopisz model telefonu i czy test był w Chrome czy APK. Kopiowanie ma zapasowy mechanizm dla WebView bez Clipboard API. Gdy oba mechanizmy zostaną zablokowane, panel wyświetli informację o niepowodzeniu; pozostaje też ręczne zaznaczanie raportu.
7. Użyj „Wyłącz testy i przywróć wygląd”.

Każda zmiana przełącznika zeruje pomiar. W raporcie można zachować kilka nazwanych próbek. Po odświeżeniu strony raport znika — skopiuj go wcześniej.

## Co mierzymy

- FPS renderera oraz średni, maksymalny i 95. percentyl odstępu między faktycznymi wywołaniami rysowania widoku gry.
- Liczbę odstępów powyżej 50 ms.
- Średni czas wykonywania kodu rysowania (bez asynchronicznego malowania/kompozycji GPU).
- Liczbę widocznych przedmiotów i ikon z animowanymi sprite'ami; liczba animowanych obejmuje także ikony zatrzymane przełącznikiem.

Przechowujemy maksymalnie 300 ostatnich odstępów. Okresy ukrycia aplikacji/karty nie są liczone jako zacięcia. Te dane pomagają wskazać kierunek optymalizacji; nie zastępują profilu CPU/GPU przeglądarki.

Nie ma potrzeby przebudowy APK: panel jest częścią webklienta. Wymaga jednak wgrania nowego JS i CSS oraz odświeżenia pamięci podręcznej starej karty.
