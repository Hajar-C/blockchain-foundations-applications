import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

public class LamportSignature {

    // Lamport private key: 2 x 256 random 32-byte values
    // Lamport public key:  2 x 256 hashes of the private values
    static class KeyPair {
        final byte[][][] priv; // [2][256][32]
        final byte[][][] pub;  // [2][256][32]

        KeyPair(byte[][][] priv, byte[][][] pub) {
            this.priv = priv;
            this.pub = pub;
        }
    }

    // Signature: 256 selected 32-byte blocks (based on hash bits)
    static class SignatureObj {
        final byte[][] sig; // [256][32]
        SignatureObj(byte[][] sig) { this.sig = sig; }
    }

    
    // Utilities
    private static byte[] sha256(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
    }

    private static int getBit(byte[] hash, int i) {
        // i in [0..255], big-endian bit order inside each byte
        int byteIndex = i / 8;
        int bitIndex = 7 - (i % 8);
        return (hash[byteIndex] >> bitIndex) & 1;
    }


    // Lamport: KeyGen, Sign, Verify
    public static KeyPair keyGen() throws Exception {
        SecureRandom rnd = new SecureRandom();

        byte[][][] priv = new byte[2][256][32];
        byte[][][] pub  = new byte[2][256][32];

        for (int b = 0; b < 2; b++) {
            for (int i = 0; i < 256; i++) {
                rnd.nextBytes(priv[b][i]);           // random 32 bytes
                pub[b][i] = sha256(priv[b][i]);      // hash => public
            }
        }
        return new KeyPair(priv, pub);
    }

    public static SignatureObj sign(byte[] message, byte[][][] priv) throws Exception {
        byte[] h = sha256(message);

        byte[][] sig = new byte[256][32];
        for (int i = 0; i < 256; i++) {
            int bit = getBit(h, i);                  // 0 or 1
            sig[i] = Arrays.copyOf(priv[bit][i], 32);
        }
        return new SignatureObj(sig);
    }

    public static boolean verify(byte[] message, SignatureObj signature, byte[][][] pub) throws Exception {
        byte[] h = sha256(message);

        for (int i = 0; i < 256; i++) {
            int bit = getBit(h, i);

            byte[] hashedSigPart = sha256(signature.sig[i]);
            if (!Arrays.equals(hashedSigPart, pub[bit][i])) {
                return false;
            }
        }
        return true;
    }


    // Test
    public static void main(String[] args) throws Exception {
        KeyPair kp = keyGen();

        String msg = "Hello Lamport";
        byte[] message = msg.getBytes(StandardCharsets.UTF_8);

        SignatureObj sig = sign(message, kp.priv);
        boolean ok = verify(message, sig, kp.pub);

        System.out.println("Lamport signature valid: " + ok);

        // Tampered message
        byte[] tampered = "Hello LAMP0RT".getBytes(StandardCharsets.UTF_8);
        boolean ok2 = verify(tampered, sig, kp.pub);
        System.out.println("Lamport signature valid (tampered message): " + ok2);
    }
}
