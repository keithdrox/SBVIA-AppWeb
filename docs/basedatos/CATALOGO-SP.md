# Catálogo de procedimientos almacenados

Los procedimientos se versionan individualmente en `db/procs/`, se instalan mediante las migraciones Flyway V6 y V7 y se declaran con `@Procedure` en `SimulacionRepository`. Todos reciben parámetros tipados; ninguno construye SQL por concatenación.

| Procedimiento | Categoría funcional | Propósito | Parámetros | Resultado | Tablas afectadas |
|---|---|---|---|---|---|
| `sp_resumen_usuario` | Consulta multi-tabla | Resume cantidad de simulaciones y promedio de puntaje de un usuario. | `p_id_usuario integer IN`; `p_total_simulaciones integer OUT`; `p_promedio_puntaje numeric OUT` | Dos valores escalares | `Usuario`, `Simulacion` (lectura) |
| `sp_calcular_puntaje_simulacion` | Cálculo agregado | Resta del puntaje base la suma de penalizaciones y finaliza la simulación. | `p_id_simulacion integer IN` | Sin cursor ni parámetro de salida | `Infraccion` (lectura), `Simulacion` (actualización) |
| `sp_generar_reporte_simulacion` | Reporte | Crea el reporte automático de una simulación existente. | `p_id_simulacion integer IN` | Sin cursor ni parámetro de salida | `Simulacion` (validación), `Reporte` (inserción) |
| `sp_cerrar_simulaciones_vencidas` | Actualización masiva | Marca como vencidas las simulaciones en progreso anteriores a una fecha de corte. | `p_fecha_corte date IN`; `p_actualizadas integer OUT` | Cantidad de filas actualizadas | `Simulacion` (actualización) |
| `sp_validar_simulacion` | Validación cruzada | Comprueba que la simulación exista y que su usuario y escenario estén activos. | `p_id_simulacion integer IN`; `p_valida boolean OUT` | Indicador de validez | `Simulacion`, `Usuario`, `Escenario` (lectura) |
| `sp_generar_codigo_reporte` | Código secuencial | Forma un código estable `REP-########` a partir del identificador de reporte. | `p_id_reporte integer IN`; `p_codigo varchar OUT` | Código generado | Ninguna |

## Invocación desde Java

Las firmas formales se encuentran en `backend/src/main/java/com/sbvia/backend/repository/SimulacionRepository.java`. Spring Data JPA registra los parámetros mediante `@Param` y los parámetros de salida mediante `outputParameterName`, conforme al contrato de cada procedimiento.

## Cursores

Ningún procedimiento devuelve cursores. Los resultados requeridos por el dominio son escalares o actualizaciones controladas.
