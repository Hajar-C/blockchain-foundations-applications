import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {
    public static byte[] sha256Bytes(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static boolean hasLeadingHexZeros(String hexHash, int zeros) {
        if (zeros <= 0) return true;
        if (hexHash.length() < zeros) return false;
        for (int i = 0; i < zeros; i++) {
            if (hexHash.charAt(i) != '0') return false;
        }
        return true;
    }
}
