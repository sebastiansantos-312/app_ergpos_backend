package com.ergpos.app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ergpos.app.model.Producto;
import com.ergpos.app.repository.ProductoRepository;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Listar todos los productos o buscar por nombre/código
    @GetMapping
    public List<Producto> listarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String codigo) {
        if (nombre != null) {
            return productoRepository.findByNombreContainingIgnoreCase(nombre);
        } else if (codigo != null) {
            return productoRepository.findByCodigo(codigo).map(List::of).orElse(List.of());
        } else {
            return productoRepository.findAll();
        }
    }

    // Obtener por UUID (opcional)
    @GetMapping("/{id}")
    public Producto obtenerProducto(@PathVariable UUID id) {
        return productoRepository.findById(id).orElse(null);
    }

    // Crear producto
    @PostMapping
    public Producto crearProducto(@RequestBody Producto producto) {
        return productoRepository.save(producto);
    }

    // Eliminar producto
    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable UUID id) {
        productoRepository.deleteById(id);
    }
}
