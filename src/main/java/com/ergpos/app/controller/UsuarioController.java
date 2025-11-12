package com.ergpos.app.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// <-- ¡Importa esto!
import org.springframework.web.bind.annotation.*;

import com.ergpos.app.model.Usuario;
import com.ergpos.app.model.LoginRequest;
import com.ergpos.app.repository.UsuarioRepository;

@CrossOrigin(origins = "*") // <-- AÑADE ESTA LÍNEA

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("📩 Login recibido: " + loginRequest.getEmail() + " / " + loginRequest.getPassword());

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            System.out.println("✅ Usuario encontrado: " + usuario.getEmail());
            System.out.println("🔑 Password en BD: " + usuario.getPassword());

            if (usuario.getPassword().equals(loginRequest.getPassword())) {
                usuario.setPassword(null);
                return ResponseEntity.ok(usuario);
            } else {
                System.out.println("❌ Contraseña incorrecta");
            }
        } else {
            System.out.println("❌ Usuario no encontrado");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"message\":\"Correo o contraseña incorrectos\"}");
    }

}