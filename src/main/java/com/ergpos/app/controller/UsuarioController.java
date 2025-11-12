package com.ergpos.app.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ergpos.app.model.Usuario;
import com.ergpos.app.model.LoginRequest;
import com.ergpos.app.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<Usuario> listarUsuarios(@RequestParam(required = false) String nombre,
                                        @RequestParam(required = false) String email) {
        if (nombre != null) {
            return usuarioRepository.findByNombreContainingIgnoreCase(nombre);
        } else if (email != null) {
            return usuarioRepository.findByEmailIgnoreCase(email)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            return usuarioRepository.findAll();
        }
    }

    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @GetMapping("/{id}")
    public Usuario obtenerUsuario(@PathVariable UUID id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable UUID id) {
        usuarioRepository.deleteById(id);
    }

    @GetMapping("/buscar/nombre/{nombre}")
    public List<Usuario> buscarPorNombre(@PathVariable String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @GetMapping("/buscar/email")
    public Usuario buscarPorEmail(@RequestParam String email) {
        return usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    // 🧩 Nuevo endpoint para login
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest loginRequest) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(loginRequest.getEmail());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getPassword().equals(loginRequest.getPassword())) {
                return ResponseEntity.ok(usuario);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}