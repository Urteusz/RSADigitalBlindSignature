# Blind RSA Signature (Zaślepiony Podpis RSA)

![Java](https://img.shields.io/badge/Java-23-orange) ![Maven](https://img.shields.io/badge/Build-Maven-blue) ![JavaFX](https://img.shields.io/badge/UI-JavaFX-1f8acb) ![License](https://img.shields.io/badge/License-GPLv3-green) ![Status](https://img.shields.io/badge/CI-pending-lightgrey) ![Coverage](https://img.shields.io/badge/Coverage-soon-lightgrey)

Edukacyjna aplikacja demonstrująca mechanizm Blind RSA Signature – uzyskanie podpisu na wiadomość bez ujawniania jej treści sygnatariuszowi, z zachowaniem możliwości publicznej weryfikacji. Obecnie klucz RSA został podniesiony do ~2048 bit (2×1024-bit prime) dla lepszego przykładu dydaktycznego.

> Uwaga: Implementacja nadal używa „surowego” RSA bez paddingu (brak RSA-PSS). Kod ma charakter **edukacyjny** i nie jest przeznaczony do produkcji.

---
## Spis treści
1. Cel i streszczenie  
2. Teoria – czym jest blind signature  
3. Algorytm – kroki matematyczne  
4. Architektura projektu  
5. Uruchomienie (Windows / Maven / JavaFX)  
6. Przykładowe użycie API (kod)  
7. Testy (stan / co dodać)  
8. Bezpieczeństwo i ograniczenia  
9. Roadmapa / Pomysły rozwoju  
10. Sekcja portfolio – wyróżniki i checklist  
11. Licencja  
12. FAQ  

---
## 1. Cel i streszczenie
Celem projektu jest pokazanie pełnego przepływu blind signature: generacja kluczy, haszowanie wiadomości, zaślepienie, podpis zasłoniętej wartości, odsłonięcie (unblinding) i finalna weryfikacja. Aplikacja oferuje GUI (JavaFX) oraz prosty model kryptograficzny.

## 2. Teoria – czym jest Blind Signature?
Blind signature (podpis z zaślepieniem) pozwala otrzymać podpis na wiadomość tak, aby sygnatariusz jej nie znał w momencie podpisywania. Po odsłonięciu podpis jest ważny i każdy może go zweryfikować. Typowe zastosowania: e‑głosowanie, e‑cash, anonimowe tokeny dostępu, systemy prywatności.

## 3. Algorytm – kroki matematyczne
Założenia: klucz publiczny (n, e), prywatny (n, d).

1. Hash: M → m = SHA256(M) (liczba dodatnia).  
2. Losowy czynnik k: 0 < k < n, gcd(k, n) = 1.  
3. Zaślepienie: m' = m * k^e mod n.  
4. Podpisanie (sygnatariusz widzi jedynie m'): s' = (m')^d mod n.  
5. Odsłonięcie: k^{-1} mod n; s = s' * k^{-1} mod n.  
6. Weryfikacja: sprawdź czy s^e mod n == m.  

Schemat:
```
Plain → SHA-256 → m --blind(k)--> m' --sign(d)--> s' --unblind(k^{-1})--> s --verify(e)--> m
```
Plik kluczowy: `Model/src/main/java/pl/kryptografia/model/BlindRSASignature.java`.

## 4. Architektura projektu
Multi-module Maven:
- `Model/` – logika RSA + blind signature (BigInteger, SecureRandom, hashowanie).  
- `View/` – JavaFX GUI (`MainApplication`, `MainController`, FXML).  
- `docs/` – diagram PlantUML, plan benchmarków, miejsce na zrzuty ekranu.  
- Pliki wsparcia: `SECURITY.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`.

Przepływ: Użytkownik → GUI → Wywołania statycznych metod modelu → Wynik (podpis / weryfikacja) → Prezentacja w UI.

## 5. Uruchomienie (Windows / PowerShell)
Wymagania: JDK 23, Maven 3.8+, internet do pobrania zależności. 

```powershell
# Budowa wszystkich modułów
mvn clean install

# Uruchomienie GUI (JavaFX)
mvn -f View/pom.xml javafx:run

# Utworzenie obrazu jlink (zależne od pluginu)
mvn -f View/pom.xml javafx:jlink
```
Ręczne uruchomienie JAR (jeśli powstanie):
```powershell
java -jar View\target\view-1.0-SNAPSHOT.jar
```
Jeśli IDE zgłasza brak JavaFX na module-path, dodaj `--module-path` i `--add-modules javafx.controls,javafx.fxml` (przy lokalnym SDK JavaFX). 

## 6. Przykładowe użycie API
Minimalny kod (bez GUI):
```java
import pl.kryptografia.model.BlindRSASignature;
import java.nio.charset.StandardCharsets;

public class Demo {
    public static void main(String[] args) {
        BlindRSASignature rsa = new BlindRSASignature(); // generuje klucze ~2048 bit
        byte[] data = "Wiadomosc testowa".getBytes(StandardCharsets.UTF_8);
        byte[] sig = BlindRSASignature.signData(data);
        boolean ok = BlindRSASignature.verifySignature(sig, data);
        System.out.println("Poprawność: " + ok);
    }
}
```

## 7. Testy – stan i propozycje
Aktualnie: Podstawowe testy JUnit znajdują się w module `Model` (`BlindRSASignatureTest`).

Do dodania:
- Test modyfikacji podpisu (mutacja bajtu → verify = false).  
- Testy dla różnych długości wiadomości (małe / większe kilka KB).  
- Property-based (jqwik): `verifySignature(signData(m), m)` zawsze true.  
- Test statystyczny różnorodności k (losowość).  
- Test odrzucenia `null` / pustych tablic (konsekwentna sygnalizacja wyjątkiem lub wynikiem).  

Uruchomienie testów (po zainstalowaniu Mavena):
```powershell
mvn -pl Model test
mvn verify  # generuje raport JaCoCo
```
Raport pokrycia: `Model/target/site/jacoco/index.html`.

## 8. Bezpieczeństwo i ograniczenia
| Obszar | Stan | Konsekwencje |
|--------|------|--------------|
| Padding | BRAK (raw RSA) | Podatne na znane ataki na podpisy / strukturalne manipulacje |
| Rozmiar klucza | ~2048 bit | Edukacyjnie ok, produkcja często ≥3072 zależnie od polityk |
| Zarządzanie kluczem | Brak formatu PEM / KeyStore | Utrudniona bezpieczna dystrybucja kluczy |
| Role | Połączone w jednym procesie | Brak izolacji klienta i sygnatariusza |
| Side-channel | Brak zabezpieczeń timing/power | Nieodporne w środowisku zagrożonym |
| Losowość | SecureRandom, brak testów entropii | Edukacyjnie wystarczające, brak dowodu jakości |
| Walidacja wejść | `null` → komunikat tylko w konsoli | Nierówne API dla produkcji (preferowane wyjątki) |

**Disclaimer:** Użycie tylko do demonstracji. Do realnych wdrożeń: RSA-PSS, dłuższe klucze (≥3072), KeyStore, separacja ról, audyt bezpieczeństwa.

## 9. Roadmapa / Pomysły rozwoju
- Dodanie RSA-PSS (padding) i uchwycenie wyjątków konsekwentnie.  
- Eksport/import kluczy w PEM (BASE64 + nagłówki).  
- Oddzielenie ról: osobny moduł „signer” z REST API (np. Spring Boot).  
- Benchmarki JMH (czas generacji, podpisu, blindingu).  
- Diagramy dodatkowe: komponent, przepływ danych, deployment.  
- Testy property-based + testy mutacyjne (PIT).  
- Tryb CLI: polecenia `generate`, `sign`, `verify`.  
- Internationalization (PL / EN wybór w GUI).  
- Integracja z GitHub Actions (badge CI, coverage).  

## 10. Sekcja portfolio – wyróżniki & checklist
Wyróżniki:
- Implementacja mniej typowego mechanizmu blind signature.  
- Modularna architektura (Model + View).  
- JavaFX + ControlsFX UI.  
- Dokumentacja (README, SECURITY, CHANGELOG, CONTRIBUTING, CODE OF CONDUCT).  
- Diagram PlantUML sekwencji.  

Checklist przed publikacją:
- [ ] Dodaj plik `LICENSE` (jeśli nie ma – GPLv3/MIT/Apache 2.0).  
- [ ] Dodaj mutacyjny test podpisu.  
- [ ] Wstaw zrzuty ekranu do `docs/screenshots` i podlinkuj.  
- [ ] Odpal CI → uzupełnij badge statusu + coverage.  
- [ ] Rozbuduj README_EN (pełna wersja).  
- [ ] Ewentualnie refaktoryzuj klasę na instancyjną (bez statycznych pól).  
- [ ] Dodaj eksport PEM.  

## 11. Licencja
Deklarowana w POM: GPLv3. Jeśli chcesz szerszej adopcji i prostszego użycia w portfolio – rozważ MIT lub Apache 2.0 (mniej restrykcyjne niż copyleft GPL).

## 12. FAQ
**Dlaczego brak RSA-PSS?**  
Projekt skupia się na pokazaniu mechaniki blindowania – padding to kolejny krok w bezpieczeństwie.

**Czy klucz ~2048 bit wystarczy?**  
Dla edukacji tak. W produkcji zwykle ≥2048, a dla długiej perspektywy ≥3072 / 4096.

**Czy mogę dodać ECDSA / Schnorr?**  
Tak – blind signature ma warianty dla Schnorra; można rozbudować moduł Model.

**Dlaczego metody i klucze są statyczne?**  
Uproszczenie dla GUI. Refaktoryzacja do instancji poprawi testowalność i możliwość wielu równoległych kluczy.
