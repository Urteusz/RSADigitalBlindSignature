package pl.kryptografia.model;

import java.math.BigInteger;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Klasa realizująca podpisywanie z zaślepieniem (blind signature) w oparciu o RSA.
 */
public class BlindRSASignature {

    // Klucze RSA
    private static BigInteger n; // Moduł klucza publicznego
    private static BigInteger e; // Wykładnik publiczny
    private static BigInteger d; // Wykładnik prywatny
    private static BigInteger k; // Losowa zaślepka (blinding factor)

    // Getter i setter dla n
    public static BigInteger getN() {
        return n;
    }

    public static void setN(BigInteger n) {
        BlindRSASignature.n = n;
    }

    // Getter i setter dla e
    public static BigInteger getE() {
        return e;
    }

    public static void setE(BigInteger e) {
        BlindRSASignature.e = e;
    }

    // Getter i setter dla d
    public static BigInteger getD() {
        return d;
    }

    public static void setD(BigInteger d) {
        BlindRSASignature.d = d;
    }

    // Getter dla k
    public static BigInteger getK() {
        return k;
    }

    /**
     * Konstruktor generujący klucze RSA przy utworzeniu obiektu.
     */
    public BlindRSASignature() {
        generateKeys();
    }

    /**
     * Generuje klucze RSA (n, e, d) oraz zaślepkę k.
     */
    public static void generateKeys() {
        Random random = new Random();

        // Losowe liczby pierwsze
        BigInteger p = BigInteger.probablePrime(512, random);
        BigInteger q = BigInteger.probablePrime(512, random);
        n = p.multiply(q); // Moduł RSA

        // Obliczenie funkcji Eulera
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // Wybór wykładnika e
        e = BigInteger.valueOf(65537); // Częsty wybór -  liczba pierwsza → możliwa odwrotność mod φ(n), mało jedynek binarnie (2), odporne na znane ataki, standard w wielu bibliotekach
        while (!phi.gcd(e).equals(BigInteger.ONE)) {
            e = e.add(BigInteger.TWO); // Szukanie względnie pierwszej liczby
        }

        // Obliczanie odwrotności e modulo phi
        d = e.modInverse(phi);

        // Losowanie zaślepki k, względnie pierwszej z n
        do {
            k = new BigInteger(n.bitLength(), random);
        } while (!k.gcd(n).equals(BigInteger.ONE));
    }

    /**
     * Podpisuje dane przy użyciu metody blind signature.
     *
     * @param bytes Dane wejściowe do podpisania.
     * @return Podpisane dane jako bajty lub null w razie błędu.
     */
    public static byte[] signData(byte[] bytes) {
        if (bytes == null) {
            System.out.println("Błąd odczytu bajtów.");
            return null;
        }

        // Haszowanie danych SHA-256
        byte[] hash;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(bytes);
        } catch (Exception e) {
            System.out.println("Błąd SHA-256: " + e.getMessage());
            return null;
        }

        // Zamiana skrótu na liczbę całkowitą
        BigInteger m = new BigInteger(1, hash);

        // Losowanie nowej zaślepki k dla tego podpisu
        Random rand = new Random();
        do {
            k = new BigInteger(n.bitLength(), rand);
        } while (!k.gcd(n).equals(BigInteger.ONE));

        // Tworzenie zaślepionego komunikatu
        BigInteger blinded = m.multiply(k.modPow(e, n)).mod(n);

        // Podpisywanie zaślepionego komunikatu
        BigInteger blindSignature = blinded.modPow(d, n);

        // Usunięcie zaślepki (odsłonięcie podpisu)
        BigInteger rInv = k.modInverse(n);
        BigInteger signature = blindSignature.multiply(rInv).mod(n);

        return signature.toByteArray();
    }

    /**
     * Weryfikuje podpis danych przez porównanie podpisu z haszem.
     *
     * @param signatureBytes Podpis (zdekodowany z pliku).
     * @param signedFileBytes Oryginalna treść podpisywanego pliku.
     * @return true, jeśli podpis jest poprawny; false w przeciwnym razie.
     */
    public static boolean verifySignature(byte[] signatureBytes, byte[] signedFileBytes) {
        if (signatureBytes == null || signedFileBytes == null) {
            System.out.println("Błąd odczytu pliku.");
            return false;
        }

        // Obliczenie skrótu oryginalnych danych
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        byte[] hash = digest.digest(signedFileBytes);

        BigInteger m = new BigInteger(1, hash);                  // Hasz danych
        BigInteger signature = new BigInteger(1, signatureBytes); // Wczytany podpis

        BigInteger verified = signature.modPow(e, n); // Weryfikacja podpisu: s^e mod n

        return verified.equals(m); // Czy s^e mod n == hash?
    }
}
