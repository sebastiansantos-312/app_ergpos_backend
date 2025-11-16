package com.ergpos.app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
//import org.springframework.security.access.prepost.PreAuthorize;

import com.ergpos.app.dto.movimientos.MovimientoInventarioRequestDTO;
import com.ergpos.app.dto.movimientos.MovimientoInventarioResponseDTO;
import com.ergpos.app.model.MovimientoInventario;
import com.ergpos.app.model.Producto;
import com.ergpos.app.repository.MovimientoInventarioRepository;
import com.ergpos.app.repository.ProductoRepository;

@Service
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepo;
    private final ProductoRepository productoRepo;

    public MovimientoInventarioService(MovimientoInventarioRepository movimientoRepo,
            ProductoRepository productoRepo) {
        this.movimientoRepo = movimientoRepo;
        this.productoRepo = productoRepo;
    }

    private MovimientoInventarioResponseDTO toDTO(MovimientoInventario movimiento) {
        MovimientoInventarioResponseDTO dto = new MovimientoInventarioResponseDTO();
        dto.setCodigoProducto(movimiento.getProducto().getCodigo());
        dto.setNombreProducto(movimiento.getProducto().getNombre());
        dto.setCantidad(movimiento.getCantidad());
        dto.setTipo(movimiento.getTipo());
        dto.setProveedor(movimiento.getProveedor());
        dto.setObservacion(movimiento.getObservacion());
        return dto;
    }

   // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE','ALMACENISTA')")
    public MovimientoInventarioResponseDTO registrarMovimiento(MovimientoInventarioRequestDTO request) {

        Producto producto = productoRepo.findByCodigo(request.getCodigoProducto())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (!producto.getActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto inactivo");
        }

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setCantidad(request.getCantidad());
        movimiento.setTipo(request.getTipo().toUpperCase());
        movimiento.setProveedor(request.getProveedor());
        movimiento.setObservacion(request.getObservacion());

        return toDTO(movimientoRepo.save(movimiento));
    }

   // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE','ALMACENISTA','VENDEDOR')")
    public List<MovimientoInventarioResponseDTO> listarTodos() {
        return movimientoRepo.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    //@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR','GERENTE','ALMACENISTA','VENDEDOR')")
    public List<MovimientoInventarioResponseDTO> listarPorProducto(Producto producto) {
        return movimientoRepo.findByProducto(producto)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
