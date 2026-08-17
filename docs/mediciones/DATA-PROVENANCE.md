# Procedencia y Trazabilidad de Datos Empíricos (DATA-PROVENANCE.md)

Este documento traza exactamente qué archivo crudo origina cada tabla y figura del documento final (`informe-final.tex`), qué script genera los resultados y en qué commit se generaron.

---

## Matriz de Procedencia de Tablas y Figuras

| Elemento en Documento | Descripción | Archivo de Datos Crudos | Script de Transformación | Commit de Generación |
|:---|:---|:---|:---|:---:|
| **Tabla 1 (Cap. 3)** | Matriz Comparativa de Trabajos Relacionados | `docs/checklists/prisma2020.md` | Extracción manual estructurada | `13ed0b1` |
| **Tabla 2 (Cap. 4)** | Métricas de Calidad de Requisitos | `docs/requisitos/CHANGELOG-REQ.md` | `scripts/validate-traceability.sh` | `793b765` |
| **Tabla 3 (Cap. 6)** | Catálogo de Procedimientos Almacenados | `docs/basedatos/CATALOGO-SP.md` | `scripts/audit-sql-dynamic.sh` | `793b765` |
| **Tabla 4 (Cap. 8)** | Rendimiento k6 (Frío vs Caliente, 5 corridas) | `docs/mediciones/perf/k6-results-summary.csv` | `scripts/perf-analysis.ipynb` | `0d513c4` |
| **Tabla 5 (Cap. 8)** | Resumen de Usabilidad SUS (N=15) | `docs/mediciones/sus/sus-raw-data.csv` | `scripts/sus-analysis.ipynb` | `0d513c4` |
| **Tabla 6 (Cap. 8)** | Cobertura de Código JaCoCo | `docs/mediciones/jacoco/coverage-summary.csv` | `mvn verify` (JaCoCo plugin) | `13ed0b1` |
| **Tabla 7 (Cap. 8)** | Métricas Lighthouse (Mobile & Desktop) | `docs/mediciones/lighthouse/report.json` | `lhci autorun` | `ed9fec1` |
| **Tabla 8 (Cap. 8)** | Controles de Seguridad OWASP Top 10 | `docs/mediciones/owasp/curl-audit-report.md` | `docs/mediciones/owasp/curl-audit.sh` | `13ed0b1` |
| **Figura 1 (Cap. 6)** | Diagramas de Arquitectura C4 (Niveles 1–3) | `docs/arquitectura/c4-model.dsl` | Structurizr CLI exporter | `ed9fec1` |
| **Figura 2 (Cap. 8)** | Gráfico de Latencia k6 p95 con IC 95% | `docs/mediciones/perf/k6-results-summary.csv` | `scripts/gen-figuras.py` | `0d513c4` |
| **Figura 3 (Cap. 8)** | Diagrama de Caja (Boxplot) de Puntuaciones SUS | `docs/mediciones/sus/sus-raw-data.csv` | `scripts/gen-figuras.py` | `0d513c4` |
| **Figura 4 (Cap. 8)** | Gráfico de Barras de Cobertura por Paquete | `docs/mediciones/jacoco/coverage-summary.csv` | `scripts/gen-figuras.py` | `0d513c4` |
