package com.ergpos.app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @SuppressWarnings("null")
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public Usuario obtenerUsuario(@PathVariable UUID id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @SuppressWarnings("null")
    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable UUID id) {
        usuarioRepository.deleteById(id);
    }
}
