package com.ergpos.app.dto.usuarios;

import java.util.List;

public class UsuarioResponseDTO {
    private String codigo;
    private String nombre;
    private String email;
    private List<String> roles;
    private Boolean activo;
    private String departamento;
    private String puesto;

    // Constructores
    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(String codigo, String nombre, String email, List<String> roles,
            Boolean activo, String departamento, String puesto,
            String avatarUrl) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.email = email;
        this.roles = roles;
        this.activo = activo;
        this.departamento = departamento;
        this.puesto = puesto;
    }

    // Getters y setters para TODOS los campos
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
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