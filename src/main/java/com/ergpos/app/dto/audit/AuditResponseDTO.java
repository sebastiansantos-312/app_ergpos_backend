package com.ergpos.app.dto.audit;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditResponseDTO {
    private Long id;
    private String eventoTipo;
    private String tablaNombre;
    private UUID registroId;
    private UUID usuarioId;
    private String detalle;
    private LocalDateTime createdAt;

    // Constructor por defecto
    public AuditResponseDTO() {
    }

    // Constructor con parámetros
    public AuditResponseDTO(Long id, String eventoTipo, String tablaNombre,
            UUID registroId, UUID usuarioId, String detalle,
            LocalDateTime createdAt) {
        this.id = id;
        this.eventoTipo = eventoTipo;
        this.tablaNombre = tablaNombre;
        this.registroId = registroId;
        this.usuarioId = usuarioId;
        this.detalle = detalle;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventoTipo() {
        return eventoTipo;
    }

    public void setEventoTipo(String eventoTipo) {
        this.eventoTipo = eventoTipo;
    }

    public String getTablaNombre() {
        return tablaNombre;
    }

    public void setTablaNombre(String tablaNombre) {
        this.tablaNombre = tablaNombre;
    }

    public UUID getRegistroId() {
        return registroId;
    }

    public void setRegistroId(UUID registroId) {
        this.registroId = registroId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // toString para debugging
    @Override
    public String toString() {
        return "AuditResponseDTO{" +
                "id=" + id +
                ", eventoTipo='" + eventoTipo + '\'' +
                ", tablaNombre='" + tablaNombre + '\'' +
                ", registroId=" + registroId +
                ", usuarioId=" + usuarioId +
                ", detalle='" + detalle + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}