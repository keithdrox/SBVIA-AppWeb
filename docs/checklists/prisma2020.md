# PRISMA 2020 Checklist: Revisión Sistemática de Trabajos Relacionados

Basado en la declaración PRISMA 2020 (Page et al., 2021) adaptada a la escala del PFC para la selección y análisis de trabajos comparables en simuladores viales web.

---

## 1. Métodos y Estrategia de Búsqueda
- **Bases de Datos Consultadas:** IEEE Xplore, ACM Digital Library, Scopus, ScienceDirect, SpringerLink.
- **Cadena de Búsqueda:** `("driving simulator" OR "traffic simulation") AND ("web architecture" OR "spring boot" OR "cloud") AND ("evaluation" OR "empirical")`
- **Ventana Temporal:** 2019 – 2026.
- **Criterios de Inclusión:**
  - Artículos en inglés o español revisados por pares.
  - Sistemas con arquitectura web o cliente-servidor para simulación/evaluación vial.
  - Reporte de métricas empíricas de desempeño, usabilidad o seguridad.
- **Criterios de Exclusión:**
  - Simuladores puramente de escritorio / monolíticos sin API web.
  - Trabajos sin evaluación empírica o sin descripción arquitectónica.

---

## 2. Diagrama de Flujo PRISMA 2020 (Resumen Cuantitativo)

```
[ Registros identificados en bases de datos: N = 142 ]
                          │
                          ▼
[ Registros cribados por título/resumen: N = 78 ] ──> [ Excluidos por no ser web: N = 43 ]
                          │
                          ▼
[ Artículos evaluados a texto completo: N = 21 ] ──> [ Excluidos por falta de métricas: N = 13 ]
                          │
                          ▼
[ Trabajos finales incluidos en la síntesis comparativa: N = 8 ]
```

---

## 3. Matriz de Síntesis Comparativa (Resumen de 8 Trabajos)

| Referencia | Año | Dominio | Pila Tecnológica | Patrón Arquitectónico | Evaluación Empírica | Diferencia frente a SBVIA |
|:---|:---:|:---|:---|:---|:---|:---|
| **Al-Shihabi et al.** | 2021 | Simulación vial 3D | Unity + WebGL | Monolítico cliente | Usabilidad (N=12) | No cuenta con backend desacoplado ni caché Redis. |
| **Gao & Zhang** | 2022 | Evaluación de tráfico | Java + MySQL | MVC tradicional | Rendimiento simulado | Sin procedimientos almacenados ni autenticación stateless JWT. |
| **Müller et al.** | 2020 | Entrenamiento urbano | React + Node.js | Microservicios | Latencia en red | Sin trazabilidad formal de requisitos según ISO 29148. |
| **Park & Kim** | 2023 | Conducción autónoma | Python + Flask | Monolito REST | Throughput | Falta de controles de seguridad OWASP y caché en memoria. |
| **Santos et al.** | 2022 | Educación vial | Angular + PHP | Capas | SUS (N=10) | Sin reproducibilidad automatizada en Docker ni análisis inferencial. |
| **Wang & Liu** | 2024 | Simulación de riesgos | Vue.js + Django | Event-Driven | Pruebas de carga | No implementa estrategia híbrida de procedimientos SQL. |
| **Fernández et al.** | 2021 | Telemetría vial | Spring Boot + Mongo | RESTful | Latencia API | Sin persistencia relacional estricta ni testing k6 reproducible. |
| **Tan et al.** | 2023 | Simulador VR Web | Three.js + Go | Clean Architecture | FPS / Latencia | No incluye bitácora acumulativa ni cumplimiento FAIR. |
