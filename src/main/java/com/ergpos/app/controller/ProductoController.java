package com.ergpos.app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    @SuppressWarnings("null")
    @PostMapping
    public Producto crearProducto(@RequestBody Producto producto) {
        return productoRepository.save(producto);
    }

    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public Producto obtenerProducto(@PathVariable UUID id) {
        return productoRepository.findById(id).orElse(null);
    }

    @SuppressWarnings("null")
    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable UUID id) {
        productoRepository.deleteById(id);
    }
}
