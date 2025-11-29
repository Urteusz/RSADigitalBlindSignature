package pl.kryptografia.model;

import java.math.BigInteger;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Klasa realizująca podpisywanie z zaślepieniem (blind signature) w oparciu o RSA.
 * Uwaga: nadal brak paddingu (RSA-PSS) – kod ma charakter edukacyjny.
 */
public class BlindRSASignature {

    // Klucze RSA
    private static BigInteger n; // Moduł klucza publicznego
    private static BigInteger e; // Wykładnik publiczny
    private static BigInteger d; // Wykładnik prywatny
    private static BigInteger k; // Ostatnio użyta zaślepka (blinding factor)

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
     * Generuje klucze RSA (n, e, d) oraz inicjalną zaślepkę k.
     */
    public static void generateKeys() {
        SecureRandom random = new SecureRandom();

        BigInteger p = BigInteger.probablePrime(1024, random);
        BigInteger q = BigInteger.probablePrime(1024, random);
        n = p.multiply(q);

        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        e = BigInteger.valueOf(65537); // Standardowy wybór
        while (!phi.gcd(e).equals(BigInteger.ONE)) {
            e = e.add(BigInteger.TWO);
        }
        d = e.modInverse(phi);

        // Inicjalna zaślepka
        k = generateBlindingFactor(random);
    }

    /**
     * Podpisuje dane przy użyciu metody blind signature.
     * Generuje nowy blinding factor k dla każdego podpisu.
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

        BigInteger m = new BigInteger(1, hash);

        // Nowa zaślepka dla tego podpisu
        k = generateBlindingFactor(new SecureRandom());

        // m' = m * k^e mod n
        BigInteger blinded = m.multiply(k.modPow(e, n)).mod(n);
        // s' = (m')^d mod n
        BigInteger blindSignature = blinded.modPow(d, n);
        // s = s' * k^{-1} mod n
        BigInteger rInv = k.modInverse(n);
        BigInteger signature = blindSignature.multiply(rInv).mod(n);

        return signature.toByteArray();
    }

    /**
     * Weryfikuje podpis danych przez porównanie podpisu z haszem.
     */
    public static boolean verifySignature(byte[] signatureBytes, byte[] signedFileBytes) {
        if (signatureBytes == null || signedFileBytes == null) {
            System.out.println("Błąd odczytu pliku.");
            return false;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        byte[] hash = digest.digest(signedFileBytes);
        BigInteger m = new BigInteger(1, hash);
        BigInteger signature = new BigInteger(1, signatureBytes);

        // s^e mod n powinno dać m
        BigInteger verified = signature.modPow(e, n);
        return verified.equals(m);
    }

    /**
     * Generuje czynnik zaślepiający k: 0 < k < n oraz gcd(k, n) = 1.
     */
    private static BigInteger generateBlindingFactor(SecureRandom random) {
        BigInteger r;
        do {
            do {
                r = new BigInteger(n.bitLength(), random);
            } while (r.compareTo(n) >= 0 || r.equals(BigInteger.ZERO));
        } while (!r.gcd(n).equals(BigInteger.ONE));
        return r;
    }
}
