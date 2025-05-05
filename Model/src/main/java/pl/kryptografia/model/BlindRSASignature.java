package pl.kryptografia.model;

import java.math.BigInteger;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import java.security.MessageDigest;


public class BlindRSASignature {

    private static BigInteger n; // Public key (modulus)
    private static BigInteger e; // Public key (exponent)
    private static BigInteger d; // Private key (exponent)
    private static BigInteger k; // Zaslepka k

    public static BigInteger getN() {
        return n;
    }
    public static void setN(BigInteger n) {
        BlindRSASignature.n = n;
    }

    public static BigInteger getE() {
        return e;
    }
    public static void setE(BigInteger e) {
        BlindRSASignature.e = e;
    }

    public static BigInteger getD() {
        return d;
    }
    public static void setD(BigInteger d) {
        BlindRSASignature.d = d;
    }

    public static BigInteger getK() {
        return k;
    }

    public BlindRSASignature() {
        generateKeys();
    }

    // 0. Generowanie kluczy RSA
    public static void generateKeys() {
        BigInteger p = BigInteger.probablePrime(512, new Random());
        BigInteger q = BigInteger.probablePrime(512, new Random());
        n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        e = BigInteger.valueOf(65537);
        while (!phi.gcd(e).equals(BigInteger.ONE)) {
            e = e.add(BigInteger.TWO);
        }

        d = e.modInverse(phi);

        Random rand = new Random();
        do {
            k = new BigInteger(n.bitLength(), rand);
        } while (!k.gcd(n).equals(BigInteger.ONE));
    }

    // 1. Podpisanie pliku
    public static byte[] signData(byte[] bytes) {
        if (bytes == null) {
            System.out.println("Błąd odczytu bajtów.");
            return null;
        }

        byte[] hash;
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(bytes);
        }
        catch (Exception e){
            System.out.println("Błąd SHA-256: " + e.getMessage());
            return null;
        }

        BigInteger m = new BigInteger(1, hash);

        Random rand = new Random();
        do {
            k = new BigInteger(n.bitLength(), rand);
        } while (!k.gcd(n).equals(BigInteger.ONE));


        BigInteger blinded = m.multiply(k.modPow(e, n)).mod(n);
        BigInteger blindSignature = blinded.modPow(d, n);
        BigInteger rInv = k.modInverse(n);
        BigInteger signature = blindSignature.multiply(rInv).mod(n);

        return signature.toByteArray();
    }

    // 2. Weryfikacja podpisu (fragment 100 bajtów)
    public static boolean verifySignature(byte[] signatureBytes, byte[] signedFileBytes) {

        if (signatureBytes == null || signedFileBytes == null) {
            System.out.println("Błąd odczytu pliku.");
            return false;
        }


//        SHA1 sha1 = new SHA1();
//        byte[] hash = sha1.digest(signedFileBytes);
//

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        byte[] hash = digest.digest(signedFileBytes);


        BigInteger m = new BigInteger(1, hash);



        BigInteger signature = new BigInteger(1, signatureBytes);

        BigInteger verified = signature.modPow(e, n);

        if (verified.equals(m)) {
            return true;
        } else {
            return false;
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
