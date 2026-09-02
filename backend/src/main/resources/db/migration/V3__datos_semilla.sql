-- ============================================================
-- V3__datos_semilla.sql
-- Datos semilla (más de 50 registros) para la Unidad III (Práctica Experimental).
-- Carga 30 escenarios de simulación + 26 reglas de tránsito = 56 registros.
-- No depende de roles ni de usuarios (esas tablas se pueblan en V5).
-- ============================================================

-- 1. Escenarios de simulación (30 registros)
INSERT INTO "Escenario" ("nombre", "descripcion", "tipo_via", "nivel_dificultad", "clima", "densidad_trafico") VALUES
('Av. 10 de Agosto', 'Avenida principal con semáforos y cruces peatonales', 'URBANA', 2, 'SOLEADO', 'MEDIA'),
('Av. Universitaria', 'Zona universitaria con alto flujo peatonal', 'URBANA', 3, 'NUBLADO', 'ALTA'),
('Calle García Moreno', 'Calle céntrica de doble sentido', 'URBANA', 2, 'SOLEADO', 'MEDIA'),
('Av. Amazonas', 'Avenida de alto tráfico vehicular', 'URBANA', 4, 'NOCTURNO', 'ALTA'),
('Calle Vargas', 'Vía secundaria residencial', 'URBANA', 1, 'SOLEADO', 'BAJA'),
('Av. Patria', 'Av. de conexión norte-sur', 'URBANA', 3, 'LLUVIOSO', 'ALTA'),
('Av. Gran Colombia', 'Avenida con carriles exclusivos de bus', 'URBANA', 3, 'SOLEADO', 'ALTA'),
('Calle La Ronda', 'Vía turística angosta', 'URBANA', 2, 'NOCTURNO', 'BAJA'),
('Calle Imbabura', 'Vía residencial con reductores de velocidad', 'URBANA', 2, 'SOLEADO', 'BAJA'),
('Av. 6 de Diciembre', 'Avenida con puentes y pasos elevados', 'URBANA', 4, 'LLUVIOSO', 'ALTA'),
('Troncal de la Sierra Norte', 'Carretera interprovincial', 'RURAL', 4, 'LLUVIOSO', 'MEDIA'),
('Vía a la Costa', 'Ruta costera con curvas pronunciadas', 'RURAL', 5, 'LLUVIOSO', 'ALTA'),
('Carretera Panamericana Sur', 'Tramo rural de alta velocidad', 'RURAL', 3, 'SOLEADO', 'BAJA'),
('Camino a Nono', 'Carretera de montaña con pendientes', 'RURAL', 5, 'NUBLADO', 'BAJA'),
('Vía Interoceánica', 'Carretera de montaña con bancos de niebla', 'RURAL', 5, 'NOCTURNO', 'MEDIA'),
('Carretera Ambato-Riobamba', 'Tramo rural con tráfico intermitente', 'RURAL', 3, 'SOLEADO', 'MEDIA'),
('Vía Rafael Quintero', 'Conexión rural a zonas agrícolas', 'RURAL', 2, 'SOLEADO', 'BAJA'),
('Carretera Ibarra-San Lorenzo', 'Ruta rural de selva', 'RURAL', 4, 'LLUVIOSO', 'BAJA'),
('Autopista General Rumiñahui', 'Autopista de alta velocidad con peaje', 'AUTOPISTA', 4, 'SOLEADO', 'ALTA'),
('Autopista Guayaquil-Samborondón', 'Autopista plana de alta velocidad', 'AUTOPISTA', 3, 'NOCTURNO', 'ALTA'),
('Autopista Quito-El Carmen', 'Autopista de montaña', 'AUTOPISTA', 5, 'NUBLADO', 'MEDIA'),
('Autopista Durán-Tambo', 'Autopista con accesos industriales', 'AUTOPISTA', 3, 'LLUVIOSO', 'ALTA'),
('Autopista Cuenca-Azogues', 'Autopista con túneles', 'AUTOPISTA', 4, 'NOCTURNO', 'MEDIA'),
('Periférico de Guayaquil', 'Anillo periférico de alta capacidad', 'AUTOPISTA', 3, 'SOLEADO', 'ALTA'),
('Vía Perimetral Metropolitana', 'Ruta mixta urbano-rural', 'MIXTA', 3, 'NUBLADO', 'MEDIA'),
('Anillo Vial de Quito', 'Circunvalación mixta', 'MIXTA', 4, 'NOCTURNO', 'ALTA'),
('Conexión Vía a la Costa - Perimetral', 'Empalme mixto', 'MIXTA', 2, 'LLUVIOSO', 'MEDIA'),
('Ruta de Integración Suroriente', 'Vía mixta en expansión', 'MIXTA', 3, 'SOLEADO', 'MEDIA'),
('Corredor Central de Ambato', 'Corredor mixto intermodal', 'MIXTA', 3, 'NUBLADO', 'ALTA'),
('Vía Alterna Norte', 'Ruta mixta de descongestión', 'MIXTA', 4, 'LLUVIOSO', 'ALTA');

