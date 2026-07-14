import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class Client {

    private static PublicKey loadPublicKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Path.of(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static byte[] encrypt(byte[] message, PublicKey pub) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pub);
        return cipher.doFinal(message);
    }

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 5000;

        System.out.println("[Client] Loading public key...");
        PublicKey pub = loadPublicKey("public.key");

        byte[] data = Files.readAllBytes(Path.of("message.txt"));
        System.out.println("[Client] Read message.txt: " + data.length + " bytes");

        byte[] ciphertext = encrypt(data, pub);
        System.out.println("[Client] Encrypted: " + ciphertext.length + " bytes");

        try (Socket socket = new Socket(host, port)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // send length then bytes
            out.writeInt(ciphertext.length);
            out.write(ciphertext);
            out.flush();

            System.out.println("[Client] Sent encrypted data to server.");
        }
    }
}
