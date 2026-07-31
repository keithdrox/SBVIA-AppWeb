# Historias de Usuario (SBVIA)

Este documento detalla 10 Historias de Usuario bajo el estándar **INVEST**, con criterios de aceptación en formato **Gherkin**.

## US-01: Registro de Conductor
**Como** conductor en formación, **quiero** registrarme en la plataforma con mi correo electrónico, **para** poder guardar mi progreso en las simulaciones viales.
* **Independent:** No depende de otra funcionalidad más que la BD.
* **Negotiable:** Los campos requeridos pueden variar.
* **Valuable:** Permite la persistencia del usuario.
* **Estimable:** 3 Story Points.
* **Small:** Un formulario sencillo y un endpoint.
* **Testable:** Vía tests de integración.
**Criterios de Aceptación (Gherkin):**
```gherkin
Dado que estoy en la pantalla de registro
Cuando ingreso "juan@sbvia.com", "Juan", "Perez" y "secreta123"
Y presiono "Registrar"
Entonces debo ver un mensaje de éxito "Registro completado"
Y debo ser redirigido a la pantalla de login
```

## US-02: Inicio de Sesión
**Como** usuario del sistema, **quiero** iniciar sesión con mis credenciales, **para** acceder a mi panel personalizado.
*(INVEST: Cumple. 2 SP)*
```gherkin
Dado que tengo una cuenta registrada
Cuando ingreso mi email y contraseña correcta
Entonces el sistema me otorga un Token JWT válido
Y me redirige al Dashboard
```

## US-03: Visualizar Listado de Escenarios
**Como** conductor, **quiero** ver la lista de escenarios viales disponibles, **para** seleccionar cuál quiero practicar.
*(INVEST: Cumple. 3 SP)*
```gherkin
Dado que he iniciado sesión
Cuando navego a "Mis Escenarios"
Entonces el sistema muestra una lista paginada de escenarios con su nivel de dificultad y clima
```

## US-04: Iniciar una Simulación
**Como** conductor, **quiero** iniciar la simulación de un escenario específico, **para** comenzar a conducir virtualmente.
*(INVEST: Cumple. 5 SP)*
```gherkin
Dado que estoy viendo los detalles de un escenario "Ciudad con Lluvia"
Cuando presiono "Iniciar Simulación"
Entonces el sistema registra una nueva sesión en la base de datos
Y la interfaz cambia al entorno 3D
```

## US-05: Finalizar Simulación Manualmente
**Como** conductor, **quiero** poder detener la simulación en cualquier momento, **para** salir si me siento mareado o necesito interrumpir la prueba.
*(INVEST: Cumple. 2 SP)*
```gherkin
Dado que estoy en una simulación activa
Cuando presiono el botón "Detener y Salir"
Entonces la simulación se marca como "Interrumpida"
Y regreso al Dashboard
```

## US-06: Ver Retroalimentación Inmediata
**Como** conductor, **quiero** recibir notificaciones en pantalla al cometer un error, **para** corregir mi comportamiento al instante.
*(INVEST: Cumple. 5 SP)*
```gherkin
Dado que estoy en una simulación activa
Cuando la IA detecta que "me he pasado un semáforo en rojo"
Entonces el sistema muestra un toast de advertencia "Infracción: Semáforo en rojo"
Y descuenta 5 puntos de mi puntaje actual
```

## US-07: Consultar Reporte Final
**Como** conductor, **quiero** ver un resumen detallado al finalizar el escenario, **para** entender en qué áreas debo mejorar.
*(INVEST: Cumple. 5 SP)*
```gherkin
Dado que he completado el escenario exitosamente
Cuando finaliza el tiempo o llego a la meta
Entonces veo una pantalla de "Reporte Final"
Y se muestra mi puntaje, errores cometidos y tiempo de reacción promedio
```

## US-08: Gestión de Escenarios (Admin)
**Como** administrador, **quiero** crear nuevos escenarios (clima, tráfico, reglas), **para** ofrecer variedad a los alumnos.
*(INVEST: Cumple. 5 SP)*
```gherkin
Dado que he iniciado sesión como "ROLE_ADMIN"
Cuando voy a "Gestión de Escenarios" y completo el formulario
Y presiono "Guardar"
Entonces el nuevo escenario queda "Activo" en la base de datos
Y es visible para los conductores
```

## US-09: Ver Estadísticas de Alumnos (Instructor)
**Como** instructor, **quiero** ver las métricas consolidadas de mis alumnos, **para** identificar quiénes necesitan tutoría.
*(INVEST: Cumple. 8 SP)*
```gherkin
Dado que he iniciado sesión como "ROLE_INSTRUCTOR"
Cuando entro al "Panel de Instructor"
Entonces veo una tabla con mis alumnos ordenados por puntaje promedio ascendente
```

## US-10: Cerrar Sesión Segura
**Como** usuario, **quiero** poder cerrar mi sesión, **para** evitar que otros usen mi cuenta en un computador compartido.
*(INVEST: Cumple. 2 SP)*
```gherkin
Dado que tengo una sesión activa
Cuando presiono "Cerrar Sesión"
Entonces el sistema elimina mi token de la aplicación
Y me redirige al login
Y el token se añade a la blacklist de Redis
```
