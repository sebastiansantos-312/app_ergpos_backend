package com.ergpos.app.controller;

import java.util.List;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ergpos.app.dto.usuarios.UsuarioRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioResponseDTO;
import com.ergpos.app.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    //@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_ADMIN')")
    @GetMapping("/activos")
    public List<UsuarioResponseDTO> listarUsuariosActivos() {
        return usuarioService.listarActivos();
    }

    //@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_ADMIN')")
    @GetMapping("/inactivos")
    public List<UsuarioResponseDTO> listarUsuariosInactivos() {
        return usuarioService.listarInactivos();
    }

    //@PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public UsuarioResponseDTO crearUsuario(@RequestBody UsuarioRequestDTO request) {
        return usuarioService.crearUsuario(request);
    }

   // @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/email/{email}/desactivar")
    public UsuarioResponseDTO desactivarUsuario(@PathVariable String email) {
        return usuarioService.desactivarUsuario(email);
    }

    //@PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/email/{email}/activar")
    public UsuarioResponseDTO activarUsuario(@PathVariable String email) {
        return usuarioService.activarUsuario(email);
    }

}
