import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

public class ServerSign {

    private static PrivateKey loadPrivateKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Path.of(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static byte[] sign(byte[] message, PrivateKey priv) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(priv);
        sig.update(message);
        return sig.sign();
    }

    public static void main(String[] args) throws Exception {
        int port = 6000;

        System.out.println("[Server] Loading private key...");
        PrivateKey priv = loadPrivateKey("private.key");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] Waiting for client...");
            Socket socket = serverSocket.accept();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            int len = in.readInt();
            byte[] fileData = in.readNBytes(len);

            System.out.println("[Server] File received, signing...");
            byte[] signature = sign(fileData, priv);

            out.writeInt(signature.length);
            out.write(signature);
            out.flush();

            System.out.println("[Server] Signature sent to client.");
        }
    }
}
