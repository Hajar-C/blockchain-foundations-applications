Chahbi Hajar (Master IGOV)
Lab 2 – Consensus Mechanisms 

src/ : Java source code implementing
- Part 1: Proof of Work (PoW) with SHA-256 and threshold in leading HEX zeros.
- Part 2: Basic Paxos (3 servers A/B/C, 2 clients) for a bank transaction log.

plot_pow.py : Python script used to generate the PoW plots.

To Compile:
javac -d out src/*.java

Run PoW:
java -cp out Main pow

Run Paxos:
java -cp out Main paxos

Generate plots:
python plot_pow.py
