package com.ergpos.app.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ergpos.app.dto.usuarios.UsuarioRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioResponseDTO;
import com.ergpos.app.model.Rol;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.RolRepository;
import com.ergpos.app.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.getActivo());

        // defensivo: si roles es null, devolver lista vacía
        List<String> roles = usuario.getRoles() == null
                ? List.of()
                : usuario.getRoles().stream()
                        .map(Rol::getNombre) // map sobre Stream<Rol>
                        .collect(Collectors.toList());

        dto.setRoles(roles);
        return dto;
    }

    // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE')")
    public List<UsuarioResponseDTO> listarActivos() {
        return usuarioRepository.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE')")
    public List<UsuarioResponseDTO> listarInactivos() {
        return usuarioRepository.findByActivoFalse()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE')")
   // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE')")
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request) {
        // validación simple: email único
        if (usuarioRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setActivo(true);

        // mapear nombres de roles a entidades Rol
        Set<Rol> roles = request.getRoles().stream()
                .map(nombre -> rolRepository.findByNombreIgnoreCase(nombre)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nombre)))
                .collect(Collectors.toSet());

        usuario.setRoles(roles);

        Usuario saved = usuarioRepository.save(usuario);
        return toDTO(saved);
    }

    // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE')")
    public UsuarioResponseDTO desactivarUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setActivo(false);
        return toDTO(usuarioRepository.save(usuario));
    }

    // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE')")
    public UsuarioResponseDTO activarUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setActivo(true);
        return toDTO(usuarioRepository.save(usuario));
    }
}
