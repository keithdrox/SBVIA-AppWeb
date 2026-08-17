# INCOSE Guide to Writing Requirements Checklist (INCOSE v4)

Evaluación de los requisitos de SBVIA según las 9 características individuales (C1-C9), 6 características de conjunto (C10-C15) y reglas de redacción de INCOSE v4.

---

## 1. Características de Calidad para Requisitos Individuales (C1–C9)

| Código | Característica | Estado | Justificación / Mecanismo de Control |
|:---:|:---|:---:|:---|
| **C1** | **Necessary** (Necesario) | ✅ | Cada requisito se deriva de las necesidades de formación de conductores (RF-01 a RF-08). |
| **C2** | **Appropriate** (Apropiado) | ✅ | Pertenece al nivel de abstracción del sistema sin restringir indebidamente la implementación interna. |
| **C3** | **Unambiguous** (No ambiguo) | ✅ | Redactados usando la sintaxis estricta: `[condición] [sujeto] deberá [acción] [objeto] [restricción]`. |
| **C4** | **Complete** (Completo) | ✅ | Contiene toda la información requerida sin omitir restricciones ni condiciones previas. |
| **C5** | **Singular** (Singular) | ✅ | Expresa una sola función o propiedad; no encadena cláusulas complejas con conjunciones múltiples. |
| **C6** | **Feasible** (Factible) | ✅ | Implementable técnicamente dentro de la pila Java 21, Angular 17, PostgreSQL y Redis. |
| **C7** | **Verifiable** (Verificable) | ✅ | Trazado a una prueba unitaria, de integración, test k6 o script automatizado en `matriz.csv`. |
| **C8** | **Correct** (Correcto) | ✅ | Validado por los docentes y alineado con los objetivos de aprendizaje vial. |
| **C9** | **Conforming** (Conforme) | ✅ | Sigue estrictamente la plantilla ISO/IEC/IEEE 29148:2018 y las 42 reglas de redacción de INCOSE. |

---

## 2. Características de Calidad para el Conjunto de Requisitos (C10–C15)

| Código | Característica | Estado | Justificación |
|:---:|:---|:---:|:---|
| **C10** | **Complete** (Conjunto Completo) | ✅ | Cubre el ciclo completo de autenticación, simulación, analítica, reportería y administración. |
| **C11** | **Consistent** (Consistente) | ✅ | No existen contradicciones entre requisitos funcionales y no funcionales. |
| **C12** | **Feasible** (Factible en conjunto) | ✅ | El sistema completo es ejecutable bajo los recursos asignados (2 vCPU, 2 GB RAM). |
| **C13** | **Comprehensible** (Comprensible) | ✅ | Glosario unificado y estructura clara para lectores técnicos y académicos. |
| **C14** | **Able to be validated** (Validable) | ✅ | Puede demostrarse su cumplimiento total en la evaluación en vivo durante la defensa. |
| **C15** | **Correct** (Conjunto Correcto) | ✅ | Representa fielmente los requerimientos del Proyecto Fin de Curso. |
