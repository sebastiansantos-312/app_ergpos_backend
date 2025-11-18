package com.ergpos.app.dto.usuarios;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class RolCambioRequestDTO {
    @NotEmpty(message = "Debe asignar al menos un rol")
    private List<String> roles; 

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
