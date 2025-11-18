package com.ergpos.app.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.ergpos.app.dto.movimientos.MovimientoInventarioRequestDTO;
import com.ergpos.app.dto.movimientos.MovimientoInventarioResponseDTO;
import com.ergpos.app.model.Producto;
import com.ergpos.app.repository.ProductoRepository;
import com.ergpos.app.service.MovimientoInventarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoInventarioController {

        private final MovimientoInventarioService movimientoService;
        private final ProductoRepository productoRepo;

        public MovimientoInventarioController(MovimientoInventarioService movimientoService,
                        ProductoRepository productoRepo) {
                this.movimientoService = movimientoService;
                this.productoRepo = productoRepo;
        }

        @PostMapping
        public MovimientoInventarioResponseDTO registrarMovimiento(
                        @Valid @RequestBody MovimientoInventarioRequestDTO request) {
                return movimientoService.registrarMovimiento(request);
        }

        @GetMapping
        public List<MovimientoInventarioResponseDTO> listarTodos() {
                return movimientoService.listarTodos();
        }

        @GetMapping("/producto/{codigo}")
        public List<MovimientoInventarioResponseDTO> listarPorProducto(@PathVariable String codigo) {
                Producto producto = productoRepo.findByCodigo(codigo)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Producto no encontrado"));

                if (!producto.getActivo()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto inactivo");
                }

                return movimientoService.listarPorProducto(producto);
        }
}
