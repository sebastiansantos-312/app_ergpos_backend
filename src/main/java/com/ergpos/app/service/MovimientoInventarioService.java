package com.ergpos.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ergpos.app.dto.movimientos.MovimientoInventarioRequestDTO;
import com.ergpos.app.dto.movimientos.MovimientoInventarioResponseDTO;
import com.ergpos.app.model.MovimientoInventario;
import com.ergpos.app.model.MovimientoInventario.TipoMovimiento;
import com.ergpos.app.model.MovimientoInventario.EstadoMovimiento;
import com.ergpos.app.model.Producto;
import com.ergpos.app.model.Proveedor;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.MovimientoInventarioRepository;
import com.ergpos.app.repository.ProductoRepository;
import com.ergpos.app.repository.ProveedorRepository;
import com.ergpos.app.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepo;
    private final ProductoRepository productoRepo;
    private final ProveedorRepository proveedorRepo;
    private final UsuarioRepository usuarioRepo;

    public MovimientoInventarioService(
            MovimientoInventarioRepository movimientoRepo,
            ProductoRepository productoRepo,
            ProveedorRepository proveedorRepo,
            UsuarioRepository usuarioRepo) {
        this.movimientoRepo = movimientoRepo;
        this.productoRepo = productoRepo;
        this.proveedorRepo = proveedorRepo;
        this.usuarioRepo = usuarioRepo;
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

        // Fechas por defecto si no se especifican
        if (desde == null) {
            desde = LocalDateTime.now().minusYears(1);
        }
        if (hasta == null) {
            hasta = LocalDateTime.now();
        }

        if (desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha inicial no puede ser mayor a la fecha final");
        }

        // Convertir strings a enums
        TipoMovimiento tipo = null;
        if (tipoStr != null && !tipoStr.trim().isEmpty()) {
            try {
                tipo = TipoMovimiento.valueOf(tipoStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tipo inválido. Debe ser ENTRADA o SALIDA");
            }
        }

        EstadoMovimiento estado = null;
        if (estadoStr != null && !estadoStr.trim().isEmpty()) {
            try {
                estado = EstadoMovimiento.valueOf(estadoStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Estado inválido. Debe ser ACTIVO, ANULADO o PENDIENTE");
            }
        }

        // Buscar entidades por códigos
        UUID productoId = null;
        if (codigoProducto != null && !codigoProducto.trim().isEmpty()) {
            Producto producto = productoRepo.findByCodigo(codigoProducto)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Producto no encontrado"));
            productoId = producto.getId();
        }

        UUID usuarioId = null;
        if (codigoUsuario != null && !codigoUsuario.trim().isEmpty()) {
            Usuario usuario = usuarioRepo.findByCodigo(codigoUsuario)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Usuario no encontrado"));
            usuarioId = usuario.getId();
        }

        UUID proveedorId = null;
        if (rucProveedor != null && !rucProveedor.trim().isEmpty()) {
            Proveedor proveedor = proveedorRepo.findByRuc(rucProveedor)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Proveedor no encontrado"));
            proveedorId = proveedor.getId();
        }

        return movimientoRepo.buscarMovimientos(
                productoId, tipo, estado, usuarioId, proveedorId, desde, hasta)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener por ID
    public MovimientoInventarioResponseDTO obtener(String id) {
        MovimientoInventario movimiento = movimientoRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Movimiento no encontrado"));
        return toDTO(movimiento);
    }

    // Crear movimiento
    @Transactional
    public MovimientoInventarioResponseDTO crear(MovimientoInventarioRequestDTO request) {
        // Validar producto
        Producto producto = productoRepo.findByCodigo(request.getCodigoProducto())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));

        if (!producto.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El producto está inactivo");
        }

        // Validar tipo
        TipoMovimiento tipo;
        try {
            tipo = TipoMovimiento.valueOf(request.getTipo().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo inválido. Debe ser ENTRADA o SALIDA");
        }

        // Determinar estado inicial (ACTIVO por defecto, PENDIENTE si se especifica)
        EstadoMovimiento estadoInicial = EstadoMovimiento.ACTIVO;
        if (request.getEstado() != null && !request.getEstado().trim().isEmpty()) {
            try {
                estadoInicial = EstadoMovimiento.valueOf(request.getEstado().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Estado inválido. Debe ser ACTIVO, ANULADO o PENDIENTE");
            }
        }

        // Validar stock para salidas (solo si el movimiento será ACTIVO inmediatamente)
        if (estadoInicial == EstadoMovimiento.ACTIVO && tipo == TipoMovimiento.SALIDA) {
            if (producto.getStockActual() < request.getCantidad()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Stock insuficiente. Disponible: %d, solicitado: %d",
                                producto.getStockActual(), request.getCantidad()));
            }
        }

        // Validar usuario
        Usuario usuario = usuarioRepo.findByCodigo(request.getCodigoUsuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        if (!usuario.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El usuario está inactivo");
        }

        // Validar proveedor (opcional)
        Proveedor proveedor = null;
        if (request.getRucProveedor() != null && !request.getRucProveedor().trim().isEmpty()) {
            proveedor = proveedorRepo.findByRuc(request.getRucProveedor())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Proveedor no encontrado"));

            if (!proveedor.getActivo()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El proveedor está inactivo");
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

        // Actualizar stock del producto solo si el movimiento está ACTIVO
        if (estadoInicial == EstadoMovimiento.ACTIVO) {
            if (tipo == TipoMovimiento.ENTRADA) {
                producto.setStockActual(producto.getStockActual() + request.getCantidad());
            } else {
                producto.setStockActual(producto.getStockActual() - request.getCantidad());
            }
            productoRepo.save(producto);
        }

        MovimientoInventario saved = movimientoRepo.save(movimiento);
        return toDTO(saved);
    }

    // Anular movimiento
    @Transactional
    public MovimientoInventarioResponseDTO anular(String id) {
        MovimientoInventario movimiento = movimientoRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Movimiento no encontrado"));

        if (movimiento.getEstado() != EstadoMovimiento.ACTIVO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se pueden anular movimientos ACTIVOS");
        }

        // Revertir el stock
        Producto producto = movimiento.getProducto();
        if (movimiento.getTipo() == TipoMovimiento.ENTRADA) {
            int nuevoStock = producto.getStockActual() - movimiento.getCantidad();
            if (nuevoStock < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se puede anular: el stock quedaría negativo");
            }
            producto.setStockActual(nuevoStock);
        } else {
            producto.setStockActual(producto.getStockActual() + movimiento.getCantidad());
        }

        movimiento.setEstado(EstadoMovimiento.ANULADO);

        productoRepo.save(producto);
        return toDTO(movimientoRepo.save(movimiento));
    }

    // Activar movimiento (solo si está PENDIENTE)
    @Transactional
    public MovimientoInventarioResponseDTO activar(String id) {
        MovimientoInventario movimiento = movimientoRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Movimiento no encontrado"));

        if (movimiento.getEstado() != EstadoMovimiento.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se pueden activar movimientos PENDIENTES");
        }

        // Validar stock para salidas
        Producto producto = movimiento.getProducto();
        if (movimiento.getTipo() == TipoMovimiento.SALIDA) {
            if (producto.getStockActual() < movimiento.getCantidad()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Stock insuficiente. Disponible: %d, requerido: %d",
                                producto.getStockActual(), movimiento.getCantidad()));
            }
        }

        // Actualizar stock
        if (movimiento.getTipo() == TipoMovimiento.ENTRADA) {
            producto.setStockActual(producto.getStockActual() + movimiento.getCantidad());
        } else {
            producto.setStockActual(producto.getStockActual() - movimiento.getCantidad());
        }

        movimiento.setEstado(EstadoMovimiento.ACTIVO);

        productoRepo.save(producto);
        return toDTO(movimientoRepo.save(movimiento));
    }
}