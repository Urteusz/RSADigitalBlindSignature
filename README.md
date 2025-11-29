# RSADigitalBlindSignature

Opis po polsku projektu demonstracyjnego implementującego koncepcję "Blind RSA Signature" (zaślepionego podpisu RSA) w formie prostej aplikacji JavaFX.

## Krótkie wprowadzenie
Ta aplikacja to edukacyjna implementacja procesu Blind RSA Signature. Pozwala na zrozumienie, jak jedna strona (sygnatariusz) może podpisać wiadomość, nie wiedząc jaka to wiadomość — jednocześnie umożliwiając weryfikację podpisu przez stronę weryfikującą. Implementacja zawiera moduł modelu (logika kryptograficzna) oraz moduł widoku (interfejs JavaFX).

Uwaga: to narzędzie ma charakter dydaktyczny i nie powinno być używane w środowisku produkcyjnym bez gruntownej rewizji bezpieczeństwa.

## Główne funkcje
- Generowanie kluczy RSA (publiczny/prywatny) — demonstracyjnie w module Model.
- Zaslepianie wiadomości (blinding) — klient zaslepia wiadomość zanim zostanie podpisana.
- Podpisywanie zaslepionej wiadomości — serwer podpisuje zaslepioną wiadomość prywatnym kluczem RSA.
- Odslepianie podpisu (unblinding) — klient usuwa zaslepienie, by otrzymać ważny podpis na oryginalnej wiadomości.
- Weryfikacja podpisu — sprawdzenie, czy podpis jest zgodny z kluczem publicznym.
- Prosty GUI (JavaFX) do krokowego pokazania procesu.

## Krótka teoria (co to jest Blind RSA Signature)
- Blind signature (zaślepiony podpis) to technika kryptograficzna pozwalająca na otrzymanie podpisu na wiadomość bez ujawniania treści sygnatariuszowi.
- Typowy przebieg:
  1. Klient generuje wartość losową (blinding factor) i używa jej do "zaslepienia" wiadomości.
  2. Klient wysyła zaslepioną wiadomość do sygnatariusza.
  3. Sygnatariusz podpisuje zaslepioną wiadomość swoim prywatnym kluczem i zwraca podpisaną, zaslepioną wartość.
  4. Klient usuwa efekt zaslepienia (unblinding) i otrzymuje podpis na oryginalnej wiadomości.
  5. Każdy może zweryfikować podpis korzystając z klucza publicznego sygnatariusza.

- W implementacji użyto podstawowego schematu RSA (modularnej arytmetyki). W praktyce należy używać sprawdzonych bibliotek i protokołów oraz odpowiednich schematów paddingu (np. RSA-PSS) i bezpiecznego zarządzania losowością.

## Struktura projektu
- `Model/` — moduł zawierający logikę kryptograficzną (generowanie kluczy, blinding, signing, unblinding, verify).
- `View/` — moduł zawierający aplikację JavaFX i kontrolery GUI (np. `MainApplication`, `MainController`, plik FXML `main-view.fxml`).
- `pom.xml` — główny plik Maven (projekt multi-modułowy). Każdy moduł ma swój `pom.xml`.

## Jak uruchomić (sposoby)
Poniżej kilka opcji — wybierz tę, która pasuje do Twojego środowiska.

Wymagania:
- Java 11+ (preferowane Java 17 lub wyższe) z obsługą modułów.
- JavaFX SDK (jeśli nie jest dostarczony przez konfigurację Maven/IDE).
- Maven (do budowania projektu).

1) Najprościej — uruchom z IDE (IntelliJ IDEA):
   - Otwórz projekt w IntelliJ jako projekt Maven.
   - W module `View` zlokalizuj klasę `pl.kryptografia.view.MainApplication` i uruchom ją jako aplikację JavaFX.

2) Za pomocą Mavena (jeśli `javafx-maven-plugin` jest skonfigurowany w `View/pom.xml`):
   - W PowerShell w katalogu głównym projektu (lub bezpośrednio w `View`):

```powershell
mvn -f View/pom.xml javafx:run
```

3) Budowa pakietu i uruchomienie (ogólny sposób):
   - Zbuduj moduły:

```powershell
mvn -pl View -am clean package
```

   - Jeśli powstał plik jar uruchomieniowy, możesz spróbować uruchomić go z odpowiednim ustawieniem module-path/--add-modules dla JavaFX. Przykład (wymaga JavaFX SDK i dopasowania ścieżek):

```powershell
java --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -jar View\target\your-app.jar
```

   - Zamiast ręcznych komend polecam uruchomienie z IDE, które automatycznie doda JavaFX do classpath/modulepath.

Uwaga: dokładne komendy zależą od konfiguracji `pom.xml` i wersji JavaFX — jeśli napotkasz problemy, podaj treść `pom.xml` modułu `View` a pomogę dostosować polecenia.

## Przykładowy przebieg w aplikacji (UI)
1. Wygeneruj parę kluczy RSA (publiczny/prywatny).
2. Wprowadź lub wybierz wiadomość do podpisu.
3. Zaslepnij wiadomość — aplikacja wygeneruje blinding factor i pokaże zaslepioną wartość.
4. Podpisz zaslepioną wartość (symulacja serwera) — aplikacja użyje prywatnego klucza do podpisu zaslepionej wartości.
5. Odslepnij podpis — aplikacja usunie zaslepienie i pokaże wynikowy podpis dla oryginalnej wiadomości.
6. Zweryfikuj podpis za pomocą klucza publicznego.

## Bezpieczeństwo i ograniczenia
- To narzędzie jest demonstracyjne. Implementacja może używać uproszczeń edukacyjnych (np. brak odpowiedniego paddingu, prosty generator losowości) — nie używaj tego do zabezpieczania rzeczywistych danych.
- Dla produkcyjnych zastosowań używaj sprawdzonych bibliotek kryptograficznych (BouncyCastle, Java Cryptography Architecture z właściwym paddingiem) oraz protokołów opartych na aktualnych standardach.

## Dalsze kroki / Rozszerzenia
- Zaimplementować RSA-PSS lub inny bezpieczny schemat podpisu.
- Dodać obsługę kluczy w plikach (eksport/import PEM).
- Dodać testy jednostkowe dla modułu `Model` (generowanie kluczy, blinding/unblinding, verify).
- Symulacja rzeczywistej komunikacji klient-serwer.

## Kontakt / Licencja
Projekt demonstracyjny — kod źródłowy możesz swobodnie modyfikować. Jeśli potrzebujesz pomocy z uruchomieniem lub chcesz dodać funkcjonalność, opisz problem w issue lub skontaktuj się bezpośrednio z autorem projektu.

---

Jeżeli chcesz, mogę:
- dopisać szczegółowe instrukcje uruchomienia dopasowane do dokładnej zawartości `pom.xml`,
- dodać przykładowe testy jednostkowe dla `Model`,
- albo wygenerować skrócony README w języku angielskim.

Powiedz, którą z tych czynności wykonam dalej.
