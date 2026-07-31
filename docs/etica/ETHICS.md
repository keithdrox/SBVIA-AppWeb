# Declaración de Ética y Responsabilidad de los Datos

Este documento declara explícitamente los aspectos éticos del uso de datos y de participantes en el proyecto **Simulador de Comportamiento Vial con IA (SBVIA)**, conforme a las directrices de la Tercera Entrega (Bloque F).

## i. Fuentes de datos y su licencia
Los datos utilizados para entrenar o validar los modelos de IA provienen de fuentes públicas y conjuntos de datos abiertos para investigación (por ejemplo, [A multi-class driver behavior dataset](https://doi.org/10.1016/J.DIB.2025.111529)). Estos datos se utilizan exclusivamente bajo los términos de sus licencias originales orientadas a la academia e investigación (generalmente CC BY 4.0 o similares).

## ii. Tratamiento de datos personales
El sistema SBVIA recopila métricas de rendimiento y comportamiento (como tiempos de reacción e infracciones) durante las simulaciones. Toda la información personal de los usuarios está encriptada y **no se comparte con terceros**. 

## iii. Mecanismo de consentimiento informado
Para las pruebas de usabilidad (System Usability Scale - SUS) y evaluaciones del sistema, se requiere que los participantes lean y firmen un formulario de **consentimiento informado**. La plantilla utilizada se encuentra en [plantilla.md](consentimientos/plantilla.md). Los consentimientos firmados (físicos o digitales) se resguardan de forma segura y **fuera del repositorio público** para proteger la identidad de los participantes.

## iv. Ausencia de datos identificables en el repositorio
Se certifica que en este repositorio público **no se incluyen datos personales identificables** (PII), ni de los usuarios del sistema, ni de los participantes en las pruebas empíricas. Los datos crudos reportados en `docs/mediciones/` han sido anonimizados mediante códigos identificadores (ej. `P01`, `P02`).
