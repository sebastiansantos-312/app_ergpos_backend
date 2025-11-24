package com.ergpos.app.dto.audit;

import java.util.UUID;

public class AuditRequestDTO {
    private String eventoTipo;
    private String tablaNombre;
    private UUID registroId;
    private UUID usuarioId;
    private String detalle;

    // Constructor por defecto
    public AuditRequestDTO() {
    }

    // Constructor con parámetros
    public AuditRequestDTO(String eventoTipo, String tablaNombre,
            UUID registroId, UUID usuarioId, String detalle) {
        this.eventoTipo = eventoTipo;
        this.tablaNombre = tablaNombre;
        this.registroId = registroId;
        this.usuarioId = usuarioId;
        this.detalle = detalle;
    }

    // Getters y Setters
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

    @Override
    public String toString() {
        return "AuditRequestDTO{" +
                "eventoTipo='" + eventoTipo + '\'' +
                ", tablaNombre='" + tablaNombre + '\'' +
                ", registroId=" + registroId +
                ", usuarioId=" + usuarioId +
                ", detalle='" + detalle + '\'' +
                '}';
    }
}