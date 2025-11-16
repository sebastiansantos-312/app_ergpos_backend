package com.ergpos.app.dto.usuarios;

import java.util.List;

public class UsuarioRolResponseDTO {

    private String nombre;
    private String email;
    private List<String> roles;

    public UsuarioRolResponseDTO(String nombre, String email, List<String> roles) {
        this.nombre = nombre;
        this.email = email;
        this.roles = roles;
    }

    // getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
