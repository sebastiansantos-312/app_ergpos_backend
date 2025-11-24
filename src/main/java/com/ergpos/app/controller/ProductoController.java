package com.ergpos.app.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.ergpos.app.dto.producto.ProductoRequestDTO;
import com.ergpos.app.dto.producto.ProductoResponseDTO;
import com.ergpos.app.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Crear producto
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    // Listar con filtros dinámicos
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(productoService.listar(buscar, categoria, activo));
    }

    // Obtener por código
    @GetMapping("/{codigo}")
    public ResponseEntity<ProductoResponseDTO> obtener(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.obtener(codigo));
    }

    // Actualizar por código
    @PutMapping("/{codigo}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody ProductoRequestDTO request) {
        return ResponseEntity.ok(productoService.actualizar(codigo, request));
    }

    // Activar por código
    @PatchMapping("/{codigo}/activar")
    public ResponseEntity<ProductoResponseDTO> activar(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.activar(codigo));
    }

    // Desactivar por código
    @PatchMapping("/{codigo}/desactivar")
    public ResponseEntity<ProductoResponseDTO> desactivar(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.desactivar(codigo));
    }
}