Feature: Simulación Vial
  Como conductor en formación
  Quiero iniciar y completar una simulación
  Para evaluar mis habilidades de conducción

  Scenario: Iniciar y finalizar simulación exitosamente
    Given que el conductor está autenticado
    And selecciona el escenario "Ciudad con Lluvia"
    When el conductor inicia la simulación
    And comete 0 infracciones
    Then la simulación finaliza con estado "FINALIZADA"
    And el puntaje final es 100
