package com.ergpos.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(unique = true, length = 50)
    private String codigo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // ✅ GENERAR CÓDIGO AUTOMÁTICO SI NO SE PROPORCIONA
        if (this.codigo == null || this.codigo.trim().isEmpty()) {
            generarCodigoAutomatico();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ✅ MÉTODO PARA GENERAR CÓDIGO AUTOMÁTICO
    private void generarCodigoAutomatico() {
        if (this.nombre != null && !this.nombre.trim().isEmpty()) {
            String codigoBase = this.nombre.toUpperCase()
                    .trim()
                    .replace(" ", "_")
                    .replace("Á", "A")
                    .replace("É", "E")
                    .replace("Í", "I")
                    .replace("Ó", "O")
                    .replace("Ú", "U")
                    .replace("Ñ", "N")
                    .replaceAll("[^A-Z0-9_]", ""); // Solo letras, números y _

            // Si el código base está vacío, usar UUID
            if (codigoBase.isEmpty()) {
                this.codigo = "CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            } else {
                this.codigo = "CAT-" + codigoBase;
            }
        } else {
            // Fallback si no hay nombre
            this.codigo = "CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // Asegurar que no exceda 50 caracteres
        if (this.codigo.length() > 50) {
            this.codigo = this.codigo.substring(0, 50);
        }
    }

    // Getters & Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        // Si el código no se ha establecido manualmente, generarlo
        if (this.codigo == null || this.codigo.startsWith("CAT-")) {
            generarCodigoAutomatico();
        }
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    // ✅ GETTER Y SETTER PARA CÓDIGO
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        // Solo establecer código si no es nulo o vacío
        if (codigo != null && !codigo.trim().isEmpty()) {
            this.codigo = codigo.trim().toUpperCase();
        }
        // Si se pasa null o vacío, se generará automáticamente en prePersist
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}