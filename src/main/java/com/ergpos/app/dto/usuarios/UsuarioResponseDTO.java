package com.ergpos.app.dto.usuarios;

import java.util.List;

public class UsuarioResponseDTO {

    private String nombre;
    private String email;
    private List<String> roles;
    private Boolean activo;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(String nombre, String email, List<String> roles, Boolean activo) {
        this.nombre = nombre;
        this.email = email;
        this.roles = roles;
        this.activo = activo;
    }

    // Getters y setters
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
