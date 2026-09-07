package com.sbvia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la petición de inicio de sesión.
 * Permite autenticarse mediante 'identificador' (que puede ser el nombre de usuario o el correo electrónico).
 * Mantiene compatibilidad total con peticiones existentes que envían 'correo'.
 */
@Data
public class LoginRequest {

    private String identificador;
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public String getIdentificador() {
        if (identificador != null && !identificador.isBlank()) {
            return identificador.trim();
        }
        if (correo != null && !correo.isBlank()) {
            return correo.trim();
        }
        return "";
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getCorreo() {
        return getIdentificador();
    }

    public void setCorreo(String correo) {
        this.correo = correo;
        if (this.identificador == null || this.identificador.isBlank()) {
            this.identificador = correo;
        }
    }
}
