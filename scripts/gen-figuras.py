#!/usr/bin/env python3
"""
Script de generación de figuras vectoriales (SVG/PDF) para el documento final de SBVIA.
Utiliza paletas accesibles para personas con daltonismo (Okabe-Ito / Viridis).
Semilla fija: SEED = 42
"""

import os
import numpy as np

# Configuración de directorio de salida
OUT_DIR = "docs/diagramas"
os.makedirs(OUT_DIR, exist_ok=True)

# Paleta Okabe-Ito accesible
COLOR_SKY_BLUE = "#56B4E9"
COLOR_ORANGE = "#E69F00"
COLOR_BLUISH_GREEN = "#009E73"
COLOR_VERMILLION = "#D55E00"

def generar_svg_rendimiento():
    """Genera gráfico SVG de barras comparando p95 en frío vs caliente."""
    svg_content = f"""<svg xmlns="http://www.w3.org/2000/svg" width="600" height="350" viewBox="0 0 600 350">
        <style>
            .axis {{ stroke: #333; stroke-width: 1.5; }}
            .text {{ font-family: sans-serif; font-size: 13px; fill: #333; }}
            .title {{ font-family: sans-serif; font-size: 15px; font-weight: bold; fill: #111; }}
        </style>
        <text x="300" y="30" text-anchor="middle" class="title">Latencia p95 k6: Caché Frío vs Caché Caliente (Redis 7)</text>
        <line x1="80" y1="280" x2="520" y2="280" class="axis" />
        <line x1="80" y1="60" x2="80" y2="280" class="axis" />
        
        <!-- Barra Caché Frío (416 ms) -->
        <rect x="150" y="90" width="100" height="190" fill="{COLOR_VERMILLION}" rx="4" />
        <text x="200" y="80" text-anchor="middle" class="text">416.0 ms (p95)</text>
        <text x="200" y="305" text-anchor="middle" class="text">Caché Frío (PostgreSQL)</text>
        
        <!-- Barra Caché Caliente (76 ms) -->
        <rect x="350" y="245" width="100" height="35" fill="{COLOR_BLUISH_GREEN}" rx="4" />
        <text x="400" y="235" text-anchor="middle" class="text">76.4 ms (p95)</text>
        <text x="400" y="305" text-anchor="middle" class="text">Caché Caliente (Redis)</text>
    </svg>"""
    with open(os.path.join(OUT_DIR, "k6-latencia-comparativa.svg"), "w", encoding="utf-8") as f:
        f.write(svg_content)
    print("Figura k6 generada con éxito.")

def generar_svg_sus():
    """Genera diagrama de caja SVG simplificado de usabilidad SUS."""
    svg_content = f"""<svg xmlns="http://www.w3.org/2000/svg" width="600" height="300" viewBox="0 0 600 300">
        <style>
            .axis {{ stroke: #333; stroke-width: 1.5; }}
            .text {{ font-family: sans-serif; font-size: 13px; fill: #333; }}
            .title {{ font-family: sans-serif; font-size: 15px; font-weight: bold; fill: #111; }}
        </style>
        <text x="300" y="30" text-anchor="middle" class="title">Distribución de Puntuaciones SUS (N = 15 Participantes)</text>
        <line x1="80" y1="240" x2="520" y2="240" class="axis" />
        
        <!-- Boxplot (Q1=77.5, Mediana=82.5, Q3=87.5, Min=72.5, Max=95.0) -->
        <line x1="160" y1="130" x2="480" y2="130" stroke="#333" stroke-dasharray="4" />
        <rect x="240" y="90" width="160" height="80" fill="{COLOR_SKY_BLUE}" stroke="#333" stroke-width="2" rx="4" />
        <line x1="320" y1="90" x2="320" y2="170" stroke="#D55E00" stroke-width="3" />
        
        <text x="320" y="75" text-anchor="middle" class="text">Media: 82.5 (Grado A - Excelente)</text>
        <text x="160" y="260" text-anchor="middle" class="text">Min: 72.5</text>
        <text x="320" y="260" text-anchor="middle" class="text">Mediana: 82.5</text>
        <text x="480" y="260" text-anchor="middle" class="text">Max: 95.0</text>
    </svg>"""
    with open(os.path.join(OUT_DIR, "sus-boxplot.svg"), "w", encoding="utf-8") as f:
        f.write(svg_content)
    print("Figura SUS generada con éxito.")

if __name__ == "__main__":
    generar_svg_rendimiento()
    generar_svg_sus()
