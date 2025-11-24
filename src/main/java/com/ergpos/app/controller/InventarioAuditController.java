package com.ergpos.app.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ergpos.app.dto.audit.AuditResponseDTO;
import com.ergpos.app.service.InventarioAuditService;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "*")
public class InventarioAuditController {

    private final InventarioAuditService auditService;

    public InventarioAuditController(InventarioAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditResponseDTO>> listarTodos() {
        List<AuditResponseDTO> auditoria = auditService.listarTodos();
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/recientes")
    public ResponseEntity<List<AuditResponseDTO>> listarRecientes() {
        List<AuditResponseDTO> auditoria = auditService.listarRecientes();
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/tabla/{tablaNombre}")
    public ResponseEntity<List<AuditResponseDTO>> buscarPorTabla(@PathVariable String tablaNombre) {
        List<AuditResponseDTO> auditoria = auditService.buscarPorTabla(tablaNombre);
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/evento/{eventoTipo}")
    public ResponseEntity<List<AuditResponseDTO>> buscarPorEvento(@PathVariable String eventoTipo) {
        List<AuditResponseDTO> auditoria = auditService.buscarPorEvento(eventoTipo);
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/registro/{tablaNombre}/{registroId}")
    public ResponseEntity<List<AuditResponseDTO>> buscarPorRegistro(
            @PathVariable String tablaNombre,
            @PathVariable UUID registroId) {
        List<AuditResponseDTO> auditoria = auditService.buscarPorRegistro(tablaNombre, registroId);
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<AuditResponseDTO>> buscarPorUsuario(@PathVariable UUID usuarioId) {
        List<AuditResponseDTO> auditoria = auditService.buscarPorUsuario(usuarioId);
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<AuditResponseDTO>> buscarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        List<AuditResponseDTO> auditoria = auditService.buscarPorRangoFechas(desde, hasta);
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/usuario-fechas/{usuarioId}")
    public ResponseEntity<List<AuditResponseDTO>> buscarPorUsuarioYFecha(
            @PathVariable UUID usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        List<AuditResponseDTO> auditoria = auditService.buscarPorUsuarioYFecha(usuarioId, desde, hasta);
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditResponseDTO> obtenerPorId(@PathVariable Long id) {
        AuditResponseDTO auditoria = auditService.obtenerPorId(id);
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<java.util.Map<String, Object>> obtenerEstadisticas() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", auditService.listarTodos().size());
        stats.put("recientes", auditService.listarRecientes().size());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/contadores")
    public ResponseEntity<java.util.Map<String, Long>> obtenerContadores() {
        java.util.Map<String, Long> contadores = new java.util.HashMap<>();
        contadores.put("total", (long) auditService.listarTodos().size());
        contadores.put("insert", auditService.contarPorEvento("INSERT"));
        contadores.put("update", auditService.contarPorEvento("UPDATE"));
        contadores.put("delete", auditService.contarPorEvento("DELETE"));
        return ResponseEntity.ok(contadores);
    }

    @GetMapping("/resumen-tabla")
    public ResponseEntity<List<Object[]>> obtenerResumenPorTabla(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        List<Object[]> resumen = auditService.obtenerResumenPorTabla(desde, hasta);
        return ResponseEntity.ok(resumen);
    }

    @DeleteMapping("/limpiar")
    public ResponseEntity<Void> limpiarRegistrosAntiguos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaLimite) {
        auditService.limpiarRegistrosAntiguos(fechaLimite);
        return ResponseEntity.noContent().build();
    }
}