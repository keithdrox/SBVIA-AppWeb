-- ============================================================
-- PROPUESTA DE ADMINISTRACIÓN DE USUARIOS, ROLES Y PRIVILEGIOS
-- Sistema de Simulación de Conducción Vial
-- Base de Datos: PostgreSQL
-- Autor: Cruz Perez, Justyn
-- Universidad Técnica Estatal de Quevedo
-- Asignatura: Administración de Base de Datos
-- ============================================================


-- ============================================================
-- PASO 1: Crear los roles funcionales (sin LOGIN directo)
-- ============================================================

-- DBA: privilegios máximos del servidor
CREATE ROLE rol_dba
    SUPERUSER
    CREATEDB
    CREATEROLE
    LOGIN
    REPLICATION
    BYPASSRLS
    PASSWORD 'DBA_SecurePass#2025';

-- Administrador del sistema: gestión de contenido
CREATE ROLE rol_admin_sistema
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOLOGIN
    NOREPLICATION
    NOBYPASSRLS
    INHERIT
    CONNECTION LIMIT 10;

-- Instructor: lectura avanzada y generación de reportes
CREATE ROLE rol_instructor
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOLOGIN
    INHERIT
    CONNECTION LIMIT 5;

-- Conductor: acceso restringido a sus propios datos
CREATE ROLE rol_conductor
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOLOGIN
    INHERIT
    CONNECTION LIMIT 3;

-- Aplicación web: acceso operativo del backend
CREATE ROLE rol_aplicacion
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOLOGIN
    INHERIT
    CONNECTION LIMIT 20;

-- Auditor: solo lectura, sin capacidad de modificación
CREATE ROLE rol_auditor
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOLOGIN
    INHERIT
    CONNECTION LIMIT 2;


-- ============================================================
-- PASO 2: Crear usuarios concretos y asignarles su rol
-- ============================================================

-- Administrador de Base de Datos
CREATE USER usr_dba
    LOGIN
    PASSWORD 'Dba@2025!Ultra';
GRANT rol_dba TO usr_dba;

-- Administrador del Sistema
CREATE USER usr_admin
    LOGIN
    PASSWORD 'Admin@Vial2025!'
    CONNECTION LIMIT 10;
GRANT rol_admin_sistema TO usr_admin;

-- Instructor / Evaluador
CREATE USER usr_instructor
    LOGIN
    PASSWORD 'Inst@Eval2025!'
    CONNECTION LIMIT 5;
GRANT rol_instructor TO usr_instructor;

-- Conductor (Alumno) – se puede crear uno por cada alumno registrado
CREATE USER usr_conductor01
    LOGIN
    PASSWORD 'Cond01@Vial!'
    CONNECTION LIMIT 3;
GRANT rol_conductor TO usr_conductor01;

-- Backend de la Aplicación Web
CREATE USER usr_app_web
    LOGIN
    PASSWORD 'App@Web2025$!'
    CONNECTION LIMIT 20;
GRANT rol_aplicacion TO usr_app_web;

-- Auditor Interno
CREATE USER usr_auditor
    LOGIN
    PASSWORD 'Audit@Read2025!'
    CONNECTION LIMIT 2;
GRANT rol_auditor TO usr_auditor;


-- ============================================================
-- PASO 3: Privilegios sobre la base de datos
-- ============================================================

GRANT CONNECT ON DATABASE sbvia_db
    TO rol_admin_sistema,
       rol_instructor,
       rol_conductor,
       rol_aplicacion,
       rol_auditor;

-- Permisos para crear tablas temporales (uso interno)
GRANT TEMPORARY ON DATABASE sbvia_db
    TO rol_admin_sistema,
       rol_aplicacion;


-- ============================================================
-- PASO 4: Privilegios sobre el esquema public
-- ============================================================

-- Todos los roles operativos necesitan USAGE para ver los objetos
GRANT USAGE ON SCHEMA public
    TO rol_admin_sistema,
       rol_instructor,
       rol_conductor,
       rol_aplicacion,
       rol_auditor;

-- Solo el DBA puede crear o eliminar objetos en el esquema
GRANT CREATE ON SCHEMA public TO rol_dba;


-- ============================================================
-- PASO 5: Privilegios para rol_admin_sistema
-- Control total sobre el contenido del sistema
-- ============================================================

GRANT SELECT, INSERT, UPDATE, DELETE
    ON TABLE "Rol",
             "Usuario",
             "Escenario",
             "Simulacion",
             "EventoVial",
             "Decision",
             "ReglaTransito",
             "Infraccion",
             "ComportamientoVial",
             "Retroalimentacion",
             "MetricaDesempeno",
             "Reporte",
             "Recompensa"
    TO rol_admin_sistema;


-- ============================================================
-- PASO 6: Privilegios para rol_instructor
-- Lectura general + inserción de retroalimentación y métricas
-- ============================================================

