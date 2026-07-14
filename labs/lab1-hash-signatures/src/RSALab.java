import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.Cipher;
public class RSALab {
// ================= KEY GENERATION =================
public static void generateKeys() throws Exception {
KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
keyGen.initialize(2048);
KeyPair pair = keyGen.generateKeyPair();
Files.write(Path.of("public.key"), pair.getPublic().getEncoded());
Files.write(Path.of("private.key"), pair.getPrivate().getEncoded());
}
// ================= LOAD KEYS =================
public static PublicKey loadPublicKey(String filename) throws Exception {
byte[] keyBytes = Files.readAllBytes(Path.of(filename));
X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
return KeyFactory.getInstance("RSA").generatePublic(spec);
}
public static PrivateKey loadPrivateKey(String filename) throws Exception {
byte[] keyBytes = Files.readAllBytes(Path.of(filename));
PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
return KeyFactory.getInstance("RSA").generatePrivate(spec);
}
// ================= ENCRYPT / DECRYPT =================
public static byte[] encrypt(byte[] message, PublicKey pub) throws Exception {
Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
cipher.init(Cipher.ENCRYPT_MODE, pub);
return cipher.doFinal(message);
}
public static byte[] decrypt(byte[] cipherText, PrivateKey priv) throws Exception {
Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
cipher.init(Cipher.DECRYPT_MODE, priv);
return cipher.doFinal(cipherText);
}
// ================= SIGN / VERIFY =================
public static byte[] sign(byte[] message, PrivateKey priv) throws Exception {
Signature sig = Signature.getInstance("SHA256withRSA");
sig.initSign(priv);
sig.update(message);
return sig.sign();
}
public static boolean verify(byte[] message, byte[] signature, PublicKey pub) throws Exception {
Signature sig = Signature.getInstance("SHA256withRSA");
sig.initVerify(pub);
sig.update(message);
return sig.verify(signature);
}
// ================= TEST =================
public static void main(String[] args) throws Exception {
generateKeys();
PublicKey pub = loadPublicKey("public.key");
PrivateKey priv = loadPrivateKey("private.key");
String msg = "Bob pays Alice 5 BTC";
byte[] cipher = encrypt(msg.getBytes(), pub);
byte[] plain = decrypt(cipher, priv);
byte[] signature = sign(msg.getBytes(), priv);
boolean ok = verify(msg.getBytes(), signature, pub);
System.out.println(new String(plain));
System.out.println("Signature valid: " + ok);
}
}
