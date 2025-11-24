package com.ergpos.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.ergpos.app.dto.audit.AuditResponseDTO;
import com.ergpos.app.model.InventarioAudit;
import com.ergpos.app.repository.InventarioAuditRepository;

@Service
@Transactional(readOnly = true)
public class InventarioAuditService {

    private final InventarioAuditRepository auditRepository;

    public InventarioAuditService(InventarioAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    private AuditResponseDTO toDTO(InventarioAudit audit) {
        AuditResponseDTO dto = new AuditResponseDTO();
        dto.setId(audit.getId());
        dto.setEventoTipo(audit.getEventoTipo());
        dto.setTablaNombre(audit.getTablaNombre());
        dto.setRegistroId(audit.getRegistroId());
        dto.setUsuarioId(audit.getUsuarioId());
        dto.setDetalle(audit.getDetalle());
        dto.setCreatedAt(audit.getCreatedAt());
        return dto;
    }

    public List<AuditResponseDTO> listarTodos() {
        return auditRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditResponseDTO> listarRecientes() {
        return auditRepository.findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditResponseDTO> buscarPorTabla(String tablaNombre) {
        return auditRepository.findByTablaNombre(tablaNombre)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditResponseDTO> buscarPorEvento(String eventoTipo) {
        return auditRepository.findByEventoTipo(eventoTipo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditResponseDTO> buscarPorRegistro(String tablaNombre, UUID registroId) {
        return auditRepository.findAuditoriaPorRegistro(tablaNombre, registroId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditResponseDTO> buscarPorUsuario(UUID usuarioId) {
        return auditRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditResponseDTO> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha inicial no puede ser mayor a la fecha final");
        }

        return auditRepository.findAuditoriaPorFecha(desde, hasta)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditResponseDTO> buscarPorUsuarioYFecha(UUID usuarioId, LocalDateTime desde, LocalDateTime hasta) {
        if (desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha inicial no puede ser mayor a la fecha final");
        }

        return auditRepository.findAuditoriaPorUsuarioYFecha(usuarioId, desde, hasta)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AuditResponseDTO obtenerPorId(Long id) {
        InventarioAudit audit = auditRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro de auditoría no encontrado con ID: " + id));
        return toDTO(audit);
    }

    public long contarPorEvento(String eventoTipo) {
        return auditRepository.countByEventoTipo(eventoTipo);
    }

    public long contarPorTabla(String tablaNombre) {
        return auditRepository.countByTablaNombre(tablaNombre);
    }

    public long contarPorUsuario(UUID usuarioId) {
        return auditRepository.countByUsuarioId(usuarioId);
    }

    public List<Object[]> obtenerResumenPorTabla(LocalDateTime desde, LocalDateTime hasta) {
        return auditRepository.resumenAuditoriaPorTabla(desde, hasta);
    }

    @Transactional
    public void limpiarRegistrosAntiguos(LocalDateTime fechaLimite) {
        auditRepository.deleteRegistrosAntiguos(fechaLimite);
    }

    // Método para registrar auditoría manualmente
    @Transactional
    public void registrarAuditoria(String eventoTipo, String tablaNombre, UUID registroId, UUID usuarioId,
            String detalle) {
        InventarioAudit audit = new InventarioAudit(eventoTipo, tablaNombre, registroId, usuarioId, detalle);
        auditRepository.save(audit);
    }
}