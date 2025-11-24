package com.ergpos.app.dto.auth;

import java.util.UUID;

public class LoginResponseDTO {

    private String token;
    private UUID usuarioId;
    private String codigo;
    private String nombre;
    private String email;
    private String rol;
    private Boolean activo;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, UUID usuarioId, String codigo, String nombre,
            String email, String rol, Boolean activo) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}