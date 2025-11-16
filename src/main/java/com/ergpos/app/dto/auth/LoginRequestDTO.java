package com.ergpos.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la petición de login.
 * Contiene el email y la contraseña del usuario.
 * Las validaciones aseguran que no se envíen campos vacíos
 * y que el email tenga formato correcto.
 */
public class LoginRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // Constructor vacío (requerido por Spring)
    public LoginRequestDTO() {
    }

    // Constructor con parámetros (opcional)
    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters y setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
