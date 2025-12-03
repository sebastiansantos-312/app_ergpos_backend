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

    @Column(name = "detalle", columnDefinition = "jsonb", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String detalle = "{}"; // Valor por defecto: JSON vacío

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructor por defecto
    public InventarioAudit() {
        this.detalle = "{}"; // Asegurar valor por defecto
    }

    // Constructor para fácil creación
    public InventarioAudit(String eventoTipo, String tablaNombre, UUID registroId, UUID usuarioId, String detalle) {
        this.eventoTipo = eventoTipo;
        this.tablaNombre = tablaNombre;
        this.registroId = registroId;
        this.usuarioId = usuarioId;
        // Asegurar que detalle no sea null
        this.detalle = (detalle != null && !detalle.trim().isEmpty()) ? detalle : "{}";
    }

    // Constructor alternativo sin detalle
    public InventarioAudit(String eventoTipo, String tablaNombre, UUID registroId, UUID usuarioId) {
        this(eventoTipo, tablaNombre, registroId, usuarioId, "{}");
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
        // Asegurar que nunca devuelva null
        return detalle != null ? detalle : "{}";
    }

    public void setDetalle(String detalle) {
        // Validar que no sea null
        this.detalle = (detalle != null && !detalle.trim().isEmpty()) ? detalle : "{}";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Método helper para crear detalle JSON desde objeto
    public void setDetalleFromObject(Object object) {
        try {
            if (object == null) {
                this.detalle = "{}";
                return;
            }

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
            // Asegurar que haya algo para deserializar
            if (this.detalle == null || this.detalle.trim().isEmpty()) {
                this.detalle = "{}";
            }
            return mapper.readValue(this.detalle, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar JSON: " + e.getMessage(), e);
        }
    }
}