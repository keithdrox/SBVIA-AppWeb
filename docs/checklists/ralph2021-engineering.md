# ACM SIGSOFT Empirical Standard: Engineering Research Checklist

Basado en el estándar empírico de Ralph et al. (2021) para investigación en ingeniería de software y desarrollo de artefactos.

---

## Atributos Esenciales (Essential Attributes)

| Ítem | Descripción | Cumplimiento en SBVIA | Evidencia en Repositorio |
|:---:|:---|:---:|:---|
| **ER1** | **Problema motivado:** Describe un problema práctico concreto y justifica la necesidad de una solución técnica. | ✅ | `informe-final.tex` (Cap. 1), sustentado con 3 fuentes cuantitativas. |
| **ER2** | **Artefacto propuesto:** Presenta un artefacto de software formalmente especificado y funcional. | ✅ | Sistema SBVIA v1.0.0 (Spring Boot 3.2, Angular 17, PostgreSQL 16, Redis 7). |
| **ER3** | **Diseño y Arquitectura:** Describe la arquitectura, componentes y decisiones clave de diseño. | ✅ | `informe-final.tex` (Cap. 6), C4 model nivel 1-3 y 7 ADRs formales. |
| **ER4** | **Evaluación empírica:** El artefacto se evalúa rigurosamente contra requisitos medibles. | ✅ | Pruebas k6 (50 VUs), usabilidad SUS (N=15), JaCoCo (>=70%), OWASP y Lighthouse. |
| **ER5** | **Amenazas a la validez:** Identifica limitaciones y amenazas según cuatro categorías (constructo, interna, externa, conclusión). | ✅ | `informe-final.tex` (Cap. 10) y `docs/etica/ETHICS.md`. |
| **ER6** | **Disponibilidad y Reproducibilidad:** El artefacto y sus datos están disponibles públicamente para replicación. | ✅ | `make all` ejecutable en un solo comando, DOIs en Zenodo, datos crudos en `docs/mediciones/`. |

---

## Atributos Deseables (Desirable Attributes)

| Ítem | Descripción | Cumplimiento en SBVIA |
|:---:|:---|:---:|
| **D1** | Comparación empírica con trabajos previos y estado del arte. | ✅ Tabla comparativa con 8 trabajos en Cap. 3 y Cap. 9. |
| **D2** | Uso de metodología formal de diseño (Design Science Research - Peffers et al.). | ✅ Declarado en Cap. 5 en 6 etapas metodológicas. |
| **D3** | Análisis estadístico inferencial con intervalos de confianza al 95% y tamaño de efecto. | ✅ Tests Wilcoxon y Cliff's delta reportados en k6. |
