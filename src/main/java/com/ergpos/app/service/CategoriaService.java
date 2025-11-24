package com.ergpos.app.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ergpos.app.dto.categorias.CategoriaRequestDTO;
import com.ergpos.app.dto.categorias.CategoriaResponseDTO;
import com.ergpos.app.model.Categoria;
import com.ergpos.app.repository.CategoriaRepository;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

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

    private Categoria buscarCategoria(String identificador) {
        if (esUUID(identificador)) {
            return categoriaRepository.findById(UUID.fromString(identificador))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Categoría no encontrada"));
        } else {
            return categoriaRepository.findByCodigoIgnoreCase(identificador)
                    .or(() -> categoriaRepository.findByNombreIgnoreCase(identificador))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Categoría no encontrada"));
        }
    }

    // Listar con búsqueda dinámica
    public List<CategoriaResponseDTO> listar(String buscar, Boolean activo) {
        return categoriaRepository.buscar(buscar, activo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener por ID, Código o Nombre
    public CategoriaResponseDTO obtener(String identificador) {
        return toDTO(buscarCategoria(identificador));
    }

    // Crear categoría
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO request) {
        // Validar nombre único
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe una categoría con ese nombre");
        }

        // Validar código único (si se proporciona)
        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty()) {
            if (categoriaRepository.existsByCodigoIgnoreCase(request.getCodigo())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe una categoría con ese código");
            }
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre().trim());
        categoria.setCodigo(request.getCodigo() != null ? request.getCodigo().trim() : null);
        categoria.setActivo(true);

        return toDTO(categoriaRepository.save(categoria));
    }

    // Actualizar por ID, Código o Nombre
    @Transactional
    public CategoriaResponseDTO actualizar(String identificador, CategoriaRequestDTO request) {
        Categoria categoria = buscarCategoria(identificador);

        String nuevoNombre = request.getNombre().trim();

        // Validar nombre único (si cambió)
        if (!categoria.getNombre().equalsIgnoreCase(nuevoNombre) &&
                categoriaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe una categoría con ese nombre");
        }

        // Validar código único (si cambió)
        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty()) {
            String nuevoCodigo = request.getCodigo().trim();
            if ((categoria.getCodigo() == null || !nuevoCodigo.equalsIgnoreCase(categoria.getCodigo())) &&
                    categoriaRepository.existsByCodigoIgnoreCase(nuevoCodigo)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe una categoría con ese código");
            }
        }

        categoria.setNombre(nuevoNombre);
        categoria.setCodigo(request.getCodigo() != null ? request.getCodigo().trim() : null);

        return toDTO(categoriaRepository.save(categoria));
    }

    // Activar por ID, Código o Nombre
    @Transactional
    public CategoriaResponseDTO activar(String identificador) {
        Categoria categoria = buscarCategoria(identificador);

        if (categoria.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La categoría ya está activa");
        }

        categoria.setActivo(true);
        return toDTO(categoriaRepository.save(categoria));
    }

    // Desactivar por ID, Código o Nombre
    @Transactional
    public CategoriaResponseDTO desactivar(String identificador) {
        Categoria categoria = buscarCategoria(identificador);

        if (!categoria.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La categoría ya está inactiva");
        }

        categoria.setActivo(false);
        return toDTO(categoriaRepository.save(categoria));
    }
}