package com.sbvia.backend.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

public class SimulacionSteps {

    private String estadoSimulacion;
    private int puntaje;
    private int infracciones;

    @Given("que el conductor está autenticado")
    public void queElConductorEstaAutenticado() {
        // Lógica de mock de autenticación
    }

    @And("selecciona el escenario {string}")
    public void seleccionaElEscenario(String escenario) {
        // Lógica para seleccionar escenario
    }

    @When("el conductor inicia la simulación")
    public void elConductorIniciaLaSimulacion() {
        this.estadoSimulacion = "EN_PROGRESO";
        this.puntaje = 100;
    }

    @And("comete {int} infracciones")
    public void cometeInfracciones(int cantidad) {
        this.infracciones = cantidad;
        this.puntaje -= (cantidad * 10);
    }

    @Then("la simulación finaliza con estado {string}")
    public void laSimulacionFinalizaConEstado(String estadoEsperado) {
        this.estadoSimulacion = "FINALIZADA"; // Simulando el cambio
        Assertions.assertEquals(estadoEsperado, this.estadoSimulacion);
    }

    @And("el puntaje final es {int}")
    public void elPuntajeFinalEs(int puntajeEsperado) {
        Assertions.assertEquals(puntajeEsperado, this.puntaje);
    }
}
