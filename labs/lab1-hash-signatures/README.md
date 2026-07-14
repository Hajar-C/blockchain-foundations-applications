# Lab 1 — Hash Functions and Signature Schemes

Master IGOV, Pr. Yahya Benkaouz — *Blockchain: Foundations & Applications*.
Sujet complet : [sujet_lab1_hash_signatures.pdf](sujet_lab1_hash_signatures.pdf).

## Contenu

Le TP est divisé en trois parties, toutes en Java, dans [src/](src/) :

### Partie 1 — RSA (`RSALab.java`, `Client.java`/`Server.java`, `ServerSign.java`/`ClientVerify.java`)

- `RSALab.java` : implémentation de référence RSA (JCA/JCE).
  - `generateKeys()` : génère une paire de clés RSA 2048 bits, sauvegardées
    dans `public.key` / `private.key`.
  - `loadPublicKey` / `loadPrivateKey` : rechargent les clés depuis un
    fichier (`X509EncodedKeySpec` / `PKCS8EncodedKeySpec`).
  - `encrypt` / `decrypt` : chiffrement RSA avec padding OAEP-SHA256.
  - `sign` / `verify` : signature/vérification SHA256withRSA.
- `Client.java` / `Server.java` : transfert de fichier sécurisé — le client
  chiffre `message.txt` avec la clé publique du serveur et l'envoie par
  socket ; le serveur déchiffre et écrit `received.txt`.
- `ServerSign.java` / `ClientVerify.java` : le serveur signe le fichier reçu
  du client avec sa clé privée et renvoie la signature ; le client la
  vérifie avec la clé publique du serveur (authenticité + intégrité).

### Partie 2 — Lamport et Merkle-Lamport (`LamportSignature.java`, `MerkleLamportSignature.java`)

- `LamportSignature.java` : schéma de signature à usage unique basé sur
  SHA-256 (pas de RSA). Clé privée = 2×256 valeurs aléatoires de 32 octets ;
  clé publique = hash SHA-256 de chacune. Pour signer, on hache le message
  et on révèle, bit à bit du hash, la valeur privée correspondante (jeu 0 ou
  1). La démo signe un message, vérifie la signature, puis vérifie qu'un
  message modifié fait échouer la vérification.
- `MerkleLamportSignature.java` : lève la limite "usage unique" de Lamport
  en générant N paires de clés Lamport, authentifiées par un arbre de
  Merkle dont la racine devient la clé publique maîtresse. Signer avec la
  clé d'index *i* produit `(i, signature Lamport, clé publique Lamport,
  chemin d'authentification Merkle)`. La vérification recombine le chemin
  d'authentification pour retrouver la racine et la compare à la clé
  maîtresse, en plus de vérifier la signature Lamport elle-même.

### Partie 3 — Blind Signature (`BlindSignature.java`)

Signature aveugle RSA : le message est haché (SHA-256) puis aveuglé par une
valeur aléatoire `r` avant d'être signé par l'autorité, qui ne voit donc
jamais le message en clair. Le requérant désaveugle ensuite la signature et
la vérifie normalement. Utile pour le vote électronique ou le cash
numérique, où l'autorité ne doit pas pouvoir lier une signature à son
contenu.

## Compiler et exécuter

Toutes les classes lisent/écrivent leurs fichiers (`message.txt`,
`public.key`, `private.key`, `received.txt`) dans le répertoire courant : se
placer dans `src/` avant de compiler et lancer.

```bash
cd src
javac *.java
```

**RSA (partie 1)** — génère `public.key` / `private.key` nécessaires aux
autres classes :

```bash
java RSALab
```

**Client/Serveur — transfert chiffré** (2 terminaux, le serveur d'abord) :

```bash
java Server        # terminal 1
java Client         # terminal 2 (lit message.txt)
```

**Client/Serveur — signature** (2 terminaux, le serveur d'abord) :

```bash
java ServerSign
java ClientVerify
```

**Lamport, Merkle-Lamport, Blind Signature** (chacun autonome) :

```bash
java LamportSignature
java MerkleLamportSignature
java BlindSignature
```

`message.txt` est le fichier d'exemple utilisé par les scénarios
client/serveur.
