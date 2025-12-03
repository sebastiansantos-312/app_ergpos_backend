package com.ergpos.app.service;

import com.ergpos.app.dto.producto.ProductoRequestDTO;
import com.ergpos.app.dto.producto.ProductoResponseDTO;
import com.ergpos.app.dto.producto.StockBajoResponseDTO;
import com.ergpos.app.dto.producto.StockVerificationResponseDTO;
import com.ergpos.app.exception.BusinessException;
import com.ergpos.app.model.Categoria;
import com.ergpos.app.model.Producto;
import com.ergpos.app.repository.CategoriaRepository;
import com.ergpos.app.repository.ProductoRepository;
import com.ergpos.app.repository.ProveedorRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

    private static final int STOCK_BAJO_UMBRAL_ABSOLUTO = 5;
    private static final double STOCK_BAJO_PORCENTAJE = 0.3;

    public ProductoService(ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            ProveedorRepository proveedorRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.proveedorRepository = proveedorRepository;
    }

    private ProductoResponseDTO toDTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setCategoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null);
        dto.setCategoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null);
        dto.setCategoriaCodigo(producto.getCategoria().getCodigo());
        dto.setPrecio(producto.getPrecio());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setStockActual(producto.getStockActual());
        dto.setUnidadMedida(producto.getUnidadMedida());
        dto.setActivo(producto.getActivo());
        dto.setCreatedAt(producto.getCreatedAt());
        dto.setUpdatedAt(producto.getUpdatedAt());
        return dto;
    }

    // ==================== VALIDACIONES ====================

    private String requireNonEmpty(String str, String fieldName) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(
                    "INVALID_INPUT",
                    String.format("El campo '%s' es obligatorio", fieldName),
                    400);
        }
        return str.trim();
    }

    private Categoria requireCategoriaValida(String codigoCategoria) {
        String codigo = requireNonEmpty(codigoCategoria, "código de categoría");

        Categoria categoria = categoriaRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new BusinessException(
                        "CATEGORIA_NOT_FOUND",
                        "Categoría no encontrada: " + codigo,
                        404));

        if (!categoria.getActivo()) {
            throw new BusinessException(
                    "CATEGORIA_INACTIVE",
                    "La categoría está inactiva: " + codigo,
                    400);
        }

        return categoria;
    }

    private void validatePositivePrice(BigDecimal precio) {
        if (precio == null) {
            throw new BusinessException(
                    "INVALID_PRICE",
                    "El precio es obligatorio",
                    400);
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "INVALID_PRICE",
                    "El precio debe ser mayor a 0",
                    400);
        }

        if (precio.compareTo(new BigDecimal("1000000")) > 0) {
            throw new BusinessException(
                    "INVALID_PRICE",
                    "El precio no puede ser mayor a 1,000,000",
                    400);
        }
    }

    private void validateStockMinimo(Integer stockMinimo) {
        if (stockMinimo == null) {
            stockMinimo = 0;
        }

        if (stockMinimo < 0) {
            throw new BusinessException(
                    "INVALID_STOCK_MINIMO",
                    "El stock mínimo no puede ser negativo",
                    400);
        }

        if (stockMinimo > 1000000) {
            throw new BusinessException(
                    "INVALID_STOCK_MINIMO",
                    "El stock mínimo no puede ser mayor a 1,000,000",
                    400);
        }
    }

    private void validateStockActual(Integer stockActual) {
        if (stockActual == null) {
            stockActual = 0;
        }

        if (stockActual < 0) {
            throw new BusinessException(
                    "INVALID_STOCK_ACTUAL",
                    "El stock actual no puede ser negativo",
                    400);
        }

        if (stockActual > 1000000) {
            throw new BusinessException(
                    "INVALID_STOCK_ACTUAL",
                    "El stock actual no puede ser mayor a 1,000,000",
                    400);
        }
    }

    private void validateStockRelationship(Integer stockActual, Integer stockMinimo, String operation) {
        if (stockActual < stockMinimo) {
            logger.warn("{} - Stock actual ({}) está por debajo del stock mínimo ({})",
                    operation, stockActual, stockMinimo);

            if ("CREAR".equals(operation) || "ACTUALIZAR".equals(operation)) {
                throw new BusinessException(
                        "STOCK_BELOW_MINIMUM",
                        String.format("El stock actual (%d) no puede ser menor al stock mínimo (%d)",
                                stockActual, stockMinimo),
                        400);
            }
        }
    }

    private void validateNombreProducto(String nombre) {
        requireNonEmpty(nombre, "nombre");

        if (nombre.length() > 255) {
            throw new BusinessException(
                    "INVALID_PRODUCT_NAME",
                    "El nombre del producto no puede tener más de 255 caracteres",
                    400);
        }

        proveedorRepository.findByNombreIgnoreCase(nombre)
                .ifPresent(proveedor -> {
                    throw new BusinessException(
                            "INVALID_PRODUCT_NAME",
                            String.format(
                                    "El nombre '%s' corresponde a un proveedor existente. Use un nombre diferente para el producto.",
                                    nombre),
                            400);
                });
    }

    private void validateUnidadMedida(String unidadMedida) {
        requireNonEmpty(unidadMedida, "unidad de medida");

        List<String> unidadesValidas = List.of(
                "UNIDAD", "KILO", "LITRO", "METRO", "GRAMO",
                "CAJA", "PAQUETE", "ROLLO", "PAR", "DOCENA");

        if (!unidadesValidas.contains(unidadMedida.toUpperCase())) {
            throw new BusinessException(
                    "INVALID_UNIDAD_MEDIDA",
                    String.format("Unidad de medida inválida. Valores permitidos: %s",
                            String.join(", ", unidadesValidas)),
                    400);
        }
    }

    private void logOperation(String operation, String resource, Object identifier) {
        logger.info("{} - {}: {}", operation, resource, identifier);
    }

    private void logWarning(String operation, String resource, Object identifier, String message) {
        logger.warn("{} - {}: {} - {}", operation, resource, identifier, message);
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    public Page<ProductoResponseDTO> listar(
            String buscar,
            String codigoCategoria,
            Boolean activo,
            Pageable pageable) {

        Specification<Producto> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (buscar != null && !buscar.trim().isEmpty()) {
                String searchPattern = "%" + buscar.toLowerCase() + "%";
                Predicate nombrePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")), searchPattern);
                Predicate codigoPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("codigo")), searchPattern);
                predicates.add(criteriaBuilder.or(nombrePredicate, codigoPredicate));
            }

            if (codigoCategoria != null && !codigoCategoria.trim().isEmpty()) {
                Categoria categoria = categoriaRepository.findByCodigoIgnoreCase(codigoCategoria)
                        .orElseThrow(() -> new BusinessException(
                                "CATEGORIA_NOT_FOUND",
                                "Categoría no encontrada: " + codigoCategoria,
                                404));

                if (!categoria.getActivo() && (activo == null || activo)) {
                    throw new BusinessException(
                            "CATEGORIA_INACTIVA",
                            "La categoría está inactiva: " + codigoCategoria,
                            400);
                }

                predicates.add(criteriaBuilder.equal(root.get("categoria"), categoria));
            }

            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        logOperation("LISTAR", "Productos", buscar != null ? buscar : "todos");
        Page<ProductoResponseDTO> productos = productoRepository.findAll(spec, pageable).map(this::toDTO);

        productos.getContent().forEach(producto -> {
            if (producto.getStockActual() < producto.getStockMinimo()) {
                logWarning("LISTAR", "Producto con stock bajo", producto.getCodigo(),
                        String.format("Stock: %d, Mínimo: %d",
                                producto.getStockActual(), producto.getStockMinimo()));
            }
        });

        return productos;
    }

    public ProductoResponseDTO obtener(String codigo) {
        String codigoNormalizado = requireNonEmpty(codigo, "código");

        Producto producto = productoRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado: " + codigoNormalizado,
                        404));

        if (producto.getStockActual() < producto.getStockMinimo()) {
            logWarning("OBTENER", "Producto con stock bajo", codigoNormalizado,
                    String.format("Stock: %d, Mínimo: %d",
                            producto.getStockActual(), producto.getStockMinimo()));
        }

        logOperation("OBTENER", "Producto", codigoNormalizado);
        return toDTO(producto);
    }

    public boolean verificarStockDisponible(String codigoProducto, Integer cantidadRequerida) {
        Producto producto = productoRepository.findByCodigo(codigoProducto)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado: " + codigoProducto,
                        404));

        return producto.getStockActual() >= cantidadRequerida;
    }

    // MÉTODO NUEVO: Para obtener StockVerificationResponseDTO
    public StockVerificationResponseDTO verificarStockDisponibleConRespuesta(String codigoProducto,
            Integer cantidadRequerida) {
        Producto producto = productoRepository.findByCodigo(codigoProducto)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado: " + codigoProducto,
                        404));

        return new StockVerificationResponseDTO(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getStockActual(),
                cantidadRequerida);
    }

    public List<StockBajoResponseDTO> obtenerProductosConStockBajo() {
        List<Producto> productos = productoRepository.findAll()
                .stream()
                .filter(Producto::getActivo)
                .collect(Collectors.toList());

        List<StockBajoResponseDTO> resultado = new ArrayList<>();

        for (Producto producto : productos) {
            if (esStockBajo(producto)) {
                resultado.add(crearStockBajoResponseDTO(producto));
            }
        }

        resultado.sort(Comparator.comparing(StockBajoResponseDTO::getNivelCriticidad).reversed());
        logger.info("Encontrados {} productos con stock bajo", resultado.size());
        return resultado;
    }

    private boolean esStockBajo(Producto producto) {
        int stockActual = producto.getStockActual();
        int stockMinimo = producto.getStockMinimo();

        if (stockMinimo > 0 && stockActual < stockMinimo) {
            return true;
        }

        if (stockMinimo == 0 && stockActual <= STOCK_BAJO_UMBRAL_ABSOLUTO) {
            return true;
        }

        if (stockMinimo > 0 && stockActual < (stockMinimo * STOCK_BAJO_PORCENTAJE)) {
            return true;
        }

        if (stockActual == 0) {
            return true;
        }

        return false;
    }

    private int calcularNivelCriticidad(Producto producto) {
        int stockActual = producto.getStockActual();
        int stockMinimo = producto.getStockMinimo();

        if (stockActual == 0) {
            return 3;
        }

        if (stockMinimo > 0 && stockActual < (stockMinimo * 0.2)) {
            return 3;
        }

        if (stockMinimo > 0 && stockActual < stockMinimo) {
            return 2;
        }

        if (stockActual <= STOCK_BAJO_UMBRAL_ABSOLUTO) {
            return 1;
        }

        return 1;
    }

    private StockBajoResponseDTO crearStockBajoResponseDTO(Producto producto) {
        StockBajoResponseDTO dto = new StockBajoResponseDTO();
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setCategoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null);
        dto.setStockActual(producto.getStockActual());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setUnidadMedida(producto.getUnidadMedida());
        dto.setPrecio(producto.getPrecio());

        dto.setNivelCriticidad(calcularNivelCriticidad(producto));
        dto.setFaltante(Math.max(0, producto.getStockMinimo() - producto.getStockActual()));
        dto.setPorcentajeStock(
                (producto.getStockMinimo() > 0) ? (producto.getStockActual() * 100.0 / producto.getStockMinimo()) : 0);
        dto.setNecesitaReabastecimiento(producto.getStockActual() < producto.getStockMinimo());

        BigDecimal valorFaltante = BigDecimal.valueOf(dto.getFaltante())
                .multiply(producto.getPrecio());
        dto.setValorFaltante(valorFaltante);

        return dto;
    }

    public Map<String, Object> obtenerEstadisticasStock() {
        List<Producto> productos = productoRepository.findAll()
                .stream()
                .filter(Producto::getActivo)
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();

        long totalProductos = productos.size();
        long productosConStockBajo = productos.stream()
                .filter(this::esStockBajo)
                .count();

        long productosSinStock = productos.stream()
                .filter(p -> p.getStockActual() == 0)
                .count();

        long productosPorDebajoMinimo = productos.stream()
                .filter(p -> p.getStockMinimo() > 0 && p.getStockActual() < p.getStockMinimo())
                .count();

        int stockTotal = productos.stream()
                .mapToInt(Producto::getStockActual)
                .sum();

        BigDecimal valorTotalInventario = productos.stream()
                .map(p -> p.getPrecio().multiply(BigDecimal.valueOf(p.getStockActual())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalProductos", totalProductos);
        stats.put("productosConStockBajo", productosConStockBajo);
        stats.put("productosSinStock", productosSinStock);
        stats.put("productosPorDebajoMinimo", productosPorDebajoMinimo);
        stats.put("stockTotal", stockTotal);
        stats.put("valorTotalInventario", valorTotalInventario);
        stats.put("porcentajeStockBajo", totalProductos > 0 ? (productosConStockBajo * 100.0 / totalProductos) : 0);

        return stats;
    }

    // ============ MÉTODOS NUEVOS PARA LOS ENDPOINTS DEL CONTROLADOR ============

    public Page<ProductoResponseDTO> buscarPorStock(
            Integer stockMinimo,
            Integer stockMaximo,
            Boolean bajoStockMinimo,
            Pageable pageable) {

        Specification<Producto> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Solo productos activos
            predicates.add(criteriaBuilder.equal(root.get("activo"), true));

            if (stockMinimo != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("stockActual"), stockMinimo));
            }

            if (stockMaximo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("stockActual"), stockMaximo));
            }

            if (bajoStockMinimo != null) {
                Predicate bajoMinimo = criteriaBuilder.lessThan(root.get("stockActual"), root.get("stockMinimo"));
                if (bajoStockMinimo) {
                    predicates.add(bajoMinimo);
                } else {
                    predicates.add(criteriaBuilder.not(bajoMinimo));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return productoRepository.findAll(spec, pageable).map(this::toDTO);
    }

    public Map<String, Long> obtenerEstadisticasCategorias() {
        List<Producto> productos = productoRepository.findAll()
                .stream()
                .filter(Producto::getActivo)
                .collect(Collectors.toList());

        Map<String, Long> estadisticas = new HashMap<>();

        productos.forEach(producto -> {
            String categoriaNombre = producto.getCategoria() != null ? producto.getCategoria().getNombre()
                    : "Sin Categoría";
            estadisticas.put(categoriaNombre, estadisticas.getOrDefault(categoriaNombre, 0L) + 1);
        });

        return estadisticas;
    }

    public List<ProductoResponseDTO> obtenerProductosStockBajoSimple() {
        List<Producto> productos = productoRepository.findAll()
                .stream()
                .filter(Producto::getActivo)
                .filter(p -> p.getStockActual() == 0 ||
                        (p.getStockMinimo() > 0 && p.getStockActual() < p.getStockMinimo()) ||
                        (p.getStockMinimo() == 0 && p.getStockActual() <= 5))
                .collect(Collectors.toList());

        return productos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductoResponseDTO> obtenerProductosStockCritico(Pageable pageable) {
        List<Producto> productos = productoRepository.findAll()
                .stream()
                .filter(Producto::getActivo)
                .filter(p -> p.getStockActual() == 0 ||
                        (p.getStockMinimo() > 0 && p.getStockActual() < (p.getStockMinimo() * 0.2)))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        List<ProductoResponseDTO> contenido = productos.stream()
                .skip(start)
                .limit(pageable.getPageSize())
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(contenido, pageable, productos.size());
    }

    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        logger.debug("Creando producto con datos: {}", request);

        String codigo = requireNonEmpty(request.getCodigo(), "código");
        String nombre = requireNonEmpty(request.getNombre(), "nombre");
        String unidadMedida = requireNonEmpty(request.getUnidadMedida(), "unidad de medida");
        String codigoCategoria = requireNonEmpty(request.getCodigoCategoria(), "código de categoría");

        validateNombreProducto(nombre);
        validateUnidadMedida(unidadMedida);
        validatePositivePrice(request.getPrecio());

        Integer stockMinimo = request.getStockMinimo() != null ? request.getStockMinimo() : 0;
        Integer stockActual = request.getStockActual() != null ? request.getStockActual() : 0;

        validateStockMinimo(stockMinimo);
        validateStockActual(stockActual);
        validateStockRelationship(stockActual, stockMinimo, "CREAR");

        if (productoRepository.existsByCodigo(codigo)) {
            throw new BusinessException(
                    "DUPLICATED_CODE",
                    "Ya existe un producto con el código: " + codigo,
                    409);
        }

        Categoria categoria = requireCategoriaValida(codigoCategoria);

        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        producto.setCategoria(categoria);
        producto.setPrecio(request.getPrecio());
        producto.setStockMinimo(stockMinimo);
        producto.setStockActual(stockActual);
        producto.setUnidadMedida(unidadMedida);
        producto.setActivo(true);

        Producto saved = productoRepository.save(producto);
        logOperation("CREAR", "Producto", saved.getCodigo());

        if (stockActual < stockMinimo) {
            logWarning("CREAR", "Producto creado con stock bajo", saved.getCodigo(),
                    String.format("Stock: %d, Mínimo: %d", stockActual, stockMinimo));
        }

        return toDTO(saved);
    }

    @Transactional
    public ProductoResponseDTO actualizar(String codigo, ProductoRequestDTO request) {
        String codigoActual = requireNonEmpty(codigo, "código actual");

        Producto producto = productoRepository.findByCodigo(codigoActual)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado: " + codigoActual,
                        404));

        String nuevoCodigo = requireNonEmpty(request.getCodigo(), "nuevo código");
        String nuevoNombre = requireNonEmpty(request.getNombre(), "nombre");
        String nuevaUnidadMedida = requireNonEmpty(request.getUnidadMedida(), "unidad de medida");
        String nuevoCodigoCategoria = requireNonEmpty(request.getCodigoCategoria(), "código de categoría");

        if (!producto.getNombre().equalsIgnoreCase(nuevoNombre)) {
            validateNombreProducto(nuevoNombre);
        }

        validateUnidadMedida(nuevaUnidadMedida);
        validatePositivePrice(request.getPrecio());

        if (!producto.getCodigo().equals(nuevoCodigo) && productoRepository.existsByCodigo(nuevoCodigo)) {
            throw new BusinessException(
                    "DUPLICATED_CODE",
                    "Ya existe un producto con el código: " + nuevoCodigo,
                    409);
        }

        Categoria nuevaCategoria = requireCategoriaValida(nuevoCodigoCategoria);

        Integer nuevoStockMinimo = request.getStockMinimo() != null ? request.getStockMinimo()
                : producto.getStockMinimo();
        Integer nuevoStockActual = request.getStockActual() != null ? request.getStockActual()
                : producto.getStockActual();

        validateStockMinimo(nuevoStockMinimo);
        validateStockActual(nuevoStockActual);
        validateStockRelationship(nuevoStockActual, nuevoStockMinimo, "ACTUALIZAR");

        producto.setCodigo(nuevoCodigo);
        producto.setNombre(nuevoNombre);
        producto.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        producto.setCategoria(nuevaCategoria);
        producto.setPrecio(request.getPrecio());
        producto.setUnidadMedida(nuevaUnidadMedida);
        producto.setStockMinimo(nuevoStockMinimo);
        producto.setStockActual(nuevoStockActual);

        Producto updated = productoRepository.save(producto);
        logOperation("ACTUALIZAR", "Producto", updated.getCodigo());

        if (nuevoStockActual < nuevoStockMinimo) {
            logWarning("ACTUALIZAR", "Producto actualizado con stock bajo", updated.getCodigo(),
                    String.format("Stock: %d, Mínimo: %d", nuevoStockActual, nuevoStockMinimo));
        }

        return toDTO(updated);
    }

    @Transactional
    public ProductoResponseDTO activar(String codigo) {
        String codigoNormalizado = requireNonEmpty(codigo, "código");

        Producto producto = productoRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado: " + codigoNormalizado,
                        404));

        if (producto.getActivo()) {
            throw new BusinessException(
                    "ALREADY_ACTIVE",
                    "El producto ya está activo",
                    400);
        }

        producto.setActivo(true);
        Producto updated = productoRepository.save(producto);

        logOperation("ACTIVAR", "Producto", codigoNormalizado);
        return toDTO(updated);
    }

    @Transactional
    public ProductoResponseDTO desactivar(String codigo) {
        String codigoNormalizado = requireNonEmpty(codigo, "código");

        Producto producto = productoRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado: " + codigoNormalizado,
                        404));

        if (!producto.getActivo()) {
            throw new BusinessException(
                    "ALREADY_INACTIVE",
                    "El producto ya está inactivo",
                    400);
        }

        if (producto.getStockActual() > 0) {
            throw new BusinessException(
                    "PRODUCTO_CON_STOCK",
                    String.format(
                            "No se puede desactivar el producto porque tiene stock disponible (%d unidades). Realice una salida de inventario primero.",
                            producto.getStockActual()),
                    400);
        }

        producto.setActivo(false);
        Producto updated = productoRepository.save(producto);

        logOperation("DESACTIVAR", "Producto", codigoNormalizado);
        return toDTO(updated);
    }

    @Transactional
    public void actualizarStock(String codigoProducto, Integer cantidad, boolean esEntrada) {
        Producto producto = productoRepository.findByCodigo(codigoProducto)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado: " + codigoProducto,
                        404));

        if (!producto.getActivo()) {
            throw new BusinessException(
                    "PRODUCTO_INACTIVE",
                    "No se puede modificar stock de un producto inactivo",
                    400);
        }

        int nuevoStock;
        if (esEntrada) {
            nuevoStock = producto.getStockActual() + cantidad;
        } else {
            if (producto.getStockActual() < cantidad) {
                throw new BusinessException(
                        "STOCK_INSUFFICIENT",
                        String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                                producto.getStockActual(), cantidad),
                        400);
            }
            nuevoStock = producto.getStockActual() - cantidad;
        }

        if (nuevoStock < 0) {
            throw new BusinessException(
                    "NEGATIVE_STOCK",
                    "El stock no puede ser negativo",
                    400);
        }

        producto.setStockActual(nuevoStock);
        productoRepository.save(producto);

        logger.info("Stock actualizado - Producto: {}, Operación: {}, Cantidad: {}, Stock final: {}",
                codigoProducto, esEntrada ? "ENTRADA" : "SALIDA", cantidad, nuevoStock);

        if (nuevoStock < producto.getStockMinimo()) {
            logWarning("ACTUALIZAR_STOCK", "Producto con stock bajo después de movimiento",
                    codigoProducto, String.format("Stock: %d, Mínimo: %d", nuevoStock, producto.getStockMinimo()));
        }
    }
}