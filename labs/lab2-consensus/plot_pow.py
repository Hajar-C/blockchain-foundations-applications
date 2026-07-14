import pandas as pd
import matplotlib.pyplot as plt

# Lit le CSV généré par Java
df = pd.read_csv("pow_results.csv")

# On calcule la moyenne par threshold
g = df.groupby("thresholdZeros").agg(
    mean_time_ms=("elapsedMillis", "mean"),
    mean_tries=("tries", "mean"),
    std_time_ms=("elapsedMillis", "std"),
    std_tries=("tries", "std"),
).reset_index()

# Courbe 1: Threshold vs running time
plt.figure()
plt.plot(g["thresholdZeros"], g["mean_time_ms"], marker="o")
plt.yscale("log")
plt.xlabel("Threshold (nombre de zéros en tête du hash hex)")
plt.ylabel("Temps moyen (ms)")
plt.title("PoW: Threshold vs Temps")
plt.grid(True)
plt.savefig("threshold_vs_time.png", dpi=200)

# Courbe 2: Threshold vs number of tries
plt.figure()
plt.plot(g["thresholdZeros"], g["mean_tries"], marker="o")
plt.yscale("log")
plt.xlabel("Threshold (nombre de zéros en tête du hash hex)")
plt.ylabel("Nombre moyen d'essais")
plt.title("PoW: Threshold vs Essais")
plt.grid(True)
plt.savefig("threshold_vs_tries.png", dpi=200)

print("Images générées: threshold_vs_time.png, threshold_vs_tries.png")
