# Declaración de Ética y Responsabilidad de los Datos

Este documento declara explícitamente los aspectos éticos del uso de datos y de participantes en el proyecto **Simulador de Comportamiento Vial con IA (SBVIA)**, conforme a las directrices de la Tercera Entrega (Bloque F).

## i. Fuentes de datos y su licencia
Los datos utilizados para entrenar o validar los modelos de IA provienen de fuentes públicas y conjuntos de datos abiertos para investigación (por ejemplo, [A multi-class driver behavior dataset](https://doi.org/10.1016/J.DIB.2025.111529)). Estos datos se utilizan exclusivamente bajo los términos de sus licencias originales orientadas a la academia e investigación (generalmente CC BY 4.0 o similares).

## ii. Tratamiento de datos personales
El sistema SBVIA registra datos de cuenta y métricas de comportamiento, como tiempos de reacción e infracciones. Las contraseñas se conservan mediante derivación BCrypt y la comunicación productiva debe utilizar HTTPS. Esto no equivale a afirmar que todos los campos de la base estén cifrados. Los datos no deben compartirse con terceros fuera de las finalidades informadas y autorizadas.

## iii. Consentimiento y aprobación institucional
Para cualquier prueba de usabilidad con personas se requiere aprobación previa de la instancia académica correspondiente y consentimiento informado individual. La plantilla propuesta se encuentra en [plantilla.md](consentimientos/plantilla.md).

El repositorio público **no contiene evidencia suficiente para declarar cerrado este requisito**: falta registrar el código, la fecha y la autoridad de la aprobación, además de confirmar la custodia externa de los 15 consentimientos y del registro de sesiones. Estos documentos no deben publicarse con firmas o datos personales; deberán mostrarse al docente por un canal privado. Hasta esa verificación, el estudio SUS se presenta como evidencia técnica pendiente de regularización ética y no como un estudio institucionalmente aprobado.

## iv. Ausencia de datos identificables en el repositorio
Se certifica que en este repositorio público **no se incluyen datos personales identificables** (PII), ni de los usuarios del sistema, ni de los participantes en las pruebas empíricas. Los datos crudos reportados en `docs/mediciones/` han sido anonimizados mediante códigos identificadores (ej. `P01`, `P02`).

## v. Evidencia que debe presentar el equipo por vía privada

- Documento de aprobación con autoridad, código, título y fecha anterior a la medición.
- Un consentimiento firmado por cada participante, con finalidad, conservación, acceso y retiro.
- Registro de sesión con código anónimo, fecha, hora, duración y modalidad.
- Confirmación de correspondencia entre los 15 códigos del registro y las 15 filas analizadas.
