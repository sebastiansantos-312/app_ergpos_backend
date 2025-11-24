package com.ergpos.app.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ergpos.app.dto.producto.ProductoRequestDTO;
import com.ergpos.app.dto.producto.ProductoResponseDTO;
import com.ergpos.app.model.Categoria;
import com.ergpos.app.model.Producto;
import com.ergpos.app.repository.CategoriaRepository;
import com.ergpos.app.repository.ProductoRepository;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    private ProductoResponseDTO toDTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setCategoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null);
        dto.setCategoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null);
        dto.setPrecio(producto.getPrecio());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setStockActual(producto.getStockActual());
        dto.setUnidadMedida(producto.getUnidadMedida());
        dto.setActivo(producto.getActivo());
        dto.setCreatedAt(producto.getCreatedAt());
        dto.setUpdatedAt(producto.getUpdatedAt());
        return dto;
    }

    // Listar con búsqueda dinámica
    public List<ProductoResponseDTO> listar(String buscar, String codigoCategoria, Boolean activo) {
        // Buscar categoría por código si se proporciona
        UUID categoriaId = null;
        if (codigoCategoria != null && !codigoCategoria.trim().isEmpty()) {
            Categoria categoria = categoriaRepository.findByCodigoIgnoreCase(codigoCategoria)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Categoría no encontrada"));
            categoriaId = categoria.getId();
        }

        return productoRepository.buscar(buscar, categoriaId, activo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener por código
    public ProductoResponseDTO obtener(String codigo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));
        return toDTO(producto);
    }

    // Crear producto
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        String codigo = request.getCodigo().trim();

        if (productoRepository.existsByCodigo(codigo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un producto con ese código");
        }

        Categoria categoria = null;
        if (request.getCodigoCategoria() != null && !request.getCodigoCategoria().trim().isEmpty()) {
            categoria = categoriaRepository.findByCodigoIgnoreCase(request.getCodigoCategoria())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Categoría no encontrada"));

            if (!categoria.getActivo()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La categoría está inactiva");
            }
        }

        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(request.getNombre().trim());
        producto.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        producto.setCategoria(categoria);
        producto.setPrecio(request.getPrecio());
        producto.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 0);
        producto.setStockActual(request.getStockActual() != null ? request.getStockActual() : 0);
        producto.setUnidadMedida(request.getUnidadMedida() != null ? request.getUnidadMedida() : "UNIDAD");
        producto.setActivo(true);

        return toDTO(productoRepository.save(producto));
    }

    // Actualizar producto
    @Transactional
    public ProductoResponseDTO actualizar(String codigo, ProductoRequestDTO request) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));

        String nuevoCodigo = request.getCodigo().trim();

        if (!producto.getCodigo().equals(nuevoCodigo) && productoRepository.existsByCodigo(nuevoCodigo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un producto con ese código");
        }

        Categoria categoria = null;
        if (request.getCodigoCategoria() != null && !request.getCodigoCategoria().trim().isEmpty()) {
            categoria = categoriaRepository.findByCodigoIgnoreCase(request.getCodigoCategoria())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Categoría no encontrada"));

            if (!categoria.getActivo()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La categoría está inactiva");
            }
        }

        producto.setCodigo(nuevoCodigo);
        producto.setNombre(request.getNombre().trim());
        producto.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        producto.setCategoria(categoria);
        producto.setPrecio(request.getPrecio());
        producto.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 0);
        producto.setStockActual(
                request.getStockActual() != null ? request.getStockActual() : producto.getStockActual());
        producto.setUnidadMedida(request.getUnidadMedida() != null ? request.getUnidadMedida() : "UNIDAD");

        return toDTO(productoRepository.save(producto));
    }

    // Activar producto
    @Transactional
    public ProductoResponseDTO activar(String codigo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));

        if (producto.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El producto ya está activo");
        }

        producto.setActivo(true);
        return toDTO(productoRepository.save(producto));
    }

    // Desactivar producto
    @Transactional
    public ProductoResponseDTO desactivar(String codigo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));

        if (!producto.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El producto ya está inactivo");
        }

        producto.setActivo(false);
        return toDTO(productoRepository.save(producto));
    }
}