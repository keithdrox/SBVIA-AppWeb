# Casos de Uso (Estructura Alistair Cockburn)

A continuación se presentan 5 casos de uso esenciales para el sistema SBVIA, modelados según el formato propuesto por Alistair Cockburn.

---

## CU-01: Iniciar Simulación Vial
- **Actor Principal:** Conductor en formación.
- **Nivel:** Objetivo de usuario.
- **Precondiciones:** El conductor ha iniciado sesión y se encuentra en el Dashboard. Existen escenarios activos.
- **Garantías de Éxito (Postcondiciones):** Se crea un registro de `Simulacion` con estado `EN_PROGRESO` en la BD.
- **Garantías Mínimas:** Si el sistema falla, no se registra puntaje negativo, se cancela la transacción.
- **Flujo Principal:**
  1. El conductor selecciona la opción "Ver Escenarios".
  2. El sistema muestra los escenarios disponibles.
  3. El conductor selecciona "Escenario Básico - Día despejado".
  4. El sistema carga los recursos 3D y las reglas asociadas.
  5. El conductor presiona "Comenzar".
  6. El sistema inicia el temporizador y la recolección de métricas.
- **Extensiones (Flujos Alternativos):**
  - 4a. Falla la carga del escenario por desconexión:
    1. El sistema muestra un mensaje de "Error de Red".
    2. El sistema regresa al conductor al menú de escenarios.

## CU-02: Evaluar Decisión en Tiempo Real
- **Actor Principal:** Sistema (Backend API).
- **Actores de Apoyo:** API de IA Externa.
- **Nivel:** Sub-función.
- **Precondiciones:** Simulación activa en el cliente.
- **Garantías de Éxito:** Se guarda un registro en `Decision` y `ComportamientoVial`.
- **Flujo Principal:**
  1. El conductor realiza una acción (ej. frena bruscamente).
  2. El cliente envía el evento al Backend.
  3. El Backend delega el evento a la API de IA Externa.
  4. La IA responde indicando "Riesgo Medio: Frenado inseguro".
  5. El Backend calcula la penalización y guarda la decisión.
  6. El Backend retorna la retroalimentación al cliente.
- **Extensiones:**
  - 3a. La IA externa no responde (Timeout):
    1. El Backend procesa el evento usando reglas heurísticas de respaldo.
    2. Continúa el paso 5.

## CU-03: Crear Nuevo Escenario
- **Actor Principal:** Administrador.
- **Nivel:** Objetivo de usuario.
- **Precondiciones:** El administrador está autenticado con `ROLE_ADMIN`.
- **Garantías de Éxito:** El escenario se guarda y es listado en la plataforma.
- **Flujo Principal:**
  1. El administrador accede al módulo de "Gestión de Escenarios".
  2. Solicita crear un nuevo escenario.
  3. El sistema presenta el formulario de configuración (Clima, Densidad, Reglas).
  4. El administrador llena el formulario y envía.
  5. El sistema valida los datos.
  6. El sistema persiste el `Escenario` en PostgreSQL.
  7. El sistema muestra mensaje de éxito.

## CU-04: Generar Reporte de Rendimiento
- **Actor Principal:** Conductor / Instructor.
- **Nivel:** Objetivo de usuario.
- **Precondiciones:** Existe al menos una simulación finalizada para el usuario consultado.
- **Garantías de Éxito:** Se renderiza y entrega un reporte en pantalla.
- **Flujo Principal:**
  1. El actor solicita ver el historial de simulaciones.
  2. Selecciona una simulación completada.
  3. El sistema invoca un *Stored Procedure* para calcular las métricas agregadas (Aciertos, Errores, Tiempo de Reacción).
  4. El sistema presenta el dashboard visual con gráficos y recomendaciones.

## CU-05: Invalidad Sesión (Logout)
- **Actor Principal:** Usuario (Cualquier rol).
- **Nivel:** Sub-función.
- **Precondiciones:** Usuario posee un Token JWT válido.
- **Garantías de Éxito:** El token actual pierde validez inmediatamente.
- **Flujo Principal:**
  1. El usuario presiona "Cerrar sesión".
  2. El cliente envía la solicitud al Backend con el token.
  3. El Backend almacena el JTI (ID del token) en Redis con un TTL igual al tiempo restante de vida del token.
  4. El Backend responde OK.
  5. El cliente elimina el token del LocalStorage y redirige a la portada.
