# Projet Blockchain

Ce dépôt regroupe deux types de travaux réalisés dans le cadre du Master IGOV,
autour de la blockchain : un projet d'analyse de recherche, et les travaux
pratiques (TPs) du cours *Blockchain: Foundations & Applications* (Pr. Yahya
Benkaouz). Les deux sont indépendants l'un de l'autre — ils partagent
uniquement le thème "blockchain" et ce dépôt comme espace de rangement.

## Structure du dépôt

```
projet blockchain/
├── README.md
├── projet/                <- projet académique : analyse d'articles de recherche
│   ├── README.md
│   ├── blockchain rapport.pdf
│   ├── ppt blockchain.pdf
│   └── (3 PDFs des articles sources)
└── labs/                  <- TPs du cours (chaque sous-dossier garde son propre README)
    ├── lab1-hash-signatures/
    ├── lab2-consensus/
    └── lab3-utxo/
```

### `projet/` — Analyse et comparaison d'articles de recherche

Rapport comparant trois articles scientifiques sur la sécurité des
blockchains (détection par IA/GAN, attaque de minage par ZKP, et le
*selfish mining* fondateur d'Eyal & Sirer). Contient le rapport complet, le
support de présentation, et les 3 articles sources en PDF.

Voir [projet/README.md](projet/README.md) pour le détail (résumé, comparaison,
conclusion, références).

### `labs/` — Travaux pratiques du cours

Master IGOV, Pr. Yahya Benkaouz — *Blockchain: Foundations & Applications*.
Trois TPs indépendants, chacun avec son sujet (PDF) et son code source.

#### lab1-hash-signatures/ — Lab 1: Hash Functions and Signature Schemes

Sujet : [sujet_lab1_hash_signatures.pdf](labs/lab1-hash-signatures/sujet_lab1_hash_signatures.pdf)
— code : [src/](labs/lab1-hash-signatures/src/) (Java)

Implémentation et étude de plusieurs schémas de signature utilisés en
blockchain :

- **RSA** (`RSALab.java`) — génération de clés 2048 bits, chiffrement/
  déchiffrement (OAEP-SHA256), signature/vérification (SHA256withRSA).
- **Client/Serveur RSA** (`Client.java` / `Server.java`) — transfert de
  fichier chiffré de bout en bout (`message.txt` → `received.txt`).
- **Client/Serveur + signature** (`ServerSign.java` / `ClientVerify.java`) —
  le serveur signe un fichier reçu, le client vérifie la signature avec la
  clé publique.
- **Lamport Signature** (`LamportSignature.java`) — signature à usage unique
  basée sur SHA-256 (256 paires de valeurs aléatoires), avec démonstration
  d'échec de vérification si le message est modifié.
- **Merkle-Lamport Signature** (`MerkleLamportSignature.java`) — étend
  Lamport à un usage multiple : N paires de clés Lamport authentifiées par
  un arbre de Merkle, dont la racine sert de clé publique maîtresse.
- **Blind Signature RSA** (`BlindSignature.java`) — signature aveugle
  (aveuglement / signature / désaveuglement) permettant à une autorité de
  signer un message sans en connaître le contenu (vote électronique, cash
  numérique).

Voir [labs/lab1-hash-signatures/README.md](labs/lab1-hash-signatures/README.md)
pour les instructions de compilation/exécution détaillées.

#### lab2-consensus/ — Lab 2: Consensus Mechanisms

Sujet : [sujet_lab2_consensus.pdf](labs/lab2-consensus/sujet_lab2_consensus.pdf)
— code : [src/](labs/lab2-consensus/src/) (Java) + `plot_pow.py`

Implémentation de deux mécanismes de consensus :

- **Proof of Work** (`ProofOfWork.java`, `PowBench.java`, `PowResult.java`) —
  minage par recherche d'un hash SHA-256 avec un nombre donné de zéros
  hexadécimaux en tête, avec benchmark de la difficulté. `plot_pow.py` génère
  les graphes de temps de minage en fonction de la difficulté.
- **Paxos** (`PaxosNode.java`, `PaxosCluster.java`, `PaxosMessageTypes.java`,
  `BankService.java`, `CryptoUtil.java`) — consensus Paxos basique avec 3
  serveurs (A/B/C) et 2 clients, appliqué à un journal de transactions
  bancaires.

Voir [labs/lab2-consensus/README.md](labs/lab2-consensus/README.md) pour les
commandes de compilation et d'exécution (`Main pow` / `Main paxos`).

#### lab3-utxo/ — Lab 3: Blockchain and UTXO Transactions

Code : [src/](labs/lab3-utxo/src/) (Python) + `report.ipynb` + `messages.txt`

Implémentation d'une blockchain simplifiée en deux parties :

- **Blockchain à blocs + Proof-of-Work** — chaque bloc stocke la
  concaténation de deux messages, sécurisé par un minage SHA-256.
- **Système de transactions UTXO** — validation des transactions par le
  modèle UTXO et signatures numériques (`blockchain_utxo.py`).
- `main.py` exécute le programme (`python -m src.main --messages-file
  messages.txt --difficulty 4`), `report.ipynb` est un notebook d'exploration
  personnel.

Voir [labs/lab3-utxo/README.md](labs/lab3-utxo/README.md) pour le détail
(prérequis : Python 3.12+, librairie `cryptography`).

## Pourquoi cette organisation

Le dépôt git `Analyse-Articles-Blockchain-Security` était à l'origine dédié
uniquement au projet d'analyse d'articles (dossier `projet/`). Les 3 TPs ont
été ajoutés dans le même dossier local par commodité ; ils sont rangés dans
`labs/` pour ne pas mélanger deux livrables distincts, tout en restant dans le
même dépôt.