GRANT SELECT
    ON TABLE "Rol",
             "Usuario",
             "Escenario",
             "Simulacion",
             "EventoVial",
             "Decision",
             "ReglaTransito",
             "Infraccion",
             "ComportamientoVial",
             "Recompensa"
    TO rol_instructor;

-- El instructor puede registrar retroalimentación, métricas y reportes
GRANT SELECT, INSERT
    ON TABLE "Retroalimentacion",
             "MetricaDesempeno",
             "Reporte"
    TO rol_instructor;


-- ============================================================
-- PASO 7: Privilegios para rol_conductor
-- Solo lectura de sus propios registros + INSERT en Decision
-- Nota: el filtro por id_Usuario se implementa con Row Level
--       Security (RLS) o mediante vistas específicas por usuario
-- ============================================================

GRANT SELECT
    ON TABLE "Simulacion",
             "EventoVial",
             "Decision",
             "Infraccion",
             "ComportamientoVial",
             "Retroalimentacion",
             "MetricaDesempeno",
             "Reporte",
             "Recompensa",
             "Escenario",
             "ReglaTransito",
             "Usuario"
    TO rol_conductor;

-- El conductor puede registrar sus propias decisiones durante la simulación
GRANT INSERT
    ON TABLE "Decision"
    TO rol_conductor;


-- ============================================================
-- PASO 8: Privilegios para rol_aplicacion
-- Acceso operativo completo (excluye DDL y gestión de roles)
-- ============================================================

GRANT SELECT, INSERT, UPDATE, DELETE
    ON TABLE "Usuario",
             "Escenario",
             "Simulacion",
             "EventoVial",
             "Decision",
             "Infraccion",
             "ComportamientoVial",
             "Retroalimentacion",
             "MetricaDesempeno",
             "Reporte",
             "Recompensa"
    TO rol_aplicacion;

-- Solo lectura en tablas de catálogo del sistema
GRANT SELECT
    ON TABLE "Rol",
             "ReglaTransito"
    TO rol_aplicacion;


-- ============================================================
-- PASO 9: Privilegios para rol_auditor
-- Solo lectura en todas las tablas del esquema public
-- ============================================================

GRANT SELECT ON ALL TABLES IN SCHEMA public
    TO rol_auditor;


-- ============================================================
-- PASO 10: Privilegios sobre secuencias (campos IDENTITY)
-- Necesarios para que los INSERT funcionen correctamente
-- ============================================================

GRANT USAGE, SELECT, UPDATE
    ON ALL SEQUENCES IN SCHEMA public
    TO rol_admin_sistema;

GRANT USAGE, SELECT, UPDATE
    ON ALL SEQUENCES IN SCHEMA public
    TO rol_aplicacion;

-- El conductor solo necesita acceso a la secuencia de Decision
GRANT USAGE, SELECT
    ON SEQUENCE "Decision_id_Decision_seq"
    TO rol_conductor;


-- ============================================================
-- PASO 11: Privilegios por defecto (DEFAULT PRIVILEGES)
-- Garantiza que futuros objetos hereden los permisos definidos
-- sin necesidad de ejecutar GRANT manualmente cada vez
-- ============================================================

-- Para el administrador del sistema
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE
    ON TABLES TO rol_admin_sistema;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT, UPDATE
    ON SEQUENCES TO rol_admin_sistema;

-- Para la aplicación web
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE
    ON TABLES TO rol_aplicacion;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT, UPDATE
    ON SEQUENCES TO rol_aplicacion;

-- Para el auditor
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT ON TABLES TO rol_auditor;

-- Para el instructor
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT ON TABLES TO rol_instructor;


-- ============================================================
-- VERIFICACIÓN: Consultas para confirmar la configuración
-- ============================================================

-- Ver todos los roles creados y sus atributos de sistema
SELECT rolname,
       rolsuper      AS superuser,
       rolcreaterole AS createrole,
       rolcreatedb   AS createdb,
       rolcanlogin   AS login,
       rolreplication AS replication,
       rolbypassrls  AS bypassrls,
       rolconnlimit  AS conn_limit
FROM pg_roles
WHERE rolname LIKE 'rol_%' OR rolname LIKE 'usr_%'
ORDER BY rolname;

-- Ver la membresía de cada usuario en sus roles
SELECT r.rolname AS rol,
       m.rolname AS miembro
FROM pg_auth_members am
JOIN pg_roles r ON am.roleid = r.oid
JOIN pg_roles m ON am.member = m.oid
WHERE r.rolname LIKE 'rol_%'
ORDER BY r.rolname, m.rolname;

-- Ver privilegios de tabla asignados por rol
SELECT grantee,
       table_name,
       privilege_type
FROM information_schema.role_table_grants
WHERE grantee LIKE 'rol_%'
ORDER BY grantee, table_name, privilege_type;

-- Ver privilegios sobre la base de datos
SELECT datname, datacl
FROM pg_database
WHERE datname = 'sbvia_db';