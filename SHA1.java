import java.nio.ByteBuffer;

public class SHA1 {

    public byte[] digest(byte[] input) {
        // 1. Padding
        byte[] padded = padMessage(input);

        // 2. Inicjalizacja rejestrów
        int h0 = 0x67452301;
        int h1 = 0xEFCDAB89;
        int h2 = 0x98BADCFE;
        int h3 = 0x10325476;
        int h4 = 0xC3D2E1F0;

        // 3. Przetwarzanie bloków
        int numBlocks = padded.length / 64;

        for (int i = 0; i < numBlocks; i++) {
            int[] w = new int[80];

            // 3.1 Wczytaj 16 słów z bloku (po 4 bajty)
            for (int j = 0; j < 16; j++) {
                int index = i * 64 + j * 4;
                w[j] = ((padded[index] & 0xFF) << 24) |
                        ((padded[index + 1] & 0xFF) << 16) |
                        ((padded[index + 2] & 0xFF) << 8) |
                        (padded[index + 3] & 0xFF);
            }

            // 3.2 Rozszerz do 80 słów
            for (int j = 16; j < 80; j++) {
                w[j] = leftRotate(w[j - 3] ^ w[j - 8] ^ w[j - 14] ^ w[j - 16], 1);
            }

            // 3.3 Rejestry robocze
            int a = h0;
            int b = h1;
            int c = h2;
            int d = h3;
            int e = h4;

            // 3.4 80 rund SHA-1
            for (int j = 0; j < 80; j++) {
                int f, k;

                if (j < 20) {
                    f = (b & c) | ((~b) & d);
                    k = 0x5A827999;
                } else if (j < 40) {
                    f = b ^ c ^ d;
                    k = 0x6ED9EBA1;
                } else if (j < 60) {
                    f = (b & c) | (b & d) | (c & d);
                    k = 0x8F1BBCDC;
                } else {
                    f = b ^ c ^ d;
                    k = 0xCA62C1D6;
                }

                int temp = leftRotate(a, 5) + f + e + k + w[j];
                e = d;
                d = c;
                c = leftRotate(b, 30);
                b = a;
                a = temp;
            }

            // 3.5 Dodaj do rejestrów głównych
            h0 += a;
            h1 += b;
            h2 += c;
            h3 += d;
            h4 += e;
        }

        // 4. Zbuduj końcowy skrót
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.putInt(h0).putInt(h1).putInt(h2).putInt(h3).putInt(h4);
        return buffer.array();
    }

    private byte[] padMessage(byte[] input) {
        int originalLength = input.length;
        long bitLength = (long) originalLength * 8;

        int paddingLength = 64 - ((originalLength + 9) % 64);
        int totalLength = originalLength + 1 + paddingLength + 8;

        byte[] padded = new byte[totalLength];
        System.arraycopy(input, 0, padded, 0, originalLength);

        padded[originalLength] = (byte) 0x80;

        for (int i = 0; i < 8; i++) {
            padded[totalLength - 1 - i] = (byte) ((bitLength >>> (8 * i)) & 0xFF);
        }

        return padded;
    }

    private int leftRotate(int value, int bits) {
        return (value << bits) | (value >>> (32 - bits));
    }

    // Testowanie
    public static void main(String[] args) {
        SHA1 sha1 = new SHA1();
        byte[] hash = sha1.digest("".getBytes());

        // Wypisz hash jako hex
        for (byte b : hash) {
            System.out.printf("%02x", b);
        }
        System.out.println();
    }
}
