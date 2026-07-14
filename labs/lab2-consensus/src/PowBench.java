import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PowBench {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random R = new Random();

    private static String randomMessage(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(ALPHABET.charAt(R.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
    Runs PoW on multiple random messages and thresholds, prints results,
    and saves a CSV to plot:
     - threshold vs time
     - threshold vs tries
     */
    public static void runAndSaveCsv(String csvPath) {
        ProofOfWork pow = new ProofOfWork();

        int[] sizes = new int[]{16, 64, 256, 1024};     // different message sizes
        int[] thresholds = new int[]{2, 3, 4, 5};       // number of leading hex zeros
        int trialsPerSize = 3;

        List<PowResult> all = new ArrayList<>();

        System.out.println("=== PoW Benchmarks ===");
        for (int size : sizes) {
            for (int t = 0; t < trialsPerSize; t++) {
                String msg = randomMessage(size);

                for (int th : thresholds) {
                    PowResult res = pow.mine(msg, th);
                    all.add(res);

                    System.out.printf("size=%d, th=%d, tries=%d, time=%dms, nonce=%d, hash=%s%n",
                            size, th, res.tries, res.elapsedMillis, res.nonce, res.hashHex);
                }
            }
        }

        // CSV
        try (FileWriter fw = new FileWriter(csvPath)) {
            fw.write("messageSize,trial,thresholdZeros,tries,elapsedMillis\n");
            int idx = 0;
            for (int size : sizes) {
                for (int t = 0; t < trialsPerSize; t++) {
                    for (int th : thresholds) {
                        PowResult res = all.get(idx++);
                        fw.write(size + "," + t + "," + th + "," + res.tries + "," + res.elapsedMillis + "\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV: " + csvPath, e);
        }

        System.out.println("\nCSV saved to: " + csvPath);
        System.out.println("You can plot in Excel: (thresholdZeros vs elapsedMillis) and (thresholdZeros vs tries).");
    }
}
