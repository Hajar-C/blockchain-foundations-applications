import java.security.SecureRandom;

public class ProofOfWork {

    private final SecureRandom rnd = new SecureRandom();

    /**
    Find a 32-bit nonce such that SHA256(message + nonce) has {@code thresholdZeros}
    leading '0' characters in its HEX representation (left-hand side).
     */
    public PowResult mine(String message, int thresholdZeros) {
        long start = System.nanoTime();
        long tries = 0;

        // Random 32-bit nonce (int). We re-sample until success.
        while (true) {
            int nonce = rnd.nextInt(); // 32-bit signed; still "random 32-bit"
            tries++;

            String candidate = message + nonce;
            String hex = CryptoUtil.toHex(CryptoUtil.sha256Bytes(candidate));

            if (CryptoUtil.hasLeadingHexZeros(hex, thresholdZeros)) {
                long end = System.nanoTime();
                long elapsedMs = (end - start) / 1_000_000L;
                return new PowResult(message, thresholdZeros, nonce, tries, elapsedMs, hex);
            }
        }
    }
}
