# Bitácora de Observaciones y Resolución Acumulativa

Esta bitácora consolida el 100% de las observaciones recibidas en los informes de retroalimentación de las Entregas 1A, 1B y 3, documentando la decisión técnica adoptada y el commit con el hash corto donde quedó resuelta.

| Código | Fuente | Criterio Afectado | Texto Íntegro de la Observación | Decisión del Equipo | Commit / Tag | Estado |
|:---:|:---:|:---:|:---|:---|:---:|:---:|
| **OBS-01** | Entrega 1A | D2 | Los 10 RF están redactados como TÍTULOS/sintagmas nominales. NO usan el patrón normativo "El sistema deberá [acción]". | Se reescribieron todos los RFs siguiendo el patrón sintáctico estricto de ISO/IEC/IEEE 29148:2018: `[condición] [sujeto] deberá [acción] [objeto] [restricción]`. | `538dc4a` | Resuelta |
| **OBS-02** | Entrega 1A | D3 | Términos ambiguos en RF-03, RNF-03, RF-07; RF-05 "retorna un veredicto" sin definir; RF-06 encadena tres objetos -> revisar singularidad. | Se eliminó la ambigüedad aplicando las 42 reglas de INCOSE v4 y separando los requisitos compuestos en requisitos atómicos singulares. | `538dc4a` | Resuelta |
| **OBS-03** | Entrega 1A | D4 | Verificabilidad: RF-03/RF-05/RF-07 carecen de umbral objetivo. | Se definieron umbrales cuantitativos exactos medibles en tiempo de respuesta, precisión de cálculo y códigos HTTP. | `538dc4a` | Resuelta |
| **OBS-04** | Entrega 1A | D1 | RNF escasos (solo 4: faltan mantenibilidad/escalabilidad/portabilidad); sin matriz de trazabilidad RF<->RNF. | Se amplió el catálogo de RNFs a 12 alineados a ISO/IEC 25010 y se estructuró la matriz de trazabilidad bidireccional. | `538dc4a` | Resuelta |
| **OBS-05** | Entrega 1B | C5 | Incorporar la colección Postman al repositorio, cubriendo el CRUD completo y la paginación. | Se incorporó la colección Postman en `docs/postman/coleccion.json` con 25 peticiones (éxito, validación, autorización y 404). | `411b8df` | Resuelta |
| **OBS-06** | Entrega 1B | C6 | Añadir la tabla de métricas de rendimiento con tiempos promedio y P95 con y sin caché Redis, y el cálculo del speedup. | Se integró Redis 7 y se midieron las diferencias de latencia frío vs caliente con k6, logrando $p95 < 200\text{ ms}$. | `884bf8a` | Resuelta |
| **OBS-07** | Entrega 1B | C8 | Crear el tag de entrega en el repositorio (p. ej. v0.1.0-entrega-1b). | Se establecieron las etiquetas Git semánticas (`v0.7.0`, `v0.7.1`). | `tag v0.7.0` | Resuelta |
| **OBS-08** | Entrega 3 | P1 | Falta consolidar la estrategia híbrida de acceso a datos con procedimientos almacenados formales sin SQL dinámico. | Se implementaron 6 SPs en `db/procs/`, invocados con `@Procedure` JPA, y se añadió el script `audit-sql-dynamic.sh`. | `793b765` | Resuelta |
| **OBS-09** | Entrega 3 | P3 | Reforzar las cabeceras de seguridad y las cookies de autenticación JWT para cumplir los seis controles OWASP. | Se configuraron cookies con `SameSite=Strict`, `HttpOnly`, `Secure` y filtros de cabeceras de seguridad CSP, HSTS, XCTO. | `fce8198` | Resuelta |
| **OBS-10** | Entrega 3 | R2 | Los datos empíricos deben contener procedencia y diccionario de datos completo para reproducibilidad FAIR. | Se crearon `DATA-DICTIONARY.md` y `DATA-PROVENANCE.md` en `docs/mediciones/` cubriendo el 100% de variables. | `ed9fec1` | Resuelta |
| **OBS-11** | Entrega 3 | D1 | El documento final debe estructurarse rigurosamente bajo el patrón IMRaD ampliado en LaTeX con $\ge 30$ referencias. | Se redactó el documento `informe-final.tex` con los 12 capítulos, anexos y `refs.bib` verificado. | `13ed0b1` | Resuelta |
| **OBS-12** | Entrega 3 | R1 | La reproducción debe ser automática en un solo comando (`make all`) sin intervención manual. | Se perfeccionó el `Makefile` y `docker-compose.yml` para orquestar la compilación, verificación y despliegue automático. | `13ed0b1` | Resuelta |

---

### Resumen de Cumplimiento por Entrega
- **Entrega 1A:** 4 observaciones recibidas | 4 resueltas (**100 %**)
- **Entrega 1B:** 3 observaciones recibidas | 3 resueltas (**100 %**)
- **Entrega 3:** 5 observaciones recibidas | 5 resueltas (**100 %**)
- **Total acumulado:** 12 observaciones | 12 resueltas (**100 %**)
