# Diagramas C4 - SBVIA

Como fue solicitado, a continuación se incluyen las representaciones en Mermaid de los diagramas C4 correspondientes a los Niveles 1, 2 y 3.

## Nivel 1: Contexto (Context)
```mermaid
C4Context
    title Nivel 1: Contexto de Sistema para SBVIA

    Person(student, "Conductor en formación", "Usuario que realiza las simulaciones.")
    Person(instructor, "Instructor", "Supervisa métricas de los alumnos.")
    Person(admin, "Administrador", "Configura reglas de tránsito y escenarios.")

    System(sbvia, "SBVIA", "Simulador de Comportamiento Vial con Inteligencia Artificial.")
    System_Ext(ai_api, "API de IA Externa", "Evalúa en tiempo real las decisiones viales.")

    Rel(student, sbvia, "Interactúa y realiza simulaciones")
    Rel(instructor, sbvia, "Revisa reportes")
    Rel(admin, sbvia, "Configura parámetros")
    
    Rel(sbvia, ai_api, "Envía eventos y recibe veredictos", "HTTPS/JSON")
```

## Nivel 2: Contenedores (Containers)
```mermaid
C4Container
    title Nivel 2: Contenedores para SBVIA

    Person(student, "Conductor", "Usuario principal")
    System_Ext(ai_api, "API de IA Externa", "Sistema de inferencia")

    System_Boundary(sbvia, "SBVIA") {
        Container(frontend, "Frontend SPA", "Angular 17", "Interfaz gráfica para usuarios.")
        Container(backend, "Backend API", "Spring Boot 3.2", "Lógica central y endpoints REST.")
        ContainerDb(db, "Base de Datos", "PostgreSQL 16", "Almacena escenarios, usuarios y métricas.")
        ContainerDb(cache, "Caché", "Redis 7", "Gestiona la blacklist JWT y tiempos de respuesta.")
    }

    Rel(student, frontend, "Visita", "HTTPS")
    Rel(frontend, backend, "Consume API", "JSON/HTTPS")
    Rel(backend, db, "Lee/Escribe", "JDBC, JPA y Procedures")
    Rel(backend, cache, "Consulta/Invalida", "Redis Protocol")
    Rel(backend, ai_api, "Solicita inferencia", "JSON/HTTPS")
```

## Nivel 3: Componentes (Components - Backend)
```mermaid
C4Component
    title Nivel 3: Componentes del Backend API
    
    Container(frontend, "Frontend SPA", "Angular", "Interfaz gráfica")
    
    Container_Boundary(backend, "Backend API (Spring Boot)") {
        Component(authFilter, "Filtro JWT", "Spring Security", "Intercepta y valida tokens")
        Component(authCtrl, "Auth Controller", "REST", "Login, Registro")
        Component(simCtrl, "Simulación Controller", "REST", "API de simulación")
        
        Component(jwtSvc, "JWT Service", "Service", "Genera y valida firmas")
        Component(simSvc, "Simulación Service", "Service", "Lógica de negocio de simulación")
        
        Component(jpaRepo, "JPA Repositories", "Spring Data", "CRUD elemental")
        Component(procRepo, "Stored Procedure Invoker", "Spring Data @Procedure", "Consultas y transacciones complejas")
    }
    
    ContainerDb(db, "PostgreSQL", "Relacional", "DB")
    
    Rel(frontend, authFilter, "Request")
    Rel(authFilter, authCtrl, "Ruta")
    Rel(authFilter, simCtrl, "Ruta")
    
    Rel(authCtrl, jwtSvc, "Usa")
    Rel(simCtrl, simSvc, "Delega lógica")
    
    Rel(simSvc, jpaRepo, "Guarda entidades")
    Rel(simSvc, procRepo, "Llama SP")
    
    Rel(jpaRepo, db, "CRUD")
    Rel(procRepo, db, "EXECUTE")
```
