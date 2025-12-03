package com.ergpos.app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ergpos.app.dto.usuarios.CambiarPasswordRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioResponseDTO;
import com.ergpos.app.dto.usuarios.UsuarioUpdateRequestDTO;
import com.ergpos.app.exception.DuplicateResourceException;
import com.ergpos.app.exception.ResourceNotFoundException;
import com.ergpos.app.exception.ValidationException;
import com.ergpos.app.model.Rol;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.RolRepository;
import com.ergpos.app.repository.UsuarioRepository;
import com.ergpos.app.util.PasswordValidator;
import com.ergpos.app.util.ValidationUtils;

/**
 * Servicio para gestión de usuarios.
 * 
 * Implementa operaciones CRUD y gestión de contraseñas con validaciones
 * de negocio y manejo estandarizado de excepciones.
 */
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

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

    /**
     * Lista usuarios con filtros opcionales.
     */
    public List<UsuarioResponseDTO> listar(String nombre, String email, String rolNombre, Boolean activo) {
        logger.debug("Listando usuarios - nombre: {}, email: {}, rol: {}, activo: {}",
                nombre, email, rolNombre, activo);

        return usuarioRepository.buscarUsuarios(nombre, email, rolNombre, activo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por email.
     */
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        String emailNormalizado = ValidationUtils.requireValidEmail(email, "email");

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailNormalizado));

        logger.debug("Usuario obtenido: {}", emailNormalizado);
        return toDTO(usuario);
    }

    /**
     * Crea un nuevo usuario.
     */
    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO request) {
        logger.info("Creando nuevo usuario con email: {}", request.getEmail());

        // Validaciones de entrada
        String nombre = ValidationUtils.requireNonEmpty(request.getNombre(), "nombre");
        ValidationUtils.requireMaxLength(nombre, "nombre", 255);

        String email = ValidationUtils.requireValidEmail(request.getEmail(), "email");

        String password = ValidationUtils.requireNonEmpty(request.getPassword(), "password");
        PasswordValidator.validateBasic(password); // Usar validateBasic o validate según requisitos

        String nombreRol = ValidationUtils.requireNonEmpty(request.getNombreRol(), "rol");

        // Validar código si se proporciona
        String codigo = null;
        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty()) {
            codigo = request.getCodigo().trim();
            ValidationUtils.requireMaxLength(codigo, "código", 50);

            if (usuarioRepository.existsByCodigo(codigo)) {
                throw new DuplicateResourceException("Usuario", "código", codigo);
            }
        }

        // Verificar que el email no exista
        if (usuarioRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Usuario", "email", email);
        }

        // Buscar y validar rol
        Rol rol = rolRepository.findByNombreIgnoreCase(nombreRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", nombreRol));

        if (!rol.getActivo()) {
            throw new ValidationException("INACTIVE_ROLE",
                    "El rol '" + nombreRol + "' está inactivo");
        }

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setCodigo(codigo);
        usuario.setRol(rol);
        usuario.setActivo(true);

        Usuario saved = usuarioRepository.save(usuario);
        logger.info("Usuario creado exitosamente: {}", email);

        return toDTO(saved);
    }

    /**
     * Actualiza un usuario existente.
     */
    @Transactional
    public UsuarioResponseDTO actualizar(String email, UsuarioUpdateRequestDTO request) {
        String emailActual = ValidationUtils.requireValidEmail(email, "email actual");

        logger.info("Actualizando usuario: {}", emailActual);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailActual)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailActual));

        // Validar nuevos datos
        String nuevoNombre = ValidationUtils.requireNonEmpty(request.getNombre(), "nombre");
        ValidationUtils.requireMaxLength(nuevoNombre, "nombre", 255);

        String nuevoEmail = ValidationUtils.requireValidEmail(request.getEmail(), "email");

        String nombreRol = ValidationUtils.requireNonEmpty(request.getNombreRol(), "rol");

        // Validar código si se proporciona
        String nuevoCodigo = null;
        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty()) {
            nuevoCodigo = request.getCodigo().trim();
            ValidationUtils.requireMaxLength(nuevoCodigo, "código", 50);

            // Verificar duplicado solo si cambió
            if (!nuevoCodigo.equals(usuario.getCodigo()) &&
                    usuarioRepository.existsByCodigo(nuevoCodigo)) {
                throw new DuplicateResourceException("Usuario", "código", nuevoCodigo);
            }
        }

        // Verificar email duplicado solo si cambió
        if (!usuario.getEmail().equals(nuevoEmail) &&
                usuarioRepository.existsByEmail(nuevoEmail)) {
            throw new DuplicateResourceException("Usuario", "email", nuevoEmail);
        }

        // Buscar y validar rol
        Rol rol = rolRepository.findByNombreIgnoreCase(nombreRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", nombreRol));

        if (!rol.getActivo()) {
            throw new ValidationException("INACTIVE_ROLE",
                    "El rol '" + nombreRol + "' está inactivo");
        }

        // Actualizar datos
        usuario.setNombre(nuevoNombre);
        usuario.setEmail(nuevoEmail);
        usuario.setCodigo(nuevoCodigo);
        usuario.setRol(rol);

        // Actualizar password solo si se proporciona
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            String newPassword = request.getPassword().trim();
            PasswordValidator.validateBasic(newPassword);
            usuario.setPasswordHash(passwordEncoder.encode(newPassword));
            logger.info("Contraseña actualizada para usuario: {}", nuevoEmail);
        }

        Usuario updated = usuarioRepository.save(usuario);
        logger.info("Usuario actualizado exitosamente: {}", nuevoEmail);

        return toDTO(updated);
    }

    /**
     * Activa un usuario.
     */
    @Transactional
    public UsuarioResponseDTO activar(String email) {
        String emailNormalizado = ValidationUtils.requireValidEmail(email, "email");

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailNormalizado));

        if (usuario.getActivo()) {
            throw new ValidationException("ALREADY_ACTIVE", "El usuario ya está activo");
        }

        usuario.setActivo(true);
        Usuario updated = usuarioRepository.save(usuario);

        logger.info("Usuario activado: {}", emailNormalizado);
        return toDTO(updated);
    }

    /**
     * Desactiva un usuario.
     */
    @Transactional
    public UsuarioResponseDTO desactivar(String email) {
        String emailNormalizado = ValidationUtils.requireValidEmail(email, "email");

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailNormalizado));

        if (!usuario.getActivo()) {
            throw new ValidationException("ALREADY_INACTIVE", "El usuario ya está inactivo");
        }

        usuario.setActivo(false);
        Usuario updated = usuarioRepository.save(usuario);

        logger.info("Usuario desactivado: {}", emailNormalizado);
        return toDTO(updated);
    }

    /**
     * Cambia la contraseña de un usuario.
     */
    @Transactional
    public void cambiarPassword(String email, CambiarPasswordRequestDTO request) {
        String emailNormalizado = ValidationUtils.requireValidEmail(email, "email");

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailNormalizado));

        // Validar contraseña actual
        String passwordActual = ValidationUtils.requireNonEmpty(
                request.getPasswordActual(), "contraseña actual");

        if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            throw new ValidationException("INVALID_PASSWORD", "La contraseña actual es incorrecta");
        }

        // Validar nueva contraseña
        String nuevoPassword = ValidationUtils.requireNonEmpty(
                request.getNuevoPassword(), "nueva contraseña");
        PasswordValidator.validateBasic(nuevoPassword);

        // Verificar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(nuevoPassword, usuario.getPasswordHash())) {
            throw new ValidationException("SAME_PASSWORD",
                    "La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setPasswordHash(passwordEncoder.encode(nuevoPassword));
        usuarioRepository.save(usuario);

        logger.info("Contraseña cambiada para usuario: {}", emailNormalizado);
    }
}