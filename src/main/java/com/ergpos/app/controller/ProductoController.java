package com.ergpos.app.controller;

import java.util.List;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ergpos.app.dto.producto.ProductoRequestDTO;
import com.ergpos.app.dto.producto.ProductoResponseDTO;
import com.ergpos.app.service.ProductoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    //@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('SUPER_ADMIN')")
    @GetMapping
    public List<ProductoResponseDTO> listarProductos(@RequestParam(required = false) Boolean activo) {
        return productoService.listarProductos(activo);
    }

    //@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public ProductoResponseDTO crearProducto(@Valid @RequestBody ProductoRequestDTO request) {
        return productoService.crearProducto(request);
    }

    //@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('SUPER_ADMIN')")
    @PutMapping("/codigo/{codigo}/desactivar")
    public ProductoResponseDTO darDeBaja(@PathVariable String codigo) {
        return productoService.cambiarEstadoProducto(codigo, false);
    }

    //@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('SUPER_ADMIN')")
    @PutMapping("/codigo/{codigo}/activar")
    public ProductoResponseDTO restaurarProducto(@PathVariable String codigo) {
        return productoService.cambiarEstadoProducto(codigo, true);
    }

}
