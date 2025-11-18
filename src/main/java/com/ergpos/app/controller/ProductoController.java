package com.ergpos.app.controller;

import java.util.List;
import java.util.Map;

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

    @GetMapping
    public List<ProductoResponseDTO> listarProductos(@RequestParam(required = false) Boolean activo) {
        return productoService.listarProductos(activo);
    }

    @GetMapping("/codigo/{codigo}")
    public ProductoResponseDTO buscarPorCodigo(@PathVariable String codigo) {
        return productoService.buscarPorCodigo(codigo);
    }

    @GetMapping("/buscar")
    public List<ProductoResponseDTO> buscarPorNombre(@RequestParam String nombre) {
        return productoService.buscarPorNombre(nombre);
    }

    @PostMapping
    public ProductoResponseDTO crearProducto(@Valid @RequestBody ProductoRequestDTO request) {
        return productoService.crearProducto(request);
    }

    @PutMapping("/codigo/{codigo}")
    public ProductoResponseDTO actualizarProducto(
            @PathVariable String codigo,
            @Valid @RequestBody ProductoRequestDTO request) {
        return productoService.actualizarProducto(codigo, request);
    }

    @PutMapping("/codigo/{codigo}/activar")
    public ProductoResponseDTO activarProducto(@PathVariable String codigo) {
        return productoService.cambiarEstadoProducto(codigo, true);
    }

    @PutMapping("/codigo/{codigo}/desactivar")
    public ProductoResponseDTO desactivarProducto(@PathVariable String codigo) {
        return productoService.cambiarEstadoProducto(codigo, false);
    }

    //ENDPOINTS DE REPORTES/ESTADÍSTICAS
    @GetMapping("/estadisticas")
    public Map<String, Object> obtenerEstadisticas() {
        return productoService.obtenerEstadisticas();
    }

    @GetMapping("/recientes")
    public List<ProductoResponseDTO> productosRecientes() {
        return productoService.productosRecientes();
    }
}