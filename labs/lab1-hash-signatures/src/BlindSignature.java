import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class BlindSignature {

    public static void main(String[] args) throws Exception {

        // Generate RSA keys
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        PrivateKey priv = kp.getPrivate();
        PublicKey pub = kp.getPublic();

        // Extract RSA parameters
        KeyFactory kf = KeyFactory.getInstance("RSA");
        BigInteger n = kf.getKeySpec(pub, java.security.spec.RSAPublicKeySpec.class).getModulus();
        BigInteger e = kf.getKeySpec(pub, java.security.spec.RSAPublicKeySpec.class).getPublicExponent();
        BigInteger d = kf.getKeySpec(priv, java.security.spec.RSAPrivateKeySpec.class).getPrivateExponent();

        // Message
        String message = "Blind signature test";
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(message.getBytes(StandardCharsets.UTF_8));
        BigInteger m = new BigInteger(1, hash);

        SecureRandom random = new SecureRandom();

        // Choose random r such that gcd(r, n) = 1
        BigInteger r;
        do {
            r = new BigInteger(n.bitLength() - 1, random);
        } while (!r.gcd(n).equals(BigInteger.ONE));

        // Step 1: Blind
        BigInteger blinded = m.multiply(r.modPow(e, n)).mod(n);

        // Step 2: Sign blinded message
        BigInteger signedBlinded = blinded.modPow(d, n);

        // Step 3: Unblind
        BigInteger rInv = r.modInverse(n);
        BigInteger signature = signedBlinded.multiply(rInv).mod(n);

        // Step 4: Verify
        BigInteger verified = signature.modPow(e, n);

        System.out.println("Blind signature valid: " + verified.equals(m));
    }
}
