# Diccionario de Datos del Paquete Empírico (DATA-DICTIONARY.md)

Este documento describe el 100% de las variables presentes en los archivos de datos crudos recolectados durante la evaluación del sistema SBVIA v1.0.0.

---

## 1. Archivo: `docs/mediciones/sus/sus-raw-data.csv` (Evaluación de Usabilidad SUS)

| Variable | Tipo | Unidad | Rango | Significado |
|:---|:---:|:---:|:---:|:---|
| `participant_id` | String | Identificador | `P01` – `P15` | Código anónimo único asignado a cada participante del estudio. |
| `age` | Integer | Años | $18 – 45$ | Edad cronológica del participante. |
| `gender` | Categorical | N/A | `M`, `F`, `Other` | Género autopercibido del participante. |
| `device` | Categorical | N/A | `Desktop`, `Laptop`, `Mobile` | Dispositivo empleado durante la prueba de usabilidad. |
| `web_experience` | Integer | Escala Likert | $1 – 5$ | Nivel de experiencia previa con plataformas web (1 = Principiante, 5 = Experto). |
| `q1` a `q10` | Integer | Escala Likert | $1 – 5$ | Respuestas individuales a los 10 ítems estándar de la escala SUS de Brooke (1996). |
| `sus_score` | Float | Puntos | $0 – 100$ | Puntuación compuesta estandarizada calculada según el algoritmo SUS. |

---

## 2. Archivo: `docs/mediciones/perf/k6-results-summary.csv` (Rendimiento y Carga)

| Variable | Tipo | Unidad | Rango | Significado |
|:---|:---:|:---:|:---:|:---|
| `run_id` | Integer | Identificador | $1 – 5$ | Número de corrida independiente del benchmark k6. |
| `scenario_type` | Categorical | N/A | `cache_cold`, `cache_hot` | Estado de la caché Redis durante la ejecución de la prueba. |
| `vus` | Integer | Conexiones | $50$ | Número de usuarios virtuales concurrentes ejecutando solicitudes. |
| `duration_sec` | Integer | Segundos | $30$ | Duración total de la ventana de medición. |
| `http_req_total` | Integer | Peticiones | $\ge 1000$ | Total de solicitudes HTTP procesadas exitosamente. |
| `p50_ms` | Float | Milisegundos | $\ge 0$ | Mediana (percentil 50) del tiempo de respuesta del servidor. |
| `p90_ms` | Float | Milisegundos | $\ge 0$ | Percentil 90 de la latencia de respuesta. |
| `p95_ms` | Float | Milisegundos | $\ge 0$ | Percentil 95 de la latencia (métrica principal del umbral de calidad). |
| `p99_ms` | Float | Milisegundos | $\ge 0$ | Percentil 99 de la latencia de respuesta. |
| `mean_ms` | Float | Milisegundos | $\ge 0$ | Media aritmética de los tiempos de respuesta. |
| `std_dev_ms` | Float | Milisegundos | $\ge 0$ | Desviación típica muestral de los tiempos de respuesta. |
| `ci95_lower` | Float | Milisegundos | $\ge 0$ | Límite inferior del intervalo de confianza al 95%. |
| `ci95_upper` | Float | Milisegundos | $\ge 0$ | Límite superior del intervalo de confianza al 95%. |
| `error_rate` | Float | Porcentaje | $0.00 – 100.0$ | Tasa de errores HTTP $\ge 400$ registrados. |

---

## 3. Archivo: `docs/mediciones/jacoco/coverage-summary.csv` (Cobertura de Código)

| Variable | Tipo | Unidad | Rango | Significado |
|:---|:---:|:---:|:---:|:---|
| `package_name` | String | N/A | `service`, `controller`, `entity` | Paquete analizado dentro del backend Spring Boot. |
| `total_lines` | Integer | Líneas | $\ge 0$ | Total de líneas de código ejecutables según JaCoCo. |
| `covered_lines` | Integer | Líneas | $\ge 0$ | Líneas de código ejecutadas durante la suite de pruebas unitarias. |
| `line_coverage_pct` | Float | Porcentaje | $0.0 – 100.0$ | Porcentaje de cobertura de líneas ($(\text{covered}/\text{total}) \times 100$). |
| `total_branches` | Integer | Ramas | $\ge 0$ | Total de ramas condicionales (if/switch/ternarios). |
| `covered_branches` | Integer | Ramas | $\ge 0$ | Ramas lógicas evaluadas en ambas direcciones lógicas. |
| `branch_coverage_pct`| Float | Porcentaje | $0.0 – 100.0$ | Porcentaje de cobertura de ramas lógicas. |
