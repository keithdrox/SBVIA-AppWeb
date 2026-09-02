# Bitácora de Cambios de Requisitos (CHANGELOG-REQ.md)

Control formal del ciclo de vida y evolución de requisitos según la norma ISO/IEC/IEEE 29148:2018 entre la Entrega 1A, 1B, 3 y la Entrega Final (v1.0.0).

---

## 1. Métricas de Calidad y Estabilidad de Requisitos

- **Requisitos Totales Especificados ($N_{total}$):** 20 (8 Funcionales, 6 No Funcionales, 6 Historias de Usuario).
- **Requisitos Modificados ($N_{mod}$):** 2 (RF-03 y RF-04 ajustados para estrategia híbrida de procedimientos almacenados).
- **Requisitos Agregados ($N_{add}$):** 4 (RF-05 a RF-08 para reportería, actualización masiva y códigos secuenciales).
- **Requisitos Eliminados ($N_{del}$):** 0.
- **Tasa de Estabilidad de Requisitos ($T_{est}$):**
  $$T_{est} = 1 - \frac{N_{mod}}{N_{total}} = 1 - \frac{2}{20} = 0.90 \quad (90.0\%)$$
- **Porcentaje de Requisitos con Prioridad *Must* Verificados:** **85.71 %** ($12/14$). RNF-01 y RNF-02 permanecen parciales hasta completar cinco corridas formales y un protocolo frío/caliente comparable.

---

## 2. Historial de Cambios por Versión

| Versión | Fecha | Requisito Afectado | Tipo de Cambio | Justificación Técnica |
|:---:|:---:|:---|:---:|:---|
| `v0.3.0` | 2026-06-04 | RF-01 a RF-10 | Creación inicial | Elicitación de requisitos base del simulador. |
| `v0.7.0` | 2026-06-14 | RF-01, RF-02 | Modificación | Adaptación a autenticación stateless con JWT y CRUD JPA. |
| `v0.9.0` | 2026-07-24 | RNF-01, RNF-02 | Modificación | Incorporación de caché Redis y umbrales p95 en k6. |
| `v1.0.0` | 2026-08-17 | RF-03 a RF-08 | Agregación / Refactor | Consolidación de estrategia híbrida con 6 Procedimientos Almacenados en PostgreSQL. |
