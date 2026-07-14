"""
Lab 3 — Blockchain (Blocks) + UTXO Transactions (English)
Université Mohammed V de Rabat — Master SNIA — Blockchain: Foundations & Applications

Implements:
- Part 1: Block + Blockchain with PoW mining (SHA-256, difficulty by leading zeros)
- Part 2: Transaction model inspired by UTXO, with validation rules and signature (ECDSA)

Run:
  python -m src.main --messages-file messages.txt --difficulty 4
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import List, Optional, Dict, Tuple
import hashlib
import json


def sha256_hex(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def sha256_hex_str(s: str) -> str:
    return sha256_hex(s.encode("utf-8"))


# ---------------- Part 1: Blocks / Blockchain ----------------

@dataclass
class Block:
    index: int
    previous_hash: Optional[str]
    data: str
    nonce: int = 0
    hash: str = ""

    def compute_hash(self) -> str:
        """
        Hash = SHA256(index + previous_hash + SHA256(data) + nonce)
        previous_hash is 'NULL' when None (genesis).
        """
        prev = self.previous_hash if self.previous_hash is not None else "NULL"
        data_hash = sha256_hex_str(self.data)
        payload = f"{self.index}{prev}{data_hash}{self.nonce}"
        return sha256_hex_str(payload)

    def mine(self, difficulty: int) -> None:
        """
        Proof of Work: find nonce so that hash starts with difficulty '0'.
        """
        assert difficulty >= 0
        prefix = "0" * difficulty
        self.nonce = 0
        while True:
            h = self.compute_hash()
            if h.startswith(prefix):
                self.hash = h
                return
            self.nonce += 1


@dataclass
class Blockchain:
    difficulty: int = 3
    chain: List[Block] = field(default_factory=list)

    def create_genesis_block(self) -> Block:
        genesis = Block(index=0, previous_hash=None, data="GENESIS")
        genesis.mine(self.difficulty)
        return genesis

    def last_block(self) -> Block:
        return self.chain[-1]

    def add_block(self, data: str) -> Block:
        if not self.chain:
            self.chain.append(self.create_genesis_block())

        prev = self.last_block()
        block = Block(index=prev.index + 1, previous_hash=prev.hash, data=data)
        block.mine(self.difficulty)

        # Verify before appending
        if not self.verify_block(block, prev):
            raise ValueError("Rejected invalid block")
        self.chain.append(block)
        return block

    def verify_block(self, block: Block, previous_block: Optional[Block]) -> bool:
        # index continuity
        if previous_block is None:
            if block.index != 0:
                return False
            if block.previous_hash is not None:
                return False
        else:
            if block.index != previous_block.index + 1:
                return False
            if block.previous_hash != previous_block.hash:
                return False

        # hash correctness
        computed = block.compute_hash()
        if block.hash != computed:
            return False

        # PoW difficulty
        if not block.hash.startswith("0" * self.difficulty):
            return False

        return True

    def is_valid(self) -> bool:
        if not self.chain:
            return True
        # verify genesis
        if not self.verify_block(self.chain[0], None):
            return False
        for i in range(1, len(self.chain)):
            if not self.verify_block(self.chain[i], self.chain[i - 1]):
                return False
        return True

    def to_dict(self) -> dict:
        return {
            "difficulty": self.difficulty,
            "chain": [
                {
                    "index": b.index,
                    "previous_hash": b.previous_hash,
                    "data": b.data,
                    "nonce": b.nonce,
                    "hash": b.hash,
                }
                for b in self.chain
            ],
        }


# ---------------- Part 2: Transactions / UTXO ----------------

@dataclass(frozen=True)
class TxInput:
    pre_tx_hash: str
    out_index: int

    def as_tuple(self) -> Tuple[str, int]:
        return (self.pre_tx_hash, self.out_index)


@dataclass
class TxOutput:
    index: int
    value: int  # satoshi
    public_key_pem: str  # PEM-encoded public key (string)

    def to_dict(self) -> dict:
        return {"index": self.index, "value": self.value, "public_key_pem": self.public_key_pem}


@dataclass
class Transaction:
    inputs: List[TxInput]
    outputs: List[TxOutput]
    # We assume all inputs belong to same owner => one signature for whole tx
    signature_b64: Optional[str] = None  # base64 signature over tx content (without signature)
    tx_hash: str = ""

    def _content_dict(self) -> dict:
        # canonical form for hashing/signing
        return {
            "inputs": [{"pre_tx_hash": i.pre_tx_hash, "out_index": i.out_index} for i in self.inputs],
            "outputs": [o.to_dict() for o in self.outputs],
        }

    def compute_hash(self) -> str:
        content_json = json.dumps(self._content_dict(), sort_keys=True, separators=(",", ":"))
        return sha256_hex_str(content_json)

    def finalize(self) -> None:
        self.tx_hash = self.compute_hash()

    def sign(self, private_key_pem: str) -> None:
        """
        Sign tx content (inputs + outputs) using ECDSA with SHA-256.
        """
        from cryptography.hazmat.primitives import hashes, serialization
        from cryptography.hazmat.primitives.asymmetric import ec

        priv = serialization.load_pem_private_key(private_key_pem.encode("utf-8"), password=None)
        assert isinstance(priv, ec.EllipticCurvePrivateKey)

        msg = json.dumps(self._content_dict(), sort_keys=True, separators=(",", ":")).encode("utf-8")
        sig = priv.sign(msg, ec.ECDSA(hashes.SHA256()))
        self.signature_b64 = _b64e(sig)
        self.finalize()

    def verify_signature(self, public_key_pem: str) -> bool:
        if self.signature_b64 is None:
            return False
        from cryptography.hazmat.primitives import hashes, serialization
        from cryptography.hazmat.primitives.asymmetric import ec

        pub = serialization.load_pem_public_key(public_key_pem.encode("utf-8"))
        assert isinstance(pub, ec.EllipticCurvePublicKey)

        msg = json.dumps(self._content_dict(), sort_keys=True, separators=(",", ":")).encode("utf-8")
        try:
            pub.verify(_b64d(self.signature_b64), msg, ec.ECDSA(hashes.SHA256()))
            return True
        except Exception:
            return False


def _b64e(b: bytes) -> str:
    import base64
    return base64.b64encode(b).decode("ascii")


def _b64d(s: str) -> bytes:
    import base64
    return base64.b64decode(s.encode("ascii"))


class UTXOSet:
    """
    Keeps track of unspent outputs: map[(tx_hash, out_index)] -> TxOutput
    """
    def __init__(self) -> None:
        self._utxos: Dict[Tuple[str, int], TxOutput] = {}

    def add_output(self, tx_hash: str, out: TxOutput) -> None:
        self._utxos[(tx_hash, out.index)] = out

    def spend(self, tx_input: TxInput) -> None:
        key = tx_input.as_tuple()
        if key not in self._utxos:
            raise ValueError("Trying to spend a non-existing / already spent output")
        del self._utxos[key]

    def get(self, pre_tx_hash: str, out_index: int) -> Optional[TxOutput]:
        return self._utxos.get((pre_tx_hash, out_index))

    def exists(self, pre_tx_hash: str, out_index: int) -> bool:
        return (pre_tx_hash, out_index) in self._utxos

    def snapshot(self) -> Dict[str, dict]:
        return {f"{k[0]}:{k[1]}": v.to_dict() for k, v in self._utxos.items()}


def validate_transaction(tx: Transaction, utxos: UTXOSet, owner_public_key_pem: str) -> Tuple[bool, str]:
    """
    Validates the rules from the statement:
    - No two inputs in same tx refer to same output
    - Claimed outputs exist and are not spent (i.e. present in UTXO set)
    - Signature is valid
    - All output values are non-negative
    - Sum(inputs) >= Sum(outputs)
    """
    # (1) no duplicate inputs
    seen = set()
    for inp in tx.inputs:
        if inp.as_tuple() in seen:
            return False, "Invalid: two inputs refer to the same previous output"
        seen.add(inp.as_tuple())

    # (2) all referenced outputs exist (unspent)
    input_sum = 0
    for inp in tx.inputs:
        out = utxos.get(inp.pre_tx_hash, inp.out_index)
        if out is None:
            return False, f"Invalid: referenced output {inp.pre_tx_hash}:{inp.out_index} is already spent or does not exist"
        input_sum += out.value

    # (3) signature
    if not tx.verify_signature(owner_public_key_pem):
        return False, "Invalid: signature check failed"

    # (4) output values non-negative
    for out in tx.outputs:
        if out.value < 0:
            return False, "Invalid: negative output value"

    # (5) conservation: sum(inputs) >= sum(outputs)
    output_sum = sum(o.value for o in tx.outputs)
    if input_sum < output_sum:
        return False, "Invalid: sum(inputs) < sum(outputs)"

    return True, "Valid"


def apply_transaction(tx: Transaction, utxos: UTXOSet) -> None:
    """
    Consumes inputs and creates outputs (updates UTXO set).
    Assumes tx has already been validated.
    """
    if not tx.tx_hash:
        tx.finalize()

    for inp in tx.inputs:
        utxos.spend(inp)
    for out in tx.outputs:
        utxos.add_output(tx.tx_hash, out)
