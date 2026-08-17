# Catálogo de Procedimientos Almacenados y Funciones SQL

| Nombre | Categoría Funcional | Propósito | Parámetros Entrada | Parámetros Salida | Tablas Afectadas |
| --- | --- | --- | --- | --- | --- |
| `sp_reporte_simulacion` | Consultas multi-tabla | Obtiene un reporte detallado uniendo Simulacion, Usuario, Escenario y MetricaDesempeno | `p_id_simulacion` (IN, INTEGER) | `simulacion_id`, `usuario_nombre`, `escenario_nombre`, `puntaje_final`, `estado_simulacion`, `tiempo_reaccion` | Ninguna (Lectura) |
| `sp_calcular_promedio_usuario` | Cálculos agregados | Calcula el puntaje promedio histórico de un usuario | `p_id_usuario` (IN, INTEGER) | `promedio` (OUT, DECIMAL) | Ninguna (Lectura) |
| `sp_reporte_actividad_diaria` | Reportes | Genera un reporte de simulaciones realizadas en una fecha | `p_fecha` (IN, DATE) | `total_simulaciones`, `promedio_puntaje` | Ninguna (Lectura) |
| `sp_actualizar_usuarios_inactivos` | Actualizaciones masivas | Desactiva usuarios cuya fecha de registro sea menor a la indicada | `p_fecha_limite` (IN, DATE) | `actualizados` (OUT, INTEGER) | `Usuario` (Escritura) |
| `sp_validar_escenario` | Validaciones cruzadas | Valida si un escenario cumple con el mínimo de reglas de tránsito | `p_id_escenario` (IN, INTEGER) | `es_valido` (OUT, BOOLEAN) | Ninguna (Lectura) |
| `sp_generar_codigo_certificado` | Generación secuencial | Genera un código de certificado único basado en secuencia y año | `p_id_simulacion` (IN, INTEGER) | `codigo_certificado` (OUT, VARCHAR) | Secuencia `seq_certificado` |
