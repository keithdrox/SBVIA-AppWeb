# Diccionario de Datos

Este diccionario describe las tablas principales de la base de datos de SBVIA, complementando el DDL provisto en `V1__schema_completo.sql`.

## Tabla: Usuario
Almacena los datos de autenticación y perfil de los actores del sistema (Conductores, Instructores, Administradores).
- `id_Usuario` (PK, Integer): Identificador único autoincremental.
- `nombre` (Varchar): Nombre(s) del usuario.
- `apellido` (Varchar): Apellido(s) del usuario.
- `email` (Varchar, UNIQUE): Correo electrónico (usado para login).
- `password_hash` (Varchar): Contraseña encriptada con BCrypt.
- `id_Rol` (FK, Integer): Referencia a la tabla `Rol`.
- `activo` (Boolean): Indica si la cuenta está habilitada.

## Tabla: Escenario
Configuraciones predefinidas del entorno virtual 3D.
- `id_Escenario` (PK, Integer): Identificador único.
- `nombre` (Varchar): Nombre descriptivo (ej. "Ciudad Lluvia Fuerte").
- `tipo_via` (Varchar): Tipo (Urbana, Rural, Autopista).
- `nivel_dificultad` (Integer): Rango del 1 al 10.
- `clima` (Varchar): Soleado, Lluvioso, Niebla, Nieve.
- `densidad_trafico` (Varchar): Baja, Media, Alta.

## Tabla: Simulacion
Registra la ejecución de un escenario por parte de un usuario.
- `id_Simulacion` (PK, Integer): Identificador único.
- `fecha_inicio` (Date): Timestamp de arranque.
- `fecha_fin` (Date): Timestamp de conclusión.
- `estado` (Varchar): EN_PROGRESO, FINALIZADA, ABORTADA.
- `puntaje_final` (Decimal): Calculado por el Stored Procedure `sp_calcular_puntaje_simulacion`.
- `id_Usuario` (FK, Integer): Usuario que conduce.
- `id_Escenario` (FK, Integer): Entorno cargado.

## Tabla: Infraccion
Eventos negativos detectados por la IA o las reglas del motor.
- `id_Infraccion` (PK, Integer): Identificador.
- `nombre` (Varchar): Tipo de falta (ej. "Semáforo en rojo").
- `gravedad` (Varchar): LEVE, GRAVE, MUY_GRAVE.
- `penalizacion` (Decimal): Puntos a descontar.
- `id_Simulacion` (FK, Integer): Simulación donde ocurrió.

*(Nota: Para el detalle de todas las entidades menores como `Decision`, `EventoVial`, `Rol`, revisar `docs/arquitectura/diagramas-c4.md` o el DDL directo en la carpeta `db/migration`)*
