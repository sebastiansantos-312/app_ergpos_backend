package com.ergpos.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ergpos.app.dto.usuarios.CambiarPasswordRequestDTO;
import com.ergpos.app.dto.usuarios.RolCambioRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioResponseDTO;
import com.ergpos.app.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponseDTO> listarTodosUsuarios() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/activos")
    public List<UsuarioResponseDTO> listarUsuariosActivos() {
        return usuarioService.listarActivos();
    }

    @GetMapping("/inactivos")
    public List<UsuarioResponseDTO> listarUsuariosInactivos() {
        return usuarioService.listarInactivos();
    }

    @PostMapping
    public UsuarioResponseDTO crearUsuario(@RequestBody UsuarioRequestDTO request) {
        return usuarioService.crearUsuario(request);
    }

    @PutMapping("/{codigo}")
    public UsuarioResponseDTO actualizarUsuario(
            @PathVariable String codigo,
            @RequestBody UsuarioRequestDTO request) {
        return usuarioService.actualizarUsuario(codigo, request);
    }

    @PutMapping("/email/{email}/desactivar")
    public UsuarioResponseDTO desactivarUsuario(@PathVariable String email) {
        return usuarioService.desactivarUsuario(email);
    }

    @PutMapping("/email/{email}/activar")
    public UsuarioResponseDTO activarUsuario(@PathVariable String email) {
        return usuarioService.activarUsuario(email);
    }

    @GetMapping("/me/perfil")
    public UsuarioResponseDTO obtenerMiPerfil(Authentication authentication) {
        return usuarioService.obtenerPorEmail(authentication.getName());
    }

    @GetMapping("/codigo/{codigo}")
    public UsuarioResponseDTO obtenerUsuarioPorCodigo(@PathVariable String codigo) {
        return usuarioService.obtenerPorCodigo(codigo);
    }

    @PutMapping("/me/cambiar-password")
    public ResponseEntity<?> cambiarMiPassword(
            @RequestBody CambiarPasswordRequestDTO request,
            Authentication authentication) {
        usuarioService.cambiarPassword(authentication.getName(), request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @GetMapping("/buscar")
    public List<UsuarioResponseDTO> buscarUsuarios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String puesto) {
        return usuarioService.buscarUsuarios(nombre, email, departamento, puesto);
    }

    @PutMapping("/{email}/roles")
    public UsuarioResponseDTO cambiarRolesUsuario(
            @PathVariable String email,
            @Valid @RequestBody RolCambioRequestDTO request) {
        return usuarioService.cambiarRoles(email, request);
    }
}