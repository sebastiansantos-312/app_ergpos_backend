package com.ergpos.app.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ergpos.app.dto.usuarios.CambiarPasswordRequestDTO;
import com.ergpos.app.dto.usuarios.RolCambioRequestDTO;
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
        dto.setCodigo(usuario.getCodigo());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setDepartamento(usuario.getDepartamento());
        dto.setPuesto(usuario.getPuesto());
        dto.setActivo(usuario.getActivo());

        List<String> roles = usuario.getRoles() == null
                ? List.of()
                : usuario.getRoles().stream()
                        .map(Rol::getNombre)
                        .collect(Collectors.toList());

        dto.setRoles(roles);
        return dto;
    }

    public List<UsuarioResponseDTO> listarActivos() {
        return usuarioRepository.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UsuarioResponseDTO> listarInactivos() {
        return usuarioRepository.findByActivoFalse()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request) {
        if (usuarioRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setCodigo(request.getCodigo());
        usuario.setDepartamento(request.getDepartamento());
        usuario.setPuesto(request.getPuesto());
        usuario.setActivo(true);

        Set<Rol> roles = request.getRoles().stream()
                .map(nombre -> rolRepository.findByNombreIgnoreCase(nombre)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nombre)))
                .collect(Collectors.toSet());

        usuario.setRoles(roles);
        Usuario saved = usuarioRepository.save(usuario);
        return toDTO(saved);
    }

    public UsuarioResponseDTO actualizarUsuario(String codigo, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con código: " + codigo));

        usuario.setNombre(request.getNombre());
        usuario.setDepartamento(request.getDepartamento());
        usuario.setPuesto(request.getPuesto());

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Rol> roles = request.getRoles().stream()
                    .map(nombre -> rolRepository.findByNombreIgnoreCase(nombre)
                            .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nombre)))
                    .collect(Collectors.toSet());
            usuario.setRoles(roles);
        }

        Usuario updated = usuarioRepository.save(usuario);
        return toDTO(updated);
    }

    public UsuarioResponseDTO desactivarUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(false);
        return toDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO activarUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(true);
        return toDTO(usuarioRepository.save(usuario));
    }

    public void cambiarPassword(String email, CambiarPasswordRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        if (request.getNuevoPassword() == null || request.getNuevoPassword().length() < 6) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevoPassword()));
        usuarioRepository.save(usuario);
    }

    public UsuarioResponseDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toDTO(usuario);
    }

    public UsuarioResponseDTO obtenerPorCodigo(String codigo) {
        Usuario usuario = usuarioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con código: " + codigo));
        return toDTO(usuario);
    }

    public List<UsuarioResponseDTO> buscarUsuarios(String nombre, String email, String departamento, String puesto) {
        return usuarioRepository.buscarUsuarios(nombre, email, departamento, puesto)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO cambiarRoles(String email, RolCambioRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        usuario.getRoles().clear();

        for (String rolNombre : request.getRoles()) {
            Rol rol = rolRepository.findByNombreIgnoreCase(rolNombre)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));
            usuario.getRoles().add(rol);
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return toDTO(usuarioActualizado);
    }
}