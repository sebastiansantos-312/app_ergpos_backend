package com.ergpos.app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ergpos.app.dto.proveedores.ProveedorRequestDTO;
import com.ergpos.app.dto.proveedores.ProveedorResponseDTO;
import com.ergpos.app.exception.DuplicateResourceException;
import com.ergpos.app.exception.ResourceNotFoundException;
import com.ergpos.app.exception.ValidationException;
import com.ergpos.app.model.Proveedor;
import com.ergpos.app.repository.ProveedorRepository;
import com.ergpos.app.util.ValidationUtils;

/**
 * Servicio para gestión de proveedores.
 * 
 * Soporta búsqueda por RUC, email o nombre.
 */
@Service
@Transactional(readOnly = true)
public class ProveedorService {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorService.class);
    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    private ProveedorResponseDTO toDTO(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.setId(proveedor.getId());
        dto.setNombre(proveedor.getNombre());
        dto.setRuc(proveedor.getRuc());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setDireccion(proveedor.getDireccion());
        dto.setActivo(proveedor.getActivo());
        dto.setCreatedAt(proveedor.getCreatedAt());
        dto.setUpdatedAt(proveedor.getUpdatedAt());
        return dto;
    }

    /**
     * Busca un proveedor por RUC, email o nombre.
     */
    private Proveedor buscarProveedor(String identificador) {
        ValidationUtils.requireNonEmpty(identificador, "identificador");

        return proveedorRepository.findByRuc(identificador)
                .or(() -> proveedorRepository.findByEmailIgnoreCase(identificador))
                .or(() -> proveedorRepository.findByNombreIgnoreCase(identificador))
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", identificador));
    }

    /**
     * Lista proveedores con búsqueda dinámica.
     */
    public List<ProveedorResponseDTO> listar(String buscar, Boolean activo) {
        logger.debug("Listando proveedores - buscar: {}, activo: {}", buscar, activo);

        return proveedorRepository.buscar(buscar, activo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un proveedor por RUC, email o nombre.
     */
    public ProveedorResponseDTO obtener(String identificador) {
        logger.debug("Obteniendo proveedor: {}", identificador);
        return toDTO(buscarProveedor(identificador));
    }

    /**
     * Crea un nuevo proveedor.
     */
    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO request) {
        logger.info("Creando proveedor: {}", request.getNombre());

        // Validaciones
        String nombre = ValidationUtils.requireNonEmpty(request.getNombre(), "nombre");
        ValidationUtils.requireMaxLength(nombre, "nombre", 255);

        if (proveedorRepository.existsByNombreIgnoreCase(nombre)) {
            throw new DuplicateResourceException("Proveedor", "nombre", nombre);
        }

        // Validar RUC si se proporciona
        String ruc = null;
        if (request.getRuc() != null && !request.getRuc().trim().isEmpty()) {
            ruc = request.getRuc().trim();
            ValidationUtils.requireMaxLength(ruc, "RUC", 20);

            if (proveedorRepository.existsByRuc(ruc)) {
                throw new DuplicateResourceException("Proveedor", "RUC", ruc);
            }
        }

        // Validar email si se proporciona
        String email = null;
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            email = ValidationUtils.requireValidEmail(request.getEmail(), "email");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(nombre);
        proveedor.setRuc(ruc);
        proveedor.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        proveedor.setEmail(email);
        proveedor.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);
        proveedor.setActivo(true);

        Proveedor saved = proveedorRepository.save(proveedor);
        logger.info("Proveedor creado: {}", saved.getNombre());

        return toDTO(saved);
    }

    /**
     * Actualiza un proveedor por RUC, email o nombre.
     */
    @Transactional
    public ProveedorResponseDTO actualizar(String identificador, ProveedorRequestDTO request) {
        logger.info("Actualizando proveedor: {}", identificador);

        Proveedor proveedor = buscarProveedor(identificador);

        String nuevoNombre = ValidationUtils.requireNonEmpty(request.getNombre(), "nombre");
        ValidationUtils.requireMaxLength(nuevoNombre, "nombre", 255);

        if (!proveedor.getNombre().equalsIgnoreCase(nuevoNombre) &&
                proveedorRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new DuplicateResourceException("Proveedor", "nombre", nuevoNombre);
        }

        // Validar RUC si se proporciona
        String nuevoRuc = null;
        if (request.getRuc() != null && !request.getRuc().trim().isEmpty()) {
            nuevoRuc = request.getRuc().trim();
            ValidationUtils.requireMaxLength(nuevoRuc, "RUC", 20);

            if ((proveedor.getRuc() == null || !nuevoRuc.equals(proveedor.getRuc())) &&
                    proveedorRepository.existsByRuc(nuevoRuc)) {
                throw new DuplicateResourceException("Proveedor", "RUC", nuevoRuc);
            }
        }

        // Validar email si se proporciona
        String nuevoEmail = null;
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            nuevoEmail = ValidationUtils.requireValidEmail(request.getEmail(), "email");
        }

        proveedor.setNombre(nuevoNombre);
        proveedor.setRuc(nuevoRuc);
        proveedor.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        proveedor.setEmail(nuevoEmail);
        proveedor.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);

        Proveedor updated = proveedorRepository.save(proveedor);
        logger.info("Proveedor actualizado: {}", updated.getNombre());

        return toDTO(updated);
    }

    /**
     * Activa un proveedor por RUC, email o nombre.
     */
    @Transactional
    public ProveedorResponseDTO activar(String identificador) {
        logger.info("Activando proveedor: {}", identificador);

        Proveedor proveedor = buscarProveedor(identificador);

        if (proveedor.getActivo()) {
            throw new ValidationException("ALREADY_ACTIVE", "El proveedor ya está activo");
        }

        proveedor.setActivo(true);
        Proveedor updated = proveedorRepository.save(proveedor);

        logger.info("Proveedor activado: {}", updated.getNombre());
        return toDTO(updated);
    }

    /**
     * Desactiva un proveedor por RUC, email o nombre.
     */
    @Transactional
    public ProveedorResponseDTO desactivar(String identificador) {
        logger.info("Desactivando proveedor: {}", identificador);

        Proveedor proveedor = buscarProveedor(identificador);

        if (!proveedor.getActivo()) {
            throw new ValidationException("ALREADY_INACTIVE", "El proveedor ya está inactivo");
        }

        proveedor.setActivo(false);
        Proveedor updated = proveedorRepository.save(proveedor);

        logger.info("Proveedor desactivado: {}", updated.getNombre());
        return toDTO(updated);
    }
}