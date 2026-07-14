from __future__ import annotations

import argparse
from pathlib import Path
from typing import List

from .blockchain_utxo import Blockchain, Transaction, TxInput, TxOutput, UTXOSet, validate_transaction, apply_transaction


def read_messages(path: Path) -> List[str]:
    lines = []
    for line in path.read_text(encoding="utf-8").splitlines():
        s = line.strip()
        if s:
            lines.append(s)
    return lines


def build_blockchain_from_messages(messages: List[str], difficulty: int) -> Blockchain:
    """
    The statement says: "The Block data is the concatenation of two messages."
    So we group messages by pairs: (m0+m1), (m2+m3), ...
    If an odd message remains, we concatenate it with an empty string.
    """
    bc = Blockchain(difficulty=difficulty)
    # Genesis
    bc.chain.append(bc.create_genesis_block())

    for i in range(0, len(messages), 2):
        m1 = messages[i]
        m2 = messages[i + 1] if i + 1 < len(messages) else ""
        data = m1 + m2
        bc.add_block(data)

    return bc


def demo_utxo() -> None:
    """
    Small demo showing a valid tx spending one UTXO and creating 2 outputs (payment + change).
    """
    from cryptography.hazmat.primitives.asymmetric import ec
    from cryptography.hazmat.primitives import serialization

    # Create a keypair for "Alice"
    priv = ec.generate_private_key(ec.SECP256K1())
    pub = priv.public_key()

    priv_pem = priv.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=serialization.NoEncryption(),
    ).decode("utf-8")

    pub_pem = pub.public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo,
    ).decode("utf-8")

    utxos = UTXOSet()

    # Create a "coinbase" output to Alice (no inputs, special case for demo)
    # We'll just insert it in UTXO set manually.
    coinbase_tx_hash = "COINBASE_TX_HASH_DEMO"
    utxos.add_output(coinbase_tx_hash, TxOutput(index=0, value=1000, public_key_pem=pub_pem))

    # Alice spends 1000 to create: 300 to Bob (fake key), 700 change back to Alice
    bob_pub_pem = pub_pem  # for demo only (normally Bob's public key is different)

    tx = Transaction(
        inputs=[TxInput(pre_tx_hash=coinbase_tx_hash, out_index=0)],
        outputs=[
            TxOutput(index=0, value=300, public_key_pem=bob_pub_pem),
            TxOutput(index=1, value=700, public_key_pem=pub_pem),
        ],
    )
    tx.sign(priv_pem)

    ok, reason = validate_transaction(tx, utxos, owner_public_key_pem=pub_pem)
    print("Transaction validation:", ok, "-", reason)

    if ok:
        apply_transaction(tx, utxos)
        print("UTXO set after applying tx:")
        for k, v in utxos.snapshot().items():
            print(" ", k, "=>", v)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--messages-file", type=str, required=False, default="messages.txt")
    parser.add_argument("--difficulty", type=int, required=False, default=3)
    args = parser.parse_args()

    msg_path = Path(args.messages_file)
    messages = read_messages(msg_path) if msg_path.exists() else ["hello", "world", "blockchain", "lab"]

    bc = build_blockchain_from_messages(messages, difficulty=args.difficulty)
    print("Blockchain valid?", bc.is_valid())
    print("Number of blocks (including genesis):", len(bc.chain))
    print("Last block hash:", bc.chain[-1].hash)

    print("\n--- UTXO demo ---")
    demo_utxo()


if __name__ == "__main__":
    main()
