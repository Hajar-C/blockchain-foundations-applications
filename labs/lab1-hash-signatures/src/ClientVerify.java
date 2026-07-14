import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class ClientVerify {

    private static PublicKey loadPublicKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Path.of(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static boolean verify(byte[] message, byte[] signature, PublicKey pub) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(pub);
        sig.update(message);
        return sig.verify(signature);
    }

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 6000;

        PublicKey pub = loadPublicKey("public.key");
        byte[] fileData = Files.readAllBytes(Path.of("message.txt"));

        try (Socket socket = new Socket(host, port)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.writeInt(fileData.length);
            out.write(fileData);
            out.flush();

            int sigLen = in.readInt();
            byte[] signature = in.readNBytes(sigLen);

            boolean ok = verify(fileData, signature, pub);
            System.out.println("Signature valid: " + ok);
        }
    }
}
