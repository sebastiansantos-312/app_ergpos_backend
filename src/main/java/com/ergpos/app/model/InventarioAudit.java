package com.ergpos.app.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventario_audit")
public class InventarioAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_tipo", nullable = false, length = 20)
    private String eventoTipo;

    @Column(name = "tabla_nombre", nullable = false, length = 100)
    private String tablaNombre;

    @Column(name = "registro_id")
    private UUID registroId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(columnDefinition = "jsonb")
    private String detalle;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructor por defecto
    public InventarioAudit() {
    }

    // Constructor para fácil creación
    public InventarioAudit(String eventoTipo, String tablaNombre, UUID registroId, UUID usuarioId, String detalle) {
        this.eventoTipo = eventoTipo;
        this.tablaNombre = tablaNombre;
        this.registroId = registroId;
        this.usuarioId = usuarioId;
        this.detalle = detalle;
    }

    // Getters & Setters
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

    // Método helper para crear detalle JSON desde objeto
    public void setDetalleFromObject(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.detalle = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // Fallback: convertir a string simple
            this.detalle = "{\"error\": \"No se pudo serializar el objeto\", \"object\": \"" +
                    object.toString() + "\"}";
        }
    }

    // Método helper para obtener objeto desde detalle JSON
    public <T> T getDetalleAsObject(Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.detalle, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar JSON: " + e.getMessage(), e);
        }
    }
}