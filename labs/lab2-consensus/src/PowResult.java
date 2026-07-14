public class PowResult {
    public final String message;
    public final int thresholdZeros;
    public final int nonce;
    public final long tries;
    public final long elapsedMillis;
    public final String hashHex;

    public PowResult(String message, int thresholdZeros, int nonce, long tries, long elapsedMillis, String hashHex) {
        this.message = message;
        this.thresholdZeros = thresholdZeros;
        this.nonce = nonce;
        this.tries = tries;
        this.elapsedMillis = elapsedMillis;
        this.hashHex = hashHex;
    }
}
