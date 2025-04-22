package pl.kryptografia.model;

import java.math.BigInteger;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class BlindRSASignatureWithMenu {

    private static BigInteger n; // Public key (modulus)
    private static BigInteger e; // Public key (exponent)
    private static BigInteger d; // Private key (exponent)

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Menu ---");
            System.out.println("0. Wygeneruj klucze RSA");
            System.out.println("1. Odczytaj plik, podpisz i zapisz podpis");
            System.out.println("2. Weryfikacja podpisu");
            System.out.println("3. Zakończ");
            System.out.print("Wybierz opcję: ");
            int choice = scanner.nextInt();
            scanner.nextLine();  // czyszczenie bufora

            switch (choice) {
                case 0:
                    generateKeys();
                    break;

                case 1:
                    System.out.print("Podaj nazwę pliku do podpisania: ");
                    String inputFileName = scanner.nextLine();
                    System.out.print("Podaj nazwę pliku wyjściowego (gdzie zapisany będzie podpis): ");
                    String outputFileName = scanner.nextLine();
                    signFile(inputFileName, outputFileName);
                    break;

                case 2:
                    System.out.print("Podaj nazwę pliku z podpisem: ");
                    String signatureFileName = scanner.nextLine();
                    System.out.print("Podaj nazwę pliku, który został podpisany: ");
                    String signedFileName = scanner.nextLine();
                    verifySignature(signatureFileName, signedFileName);
                    break;

                case 3:
                    System.out.println("Zakończono program.");
                    scanner.close();
                    return;

                default:
                    System.out.println("Niepoprawna opcja. Spróbuj ponownie.");
            }
        }
    }

    // 0. Generowanie kluczy RSA
    private static void generateKeys() {
        BigInteger p = BigInteger.probablePrime(512, new Random());
        BigInteger q = BigInteger.probablePrime(512, new Random());
        this.n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        this.e = BigInteger.valueOf(65537);
        while (!phi.gcd(e).equals(BigInteger.ONE)) {
            e = e.add(BigInteger.TWO);
        }

        d = e.modInverse(phi);

        writeFile("publicKey_n.txt", n.toByteArray());
        writeFile("publicKey_e.txt", e.toByteArray());
        writeFile("privateKey_d.txt", d.toByteArray());

        System.out.println("Klucze zostały wygenerowane i zapisane.");
    }

    // 1. Podpisanie pliku (fragment 100 bajtów)
    private static void signFile(String inputFileName, String outputFileName) {
        byte[] fileBytes = readFile(inputFileName);
        if (fileBytes == null) {
            System.out.println("Błąd odczytu pliku.");
            return;
        }

        // Wczytaj klucze z plików
        n = new BigInteger(readFile("publicKey_n.txt"));
        e = new BigInteger(readFile("publicKey_e.txt"));
        d = new BigInteger(readFile("privateKey_d.txt"));

        SHA1 sha1 = new SHA1();
        byte[] hash = sha1.digest(fileBytes); // podpisujemy skrót SHA-1
        BigInteger m = new BigInteger(1, hash);


        // Współczynnik zaślepiający
        BigInteger r;
        Random rand = new Random();
        do {
            r = new BigInteger(n.bitLength(), rand);
        } while (!r.gcd(n).equals(BigInteger.ONE));

        BigInteger blinded = m.multiply(r.modPow(e, n)).mod(n);
        BigInteger blindSignature = blinded.modPow(d, n);
        BigInteger rInv = r.modInverse(n);
        BigInteger signature = blindSignature.multiply(rInv).mod(n);

        writeFile(outputFileName, signature.toByteArray());
        System.out.println("Podpis zapisano do pliku " + outputFileName);
    }

    // 2. Weryfikacja podpisu (fragment 100 bajtów)
    private static void verifySignature(String signatureFileName, String signedFileName) {
        byte[] signatureBytes = readFile(signatureFileName);
        byte[] signedFileBytes = readFile(signedFileName);

        if (signatureBytes == null || signedFileBytes == null) {
            System.out.println("Błąd odczytu pliku.");
            return;
        }

        n = new BigInteger(readFile("publicKey_n.txt"));
        e = new BigInteger(readFile("publicKey_e.txt"));

        SHA1 sha1 = new SHA1();
        byte[] hash = sha1.digest(signedFileBytes);
        BigInteger m = new BigInteger(1, hash);

        BigInteger signature = new BigInteger(1, signatureBytes);

        BigInteger verified = signature.modPow(e, n);

        if (verified.equals(m)) {
            System.out.println("✅ Podpis jest poprawny.");
        } else {
            System.out.println("❌ Podpis jest niepoprawny.");
        }
    }

    // Pomocnicze: Odczyt pliku binarnego
    private static byte[] readFile(String fileName) {
        try {
            File file = new File(fileName);
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(bytes);
            fis.close();
            return bytes;
        } catch (IOException e) {
            System.out.println("Błąd odczytu pliku: " + e.getMessage());
            return null;
        }
    }

    // Pomocnicze: Zapis pliku binarnego
    private static void writeFile(String fileName, byte[] content) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(content);
            fos.close();
        } catch (IOException e) {
            System.out.println("Błąd zapisu do pliku: " + e.getMessage());
        }
    }
}
