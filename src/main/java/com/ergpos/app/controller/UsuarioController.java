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

@CrossOrigin(origins = "*")
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

    // ✅ Nuevo método login corregido
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> login(@RequestBody(required = false) LoginRequest loginRequest) {
        if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body("{\"message\": \"Solicitud inválida. Faltan datos.\"}");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(loginRequest.getEmail().trim());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Correo o contraseña incorrectos.\"}");
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getPassword().trim().equals(loginRequest.getPassword().trim())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Correo o contraseña incorrectos.\"}");
        }

        usuario.setPassword(null);
        return ResponseEntity.ok(usuario);
    }
}
