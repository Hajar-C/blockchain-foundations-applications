
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

public class Server {

    private static PrivateKey loadPrivateKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Path.of(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static byte[] decrypt(byte[] ciphertext, PrivateKey priv) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, priv);
        return cipher.doFinal(ciphertext);
    }

    public static void main(String[] args) throws Exception {
        int port = 5000;

        System.out.println("[Server] Loading private key...");
        PrivateKey priv = loadPrivateKey("private.key");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] Listening on port " + port + " ...");
            try (Socket socket = serverSocket.accept()) {
                System.out.println("[Server] Client connected: " + socket.getInetAddress());

                DataInputStream in = new DataInputStream(socket.getInputStream());

                // 1) read ciphertext length, then ciphertext bytes
                int n = in.readInt();
                byte[] ciphertext = in.readNBytes(n);

                System.out.println("[Server] Received encrypted data: " + n + " bytes");
                byte[] plaintext = decrypt(ciphertext, priv);

                Files.write(Path.of("received.txt"), plaintext);
                System.out.println("[Server] Decrypted and saved as received.txt");
            }
        }
    }
}