-- 2. Reglas de tránsito por escenario (26 registros)
INSERT INTO "ReglaTransito" ("nombre", "descripcion", "categoria", "id_Escenario") VALUES
('Límite 50 km/h', 'No superar 50 km/h en zona urbana', 'Velocidad', 1),
('Ceda el paso peatonal', 'Detenerse en todos los cruces peatonales', 'Prioridad', 1),
('Semáforo en rojo = alto total', 'Respetar los semáforos', 'Señalización', 2),
('No adelantar en zona escolar', 'Prohibido adelantar cerca de planteles', 'Adelantamiento', 2),
('Velocidad máxima 30 km/h', 'Reducir velocidad en zona comercial', 'Velocidad', 3),
('Distancia de seguridad 2s', 'Mantener distancia con el vehículo frontal', 'Seguridad', 4),
('No usar celular', 'Prohibido el uso del celular al conducir', 'Seguridad', 4),
('Ceder paso a la derecha', 'Ceder el paso en intersecciones sin señal', 'Prioridad', 5),
('Carril izquierdo solo adelantar', 'Usar el carril izquierdo solo para rebasar', 'Adelantamiento', 6),
('Cinturón obligatorio', 'Todos los ocupantes con cinturón', 'Seguridad', 7),
('Velocidad máxima 20 km/h', 'Limitar velocidad en zona patrimonial', 'Velocidad', 8),
('Respetar reductores', 'Reducir velocidad en juanmortales', 'Señalización', 9),
('No cambiar de carril en puente', 'Prohibido el cambio de carril en estructuras', 'Adelantamiento', 10),
('Adelantamiento prohibido', 'Prohibido adelantar en curvas', 'Adelantamiento', 11),
('Clima adverso: doble distancia', 'Duplicar distancia en lluvia', 'Seguridad', 12),
('Luces bajas encendidas', 'Usar luces bajas en tramo rural', 'Iluminación', 13),
('Velocidad máxima 40 km/h', 'Reducir velocidad en pendiente', 'Velocidad', 14),
('Precaución por niebla', 'Reducir velocidad ante bancos de niebla', 'Clima', 15),
('No estacionar en berma', 'Prohibido detenerse en la berma', 'Estacionamiento', 16),
('Respetar paso de ganado', 'Ceder paso a animales en la vía', 'Prioridad', 17),
('Velocidad máxima 60 km/h', 'Límite en tramo de selva', 'Velocidad', 18),
('Peaje: pago anticipado', 'Carril reducido en caseta de peaje', 'Señalización', 19),
('Velocidad máxima 120 km/h', 'Límite máximo en autopista', 'Velocidad', 20),
('No frenar bruscamente', 'Frenado progresivo en descenso', 'Seguridad', 21),
('Respetar paso a nivel', 'Detenerse ante cruce de ferrocarril', 'Prioridad', 22),
('Vía de incorporación', 'Cedido en carril de aceleración', 'Adelantamiento', 23);
