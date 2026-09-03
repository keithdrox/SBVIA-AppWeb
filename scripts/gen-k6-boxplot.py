#!/usr/bin/env python3
"""
Genera el boxplot de latencia k6 (docs/mediciones/perf/k6-boxplot.png) a partir de
las latencias reales del endpoint GET /api/escenarios registradas en los JSON crudos
(k6-run1.json, k6-run2.json, k6-run3.json). Excluye las peticiones de setup() (login).
"""
import json
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

PERF = os.path.join("docs", "mediciones", "perf")
OUT = os.path.join(PERF, "k6-boxplot.png")

def latencias(run):
    values = []
    path = os.path.join(PERF, f"k6-run{run}.json")
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            if '"type":"Point"' not in line:
                continue
            if '"metric":"http_req_duration"' not in line:
                continue
            if '"group":"::setup"' in line:
                continue
            data = json.loads(line)
            values.append(data["data"]["value"])  # k6 reporta latencia en ms
    return np.array(values)

def main():
    runs = [latencias(r) for r in (1, 2, 3)]
    labels = ["Corrida 1", "Corrida 2", "Corrida 3"]

    fig, ax = plt.subplots(figsize=(8, 5))
    bp = ax.boxplot(runs, tick_labels=labels, patch_artist=True,
                    boxprops=dict(facecolor="#009E73", color="#333"),
                    medianprops=dict(color="#D55E00", linewidth=2))
    ax.axhline(2000, color="red", linestyle="--", linewidth=1.2, label="Umbral RNF-01 (2000 ms)")
    ax.set_ylabel("Latencia (ms)")
    ax.set_title("Distribución de latencia GET /api/escenarios (50 VUs, 30 s) — Redis caché caliente")
    ax.legend()
    ax.set_ylim(0, max(np.percentile(runs, 99) for runs in runs) * 1.2)
    fig.tight_layout()
    fig.savefig(OUT, dpi=120)
    print(f"Boxplot real generado: {OUT}")

    # Resumen por corrida (p50, p90, p95) en ms
    for i, r in enumerate(labels):
        print(f"{r}: p50={np.percentile(runs[i],50):.1f}ms p90={np.percentile(runs[i],90):.1f}ms "
              f"p95={np.percentile(runs[i],95):.1f}ms n={len(runs[i])}")

if __name__ == "__main__":
    main()
