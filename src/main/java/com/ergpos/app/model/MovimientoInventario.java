package com.ergpos.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    // Enums para mejor type safety
    public enum TipoMovimiento {
        ENTRADA, SALIDA
    }

    public enum EstadoMovimiento {
        ACTIVO, ANULADO, PENDIENTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipo;

    @Column(nullable = false)
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;

    @Column(length = 255)
    private String observacion;

    @Column(name = "documento_ref", length = 100)
    private String documentoRef;

    @Column(name = "costo_unitario", precision = 10, scale = 2)
    @Min(value = 0, message = "El costo unitario no puede ser negativo")
    private BigDecimal costoUnitario;

    @Column(nullable = false)
    @NotNull(message = "La fecha es obligatoria")
    private LocalDateTime fecha;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "El estado es obligatorio")
    private EstadoMovimiento estado = EstadoMovimiento.ACTIVO;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoMovimiento.ACTIVO;
        }
    }

    // Métodos de negocio
    public boolean esEntrada() {
        return TipoMovimiento.ENTRADA.equals(this.tipo);
    }

    public boolean esSalida() {
        return TipoMovimiento.SALIDA.equals(this.tipo);
    }

    public boolean puedeAnular() {
        return EstadoMovimiento.ACTIVO.equals(this.estado);
    }

    public BigDecimal calcularCostoTotal() {
        if (costoUnitario != null && cantidad != null) {
            return costoUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
        return BigDecimal.ZERO;
    }

    // Getters & Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    // Método para compatibilidad con String (si necesitas)
    public void setTipoFromString(String tipo) {
        try {
            this.tipo = TipoMovimiento.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + tipo);
        }
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getDocumentoRef() {
        return documentoRef;
    }

    public void setDocumentoRef(String documentoRef) {
        this.documentoRef = documentoRef;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public EstadoMovimiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoMovimiento estado) {
        this.estado = estado;
    }

    // Método para compatibilidad con String (si necesitas)
    public void setEstadoFromString(String estado) {
        try {
            this.estado = EstadoMovimiento.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de movimiento inválido: " + estado);
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Método para anular movimiento
    public void anular() {
        if (puedeAnular()) {
            this.estado = EstadoMovimiento.ANULADO;
        } else {
            throw new IllegalStateException("No se puede anular un movimiento que no está ACTIVO");
        }
    }

    // toString para debugging
    @Override
    public String toString() {
        return "MovimientoInventario{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getNombre() : "null") +
                ", tipo=" + tipo +
                ", cantidad=" + cantidad +
                ", estado=" + estado +
                ", fecha=" + fecha +
                '}';
    }
}