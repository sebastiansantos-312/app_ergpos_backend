package com.ergpos.app.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ergpos.app.dto.categorias.CategoriaRequestDTO;
import com.ergpos.app.dto.categorias.CategoriaResponseDTO;
import com.ergpos.app.exception.DuplicateResourceException;
import com.ergpos.app.exception.ResourceNotFoundException;
import com.ergpos.app.exception.ValidationException;
import com.ergpos.app.model.Categoria;
import com.ergpos.app.repository.CategoriaRepository;
import com.ergpos.app.util.ValidationUtils;

/**
 * Servicio para gestión de categorías.
 * 
 * Soporta búsqueda por ID (UUID), código o nombre.
 */
@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaService.class);
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    private CategoriaResponseDTO toDTO(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setCodigo(categoria.getCodigo());
        dto.setActivo(categoria.getActivo());
        dto.setCreatedAt(categoria.getCreatedAt());
        dto.setUpdatedAt(categoria.getUpdatedAt());
        return dto;
    }

    private boolean esUUID(String identificador) {
        try {
            UUID.fromString(identificador);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Busca una categoría por ID, código o nombre.
     */
    private Categoria buscarCategoria(String identificador) {
        ValidationUtils.requireNonEmpty(identificador, "identificador");

        if (esUUID(identificador)) {
            return categoriaRepository.findById(UUID.fromString(identificador))
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", "ID", identificador));
        } else {
            return categoriaRepository.findByCodigoIgnoreCase(identificador)
                    .or(() -> categoriaRepository.findByNombreIgnoreCase(identificador))
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", identificador));
        }
    }

    /**
     * Lista categorías con búsqueda dinámica.
     */
    public List<CategoriaResponseDTO> listar(String buscar, Boolean activo) {
        logger.debug("Listando categorías - buscar: {}, activo: {}", buscar, activo);

        return categoriaRepository.buscar(buscar, activo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una categoría por ID, código o nombre.
     */
    public CategoriaResponseDTO obtener(String identificador) {
        logger.debug("Obteniendo categoría: {}", identificador);
        return toDTO(buscarCategoria(identificador));
    }

    /**
     * Crea una nueva categoría.
     */
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO request) {
        logger.info("Creando categoría: {}", request.getNombre());

        // Validaciones
        String nombre = ValidationUtils.requireNonEmpty(request.getNombre(), "nombre");
        ValidationUtils.requireMaxLength(nombre, "nombre", 255);

        // Validar nombre único
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new DuplicateResourceException("Categoría", "nombre", nombre);
        }

        // Validar código único (si se proporciona)
        String codigo = null;
        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty()) {
            codigo = request.getCodigo().trim();
            ValidationUtils.requireMaxLength(codigo, "código", 50);

            if (categoriaRepository.existsByCodigoIgnoreCase(codigo)) {
                throw new DuplicateResourceException("Categoría", "código", codigo);
            }
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setCodigo(codigo);
        categoria.setActivo(true);

        Categoria saved = categoriaRepository.save(categoria);
        logger.info("Categoría creada: {}", saved.getNombre());

        return toDTO(saved);
    }

    /**
     * Actualiza una categoría por ID, código o nombre.
     */
    @Transactional
    public CategoriaResponseDTO actualizar(String identificador, CategoriaRequestDTO request) {
        logger.info("Actualizando categoría: {}", identificador);

        Categoria categoria = buscarCategoria(identificador);

        String nuevoNombre = ValidationUtils.requireNonEmpty(request.getNombre(), "nombre");
        ValidationUtils.requireMaxLength(nuevoNombre, "nombre", 255);

        // Validar nombre único (si cambió)
        if (!categoria.getNombre().equalsIgnoreCase(nuevoNombre) &&
                categoriaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new DuplicateResourceException("Categoría", "nombre", nuevoNombre);
        }

        // Validar código único (si cambió)
        String nuevoCodigo = null;
        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty()) {
            nuevoCodigo = request.getCodigo().trim();
            ValidationUtils.requireMaxLength(nuevoCodigo, "código", 50);

            if ((categoria.getCodigo() == null || !nuevoCodigo.equalsIgnoreCase(categoria.getCodigo())) &&
                    categoriaRepository.existsByCodigoIgnoreCase(nuevoCodigo)) {
                throw new DuplicateResourceException("Categoría", "código", nuevoCodigo);
            }
        }

        categoria.setNombre(nuevoNombre);
        categoria.setCodigo(nuevoCodigo);

        Categoria updated = categoriaRepository.save(categoria);
        logger.info("Categoría actualizada: {}", updated.getNombre());

        return toDTO(updated);
    }

    /**
     * Activa una categoría por ID, código o nombre.
     */
    @Transactional
    public CategoriaResponseDTO activar(String identificador) {
        logger.info("Activando categoría: {}", identificador);

        Categoria categoria = buscarCategoria(identificador);

        if (categoria.getActivo()) {
            throw new ValidationException("ALREADY_ACTIVE", "La categoría ya está activa");
        }

        categoria.setActivo(true);
        Categoria updated = categoriaRepository.save(categoria);

        logger.info("Categoría activada: {}", updated.getNombre());
        return toDTO(updated);
    }

    /**
     * Desactiva una categoría por ID, código o nombre.
     */
    @Transactional
    public CategoriaResponseDTO desactivar(String identificador) {
        logger.info("Desactivando categoría: {}", identificador);

        Categoria categoria = buscarCategoria(identificador);

        if (!categoria.getActivo()) {
            throw new ValidationException("ALREADY_INACTIVE", "La categoría ya está inactiva");
        }

        categoria.setActivo(false);
        Categoria updated = categoriaRepository.save(categoria);

        logger.info("Categoría desactivada: {}", updated.getNombre());
        return toDTO(updated);
    }
}