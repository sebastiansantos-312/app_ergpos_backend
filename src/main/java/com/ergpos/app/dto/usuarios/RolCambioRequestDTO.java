package com.ergpos.app.dto.usuarios;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class RolCambioRequestDTO {

    @NotEmpty(message = "Debe asignar al menos un rol")
    private List<String> rolNombres; // nombres de roles nuevos

    public List<String> getRolNombres() {
        return rolNombres;
    }

    public void setRolNombres(List<String> rolNombres) {
        this.rolNombres = rolNombres;
    }
}
