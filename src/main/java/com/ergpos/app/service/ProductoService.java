package com.ergpos.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.ergpos.app.dto.producto.ProductoRequestDTO;
import com.ergpos.app.dto.producto.ProductoResponseDTO;
import com.ergpos.app.model.Producto;
import com.ergpos.app.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    private ProductoResponseDTO toDTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setActivo(producto.getActivo());
        return dto;
    }

    public List<ProductoResponseDTO> listarProductos(Boolean activo) {

        List<Producto> productos;

        if (activo == null) {
            productos = productoRepository.findAll();
        } else if (activo) {
            productos = productoRepository.findByActivoTrue();
        } else {
            productos = productoRepository.findByActivoFalse();
        }

        return productos.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProductoResponseDTO crearProducto(ProductoRequestDTO request) {
        if (productoRepository.findByCodigo(request.getCodigo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de producto ya existe");
        }

        Producto producto = new Producto();
        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setActivo(true);

        return toDTO(productoRepository.save(producto));
    }

    public ProductoResponseDTO cambiarEstadoProducto(String codigo, boolean activo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        producto.setActivo(activo);
        return toDTO(productoRepository.save(producto));
    }

    //NUEVOS MÉTODOS SUGERIDOS

    public ProductoResponseDTO actualizarProducto(String codigo, ProductoRequestDTO request) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Verificar si el nuevo código ya existe (y no es el mismo producto)
        if (!codigo.equals(request.getCodigo()) &&
                productoRepository.findByCodigo(request.getCodigo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nuevo código ya existe");
        }

        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        // Nota: No actualizamos 'activo' aquí, usa cambiarEstadoProducto para eso

        return toDTO(productoRepository.save(producto));
    }

    public ProductoResponseDTO buscarPorCodigo(String codigo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        return toDTO(producto);
    }

    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        // Usamos el método que ya tienes en el repository
        List<Producto> productos = productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
        return productos.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Map<String, Object> obtenerEstadisticas() {
        long totalProductos = productoRepository.count();
        long productosActivos = productoRepository.findByActivoTrue().size();
        long productosInactivos = productoRepository.findByActivoFalse().size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProductos", totalProductos);
        stats.put("productosActivos", productosActivos);
        stats.put("productosInactivos", productosInactivos);
        stats.put("porcentajeActivos", totalProductos > 0 ? (productosActivos * 100.0 / totalProductos) : 0);

        return stats;
    }

    public List<ProductoResponseDTO> productosRecientes() {
        // Para este método necesitarías agregar el método en el repository
        // Por ahora usamos una implementación simple
        List<Producto> todosProductos = productoRepository.findAll();
        return todosProductos.stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(10)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}