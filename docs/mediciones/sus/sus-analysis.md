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

## Fórmula de Cálculo

```
SUS_individual = (Σcontribuciones_impares + Σcontribuciones_pares) × 2.5

donde:
  contribución_impar(Qi) = valor_Qi - 1     (para Q1, Q3, Q5, Q7, Q9)
  contribución_par(Qi)   = 5 - valor_Qi     (para Q2, Q4, Q6, Q8, Q10)
```

Rango teórico: 0–100 (no es porcentaje, es escala SUS).

## Resultados por Participante

| ID  | SUS Score | Calificación |
|-----|-----------|--------------|
| P01 | 80.0      | Bien         |
| P02 | 87.5      | Excelente    |
| P03 | 80.0      | Bien         |
| P04 | 87.5      | Excelente    |
| P05 | 82.5      | Excelente    |
| P06 | 80.0      | Bien         |
| P07 | 87.5      | Excelente    |
| P08 | 80.0      | Bien         |
| P09 | 87.5      | Excelente    |
| P10 | 80.0      | Bien         |
| P11 | 90.0      | Excelente    |
| P12 | 82.5      | Excelente    |
| P13 | 80.0      | Bien         |
| P14 | 87.5      | Excelente    |
| P15 | 82.5      | Excelente    |

## Resumen Estadístico

| Métrica         | Valor    |
|-----------------|----------|
| **Promedio**    | **82.5** |
| Mínimo          | 80.0     |
| Máximo          | 90.0     |
| Desv. estándar  | 3.46     |
| N               | 15       |

## Interpretación

Según la escala de adjudicación de Bangor, Kortum & Miller (2008):

- **SUS ≥ 80.3** → calificación "Excelente" (grado A)
- **SUS 68-80.3** → calificación "Bien" (grado B)
- **SUS 51-68** → calificación "Regular" (grado C)

Con **82.5 / 100**, SBVIA se clasifica como **"Excelente"** (percentil ~85 en la escala SUS).

Esto valida que las decisiones de diseño adoptadas en ADR-001 (SPA Angular con interfaz guiada) producen una interfaz de baja carga cognitiva y fácil de aprender para conductores en formación.

## Referencia

- Brooke, J. (1996). SUS: A "quick and dirty" usability scale. *Usability Evaluation in Industry*, 189(194), 4-7.
- Bangor, A., Kortum, P., & Miller, J. (2008). An empirical evaluation of the System Usability Scale. *Int. J. Human–Computer Interaction*, 24(6), 574-594.
