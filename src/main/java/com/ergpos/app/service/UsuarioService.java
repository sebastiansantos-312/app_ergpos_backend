package com.ergpos.app.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ergpos.app.dto.usuarios.CambiarPasswordRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioResponseDTO;
import com.ergpos.app.model.Rol;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.RolRepository;
import com.ergpos.app.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setCodigo(usuario.getCodigo());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol() != null ? usuario.getRol().getNombre() : null);
        dto.setActivo(usuario.getActivo());
        dto.setCreatedAt(usuario.getCreatedAt());
        dto.setUpdatedAt(usuario.getUpdatedAt());
        return dto;
    }

    // Búsqueda dinámica con filtros
    public List<UsuarioResponseDTO> listar(String nombre, String email, String rolNombre, Boolean activo) {
        return usuarioRepository.buscarUsuarios(nombre, email, rolNombre, activo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener por email
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        String emailNormalizado = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con email: " + emailNormalizado));
        return toDTO(usuario);
    }

    // Crear usuario
    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un usuario con el email: " + email);
        }

        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty()) {
            String codigo = request.getCodigo().trim();
            if (usuarioRepository.existsByCodigo(codigo)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe un usuario con el código: " + codigo);
            }
        }

        // Buscar rol por nombre
        Rol rol = rolRepository.findByNombreIgnoreCase(request.getNombreRol())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol no encontrado: " + request.getNombreRol()));

        if (!rol.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El rol está inactivo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setCodigo(request.getCodigo() != null ? request.getCodigo().trim() : null);
        usuario.setRol(rol);
        usuario.setActivo(true);

        Usuario saved = usuarioRepository.save(usuario);
        return toDTO(saved);
    }

    // Actualizar usuario
    @Transactional
    public UsuarioResponseDTO actualizar(String email, UsuarioRequestDTO request) {
        String emailActual = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailActual)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con email: " + emailActual));

        String nuevoEmail = request.getEmail().trim().toLowerCase();

        if (!usuario.getEmail().equals(nuevoEmail) && usuarioRepository.existsByEmail(nuevoEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un usuario con el email: " + nuevoEmail);
        }

        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty()) {
            String nuevoCodigo = request.getCodigo().trim();
            if (!nuevoCodigo.equals(usuario.getCodigo()) && usuarioRepository.existsByCodigo(nuevoCodigo)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe un usuario con el código: " + nuevoCodigo);
            }
        }

        // Buscar rol por nombre
        Rol rol = rolRepository.findByNombreIgnoreCase(request.getNombreRol())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol no encontrado: " + request.getNombreRol()));

        if (!rol.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El rol está inactivo");
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(nuevoEmail);
        usuario.setCodigo(request.getCodigo() != null ? request.getCodigo().trim() : null);
        usuario.setRol(rol);

        // Solo actualizar password si se proporciona uno nuevo
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        Usuario updated = usuarioRepository.save(usuario);
        return toDTO(updated);
    }

    // Activar usuario
    @Transactional
    public UsuarioResponseDTO activar(String email) {
        String emailNormalizado = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con email: " + emailNormalizado));

        if (usuario.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El usuario ya está activo");
        }

        usuario.setActivo(true);
        Usuario updated = usuarioRepository.save(usuario);
        return toDTO(updated);
    }

    // Desactivar usuario
    @Transactional
    public UsuarioResponseDTO desactivar(String email) {
        String emailNormalizado = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con email: " + emailNormalizado));

        if (!usuario.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El usuario ya está inactivo");
        }

        usuario.setActivo(false);
        Usuario updated = usuarioRepository.save(usuario);
        return toDTO(updated);
    }

    // Cambiar contraseña
    @Transactional
    public void cambiarPassword(String email, CambiarPasswordRequestDTO request) {
        String emailNormalizado = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con email: " + emailNormalizado));

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevoPassword()));
        usuarioRepository.save(usuario);
    }
}