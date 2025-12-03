package com.ergpos.app.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ergpos.app.dto.roles.RolRequestDTO;
import com.ergpos.app.dto.roles.RolResponseDTO;
import com.ergpos.app.exception.DuplicateResourceException;
import com.ergpos.app.exception.ResourceNotFoundException;
import com.ergpos.app.exception.ValidationException;
import com.ergpos.app.model.Rol;
import com.ergpos.app.repository.RolRepository;
import com.ergpos.app.util.ValidationUtils;

/**
 * Servicio para gestión de roles.
 * 
 * Los nombres de roles se normalizan a MAYÚSCULAS.
 */
@Service
@Transactional(readOnly = true)
public class RolService {

    private static final Logger logger = LoggerFactory.getLogger(RolService.class);
    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    private RolResponseDTO toDTO(Rol rol) {
        RolResponseDTO dto = new RolResponseDTO();
        dto.setId(rol.getId());
        dto.setNombre(rol.getNombre());
        dto.setActivo(rol.getActivo());
        dto.setCreatedAt(rol.getCreatedAt());
        dto.setUpdatedAt(rol.getUpdatedAt());
        return dto;
    }

    /**
     * Lista roles con búsqueda dinámica.
     */
    public List<RolResponseDTO> listar(String buscar, Boolean activo) {
        logger.debug("Listando roles - buscar: {}, activo: {}", buscar, activo);

        return rolRepository.buscar(buscar, activo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un rol por ID.
     */
    public RolResponseDTO obtenerPorId(UUID id) {
        ValidationUtils.requireNonNull(id, "ID");

        logger.debug("Obteniendo rol por ID: {}", id);

        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "ID", id));

        return toDTO(rol);
    }

    /**
     * Obtiene un rol por nombre.
     */
    public RolResponseDTO obtenerPorNombre(String nombre) {
        String nombreNormalizado = ValidationUtils.requireNonEmpty(nombre, "nombre").toUpperCase();

        logger.debug("Obteniendo rol por nombre: {}", nombreNormalizado);

        Rol rol = rolRepository.findByNombreIgnoreCase(nombreNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", nombreNormalizado));

        return toDTO(rol);
    }

    /**
     * Crea un nuevo rol.
     */
    @Transactional
    public RolResponseDTO crear(RolRequestDTO request) {
        logger.info("Creando rol: {}", request.getNombre());

        String nombreRol = ValidationUtils.requireNonEmpty(request.getNombre(), "nombre").toUpperCase();
        ValidationUtils.requireMaxLength(nombreRol, "nombre", 50);

        if (rolRepository.existsByNombreIgnoreCase(nombreRol)) {
            throw new DuplicateResourceException("Rol", "nombre", nombreRol);
        }

        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        rol.setActivo(true);

        Rol saved = rolRepository.save(rol);
        logger.info("Rol creado: {}", saved.getNombre());

        return toDTO(saved);
    }

    /**
     * Actualiza un rol.
     */
    @Transactional
    public RolResponseDTO actualizar(String nombre, RolRequestDTO request) {
        logger.info("Actualizando rol: {}", nombre);

        String nombreActual = ValidationUtils.requireNonEmpty(nombre, "nombre actual").toUpperCase();

        Rol rol = rolRepository.findByNombreIgnoreCase(nombreActual)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", nombreActual));

        String nuevoNombre = ValidationUtils.requireNonEmpty(request.getNombre(), "nombre").toUpperCase();
        ValidationUtils.requireMaxLength(nuevoNombre, "nombre", 50);

        if (!rol.getNombre().equalsIgnoreCase(nuevoNombre) &&
                rolRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new DuplicateResourceException("Rol", "nombre", nuevoNombre);
        }

        rol.setNombre(nuevoNombre);
        Rol updated = rolRepository.save(rol);

        logger.info("Rol actualizado: {}", updated.getNombre());
        return toDTO(updated);
    }

    /**
     * Activa un rol.
     */
    @Transactional
    public RolResponseDTO activar(String nombre) {
        logger.info("Activando rol: {}", nombre);

        String nombreNormalizado = ValidationUtils.requireNonEmpty(nombre, "nombre").toUpperCase();

        Rol rol = rolRepository.findByNombreIgnoreCase(nombreNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", nombreNormalizado));

        if (rol.getActivo()) {
            throw new ValidationException("ALREADY_ACTIVE", "El rol ya está activo");
        }

        rol.setActivo(true);
        Rol updated = rolRepository.save(rol);

        logger.info("Rol activado: {}", updated.getNombre());
        return toDTO(updated);
    }

    /**
     * Desactiva un rol.
     */
    @Transactional
    public RolResponseDTO desactivar(String nombre) {
        logger.info("Desactivando rol: {}", nombre);

        String nombreNormalizado = ValidationUtils.requireNonEmpty(nombre, "nombre").toUpperCase();

        Rol rol = rolRepository.findByNombreIgnoreCase(nombreNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", nombreNormalizado));

        if (!rol.getActivo()) {
            throw new ValidationException("ALREADY_INACTIVE", "El rol ya está inactivo");
        }

        rol.setActivo(false);
        Rol updated = rolRepository.save(rol);

        logger.info("Rol desactivado: {}", updated.getNombre());
        return toDTO(updated);
    }
}