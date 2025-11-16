package com.ergpos.app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
//import org.springframework.security.access.prepost.PreAuthorize;

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

    //@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE','ALMACENISTA','VENDEDOR')")
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

  //  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE','ALMACENISTA')")
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

   // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE','ALMACENISTA')")
    public ProductoResponseDTO cambiarEstadoProducto(String codigo, boolean activo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        producto.setActivo(activo);
        return toDTO(productoRepository.save(producto));
    }
}
