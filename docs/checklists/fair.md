# Checklist de Principios FAIR (Findable, Accessible, Interoperable, Reusable)

Evaluación del paquete de datos, software y metadatos de SBVIA según las directrices de Wilkinson et al. (2016).

---

## 1. Encontrabilidad (Findability)

- [x] **F1. Metadatos y datos asignados con un identificador global único y persistente:**
  - Software: DOI de Zenodo (`10.5281/zenodo.10892341`).
  - Dataset de mediciones: DOI independiente en Zenodo (`10.5281/zenodo.10892342`).
- [x] **F2. Los datos se describen con metadatos enriquecidos:**
  - Definidos en `CITATION.cff` (v1.2.0), `CONTRIBUTORS.md` (CRediT) y `DATA-DICTIONARY.md`.
- [x] **F3. Los metadatos incluyen explícitamente el identificador de los datos que describen:**
  - Enlaces bidireccionales entre el repositorio Git y los registros Zenodo.
- [x] **F4. Los metadatos y datos son indexables y buscables:**
  - Indexados en OpenAIRE, DataCite y Zenodo Search.

---

## 2. Accesibilidad (Accessibility)

- [x] **A1. Recuperables mediante su identificador utilizando un protocolo de comunicaciones estándar y abierto:**
  - Protocolo HTTPS y API REST de GitHub / Zenodo.
- [x] **A1.1. El protocolo es abierto, gratuito y universalmente implementable:**
  - HTTP/HTTPS estándar.
- [x] **A1.2. El protocolo permite autenticación y autorización si es necesario:**
  - Descargas públicas sin restricciones de autenticación.
- [x] **A2. Los metadatos siguen siendo accesibles incluso si los datos ya no están disponibles:**
  - Política de preservación permanente de Zenodo / CERN.

---

## 3. Interoperabilidad (Interoperability)

- [x] **I1. Los datos utilizan un lenguaje formal, accesible y ampliamente aplicable para la representación del conocimiento:**
  - Formatos abiertos y universales: CSV, JSON, Markdown, YAML y LaTeX.
- [x] **I2. Vocabularios controlados que siguen los principios FAIR:**
  - Taxonomía CRediT para contribuciones, clasificaciones ACM CCS 2012 / IEEE Thesaurus para palabras clave.
- [x] **I3. Inclusión de referencias cruzadas cualificadas a otros datos:**
  - Referencias hacia repositorios de código, papers primarios y normas ISO/IEC/IEEE.

---

## 4. Reutilizabilidad (Reusability)

- [x] **R1. Los metadatos y datos se describen profusamente con atributos precisos y relevantes:**
  - `DATA-PROVENANCE.md` detalla qué script genera cada gráfico y desde qué datos brutos.
- [x] **R1.1. Licencia de uso clara y accesible:**
  - Software: Licencia MIT (OSI-approved).
  - Dataset: Licencia Creative Commons Attribution 4.0 International (CC BY 4.0).
- [x] **R1.2. Procedencia detallada:**
  - Trazabilidad hacia commits de Git y hashes SHA256 de contenedores.
- [x] **R1.3. Cumplimiento de estándares de la comunidad:**
  - Adhesión a las guías empíricas ACM SIGSOFT (Ralph et al. 2021) e ISO/IEC/IEEE 29148:2018.
