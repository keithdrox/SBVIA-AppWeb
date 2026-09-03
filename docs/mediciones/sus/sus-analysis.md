# Análisis de Usabilidad — System Usability Scale (SUS)

**Proyecto:** SBVIA — Simulador de Comportamiento Vial con IA  
**Fecha de aplicación:** 2026-07-28 / 2026-07-29  
**N participantes:** 15 (conductores en formación, INTAE)  
**Instrumento:** Cuestionario SUS estándar de 10 ítems (escala Likert 1-5)

## Metodología

Se aplicó el cuestionario SUS de Brooke (1996) a 15 participantes durante las sesiones de evaluación. El cuestionario fue administrado inmediatamente después de que cada participante completara una sesión de simulación de 20 minutos en SBVIA.

**Población:** Estudiantes de conducción, edades 18-35 años.  
**Consentimiento:** Todos firmaron formulario de consentimiento informado (ver `docs/etica/consentimientos/`).  
**Datos crudos:** `sus-raw-data.csv` en este mismo directorio.

## Fórmula de Cálculo (Brooke, 1996)

```
SUS_individual = (Σcontribuciones_impares + Σcontribuciones_pares) × 2.5

donde:
  contribución_impar(Qi) = valor_Qi - 1     (para Q1, Q3, Q5, Q7, Q9)
  contribución_par(Qi)   = 5 - valor_Qi     (para Q2, Q4, Q6, Q8, Q10)
```

Rango teórico: 0–100 (no es porcentaje, es escala SUS).

**Ejemplo de verificación — P01** (Q1=4, Q2=2, Q3=4, Q4=2, Q5=4, Q6=2, Q7=4, Q8=2, Q9=4, Q10=2):
- Impares: (4-1)+(4-1)+(4-1)+(4-1)+(4-1) = 3+3+3+3+3 = 15
- Pares:   (5-2)+(5-2)+(5-2)+(5-2)+(5-2) = 3+3+3+3+3 = 15
- Score = (15+15) × 2.5 = **75.0**

## Resultados por Participante

| ID  | SUS Score | Calificación (Bangor et al., 2008) |
|-----|-----------|------------------------------------|
| P01 | 75.0      | Bien (OK)                          |
| P02 | 92.5      | Excelente                          |
| P03 | 75.0      | Bien (OK)                          |
| P04 | 92.5      | Excelente                          |
| P05 | 85.0      | Bien+                              |
| P06 | 75.0      | Bien (OK)                          |
| P07 | 95.0      | Excelente                          |
| P08 | 75.0      | Bien (OK)                          |
| P09 | 92.5      | Excelente                          |
| P10 | 77.5      | Bien                               |
| P11 | 95.0      | Excelente                          |
| P12 | 80.0      | Bien                               |
| P13 | 75.0      | Bien (OK)                          |
| P14 | 95.0      | Excelente                          |
| P15 | 82.5      | Bien+                              |

## Resumen Estadístico

| Métrica             | Valor       |
|---------------------|-------------|
| **Media**           | **84.17**   |
| Mínimo              | 75.0        |
| Máximo              | 95.0        |
| Desv. estándar (DT) | 8.64        |
| IC 95% (t-Student)  | [79.38, 88.95] |
| N                   | 15          |

**Cálculo verificable:**  
Scores: 75, 92.5, 75, 92.5, 85, 75, 95, 75, 92.5, 77.5, 95, 80, 75, 95, 82.5  
Suma = 1262.5 → Media = 1262.5 / 15 = **84.17**

## Interpretación

Según la escala de adjudicación de Bangor, Kortum & Miller (2008):

- **SUS ≥ 85.5** → calificación "Excelente" (grado A)
- **SUS 80.3–85.4** → calificación "Bien+" (grado B+)
- **SUS 68–80.2** → calificación "Bien / Good" (grado B)
- **SUS 51–67** → calificación "Regular" (grado C)

Con **84.17 / 100**, SBVIA se clasifica como **"Bien+" (grado B+)** según los intervalos de interpretación adoptados para este análisis.

Esto valida que las decisiones de diseño adoptadas en ADR-001 (SPA Angular con interfaz guiada) producen una interfaz de carga cognitiva aceptable. El margen de mejora identificado apunta hacia la complejidad del módulo de simulación interactiva (Q2 y Q6 con mayor dispersión).

## Referencia

- Brooke, J. (1996). SUS: A "quick and dirty" usability scale. *Usability Evaluation in Industry*, 189(194), 4-7.
- Bangor, A., Kortum, P., & Miller, J. (2008). An empirical evaluation of the System Usability Scale. *Int. J. Human–Computer Interaction*, 24(6), 574-594.
- Sauro, J., & Lewis, J. R. (2016). *Quantifying the user experience* (2nd ed.). Morgan Kaufmann.
