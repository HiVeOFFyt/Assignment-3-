# makes plots from the csv tables (run after Main)
import csv
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

def read(name):
    with open("results/tables/" + name + ".csv") as f:
        return list(csv.DictReader(f))

mst = read("mst")
topo = read("topo")
scc = read("scc")
dij = read("dijkstra")
dsu = read("dsu")

series = {}   # name -> list of (V, E, time)
for r in mst:
    series.setdefault(r["algorithm"], []).append((int(r["V"]), int(r["E"]), float(r["avg_time_us"])))
for r in topo:
    if r["input"] == "DAG":
        series.setdefault("Topological sort (DAG)", []).append((int(r["V"]), int(r["E"]), float(r["avg_time_us"])))
for r in scc:
    series.setdefault("SCC (Tarjan)", []).append((int(r["V"]), int(r["E"]), float(r["avg_time_us"])))
for r in dij:
    series.setdefault("Dijkstra", []).append((int(r["V"]), int(r["E"]), float(r["avg_time_us"])))

def plot(index, xlabel, title, filename):
    plt.figure(figsize=(7, 4.5))
    for name, pts in series.items():
        plt.plot([p[index] for p in pts], [p[2] for p in pts], marker="o", label=name)
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel(xlabel)
    plt.ylabel("average time (microseconds)")
    plt.title(title)
    plt.grid(True, which="both", alpha=0.3)
    plt.legend()
    plt.tight_layout()
    plt.savefig("results/plots/" + filename, dpi=150)
    plt.close()

plot(0, "number of vertices (V)", "Execution time vs number of vertices", "time_vs_vertices.png")
plot(1, "number of edges (E)", "Execution time vs number of edges", "time_vs_edges.png")

# Kruskal vs Prim bar chart
plt.figure(figsize=(7, 4.5))
labels = ["Small", "Medium", "Large"]
k = [float(r["avg_time_us"]) for r in mst if r["algorithm"] == "Kruskal"]
p = [float(r["avg_time_us"]) for r in mst if r["algorithm"] == "Prim"]
x = range(3)
plt.bar([i - 0.2 for i in x], k, width=0.4, label="Kruskal")
plt.bar([i + 0.2 for i in x], p, width=0.4, label="Prim")
plt.xticks(list(x), labels)
plt.yscale("log")
plt.ylabel("average time (microseconds)")
plt.title("Kruskal vs Prim")
plt.legend()
plt.tight_layout()
plt.savefig("results/plots/kruskal_vs_prim.png", dpi=150)
plt.close()

# DSU variants: parent steps on the large graph size
plt.figure(figsize=(7, 4.5))
variants = ["no optimization", "path compression only", "union by rank only", "both"]
steps_large = [int(next(r["parent_steps"] for r in dsu if r["graph"] == "Large" and r["variant"] == v)) for v in variants]
plt.bar(variants, steps_large)
plt.yscale("log")
plt.ylabel("parent links followed (log scale)")
plt.title("DSU variants inside Kruskal (V=1000, E=5000)")
plt.xticks(rotation=15)
plt.tight_layout()
plt.savefig("results/plots/dsu_variants.png", dpi=150)
plt.close()
print("plots saved")
