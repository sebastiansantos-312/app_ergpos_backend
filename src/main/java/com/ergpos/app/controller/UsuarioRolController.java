package com.ergpos.app.controller;

import org.springframework.web.bind.annotation.*;
import com.ergpos.app.dto.usuarios.RolCambioRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioRolResponseDTO;
import com.ergpos.app.service.UsuarioRolService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioRolController {

    private final UsuarioRolService usuarioRolService;

    public UsuarioRolController(UsuarioRolService usuarioRolService) {
        this.usuarioRolService = usuarioRolService;
    }

    //@PreAuthorize("hasAuthority('USUARIO_CAMBIAR_ROLES')")
    @PutMapping("/{email}/roles")
    public UsuarioRolResponseDTO cambiarRoles(@PathVariable String email,
            @Valid @RequestBody RolCambioRequestDTO request) {
        return usuarioRolService.cambiarRolesUsuario(email, request);
    }
}
