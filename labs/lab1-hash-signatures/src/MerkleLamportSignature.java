import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MerkleLamportSignature {

    // SHA-256 utilities
    static byte[] sha256(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
    }

    static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    static int getBit(byte[] hash, int i) {
        int byteIndex = i / 8;
        int bitIndex = 7 - (i % 8);
        return (hash[byteIndex] >> bitIndex) & 1;
    }

    // Lamport OTS
    static class LamportKeyPair {
        final byte[][][] priv; // [2][256][32]
        final byte[][][] pub;  // [2][256][32]

        LamportKeyPair(byte[][][] priv, byte[][][] pub) {
            this.priv = priv;
            this.pub = pub;
        }
    }

    static LamportKeyPair lamportKeyGen() throws Exception {
        SecureRandom rnd = new SecureRandom();
        byte[][][] priv = new byte[2][256][32];
        byte[][][] pub  = new byte[2][256][32];

        for (int b = 0; b < 2; b++) {
            for (int i = 0; i < 256; i++) {
                rnd.nextBytes(priv[b][i]);
                pub[b][i] = sha256(priv[b][i]);
            }
        }
        return new LamportKeyPair(priv, pub);
    }

    static byte[][] lamportSign(byte[] message, byte[][][] priv) throws Exception {
        byte[] h = sha256(message);
        byte[][] sig = new byte[256][32];
        for (int i = 0; i < 256; i++) {
            int bit = getBit(h, i);
            sig[i] = Arrays.copyOf(priv[bit][i], 32);
        }
        return sig;
    }

    static boolean lamportVerify(byte[] message, byte[][] signature, byte[][][] pub) throws Exception {
        byte[] h = sha256(message);
        for (int i = 0; i < 256; i++) {
            int bit = getBit(h, i);
            byte[] hashed = sha256(signature[i]);
            if (!Arrays.equals(hashed, pub[bit][i])) return false;
        }
        return true;
    }

    // Serialize Lamport public key (2*256*32 bytes) then hash to get leaf
    static byte[] lamportPublicKeyLeafHash(byte[][][] lamportPub) throws Exception {
        byte[] buf = new byte[2 * 256 * 32];
        int k = 0;
        for (int b = 0; b < 2; b++) {
            for (int i = 0; i < 256; i++) {
                System.arraycopy(lamportPub[b][i], 0, buf, k, 32);
                k += 32;
            }
        }
        return sha256(buf);
    }


    // Merkle tree
    static class AuthNode {
        final byte[] siblingHash;   // 32 bytes
        final boolean siblingIsLeft; // true if sibling is on the left of current node

        AuthNode(byte[] siblingHash, boolean siblingIsLeft) {
            this.siblingHash = siblingHash;
            this.siblingIsLeft = siblingIsLeft;
        }
    }

    static class MerkleTree {
        final List<List<byte[]>> levels; // levels[0] = leaves
        final byte[] root;

        MerkleTree(List<byte[]> leaves) throws Exception {
            this.levels = new ArrayList<>();
            this.levels.add(new ArrayList<>(leaves));

            List<byte[]> current = leaves;
            while (current.size() > 1) {
                List<byte[]> next = new ArrayList<>();
                for (int i = 0; i < current.size(); i += 2) {
                    byte[] left = current.get(i);
                    byte[] right = (i + 1 < current.size()) ? current.get(i + 1) : current.get(i); // duplicate if odd
                    next.add(sha256(concat(left, right)));
                }
                levels.add(next);
                current = next;
            }
            this.root = levels.get(levels.size() - 1).get(0);
        }

        List<AuthNode> getAuthPath(int leafIndex) {
            List<AuthNode> path = new ArrayList<>();
            int idx = leafIndex;

            for (int level = 0; level < levels.size() - 1; level++) {
                List<byte[]> nodes = levels.get(level);
                int siblingIdx = (idx % 2 == 0) ? idx + 1 : idx - 1;

                byte[] sibling = (siblingIdx < nodes.size()) ? nodes.get(siblingIdx) : nodes.get(idx); // duplicate rule
                boolean siblingIsLeft = (idx % 2 == 1); // if idx is right child, sibling is left

                path.add(new AuthNode(sibling, siblingIsLeft));
                idx /= 2;
            }
            return path;
        }

        static byte[] computeRootFromPath(byte[] leafHash, int leafIndex, List<AuthNode> path) throws Exception {
            byte[] acc = leafHash;
            int idx = leafIndex;

            for (AuthNode node : path) {
                if (node.siblingIsLeft) {
                    acc = sha256(concat(node.siblingHash, acc));
                } else {
                    acc = sha256(concat(acc, node.siblingHash));
                }
                idx /= 2;
            }
            return acc;
        }
    }


    // Merkle-Lamport signature object
    static class MerkleLamportSig {
        final int index;                // which Lamport key used
        final byte[][] lamportSignature; // [256][32]
        final byte[][][] lamportPublic;  // [2][256][32] included so verifier can hash it into leaf
        final List<AuthNode> authPath;   // Merkle authentication path

        MerkleLamportSig(int index, byte[][] lamportSignature, byte[][][] lamportPublic, List<AuthNode> authPath) {
            this.index = index;
            this.lamportSignature = lamportSignature;
            this.lamportPublic = lamportPublic;
            this.authPath = authPath;
        }
    }


    // Demo: generate N Lamport keys, build Merkle tree, sign+verify
    public static void main(String[] args) throws Exception {
        int N = 8; // choose N as needed (e.g., 8, 16, 32...)

        // 1) Generate N Lamport key pairs
        LamportKeyPair[] keys = new LamportKeyPair[N];
        List<byte[]> leaves = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            keys[i] = lamportKeyGen();
            leaves.add(lamportPublicKeyLeafHash(keys[i].pub));
        }

        // 2) Build Merkle tree of public keys
        MerkleTree tree = new MerkleTree(leaves);

        // 3) Publish Merkle root as master public key
        byte[] masterPublicKey = tree.root;
        System.out.println("Master public key (Merkle root) computed.");

        // 4) Sign a message using Lamport key at some index + auth path
        String msg = "Merkle-Lamport test message";
        byte[] message = msg.getBytes(StandardCharsets.UTF_8);

        int indexToUse = 3; // pick any unused index in [0..N-1]
        byte[][] lamportSig = lamportSign(message, keys[indexToUse].priv);
        List<AuthNode> path = tree.getAuthPath(indexToUse);

        MerkleLamportSig mts = new MerkleLamportSig(indexToUse, lamportSig, keys[indexToUse].pub, path);

        // 5) Verify: Lamport verify + Merkle auth path verify
        boolean lamportOk = lamportVerify(message, mts.lamportSignature, mts.lamportPublic);
        byte[] leafHash = lamportPublicKeyLeafHash(mts.lamportPublic);
        byte[] recomputedRoot = MerkleTree.computeRootFromPath(leafHash, mts.index, mts.authPath);
        boolean merkleOk = Arrays.equals(recomputedRoot, masterPublicKey);

        System.out.println("Lamport signature valid: " + lamportOk);
        System.out.println("Merkle authentication valid: " + merkleOk);
        System.out.println("Merkle-Lamport signature valid: " + (lamportOk && merkleOk));
    }
}
