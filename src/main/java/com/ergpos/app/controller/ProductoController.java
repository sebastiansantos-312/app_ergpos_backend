package com.ergpos.app.controller;

import com.ergpos.app.dto.producto.ProductoRequestDTO;
import com.ergpos.app.dto.producto.ProductoResponseDTO;
import com.ergpos.app.dto.producto.StockBajoResponseDTO;
import com.ergpos.app.dto.producto.StockUpdateRequestDTO;
import com.ergpos.app.dto.producto.StockVerificationResponseDTO;
import com.ergpos.app.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // ============ ENDPOINTS CRUD BÁSICOS ============

    // Crear producto
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    // Listar CON PAGINACIÓN
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductoResponseDTO> productosPage = productoService.listar(
                buscar, categoria, activo, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("productos", productosPage.getContent());
        response.put("currentPage", productosPage.getNumber());
        response.put("totalItems", productosPage.getTotalElements());
        response.put("totalPages", productosPage.getTotalPages());
        response.put("pageSize", productosPage.getSize());
        response.put("hasNext", productosPage.hasNext());
        response.put("hasPrevious", productosPage.hasPrevious());

        return ResponseEntity.ok(response);
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

    // ============ ENDPOINTS DE STOCK ============

    // Verificar stock disponible de un producto
    @GetMapping("/{codigo}/stock-disponible")
    public ResponseEntity<StockVerificationResponseDTO> verificarStockDisponible(
            @PathVariable String codigo,
            @RequestParam Integer cantidad) {

        ProductoResponseDTO producto = productoService.obtener(codigo);

        StockVerificationResponseDTO response = new StockVerificationResponseDTO(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getStockActual(),
                cantidad); // Constructor de 4 parámetros

        return ResponseEntity.ok(response);
    }

    // Reporte de stock bajo con estadísticas
    @GetMapping("/reportes/stock-bajo")
    public ResponseEntity<Map<String, Object>> obtenerReporteStockBajo() {
        List<StockBajoResponseDTO> productos = productoService.obtenerProductosConStockBajo();
        Map<String, Object> estadisticas = productoService.obtenerEstadisticasStock();

        Map<String, Object> response = new HashMap<>();
        response.put("productos", productos);
        response.put("estadisticas", estadisticas);
        response.put("totalProductosStockBajo", productos.size());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    // Obtener productos críticos (sin stock)
    @GetMapping("/reportes/stock-critico")
    public ResponseEntity<List<StockBajoResponseDTO>> obtenerProductosStockCritico() {
        List<StockBajoResponseDTO> productos = productoService.obtenerProductosConStockBajo()
                .stream()
                .filter(p -> p.getNivelCriticidad() == 3)
                .collect(Collectors.toList());

        return ResponseEntity.ok(productos);
    }

    // Actualizar stock manualmente (para administradores)
    @PatchMapping("/{codigo}/stock")
    public ResponseEntity<Map<String, Object>> actualizarStock(
            @PathVariable String codigo,
            @Valid @RequestBody StockUpdateRequestDTO request) {

        // Validar tipo
        if (!"ENTRADA".equalsIgnoreCase(request.getTipo()) &&
                !"SALIDA".equalsIgnoreCase(request.getTipo())) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", "INVALID_TIPO");
            error.put("message", "Tipo inválido. Debe ser 'ENTRADA' o 'SALIDA'");
            error.put("status", 400);
            return ResponseEntity.badRequest().body(error);
        }

        // Validar cantidad positiva
        if (request.getCantidad() <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", "INVALID_CANTIDAD");
            error.put("message", "La cantidad debe ser mayor a 0");
            error.put("status", 400);
            return ResponseEntity.badRequest().body(error);
        }

        boolean esEntrada = "ENTRADA".equalsIgnoreCase(request.getTipo());

        try {
            // Actualizar stock usando el servicio
            productoService.actualizarStock(codigo, request.getCantidad(), esEntrada);

            // Obtener producto actualizado para la respuesta
            ProductoResponseDTO productoActualizado = productoService.obtener(codigo);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stock actualizado correctamente");
            response.put("tipo", request.getTipo());
            response.put("cantidad", request.getCantidad());
            response.put("observacion", request.getObservacion());
            response.put("producto", productoActualizado);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", "STOCK_INSUFICIENTE");
            error.put("message", e.getMessage());
            error.put("status", 400);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Obtener reporte completo de stock
    @GetMapping("/reportes/stock")
    public ResponseEntity<Map<String, Object>> obtenerReporteStock() {
        Page<ProductoResponseDTO> productosPage = productoService.listar(
                null, null, true, PageRequest.of(0, 1000));

        List<ProductoResponseDTO> productos = productosPage.getContent();

        // Calcular estadísticas
        long totalProductos = productos.size();
        long productosConStockBajo = productos.stream()
                .filter(p -> p.getStockActual() == 0 ||
                        (p.getStockMinimo() > 0 && p.getStockActual() < p.getStockMinimo()))
                .count();

        long productosSinStock = productos.stream()
                .filter(p -> p.getStockActual() == 0)
                .count();

        int stockTotal = productos.stream()
                .mapToInt(ProductoResponseDTO::getStockActual)
                .sum();

        double valorTotalInventario = productos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue() * p.getStockActual())
                .sum();

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("totalProductos", totalProductos);
        reporte.put("productosConStockBajo", productosConStockBajo);
        reporte.put("productosSinStock", productosSinStock);
        reporte.put("stockTotal", stockTotal);
        reporte.put("valorTotalInventario", valorTotalInventario);
        reporte.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(reporte);
    }

    // Buscar productos por stock (filtros) - Versión paginada
    @GetMapping("/buscar/stock")
    public ResponseEntity<Map<String, Object>> buscarPorStock(
            @RequestParam(required = false) Integer stockMinimo,
            @RequestParam(required = false) Integer stockMaximo,
            @RequestParam(required = false) Boolean bajoStockMinimo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "stockActual") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductoResponseDTO> productosPage = productoService.buscarPorStock(
                stockMinimo, stockMaximo, bajoStockMinimo, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("productos", productosPage.getContent());
        response.put("currentPage", productosPage.getNumber());
        response.put("totalItems", productosPage.getTotalElements());
        response.put("totalPages", productosPage.getTotalPages());
        response.put("filtros", Map.of(
                "stockMinimo", stockMinimo,
                "stockMaximo", stockMaximo,
                "bajoStockMinimo", bajoStockMinimo));
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    // ============ ENDPOINTS ADICIONALES ============

    // Obtener productos por categoría
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<Map<String, Object>> obtenerPorCategoria(
            @PathVariable String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductoResponseDTO> productosPage = productoService.listar(
                null, categoria, true, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("productos", productosPage.getContent());
        response.put("categoria", categoria);
        response.put("currentPage", productosPage.getNumber());
        response.put("totalItems", productosPage.getTotalElements());
        response.put("totalPages", productosPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // Verificar múltiples productos en stock
    @PostMapping("/verificar-stock-multiple")
    public ResponseEntity<List<StockVerificationResponseDTO>> verificarStockMultiple(
            @RequestBody List<Map<String, Object>> productos) {

        List<StockVerificationResponseDTO> resultados = productos.stream()
                .map(p -> {
                    String codigo = (String) p.get("codigo");
                    Integer cantidad = (Integer) p.get("cantidad");

                    ProductoResponseDTO producto = productoService.obtener(codigo);
                    return new StockVerificationResponseDTO(
                            producto.getCodigo(),
                            producto.getNombre(),
                            producto.getStockActual(),
                            cantidad);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultados);
    }

    
    // Obtener dashboard de estadísticas
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> obtenerDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // Estadísticas generales
        Page<ProductoResponseDTO> todosProductos = productoService.listar(
                null, null, true, PageRequest.of(0, 1));

        long totalProductos = todosProductos.getTotalElements();
        dashboard.put("totalProductos", totalProductos);

        // Productos con stock bajo
        List<StockBajoResponseDTO> stockBajo = productoService.obtenerProductosConStockBajo();
        dashboard.put("productosStockBajo", stockBajo.size());

        // Productos críticos
        long criticos = stockBajo.stream()
                .filter(p -> p.getNivelCriticidad() == 3)
                .count();
        dashboard.put("productosCriticos", criticos);

        // Categorías con más productos
        Map<String, Long> categorias = productoService.obtenerEstadisticasCategorias();
        dashboard.put("distribucionCategorias", categorias);

        // Valor total del inventario
        Map<String, Object> estadisticasStock = productoService.obtenerEstadisticasStock();
        dashboard.put("valorInventario", estadisticasStock.getOrDefault("valorTotalInventario", 0));

        dashboard.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(dashboard);
    }
}