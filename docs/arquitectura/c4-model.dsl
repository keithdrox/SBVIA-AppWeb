workspace "SBVIA" "Simulador de Comportamiento Vial con IA" {
    model {
        student = person "Conductor en formación" "Usuario que realiza las simulaciones viales."
        instructor = person "Instructor" "Revisa las métricas y desempeño de los alumnos."
        admin = person "Administrador" "Configura reglas y escenarios."
        
        aiSystem = softwareSystem "API de IA Externa" "Sistema externo que evalúa las acciones viales." "Existing System"

        sbvia = softwareSystem "SBVIA" "Sistema principal de Simulación de Comportamiento Vial" {
            frontend = container "Frontend Angular" "SPA que provee la interfaz gráfica" "Angular 17+" "Web Browser"
            backend = container "Backend Spring Boot" "API REST que gestiona la lógica de negocio" "Spring Boot 3.2.x, Java 21"
            db = container "Base de Datos PostgreSQL" "Almacenamiento relacional de usuarios, escenarios y simulaciones" "PostgreSQL 16" "Database"
            cache = container "Caché Redis" "Almacenamiento clave-valor para tokens JWT y métricas frecuentes" "Redis 7" "Cache"
            
            student -> frontend "Accede a escenarios e interactúa con el simulador"
            instructor -> frontend "Consulta reportes de alumnos"
            admin -> frontend "Gestiona escenarios y reglas"
            
            frontend -> backend "Realiza llamadas API" "JSON/HTTPS"
            backend -> db "Lee y escribe datos" "JDBC"
            backend -> cache "Almacena y consulta" "Redis Protocol"
        }
        
        backend -> aiSystem "Envía eventos y recibe análisis" "JSON/HTTPS"
    }

    views {
        systemContext sbvia "SystemContext" {
            include *
            autoLayout
        }
        container sbvia "Containers" {
            include *
            autoLayout
        }
        theme default
    }
}
