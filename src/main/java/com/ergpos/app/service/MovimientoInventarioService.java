
package com.ergpos.app.service;
import com.ergpos.app.model.Producto;

import java.util.UUID;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ergpos.app.dto.movimientos.MovimientoInventarioRequestDTO;
import com.ergpos.app.dto.movimientos.MovimientoInventarioResponseDTO;
import com.ergpos.app.model.MovimientoInventario;
import com.ergpos.app.model.MovimientoInventario.TipoMovimiento;
import com.ergpos.app.model.MovimientoInventario.EstadoMovimiento;
import com.ergpos.app.model.Proveedor;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.MovimientoInventarioRepository;
import com.ergpos.app.repository.ProductoRepository;
import com.ergpos.app.repository.ProveedorRepository;
import com.ergpos.app.repository.UsuarioRepository;
import com.ergpos.app.exception.StockInsufficiencyException;
import com.ergpos.app.exception.BusinessException;

@Service
@Transactional(readOnly = true)
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepo;
    private final ProductoRepository productoRepo;
    private final ProveedorRepository proveedorRepo;
    private final UsuarioRepository usuarioRepo;
    private final InventarioAuditService auditService;

    public MovimientoInventarioService(
            MovimientoInventarioRepository movimientoRepo,
            ProductoRepository productoRepo,
            ProveedorRepository proveedorRepo,
            UsuarioRepository usuarioRepo,
            InventarioAuditService auditService) {
        this.movimientoRepo = movimientoRepo;
        this.productoRepo = productoRepo;
        this.proveedorRepo = proveedorRepo;
        this.usuarioRepo = usuarioRepo;
        this.auditService = auditService;
    }

    private MovimientoInventarioResponseDTO toDTO(MovimientoInventario mov) {
        MovimientoInventarioResponseDTO dto = new MovimientoInventarioResponseDTO();
        dto.setId(mov.getId());
        dto.setCodigoProducto(mov.getProducto().getCodigo());
        dto.setNombreProducto(mov.getProducto().getNombre());
        dto.setCantidad(mov.getCantidad());
        dto.setTipo(mov.getTipo().name());
        dto.setProveedorNombre(mov.getProveedor() != null ? mov.getProveedor().getNombre() : null);
        dto.setUsuarioNombre(mov.getUsuario().getNombre());
        dto.setObservacion(mov.getObservacion());
        dto.setDocumentoRef(mov.getDocumentoRef());
        dto.setCostoUnitario(mov.getCostoUnitario());
        dto.setFecha(mov.getFecha());
        dto.setEstado(mov.getEstado().name());
        dto.setCreatedAt(mov.getCreatedAt());
        return dto;
    }

    // Listar con búsqueda dinámica
    public List<MovimientoInventarioResponseDTO> listar(
            String codigoProducto,
            String tipoStr,
            String estadoStr,
            String codigoUsuario,
            String rucProveedor,
            LocalDateTime desde,
            LocalDateTime hasta) {

        if (desde == null) {
            desde = LocalDateTime.now().minusYears(1);
        }
        if (hasta == null) {
            hasta = LocalDateTime.now();
        }

        if (desde.isAfter(hasta)) {
            throw new BusinessException(
                    "INVALID_DATE_RANGE",
                    "La fecha inicial no puede ser mayor a la fecha final",
                    400);
        }

        TipoMovimiento tipo = null;
        if (tipoStr != null && !tipoStr.trim().isEmpty()) {
            try {
                tipo = TipoMovimiento.valueOf(tipoStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(
                        "INVALID_TIPO",
                        "Tipo inválido. Debe ser ENTRADA o SALIDA",
                        400);
            }
        }

        EstadoMovimiento estado = null;
        if (estadoStr != null && !estadoStr.trim().isEmpty()) {
            try {
                estado = EstadoMovimiento.valueOf(estadoStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(
                        "INVALID_ESTADO",
                        "Estado inválido. Debe ser ACTIVO, ANULADO o PENDIENTE",
                        400);
            }
        }

        UUID productoId = null;
        if (codigoProducto != null && !codigoProducto.trim().isEmpty()) {
            Producto producto = productoRepo.findByCodigo(codigoProducto)
                    .orElseThrow(() -> new BusinessException(
                            "PRODUCTO_NOT_FOUND",
                            "Producto no encontrado",
                            404));
            productoId = producto.getId();
        }

        UUID usuarioId = null;
        if (codigoUsuario != null && !codigoUsuario.trim().isEmpty()) {
            Usuario usuario = usuarioRepo.findByCodigo(codigoUsuario)
                    .orElseThrow(() -> new BusinessException(
                            "USUARIO_NOT_FOUND",
                            "Usuario no encontrado",
                            404));
            usuarioId = usuario.getId();
        }

        UUID proveedorId = null;
        if (rucProveedor != null && !rucProveedor.trim().isEmpty()) {
            Proveedor proveedor = proveedorRepo.findByRuc(rucProveedor)
                    .orElseThrow(() -> new BusinessException(
                            "PROVEEDOR_NOT_FOUND",
                            "Proveedor no encontrado",
                            404));
            proveedorId = proveedor.getId();
        }

        return movimientoRepo.buscarMovimientos(
                productoId, tipo, estado, usuarioId, proveedorId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener por ID
    public MovimientoInventarioResponseDTO obtener(String id) {
        MovimientoInventario movimiento = movimientoRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException(
                        "MOVIMIENTO_NOT_FOUND",
                        "Movimiento no encontrado",
                        404));
        return toDTO(movimiento);
    }

    // CREAR MOVIMIENTO CON LOCKS PESSIMISTAS
    @Transactional
    public MovimientoInventarioResponseDTO crear(MovimientoInventarioRequestDTO request) {

        //LOCK PESSIMISTA: Obtener producto con lock
        Producto producto = productoRepo.findByCodigoWithLock(request.getCodigoProducto())
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado",
                        404));

        if (!producto.getActivo()) {
            throw new BusinessException(
                    "PRODUCTO_INACTIVE",
                    "El producto está inactivo",
                    400);
        }

        // Validar tipo
        TipoMovimiento tipo;
        try {
            tipo = TipoMovimiento.valueOf(request.getTipo().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    "INVALID_TIPO",
                    "Tipo inválido. Debe ser ENTRADA o SALIDA",
                    400);
        }

        // Determinar estado inicial
        EstadoMovimiento estadoInicial = EstadoMovimiento.ACTIVO;
        if (request.getEstado() != null && !request.getEstado().trim().isEmpty()) {
            try {
                estadoInicial = EstadoMovimiento.valueOf(request.getEstado().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(
                        "INVALID_ESTADO",
                        "Estado inválido. Debe ser ACTIVO, ANULADO o PENDIENTE",
                        400);
            }
        }

        // VALIDACIÓN BAJO LOCK: Validar stock DENTRO de la transacción
        if (estadoInicial == EstadoMovimiento.ACTIVO && tipo == TipoMovimiento.SALIDA) {
            if (producto.getStockActual() < request.getCantidad()) {
                throw new StockInsufficiencyException(
                        producto.getStockActual(),
                        request.getCantidad());
            }
        }

        // Validar usuario
        Usuario usuario = usuarioRepo.findByCodigo(request.getCodigoUsuario())
                .orElseThrow(() -> new BusinessException(
                        "USUARIO_NOT_FOUND",
                        "Usuario no encontrado",
                        404));

        if (!usuario.getActivo()) {
            throw new BusinessException(
                    "USUARIO_INACTIVE",
                    "El usuario está inactivo",
                    400);
        }

        // Validar proveedor (opcional)
        Proveedor proveedor = null;
        if (request.getRucProveedor() != null && !request.getRucProveedor().trim().isEmpty()) {
            proveedor = proveedorRepo.findByRuc(request.getRucProveedor())
                    .orElseThrow(() -> new BusinessException(
                            "PROVEEDOR_NOT_FOUND",
                            "Proveedor no encontrado",
                            404));

            if (!proveedor.getActivo()) {
                throw new BusinessException(
                        "PROVEEDOR_INACTIVE",
                        "El proveedor está inactivo",
                        400);
            }
        }

        // Crear movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(request.getCantidad());
        movimiento.setProveedor(proveedor);
        movimiento.setUsuario(usuario);
        movimiento.setObservacion(request.getObservacion());
        movimiento.setDocumentoRef(request.getDocumentoRef());
        movimiento.setCostoUnitario(request.getCostoUnitario());
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setEstado(estadoInicial);

        // Actualizar stock BAJO LOCK (dentro de transacción)
        if (estadoInicial == EstadoMovimiento.ACTIVO) {
            if (tipo == TipoMovimiento.ENTRADA) {
                producto.setStockActual(producto.getStockActual() + request.getCantidad());
            } else {
                producto.setStockActual(producto.getStockActual() - request.getCantidad());
            }
            productoRepo.save(producto);
        }

        MovimientoInventario saved = movimientoRepo.save(movimiento);

        //REGISTRAR AUDITORÍA
        auditService.registrarAuditoria(
                "INSERT",
                "movimientos_inventario",
                saved.getId(),
                usuario.getId(),
                String.format("Movimiento %s: Producto %s, Cantidad %d",
                        tipo.name(),
                        producto.getNombre(),
                        request.getCantidad()));

        return toDTO(saved);
    }

    // ANULAR MOVIMIENTO CON LOCKS
    @Transactional
    public MovimientoInventarioResponseDTO anular(String id) {
        MovimientoInventario movimiento = movimientoRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException(
                        "MOVIMIENTO_NOT_FOUND",
                        "Movimiento no encontrado",
                        404));

        if (movimiento.getEstado() != EstadoMovimiento.ACTIVO) {
            throw new BusinessException(
                    "INVALID_MOVEMENT_STATE",
                    "Solo se pueden anular movimientos ACTIVOS",
                    400);
        }

        //LOCK PESSIMISTA: Obtener producto con lock
        Producto producto = productoRepo.findByIdWithLock(movimiento.getProducto().getId())
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado",
                        404));

        // Revertir stock bajo lock
        if (movimiento.getTipo() == TipoMovimiento.ENTRADA) {
            int nuevoStock = producto.getStockActual() - movimiento.getCantidad();
            if (nuevoStock < 0) {
                throw new BusinessException(
                        "NEGATIVE_STOCK",
                        "No se puede anular: el stock quedaría negativo",
                        400);
            }
            producto.setStockActual(nuevoStock);
        } else {
            producto.setStockActual(producto.getStockActual() + movimiento.getCantidad());
        }

        movimiento.setEstado(EstadoMovimiento.ANULADO);

        productoRepo.save(producto);
        MovimientoInventario updated = movimientoRepo.save(movimiento);

        //REGISTRAR AUDITORÍA
        auditService.registrarAuditoria(
                "UPDATE",
                "movimientos_inventario",
                movimiento.getId(),
                movimiento.getUsuario().getId(),
                String.format("Movimiento anulado. Stock revertido de %d",
                        movimiento.getCantidad()));

        return toDTO(updated);
    }

    // ACTIVAR MOVIMIENTO CON LOCKS
    @Transactional
    public MovimientoInventarioResponseDTO activar(String id) {
        MovimientoInventario movimiento = movimientoRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException(
                        "MOVIMIENTO_NOT_FOUND",
                        "Movimiento no encontrado",
                        404));

        if (movimiento.getEstado() != EstadoMovimiento.PENDIENTE) {
            throw new BusinessException(
                    "INVALID_MOVEMENT_STATE",
                    "Solo se pueden activar movimientos PENDIENTES",
                    400);
        }

        // LOCK PESSIMISTA: Obtener producto con lock
        Producto producto = productoRepo.findByIdWithLock(movimiento.getProducto().getId())
                .orElseThrow(() -> new BusinessException(
                        "PRODUCTO_NOT_FOUND",
                        "Producto no encontrado",
                        404));

        // Validar stock para salidas (bajo lock)
        if (movimiento.getTipo() == TipoMovimiento.SALIDA) {
            if (producto.getStockActual() < movimiento.getCantidad()) {
                throw new StockInsufficiencyException(
                        producto.getStockActual(),
                        movimiento.getCantidad());
            }
        }

        // Actualizar stock bajo lock
        if (movimiento.getTipo() == TipoMovimiento.ENTRADA) {
            producto.setStockActual(producto.getStockActual() + movimiento.getCantidad());
        } else {
            producto.setStockActual(producto.getStockActual() - movimiento.getCantidad());
        }

        movimiento.setEstado(EstadoMovimiento.ACTIVO);

        productoRepo.save(producto);
        MovimientoInventario updated = movimientoRepo.save(movimiento);

        // REGISTRAR AUDITORÍA
        auditService.registrarAuditoria(
                "UPDATE",
                "movimientos_inventario",
                movimiento.getId(),
                movimiento.getUsuario().getId(),
                String.format("Movimiento PENDIENTE activado. Stock actualizado %d %s",
                        movimiento.getCantidad(),
                        movimiento.getTipo().name()));

        return toDTO(updated);
    }
}