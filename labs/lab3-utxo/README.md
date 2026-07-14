# Lab 3 — Blockchain and UTXO Transactions

## Description
This project implements basic blockchain concepts.  
It is divided into two parts:

- **Part 1:** Blockchain implementation using blocks and Proof-of-Work.
- **Part 2:** Transaction system based on the UTXO model.

---

## Requirements
- Python 3.12+
- cryptography library

Install dependency:

```bash
python -m pip install cryptography
```

---

## How to Run

```bash
python -m src.main --messages-file messages.txt --difficulty 4
```

---

## Files

- `src/blockchain_utxo.py` → Blockchain and transaction logic  
- `src/main.py` → Program execution  
- `messages.txt` → Input messages stored in blocks  
- `report.ipynb` → Small personal notebook created to help me better understand and test the implementation  

---

## Notes
- Each block stores the concatenation of two messages.
- Proof-of-Work secures blocks using SHA-256 hashing.
- Transactions are validated using the UTXO model and digital signatures.
