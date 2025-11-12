package com.ergpos.app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Listar todos los usuarios o filtrar por nombre/email opcionalmente
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

    // Crear un nuevo usuario
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Obtener usuario por ID
    @GetMapping("/{id}")
    public Usuario obtenerUsuario(@PathVariable UUID id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    // Eliminar usuario por ID
    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable UUID id) {
        usuarioRepository.deleteById(id);
    }

    // Búsqueda por nombre (endpoint específico)
    @GetMapping("/buscar/nombre/{nombre}")
    public List<Usuario> buscarPorNombre(@PathVariable String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // Búsqueda por email (endpoint específico)
    @GetMapping("/buscar/email")
    public Usuario buscarPorEmail(@RequestParam String email) {
        return usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
    }
}
