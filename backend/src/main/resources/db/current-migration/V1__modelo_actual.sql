--
-- PostgreSQL database dump
--



-- Dumped from database version 16.15
-- Dumped by pg_dump version 16.15

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: fn_actualizar_ultimo_acceso(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_ultimo_acceso() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.acceso_exitoso = TRUE THEN
        UPDATE usuario
        SET ultimo_acceso = NEW.fecha_hora,
            intentos_fallidos = 0
        WHERE id_usuario = NEW.id_usuario;
    ELSE
        UPDATE usuario
        SET intentos_fallidos = intentos_fallidos + 1
        WHERE id_usuario = NEW.id_usuario;
    END IF;

    RETURN NEW;
END;
$$;


--
-- Name: fn_finalizar_simulacion_por_progreso(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_finalizar_simulacion_por_progreso() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_estado_completada INTEGER;
BEGIN
    IF NEW.porcentaje >= 100 THEN
        SELECT id_estado_simulacion
          INTO v_estado_completada
          FROM estado_simulacion
         WHERE nombre = 'COMPLETADA'
         LIMIT 1;

        UPDATE simulacion
           SET porcentaje_progreso = 100,
               fecha_fin = COALESCE(fecha_fin, NEW.fecha_hora),
               id_estado_simulacion = COALESCE(
                   v_estado_completada,
                   id_estado_simulacion
               )
         WHERE id_simulacion = NEW.id_simulacion;
    END IF;

    RETURN NEW;
END;
$$;


--
-- Name: fn_recalcular_puntaje_simulacion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_recalcular_puntaje_simulacion() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_simulacion BIGINT;
    v_penalizacion NUMERIC(10,2);
BEGIN
    v_id_simulacion := COALESCE(NEW.id_simulacion, OLD.id_simulacion);

    SELECT COALESCE(SUM(penalizacion_aplicada), 0)
      INTO v_penalizacion
      FROM infraccion
     WHERE id_simulacion = v_id_simulacion;

    UPDATE simulacion
       SET puntaje_final = GREATEST(0, 100 - v_penalizacion)
     WHERE id_simulacion = v_id_simulacion;

    RETURN COALESCE(NEW, OLD);
END;
$$;


--
-- Name: fn_sincronizar_progreso_simulacion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_sincronizar_progreso_simulacion() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE simulacion
    SET porcentaje_progreso = GREATEST(porcentaje_progreso, NEW.porcentaje)
    WHERE id_simulacion = NEW.id_simulacion;

    RETURN NEW;
END;
$$;


--
-- Name: fn_validar_usuario_sesion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_validar_usuario_sesion() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_usuario_sesion BIGINT;
BEGIN
    SELECT id_usuario
    INTO v_usuario_sesion
    FROM sesion_entrenamiento
    WHERE id_sesion = NEW.id_sesion;

    IF v_usuario_sesion IS NULL THEN
        RAISE EXCEPTION 'La sesi??n % no existe.', NEW.id_sesion;
    END IF;

    IF v_usuario_sesion <> NEW.id_usuario THEN
        RAISE EXCEPTION
            'El usuario de la simulaci??n debe coincidir con el usuario de la sesi??n.';
    END IF;

    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: comportamiento_vial; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.comportamiento_vial (
    id_comportamiento bigint NOT NULL,
    id_simulacion bigint NOT NULL,
    clasificacion character varying(30) NOT NULL,
    nivel_riesgo integer NOT NULL,
    puntaje_seguridad numeric(5,2),
    puntaje_responsabilidad numeric(5,2),
    puntaje_cumplimiento numeric(5,2),
    observaciones text,
    fecha_evaluacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_comportamiento_clasificacion CHECK (((clasificacion)::text = ANY (ARRAY[('EXCELENTE'::character varying)::text, ('BUENO'::character varying)::text, ('REGULAR'::character varying)::text, ('RIESGOSO'::character varying)::text, ('CRITICO'::character varying)::text]))),
    CONSTRAINT chk_comportamiento_cumplimiento CHECK (((puntaje_cumplimiento IS NULL) OR ((puntaje_cumplimiento >= (0)::numeric) AND (puntaje_cumplimiento <= (100)::numeric)))),
    CONSTRAINT chk_comportamiento_responsabilidad CHECK (((puntaje_responsabilidad IS NULL) OR ((puntaje_responsabilidad >= (0)::numeric) AND (puntaje_responsabilidad <= (100)::numeric)))),
    CONSTRAINT chk_comportamiento_riesgo CHECK (((nivel_riesgo >= 1) AND (nivel_riesgo <= 10))),
    CONSTRAINT chk_comportamiento_seguridad CHECK (((puntaje_seguridad IS NULL) OR ((puntaje_seguridad >= (0)::numeric) AND (puntaje_seguridad <= (100)::numeric))))
);


--
-- Name: TABLE comportamiento_vial; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.comportamiento_vial IS 'Evaluaci??n global derivada del conjunto de acciones de una simulaci??n.';


--
-- Name: comportamiento_vial_id_comportamiento_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.comportamiento_vial ALTER COLUMN id_comportamiento ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.comportamiento_vial_id_comportamiento_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: decision; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.decision (
    id_decision bigint NOT NULL,
    id_simulacion bigint NOT NULL,
    id_evento_vial bigint,
    accion_realizada character varying(255) NOT NULL,
    resultado character varying(30) NOT NULL,
    tiempo_reaccion_ms integer,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    posicion_x numeric(12,4),
    posicion_y numeric(12,4),
    observacion text,
    CONSTRAINT chk_decision_accion CHECK ((btrim((accion_realizada)::text) <> ''::text)),
    CONSTRAINT chk_decision_reaccion CHECK (((tiempo_reaccion_ms IS NULL) OR (tiempo_reaccion_ms >= 0))),
    CONSTRAINT chk_decision_resultado CHECK (((resultado)::text = ANY (ARRAY[('CORRECTA'::character varying)::text, ('PARCIAL'::character varying)::text, ('INCORRECTA'::character varying)::text, ('NO_REALIZADA'::character varying)::text])))
);


--
-- Name: TABLE decision; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.decision IS 'Acciones tomadas por el conductor ante eventos o situaciones viales.';


--
-- Name: decision_id_decision_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.decision ALTER COLUMN id_decision ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.decision_id_decision_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: escenario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.escenario (
    id_escenario bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    longitud_km numeric(8,2),
    tiempo_estimado_minutos integer,
    densidad_trafico character varying(20) NOT NULL,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    id_tipo_via integer NOT NULL,
    id_nivel_dificultad integer NOT NULL,
    id_tipo_clima integer NOT NULL,
    CONSTRAINT chk_escenario_densidad CHECK (((densidad_trafico)::text = ANY (ARRAY[('BAJA'::character varying)::text, ('MEDIA'::character varying)::text, ('ALTA'::character varying)::text, ('MUY_ALTA'::character varying)::text]))),
    CONSTRAINT chk_escenario_longitud CHECK (((longitud_km IS NULL) OR (longitud_km > (0)::numeric))),
    CONSTRAINT chk_escenario_nombre CHECK ((btrim((nombre)::text) <> ''::text)),
    CONSTRAINT chk_escenario_tiempo CHECK (((tiempo_estimado_minutos IS NULL) OR (tiempo_estimado_minutos > 0)))
);


--
-- Name: TABLE escenario; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.escenario IS 'Configuraci??n de los ambientes de entrenamiento vial.';


--
-- Name: escenario_id_escenario_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.escenario ALTER COLUMN id_escenario ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.escenario_id_escenario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: estado_simulacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.estado_simulacion (
    id_estado_simulacion integer NOT NULL,
    nombre character varying(40) NOT NULL,
    descripcion character varying(255),
    es_estado_final boolean DEFAULT false NOT NULL
);


--
-- Name: TABLE estado_simulacion; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.estado_simulacion IS 'Cat??logo de estados del ciclo de vida de una simulaci??n.';


--
-- Name: estado_simulacion_id_estado_simulacion_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.estado_simulacion ALTER COLUMN id_estado_simulacion ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.estado_simulacion_id_estado_simulacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: estado_usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.estado_usuario (
    id_estado_usuario integer NOT NULL,
    nombre character varying(30) NOT NULL,
    descripcion character varying(255),
    permite_acceso boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_estado_usuario_nombre CHECK ((btrim((nombre)::text) <> ''::text))
);


--
-- Name: TABLE estado_usuario; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.estado_usuario IS 'Cat??logo de estados posibles de una cuenta de usuario.';


--
-- Name: estado_usuario_id_estado_usuario_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.estado_usuario ALTER COLUMN id_estado_usuario ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.estado_usuario_id_estado_usuario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: evaluacion_ia; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.evaluacion_ia (
    id_evaluacion_ia bigint NOT NULL,
    id_simulacion bigint NOT NULL,
    id_modelo_ia integer NOT NULL,
    resultado text NOT NULL,
    clasificacion_predicha character varying(30),
    nivel_confianza numeric(5,2),
    recomendacion text,
    datos_entrada jsonb,
    fecha_evaluacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_evaluacion_ia_clasificacion CHECK (((clasificacion_predicha IS NULL) OR ((clasificacion_predicha)::text = ANY (ARRAY[('EXCELENTE'::character varying)::text, ('BUENO'::character varying)::text, ('REGULAR'::character varying)::text, ('RIESGOSO'::character varying)::text, ('CRITICO'::character varying)::text])))),
    CONSTRAINT chk_evaluacion_ia_confianza CHECK (((nivel_confianza IS NULL) OR ((nivel_confianza >= (0)::numeric) AND (nivel_confianza <= (100)::numeric))))
);


--
-- Name: TABLE evaluacion_ia; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.evaluacion_ia IS 'Resultados producidos por un modelo de IA para cada simulaci??n.';


--
-- Name: evaluacion_ia_id_evaluacion_ia_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.evaluacion_ia ALTER COLUMN id_evaluacion_ia ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.evaluacion_ia_id_evaluacion_ia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: evento_vial; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.evento_vial (
    id_evento_vial bigint NOT NULL,
    id_simulacion bigint NOT NULL,
    id_tipo_evento integer NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    nivel_riesgo integer NOT NULL,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    posicion_x numeric(12,4),
    posicion_y numeric(12,4),
    velocidad_vehiculo_kmh numeric(6,2),
    CONSTRAINT chk_evento_nombre CHECK ((btrim((nombre)::text) <> ''::text)),
    CONSTRAINT chk_evento_riesgo CHECK (((nivel_riesgo >= 1) AND (nivel_riesgo <= 10))),
    CONSTRAINT chk_evento_velocidad CHECK (((velocidad_vehiculo_kmh IS NULL) OR (velocidad_vehiculo_kmh >= (0)::numeric)))
);


--
-- Name: TABLE evento_vial; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.evento_vial IS 'Eventos concretos generados durante una simulaci??n.';


--
-- Name: evento_vial_id_evento_vial_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.evento_vial ALTER COLUMN id_evento_vial ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.evento_vial_id_evento_vial_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: historial_acceso; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.historial_acceso (
    id_historial_acceso bigint NOT NULL,
    id_usuario bigint NOT NULL,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    direccion_ip inet,
    dispositivo character varying(150),
    navegador character varying(100),
    acceso_exitoso boolean NOT NULL,
    detalle character varying(255)
);


--
-- Name: TABLE historial_acceso; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.historial_acceso IS 'Registro hist??rico de accesos exitosos y fallidos de los usuarios.';


--
-- Name: historial_acceso_id_historial_acceso_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.historial_acceso ALTER COLUMN id_historial_acceso ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.historial_acceso_id_historial_acceso_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: infraccion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.infraccion (
    id_infraccion bigint NOT NULL,
    id_simulacion bigint NOT NULL,
    id_decision bigint,
    id_regla_transito integer NOT NULL,
    id_nivel_gravedad integer NOT NULL,
    descripcion text,
    penalizacion_aplicada numeric(8,2) DEFAULT 0 NOT NULL,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_infraccion_penalizacion CHECK ((penalizacion_aplicada >= (0)::numeric))
);


--
-- Name: TABLE infraccion; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.infraccion IS 'Incumplimientos detectados durante cada intento de simulaci??n.';


--
-- Name: infraccion_id_infraccion_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.infraccion ALTER COLUMN id_infraccion ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.infraccion_id_infraccion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: metrica_desempeno; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.metrica_desempeno (
    id_metrica bigint NOT NULL,
    id_simulacion bigint NOT NULL,
    id_tipo_metrica integer NOT NULL,
    valor numeric(12,4) NOT NULL,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    observacion character varying(255)
);


--
-- Name: TABLE metrica_desempeno; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.metrica_desempeno IS 'Valores medidos durante o al finalizar una simulaci??n.';


--
-- Name: metrica_desempeno_id_metrica_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.metrica_desempeno ALTER COLUMN id_metrica ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.metrica_desempeno_id_metrica_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: modelo_ia; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.modelo_ia (
    id_modelo_ia integer NOT NULL,
    nombre character varying(100) NOT NULL,
    version character varying(40) NOT NULL,
    tipo_modelo character varying(80) NOT NULL,
    descripcion text,
    fecha_entrenamiento timestamp without time zone,
    precision_modelo numeric(5,2),
    parametros jsonb,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_modelo_ia_precision CHECK (((precision_modelo IS NULL) OR ((precision_modelo >= (0)::numeric) AND (precision_modelo <= (100)::numeric))))
);


--
-- Name: TABLE modelo_ia; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.modelo_ia IS 'Modelos de inteligencia artificial disponibles para evaluar simulaciones.';


--
-- Name: COLUMN modelo_ia.parametros; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.modelo_ia.parametros IS 'Configuraci??n t??cnica del modelo almacenada en formato JSON.';


--
-- Name: modelo_ia_id_modelo_ia_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.modelo_ia ALTER COLUMN id_modelo_ia ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.modelo_ia_id_modelo_ia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: nivel_dificultad; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.nivel_dificultad (
    id_nivel_dificultad integer NOT NULL,
    nombre character varying(40) NOT NULL,
    valor integer NOT NULL,
    descripcion character varying(255),
    CONSTRAINT chk_nivel_dificultad_valor CHECK (((valor >= 1) AND (valor <= 10)))
);


--
-- Name: TABLE nivel_dificultad; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.nivel_dificultad IS 'Cat??logo de niveles de dificultad de los escenarios.';


--
-- Name: nivel_dificultad_id_nivel_dificultad_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.nivel_dificultad ALTER COLUMN id_nivel_dificultad ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.nivel_dificultad_id_nivel_dificultad_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: nivel_gravedad; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.nivel_gravedad (
    id_nivel_gravedad integer NOT NULL,
    nombre character varying(40) NOT NULL,
    valor integer NOT NULL,
    descripcion character varying(255),
    multiplicador_penalizacion numeric(5,2) DEFAULT 1.00 NOT NULL,
    CONSTRAINT chk_nivel_gravedad_multiplicador CHECK ((multiplicador_penalizacion > (0)::numeric)),
    CONSTRAINT chk_nivel_gravedad_valor CHECK (((valor >= 1) AND (valor <= 10)))
);


--
-- Name: TABLE nivel_gravedad; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.nivel_gravedad IS 'Cat??logo de gravedad de las infracciones.';


--
-- Name: nivel_gravedad_id_nivel_gravedad_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.nivel_gravedad ALTER COLUMN id_nivel_gravedad ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.nivel_gravedad_id_nivel_gravedad_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: progreso_simulacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.progreso_simulacion (
    id_progreso bigint NOT NULL,
    id_simulacion bigint NOT NULL,
    porcentaje numeric(5,2) NOT NULL,
    etapa character varying(100),
    posicion_x numeric(12,4),
    posicion_y numeric(12,4),
    velocidad_actual_kmh numeric(6,2),
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_progreso_porcentaje CHECK (((porcentaje >= (0)::numeric) AND (porcentaje <= (100)::numeric))),
    CONSTRAINT chk_progreso_velocidad CHECK (((velocidad_actual_kmh IS NULL) OR (velocidad_actual_kmh >= (0)::numeric)))
);


--
-- Name: TABLE progreso_simulacion; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.progreso_simulacion IS 'Puntos de control que permiten guardar y reconstruir el avance parcial.';


--
-- Name: progreso_simulacion_id_progreso_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.progreso_simulacion ALTER COLUMN id_progreso ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.progreso_simulacion_id_progreso_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: regla_transito; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.regla_transito (
    id_regla_transito integer NOT NULL,
    codigo character varying(30) NOT NULL,
    nombre character varying(120) NOT NULL,
    descripcion text,
    categoria character varying(50) NOT NULL,
    penalizacion_base numeric(8,2) DEFAULT 0 NOT NULL,
    activa boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_regla_categoria CHECK (((categoria)::text = ANY (ARRAY[('VELOCIDAD'::character varying)::text, ('SENALIZACION'::character varying)::text, ('PRIORIDAD'::character varying)::text, ('SEGURIDAD'::character varying)::text, ('ESTACIONAMIENTO'::character varying)::text, ('DOCUMENTACION'::character varying)::text, ('OTRA'::character varying)::text]))),
    CONSTRAINT chk_regla_nombre CHECK ((btrim((nombre)::text) <> ''::text)),
    CONSTRAINT chk_regla_penalizacion CHECK ((penalizacion_base >= (0)::numeric))
);


--
-- Name: TABLE regla_transito; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.regla_transito IS 'Reglas de tr??nsito utilizadas para evaluar el comportamiento del participante.';


--
-- Name: regla_transito_id_regla_transito_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.regla_transito ALTER COLUMN id_regla_transito ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.regla_transito_id_regla_transito_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: retroalimentacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.retroalimentacion (
    id_retroalimentacion bigint NOT NULL,
    id_simulacion bigint NOT NULL,
    id_comportamiento bigint,
    comentario text NOT NULL,
    recomendacion text,
    origen character varying(20) DEFAULT 'SISTEMA'::character varying NOT NULL,
    fecha_generacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_retroalimentacion_comentario CHECK ((btrim(comentario) <> ''::text)),
    CONSTRAINT chk_retroalimentacion_origen CHECK (((origen)::text = ANY ((ARRAY['SISTEMA'::character varying, 'INSTRUCTOR'::character varying, 'IA'::character varying, 'IA_LOCAL'::character varying, 'OPENAI'::character varying])::text[])))
);


--
-- Name: TABLE retroalimentacion; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.retroalimentacion IS 'Comentarios y recomendaciones generados para el participante.';


--
-- Name: retroalimentacion_id_retroalimentacion_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.retroalimentacion ALTER COLUMN id_retroalimentacion ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.retroalimentacion_id_retroalimentacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: rol; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rol (
    id_rol integer NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion character varying(255),
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_rol_nombre CHECK ((btrim((nombre)::text) <> ''::text))
);


--
-- Name: TABLE rol; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.rol IS 'Cat??logo de roles disponibles en el sistema.';


--
-- Name: COLUMN rol.nombre; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.rol.nombre IS 'Nombre ??nico del rol, por ejemplo ADMINISTRADOR o PARTICIPANTE.';


--
-- Name: rol_id_rol_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.rol ALTER COLUMN id_rol ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.rol_id_rol_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: sesion_entrenamiento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sesion_entrenamiento (
    id_sesion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    fecha_inicio timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_fin timestamp without time zone,
    estado character varying(20) DEFAULT 'ABIERTA'::character varying NOT NULL,
    objetivo text,
    observaciones text,
    CONSTRAINT chk_sesion_estado CHECK (((estado)::text = ANY (ARRAY[('ABIERTA'::character varying)::text, ('FINALIZADA'::character varying)::text, ('CANCELADA'::character varying)::text]))),
    CONSTRAINT chk_sesion_fechas CHECK (((fecha_fin IS NULL) OR (fecha_fin >= fecha_inicio)))
);


--
-- Name: TABLE sesion_entrenamiento; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sesion_entrenamiento IS 'Agrupa uno o varios intentos realizados por un participante.';


--
-- Name: sesion_entrenamiento_id_sesion_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.sesion_entrenamiento ALTER COLUMN id_sesion ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.sesion_entrenamiento_id_sesion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: simulacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.simulacion (
    id_simulacion bigint NOT NULL,
    id_sesion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    id_escenario bigint NOT NULL,
    id_vehiculo bigint NOT NULL,
    id_estado_simulacion integer NOT NULL,
    numero_intento integer NOT NULL,
    fecha_inicio timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_fin timestamp without time zone,
    porcentaje_progreso numeric(5,2) DEFAULT 0 NOT NULL,
    puntaje_final numeric(8,2),
    duracion_segundos integer,
    completada boolean DEFAULT false NOT NULL,
    observaciones text,
    CONSTRAINT chk_simulacion_duracion CHECK (((duracion_segundos IS NULL) OR (duracion_segundos >= 0))),
    CONSTRAINT chk_simulacion_fechas CHECK (((fecha_fin IS NULL) OR (fecha_fin >= fecha_inicio))),
    CONSTRAINT chk_simulacion_intento CHECK ((numero_intento > 0)),
    CONSTRAINT chk_simulacion_progreso CHECK (((porcentaje_progreso >= (0)::numeric) AND (porcentaje_progreso <= (100)::numeric))),
    CONSTRAINT chk_simulacion_puntaje CHECK (((puntaje_final IS NULL) OR (puntaje_final >= (0)::numeric)))
);


--
-- Name: TABLE simulacion; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.simulacion IS 'Representa cada intento de conducci??n realizado por un usuario.';


--
-- Name: simulacion_id_simulacion_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.simulacion ALTER COLUMN id_simulacion ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.simulacion_id_simulacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_clima; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipo_clima (
    id_tipo_clima integer NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion character varying(255),
    factor_visibilidad numeric(4,2) DEFAULT 1.00 NOT NULL,
    factor_adherencia numeric(4,2) DEFAULT 1.00 NOT NULL,
    CONSTRAINT chk_tipo_clima_adherencia CHECK (((factor_adherencia >= (0)::numeric) AND (factor_adherencia <= (1)::numeric))),
    CONSTRAINT chk_tipo_clima_visibilidad CHECK (((factor_visibilidad >= (0)::numeric) AND (factor_visibilidad <= (1)::numeric)))
);


--
-- Name: TABLE tipo_clima; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.tipo_clima IS 'Cat??logo de condiciones clim??ticas que afectan la conducci??n.';


--
-- Name: tipo_clima_id_tipo_clima_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.tipo_clima ALTER COLUMN id_tipo_clima ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tipo_clima_id_tipo_clima_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_evento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipo_evento (
    id_tipo_evento integer NOT NULL,
    nombre character varying(60) NOT NULL,
    descripcion character varying(255),
    categoria character varying(40) NOT NULL,
    CONSTRAINT chk_tipo_evento_categoria CHECK (((categoria)::text = ANY (ARRAY[('TRAFICO'::character varying)::text, ('PEATON'::character varying)::text, ('CLIMA'::character varying)::text, ('SENALIZACION'::character varying)::text, ('EMERGENCIA'::character varying)::text, ('OTRO'::character varying)::text])))
);


--
-- Name: TABLE tipo_evento; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.tipo_evento IS 'Cat??logo de eventos que pueden presentarse durante una simulaci??n.';


--
-- Name: tipo_evento_id_tipo_evento_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.tipo_evento ALTER COLUMN id_tipo_evento ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tipo_evento_id_tipo_evento_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_metrica; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipo_metrica (
    id_tipo_metrica integer NOT NULL,
    nombre character varying(70) NOT NULL,
    unidad_medida character varying(30),
    descripcion character varying(255),
    valor_minimo numeric(12,4),
    valor_maximo numeric(12,4),
    CONSTRAINT chk_tipo_metrica_rango CHECK (((valor_minimo IS NULL) OR (valor_maximo IS NULL) OR (valor_maximo >= valor_minimo)))
);


--
-- Name: TABLE tipo_metrica; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.tipo_metrica IS 'Define las m??tricas utilizadas para medir el desempe??o.';


--
-- Name: tipo_metrica_id_tipo_metrica_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.tipo_metrica ALTER COLUMN id_tipo_metrica ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tipo_metrica_id_tipo_metrica_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_vehiculo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipo_vehiculo (
    id_tipo_vehiculo integer NOT NULL,
    nombre character varying(60) NOT NULL,
    descripcion character varying(255),
    requiere_licencia character varying(10),
    CONSTRAINT chk_tipo_vehiculo_nombre CHECK ((btrim((nombre)::text) <> ''::text))
);


--
-- Name: TABLE tipo_vehiculo; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.tipo_vehiculo IS 'Cat??logo de veh??culos disponibles en el simulador.';


--
-- Name: tipo_vehiculo_id_tipo_vehiculo_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.tipo_vehiculo ALTER COLUMN id_tipo_vehiculo ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tipo_vehiculo_id_tipo_vehiculo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_via; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipo_via (
    id_tipo_via integer NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion character varying(255),
    velocidad_referencial_kmh numeric(6,2),
    CONSTRAINT chk_tipo_via_nombre CHECK ((btrim((nombre)::text) <> ''::text)),
    CONSTRAINT chk_tipo_via_velocidad CHECK (((velocidad_referencial_kmh IS NULL) OR (velocidad_referencial_kmh > (0)::numeric)))
);


--
-- Name: TABLE tipo_via; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.tipo_via IS 'Cat??logo de clases de v??as utilizadas por los escenarios.';


--
-- Name: tipo_via_id_tipo_via_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.tipo_via ALTER COLUMN id_tipo_via ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tipo_via_id_tipo_via_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuario (
    id_usuario bigint NOT NULL,
    nombres character varying(100) NOT NULL,
    apellidos character varying(100) NOT NULL,
    correo character varying(150) NOT NULL,
    nombre_usuario character varying(60) NOT NULL,
    contrasena_hash character varying(255) NOT NULL,
    telefono character varying(20),
    fecha_nacimiento date,
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ultimo_acceso timestamp without time zone,
    intentos_fallidos integer DEFAULT 0 NOT NULL,
    cuenta_bloqueada boolean DEFAULT false NOT NULL,
    id_rol integer NOT NULL,
    id_estado_usuario integer NOT NULL,
    CONSTRAINT chk_usuario_apellidos CHECK ((btrim((apellidos)::text) <> ''::text)),
    CONSTRAINT chk_usuario_contrasena_hash CHECK ((length(btrim((contrasena_hash)::text)) >= 20)),
    CONSTRAINT chk_usuario_correo CHECK (((correo)::text ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'::text)),
    CONSTRAINT chk_usuario_fecha_nacimiento CHECK (((fecha_nacimiento IS NULL) OR (fecha_nacimiento <= CURRENT_DATE))),
    CONSTRAINT chk_usuario_intentos_fallidos CHECK ((intentos_fallidos >= 0)),
    CONSTRAINT chk_usuario_nombre_usuario CHECK ((length(btrim((nombre_usuario)::text)) >= 4)),
    CONSTRAINT chk_usuario_nombres CHECK ((btrim((nombres)::text) <> ''::text))
);


--
-- Name: TABLE usuario; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.usuario IS 'Informaci??n personal, credenciales y estado de los participantes del sistema.';


--
-- Name: COLUMN usuario.contrasena_hash; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.usuario.contrasena_hash IS 'Contrase??a almacenada mediante un algoritmo seguro, nunca en texto plano.';


--
-- Name: usuario_id_usuario_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.usuario ALTER COLUMN id_usuario ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.usuario_id_usuario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: vehiculo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vehiculo (
    id_vehiculo bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    marca character varying(60),
    modelo character varying(60),
    anio integer,
    transmision character varying(20) NOT NULL,
    velocidad_maxima_kmh numeric(6,2),
    potencia_hp numeric(8,2),
    activo boolean DEFAULT true NOT NULL,
    id_tipo_vehiculo integer NOT NULL,
    CONSTRAINT chk_vehiculo_anio CHECK (((anio IS NULL) OR ((anio >= 1900) AND (anio <= ((EXTRACT(year FROM CURRENT_DATE))::integer + 1))))),
    CONSTRAINT chk_vehiculo_potencia CHECK (((potencia_hp IS NULL) OR (potencia_hp > (0)::numeric))),
    CONSTRAINT chk_vehiculo_transmision CHECK (((transmision)::text = ANY (ARRAY[('MANUAL'::character varying)::text, ('AUTOMATICA'::character varying)::text, ('SEMIAUTOMATICA'::character varying)::text]))),
    CONSTRAINT chk_vehiculo_velocidad CHECK (((velocidad_maxima_kmh IS NULL) OR (velocidad_maxima_kmh > (0)::numeric)))
);


--
-- Name: TABLE vehiculo; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.vehiculo IS 'Veh??culos concretos que pueden utilizarse durante una simulaci??n.';


--
-- Name: vehiculo_id_vehiculo_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.vehiculo ALTER COLUMN id_vehiculo ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.vehiculo_id_vehiculo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: vw_historial_simulaciones; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vw_historial_simulaciones AS
 SELECT s.id_simulacion,
    u.id_usuario,
    concat(u.nombres, ' ', u.apellidos) AS participante,
    e.nombre AS escenario,
    v.nombre AS vehiculo,
    es.nombre AS estado,
    s.numero_intento,
    s.fecha_inicio,
    s.fecha_fin,
    s.porcentaje_progreso,
    s.puntaje_final,
    s.completada
   FROM ((((public.simulacion s
     JOIN public.usuario u ON ((u.id_usuario = s.id_usuario)))
     JOIN public.escenario e ON ((e.id_escenario = s.id_escenario)))
     JOIN public.vehiculo v ON ((v.id_vehiculo = s.id_vehiculo)))
     JOIN public.estado_simulacion es ON ((es.id_estado_simulacion = s.id_estado_simulacion)));


--
-- Name: VIEW vw_historial_simulaciones; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.vw_historial_simulaciones IS 'Resumen de intentos de simulaci??n realizados por los participantes.';


--
-- Name: vw_infracciones_detalladas; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vw_infracciones_detalladas AS
 SELECT i.id_infraccion,
    i.id_simulacion,
    concat(u.nombres, ' ', u.apellidos) AS participante,
    e.nombre AS escenario,
    rt.codigo AS codigo_regla,
    rt.nombre AS regla_incumplida,
    ng.nombre AS gravedad,
    i.penalizacion_aplicada,
    i.descripcion,
    i.fecha_hora
   FROM (((((public.infraccion i
     JOIN public.simulacion s ON ((s.id_simulacion = i.id_simulacion)))
     JOIN public.usuario u ON ((u.id_usuario = s.id_usuario)))
     JOIN public.escenario e ON ((e.id_escenario = s.id_escenario)))
     JOIN public.regla_transito rt ON ((rt.id_regla_transito = i.id_regla_transito)))
     JOIN public.nivel_gravedad ng ON ((ng.id_nivel_gravedad = i.id_nivel_gravedad)));


--
-- Name: vw_metricas_simulacion; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vw_metricas_simulacion AS
 SELECT s.id_simulacion,
    u.id_usuario,
    concat(u.nombres, ' ', u.apellidos) AS participante,
    e.nombre AS escenario,
    tm.nombre AS metrica,
    tm.unidad_medida,
    md.valor,
    md.fecha_hora
   FROM ((((public.metrica_desempeno md
     JOIN public.simulacion s ON ((s.id_simulacion = md.id_simulacion)))
     JOIN public.usuario u ON ((u.id_usuario = s.id_usuario)))
     JOIN public.escenario e ON ((e.id_escenario = s.id_escenario)))
     JOIN public.tipo_metrica tm ON ((tm.id_tipo_metrica = md.id_tipo_metrica)));


--
-- Name: vw_ranking_participantes; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vw_ranking_participantes AS
 SELECT u.id_usuario,
    concat(u.nombres, ' ', u.apellidos) AS participante,
    count(s.id_simulacion) FILTER (WHERE ((es.nombre)::text = 'COMPLETADA'::text)) AS simulaciones_completadas,
    round(avg(s.puntaje_final) FILTER (WHERE (s.puntaje_final IS NOT NULL)), 2) AS puntaje_promedio,
    max(s.puntaje_final) AS mejor_puntaje
   FROM ((public.usuario u
     LEFT JOIN public.simulacion s ON ((s.id_usuario = u.id_usuario)))
     LEFT JOIN public.estado_simulacion es ON ((es.id_estado_simulacion = s.id_estado_simulacion)))
  GROUP BY u.id_usuario, u.nombres, u.apellidos;


--
-- Name: vw_retroalimentacion_usuario; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vw_retroalimentacion_usuario AS
 SELECT r.id_retroalimentacion,
    r.id_simulacion,
    u.id_usuario,
    concat(u.nombres, ' ', u.apellidos) AS participante,
    e.nombre AS escenario,
    r.comentario,
    r.recomendacion,
    r.fecha_generacion
   FROM (((public.retroalimentacion r
     JOIN public.simulacion s ON ((s.id_simulacion = r.id_simulacion)))
     JOIN public.usuario u ON ((u.id_usuario = s.id_usuario)))
     JOIN public.escenario e ON ((e.id_escenario = s.id_escenario)));


--
-- Name: comportamiento_vial pk_comportamiento_vial; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comportamiento_vial
    ADD CONSTRAINT pk_comportamiento_vial PRIMARY KEY (id_comportamiento);


--
-- Name: decision pk_decision; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.decision
    ADD CONSTRAINT pk_decision PRIMARY KEY (id_decision);


--
-- Name: escenario pk_escenario; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escenario
    ADD CONSTRAINT pk_escenario PRIMARY KEY (id_escenario);


--
-- Name: estado_simulacion pk_estado_simulacion; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estado_simulacion
    ADD CONSTRAINT pk_estado_simulacion PRIMARY KEY (id_estado_simulacion);


--
-- Name: estado_usuario pk_estado_usuario; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estado_usuario
    ADD CONSTRAINT pk_estado_usuario PRIMARY KEY (id_estado_usuario);


--
-- Name: evaluacion_ia pk_evaluacion_ia; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluacion_ia
    ADD CONSTRAINT pk_evaluacion_ia PRIMARY KEY (id_evaluacion_ia);


--
-- Name: evento_vial pk_evento_vial; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evento_vial
    ADD CONSTRAINT pk_evento_vial PRIMARY KEY (id_evento_vial);


--
-- Name: historial_acceso pk_historial_acceso; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historial_acceso
    ADD CONSTRAINT pk_historial_acceso PRIMARY KEY (id_historial_acceso);


--
-- Name: infraccion pk_infraccion; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.infraccion
    ADD CONSTRAINT pk_infraccion PRIMARY KEY (id_infraccion);


--
-- Name: metrica_desempeno pk_metrica_desempeno; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metrica_desempeno
    ADD CONSTRAINT pk_metrica_desempeno PRIMARY KEY (id_metrica);


--
-- Name: modelo_ia pk_modelo_ia; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.modelo_ia
    ADD CONSTRAINT pk_modelo_ia PRIMARY KEY (id_modelo_ia);


--
-- Name: nivel_dificultad pk_nivel_dificultad; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nivel_dificultad
    ADD CONSTRAINT pk_nivel_dificultad PRIMARY KEY (id_nivel_dificultad);


--
-- Name: nivel_gravedad pk_nivel_gravedad; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nivel_gravedad
    ADD CONSTRAINT pk_nivel_gravedad PRIMARY KEY (id_nivel_gravedad);


--
-- Name: progreso_simulacion pk_progreso_simulacion; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.progreso_simulacion
    ADD CONSTRAINT pk_progreso_simulacion PRIMARY KEY (id_progreso);


--
-- Name: regla_transito pk_regla_transito; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.regla_transito
    ADD CONSTRAINT pk_regla_transito PRIMARY KEY (id_regla_transito);


--
-- Name: retroalimentacion pk_retroalimentacion; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retroalimentacion
    ADD CONSTRAINT pk_retroalimentacion PRIMARY KEY (id_retroalimentacion);


--
-- Name: rol pk_rol; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT pk_rol PRIMARY KEY (id_rol);


--
-- Name: sesion_entrenamiento pk_sesion_entrenamiento; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sesion_entrenamiento
    ADD CONSTRAINT pk_sesion_entrenamiento PRIMARY KEY (id_sesion);


--
-- Name: simulacion pk_simulacion; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulacion
    ADD CONSTRAINT pk_simulacion PRIMARY KEY (id_simulacion);


--
-- Name: tipo_clima pk_tipo_clima; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_clima
    ADD CONSTRAINT pk_tipo_clima PRIMARY KEY (id_tipo_clima);


--
-- Name: tipo_evento pk_tipo_evento; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_evento
    ADD CONSTRAINT pk_tipo_evento PRIMARY KEY (id_tipo_evento);


--
-- Name: tipo_metrica pk_tipo_metrica; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_metrica
    ADD CONSTRAINT pk_tipo_metrica PRIMARY KEY (id_tipo_metrica);


--
-- Name: tipo_vehiculo pk_tipo_vehiculo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_vehiculo
    ADD CONSTRAINT pk_tipo_vehiculo PRIMARY KEY (id_tipo_vehiculo);


--
-- Name: tipo_via pk_tipo_via; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_via
    ADD CONSTRAINT pk_tipo_via PRIMARY KEY (id_tipo_via);


--
-- Name: usuario pk_usuario; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT pk_usuario PRIMARY KEY (id_usuario);


--
-- Name: vehiculo pk_vehiculo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehiculo
    ADD CONSTRAINT pk_vehiculo PRIMARY KEY (id_vehiculo);


--
-- Name: comportamiento_vial uq_comportamiento_simulacion; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comportamiento_vial
    ADD CONSTRAINT uq_comportamiento_simulacion UNIQUE (id_simulacion);


--
-- Name: escenario uq_escenario_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escenario
    ADD CONSTRAINT uq_escenario_nombre UNIQUE (nombre);


--
-- Name: estado_simulacion uq_estado_simulacion_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estado_simulacion
    ADD CONSTRAINT uq_estado_simulacion_nombre UNIQUE (nombre);


--
-- Name: estado_usuario uq_estado_usuario_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estado_usuario
    ADD CONSTRAINT uq_estado_usuario_nombre UNIQUE (nombre);


--
-- Name: modelo_ia uq_modelo_ia_nombre_version; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.modelo_ia
    ADD CONSTRAINT uq_modelo_ia_nombre_version UNIQUE (nombre, version);


--
-- Name: nivel_dificultad uq_nivel_dificultad_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nivel_dificultad
    ADD CONSTRAINT uq_nivel_dificultad_nombre UNIQUE (nombre);


--
-- Name: nivel_dificultad uq_nivel_dificultad_valor; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nivel_dificultad
    ADD CONSTRAINT uq_nivel_dificultad_valor UNIQUE (valor);


--
-- Name: nivel_gravedad uq_nivel_gravedad_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nivel_gravedad
    ADD CONSTRAINT uq_nivel_gravedad_nombre UNIQUE (nombre);


--
-- Name: nivel_gravedad uq_nivel_gravedad_valor; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nivel_gravedad
    ADD CONSTRAINT uq_nivel_gravedad_valor UNIQUE (valor);


--
-- Name: regla_transito uq_regla_transito_codigo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.regla_transito
    ADD CONSTRAINT uq_regla_transito_codigo UNIQUE (codigo);


--
-- Name: rol uq_rol_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT uq_rol_nombre UNIQUE (nombre);


--
-- Name: simulacion uq_simulacion_intento; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulacion
    ADD CONSTRAINT uq_simulacion_intento UNIQUE (id_sesion, numero_intento);


--
-- Name: tipo_clima uq_tipo_clima_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_clima
    ADD CONSTRAINT uq_tipo_clima_nombre UNIQUE (nombre);


--
-- Name: tipo_evento uq_tipo_evento_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_evento
    ADD CONSTRAINT uq_tipo_evento_nombre UNIQUE (nombre);


--
-- Name: tipo_metrica uq_tipo_metrica_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_metrica
    ADD CONSTRAINT uq_tipo_metrica_nombre UNIQUE (nombre);


--
-- Name: tipo_vehiculo uq_tipo_vehiculo_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_vehiculo
    ADD CONSTRAINT uq_tipo_vehiculo_nombre UNIQUE (nombre);


--
-- Name: tipo_via uq_tipo_via_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_via
    ADD CONSTRAINT uq_tipo_via_nombre UNIQUE (nombre);


--
-- Name: usuario uq_usuario_correo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT uq_usuario_correo UNIQUE (correo);


--
-- Name: usuario uq_usuario_nombre_usuario; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT uq_usuario_nombre_usuario UNIQUE (nombre_usuario);


--
-- Name: vehiculo uq_vehiculo_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehiculo
    ADD CONSTRAINT uq_vehiculo_nombre UNIQUE (nombre);


--
-- Name: idx_decision_simulacion_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_decision_simulacion_fecha ON public.decision USING btree (id_simulacion, fecha_hora);


--
-- Name: idx_evaluacion_ia_simulacion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_evaluacion_ia_simulacion ON public.evaluacion_ia USING btree (id_simulacion);


--
-- Name: idx_evento_simulacion_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_evento_simulacion_fecha ON public.evento_vial USING btree (id_simulacion, fecha_hora);


--
-- Name: idx_historial_acceso_usuario_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_historial_acceso_usuario_fecha ON public.historial_acceso USING btree (id_usuario, fecha_hora DESC);


--
-- Name: idx_infraccion_simulacion_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_infraccion_simulacion_fecha ON public.infraccion USING btree (id_simulacion, fecha_hora);


--
-- Name: idx_metrica_simulacion_tipo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_metrica_simulacion_tipo ON public.metrica_desempeno USING btree (id_simulacion, id_tipo_metrica);


--
-- Name: idx_progreso_simulacion_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_progreso_simulacion_fecha ON public.progreso_simulacion USING btree (id_simulacion, fecha_hora);


--
-- Name: idx_sesion_usuario_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sesion_usuario_fecha ON public.sesion_entrenamiento USING btree (id_usuario, fecha_inicio DESC);


--
-- Name: idx_simulacion_escenario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_simulacion_escenario ON public.simulacion USING btree (id_escenario);


--
-- Name: idx_simulacion_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_simulacion_estado ON public.simulacion USING btree (id_estado_simulacion);


--
-- Name: idx_simulacion_usuario_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_simulacion_usuario_fecha ON public.simulacion USING btree (id_usuario, fecha_inicio DESC);


--
-- Name: idx_usuario_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_usuario_estado ON public.usuario USING btree (id_estado_usuario);


--
-- Name: idx_usuario_rol; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_usuario_rol ON public.usuario USING btree (id_rol);


--
-- Name: historial_acceso trg_actualizar_ultimo_acceso; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_actualizar_ultimo_acceso AFTER INSERT ON public.historial_acceso FOR EACH ROW EXECUTE FUNCTION public.fn_actualizar_ultimo_acceso();


--
-- Name: progreso_simulacion trg_finalizar_simulacion_progreso; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_finalizar_simulacion_progreso AFTER INSERT OR UPDATE OF porcentaje ON public.progreso_simulacion FOR EACH ROW EXECUTE FUNCTION public.fn_finalizar_simulacion_por_progreso();


--
-- Name: infraccion trg_recalcular_puntaje_infraccion; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_recalcular_puntaje_infraccion AFTER INSERT OR DELETE OR UPDATE ON public.infraccion FOR EACH ROW EXECUTE FUNCTION public.fn_recalcular_puntaje_simulacion();


--
-- Name: progreso_simulacion trg_sincronizar_progreso; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_sincronizar_progreso AFTER INSERT ON public.progreso_simulacion FOR EACH ROW EXECUTE FUNCTION public.fn_sincronizar_progreso_simulacion();


--
-- Name: simulacion trg_validar_usuario_sesion; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_validar_usuario_sesion BEFORE INSERT OR UPDATE OF id_sesion, id_usuario ON public.simulacion FOR EACH ROW EXECUTE FUNCTION public.fn_validar_usuario_sesion();


--
-- Name: comportamiento_vial fk_comportamiento_simulacion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comportamiento_vial
    ADD CONSTRAINT fk_comportamiento_simulacion FOREIGN KEY (id_simulacion) REFERENCES public.simulacion(id_simulacion) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: decision fk_decision_evento; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.decision
    ADD CONSTRAINT fk_decision_evento FOREIGN KEY (id_evento_vial) REFERENCES public.evento_vial(id_evento_vial) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: decision fk_decision_simulacion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.decision
    ADD CONSTRAINT fk_decision_simulacion FOREIGN KEY (id_simulacion) REFERENCES public.simulacion(id_simulacion) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: escenario fk_escenario_nivel_dificultad; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escenario
    ADD CONSTRAINT fk_escenario_nivel_dificultad FOREIGN KEY (id_nivel_dificultad) REFERENCES public.nivel_dificultad(id_nivel_dificultad) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: escenario fk_escenario_tipo_clima; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escenario
    ADD CONSTRAINT fk_escenario_tipo_clima FOREIGN KEY (id_tipo_clima) REFERENCES public.tipo_clima(id_tipo_clima) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: escenario fk_escenario_tipo_via; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escenario
    ADD CONSTRAINT fk_escenario_tipo_via FOREIGN KEY (id_tipo_via) REFERENCES public.tipo_via(id_tipo_via) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: evaluacion_ia fk_evaluacion_ia_modelo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluacion_ia
    ADD CONSTRAINT fk_evaluacion_ia_modelo FOREIGN KEY (id_modelo_ia) REFERENCES public.modelo_ia(id_modelo_ia) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: evaluacion_ia fk_evaluacion_ia_simulacion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluacion_ia
    ADD CONSTRAINT fk_evaluacion_ia_simulacion FOREIGN KEY (id_simulacion) REFERENCES public.simulacion(id_simulacion) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: evento_vial fk_evento_simulacion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evento_vial
    ADD CONSTRAINT fk_evento_simulacion FOREIGN KEY (id_simulacion) REFERENCES public.simulacion(id_simulacion) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: evento_vial fk_evento_tipo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evento_vial
    ADD CONSTRAINT fk_evento_tipo FOREIGN KEY (id_tipo_evento) REFERENCES public.tipo_evento(id_tipo_evento) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: historial_acceso fk_historial_acceso_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historial_acceso
    ADD CONSTRAINT fk_historial_acceso_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: infraccion fk_infraccion_decision; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.infraccion
    ADD CONSTRAINT fk_infraccion_decision FOREIGN KEY (id_decision) REFERENCES public.decision(id_decision) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: infraccion fk_infraccion_gravedad; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.infraccion
    ADD CONSTRAINT fk_infraccion_gravedad FOREIGN KEY (id_nivel_gravedad) REFERENCES public.nivel_gravedad(id_nivel_gravedad) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: infraccion fk_infraccion_regla; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.infraccion
    ADD CONSTRAINT fk_infraccion_regla FOREIGN KEY (id_regla_transito) REFERENCES public.regla_transito(id_regla_transito) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: infraccion fk_infraccion_simulacion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.infraccion
    ADD CONSTRAINT fk_infraccion_simulacion FOREIGN KEY (id_simulacion) REFERENCES public.simulacion(id_simulacion) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: metrica_desempeno fk_metrica_simulacion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metrica_desempeno
    ADD CONSTRAINT fk_metrica_simulacion FOREIGN KEY (id_simulacion) REFERENCES public.simulacion(id_simulacion) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: metrica_desempeno fk_metrica_tipo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.metrica_desempeno
    ADD CONSTRAINT fk_metrica_tipo FOREIGN KEY (id_tipo_metrica) REFERENCES public.tipo_metrica(id_tipo_metrica) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: progreso_simulacion fk_progreso_simulacion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.progreso_simulacion
    ADD CONSTRAINT fk_progreso_simulacion FOREIGN KEY (id_simulacion) REFERENCES public.simulacion(id_simulacion) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: retroalimentacion fk_retroalimentacion_comportamiento; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retroalimentacion
    ADD CONSTRAINT fk_retroalimentacion_comportamiento FOREIGN KEY (id_comportamiento) REFERENCES public.comportamiento_vial(id_comportamiento) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: retroalimentacion fk_retroalimentacion_simulacion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retroalimentacion
    ADD CONSTRAINT fk_retroalimentacion_simulacion FOREIGN KEY (id_simulacion) REFERENCES public.simulacion(id_simulacion) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: sesion_entrenamiento fk_sesion_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sesion_entrenamiento
    ADD CONSTRAINT fk_sesion_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: simulacion fk_simulacion_escenario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulacion
    ADD CONSTRAINT fk_simulacion_escenario FOREIGN KEY (id_escenario) REFERENCES public.escenario(id_escenario) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: simulacion fk_simulacion_estado; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulacion
    ADD CONSTRAINT fk_simulacion_estado FOREIGN KEY (id_estado_simulacion) REFERENCES public.estado_simulacion(id_estado_simulacion) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: simulacion fk_simulacion_sesion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulacion
    ADD CONSTRAINT fk_simulacion_sesion FOREIGN KEY (id_sesion) REFERENCES public.sesion_entrenamiento(id_sesion) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: simulacion fk_simulacion_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulacion
    ADD CONSTRAINT fk_simulacion_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: simulacion fk_simulacion_vehiculo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simulacion
    ADD CONSTRAINT fk_simulacion_vehiculo FOREIGN KEY (id_vehiculo) REFERENCES public.vehiculo(id_vehiculo) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: usuario fk_usuario_estado; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT fk_usuario_estado FOREIGN KEY (id_estado_usuario) REFERENCES public.estado_usuario(id_estado_usuario) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: usuario fk_usuario_rol; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: vehiculo fk_vehiculo_tipo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehiculo
    ADD CONSTRAINT fk_vehiculo_tipo FOREIGN KEY (id_tipo_vehiculo) REFERENCES public.tipo_vehiculo(id_tipo_vehiculo) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- PostgreSQL database dump complete
--






