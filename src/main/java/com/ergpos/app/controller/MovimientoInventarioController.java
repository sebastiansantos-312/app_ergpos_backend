package com.ergpos.app.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.ergpos.app.dto.movimientos.MovimientoInventarioRequestDTO;
import com.ergpos.app.dto.movimientos.MovimientoInventarioResponseDTO;
import com.ergpos.app.service.MovimientoInventarioService;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoInventarioController {

        private final MovimientoInventarioService movimientoService;

        public MovimientoInventarioController(MovimientoInventarioService movimientoService) {
                this.movimientoService = movimientoService;
        }

        // Crear movimiento (entrada o salida)
        @PostMapping
        public ResponseEntity<MovimientoInventarioResponseDTO> crear(
                        @Valid @RequestBody MovimientoInventarioRequestDTO request) {
                return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.crear(request));
        }

        // Listar con filtros dinámicos
        @GetMapping
        public ResponseEntity<List<MovimientoInventarioResponseDTO>> listar(
                        @RequestParam(required = false) String producto,
                        @RequestParam(required = false) String tipo,
                        @RequestParam(required = false) String estado,
                        @RequestParam(required = false) String usuario,
                        @RequestParam(required = false) String proveedor,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
                return ResponseEntity
                                .ok(movimientoService.listar(producto, tipo, estado, usuario, proveedor, desde, hasta));
        }

        // Obtener por ID
        @GetMapping("/{id}")
        public ResponseEntity<MovimientoInventarioResponseDTO> obtener(@PathVariable String id) {
                return ResponseEntity.ok(movimientoService.obtener(id));
        }

        // Anular movimiento (cambiar estado a ANULADO y revertir stock)
        @PatchMapping("/{id}/anular")
        public ResponseEntity<MovimientoInventarioResponseDTO> anular(@PathVariable String id) {
                return ResponseEntity.ok(movimientoService.anular(id));
        }

        // Activar movimiento (solo si está PENDIENTE)
        @PatchMapping("/{id}/activar")
        public ResponseEntity<MovimientoInventarioResponseDTO> activar(@PathVariable String id) {
                return ResponseEntity.ok(movimientoService.activar(id));
        }
}