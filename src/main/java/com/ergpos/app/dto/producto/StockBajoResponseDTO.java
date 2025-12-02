package com.ergpos.app.dto.producto;

import java.math.BigDecimal;
import java.util.UUID;

public class StockBajoResponseDTO {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoriaNombre;
    private Integer stockActual;
    private Integer stockMinimo;
    private String unidadMedida;
    private BigDecimal precio;
    private Integer nivelCriticidad; // 1-3
    private Integer faltante;
    private Double porcentajeStock;
    private Boolean necesitaReabastecimiento;
    private BigDecimal valorFaltante;
    private String mensajeAlerta;

    // Constructor
    public StockBajoResponseDTO() {
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getNivelCriticidad() {
        return nivelCriticidad;
    }

    public void setNivelCriticidad(Integer nivelCriticidad) {
        this.nivelCriticidad = nivelCriticidad;
    }

    public Integer getFaltante() {
        return faltante;
    }

    public void setFaltante(Integer faltante) {
        this.faltante = faltante;
    }

    public Double getPorcentajeStock() {
        return porcentajeStock;
    }

    public void setPorcentajeStock(Double porcentajeStock) {
        this.porcentajeStock = porcentajeStock;
    }

    public Boolean getNecesitaReabastecimiento() {
        return necesitaReabastecimiento;
    }

    public void setNecesitaReabastecimiento(Boolean necesitaReabastecimiento) {
        this.necesitaReabastecimiento = necesitaReabastecimiento;
    }

    public BigDecimal getValorFaltante() {
        return valorFaltante;
    }

    public void setValorFaltante(BigDecimal valorFaltante) {
        this.valorFaltante = valorFaltante;
    }

    public String getMensajeAlerta() {
        if (mensajeAlerta == null) {
            generarMensajeAlerta();
        }
        return mensajeAlerta;
    }

    private void generarMensajeAlerta() {
        if (stockActual == 0) {
            mensajeAlerta = "SIN STOCK - Reabastecimiento urgente requerido";
        } else if (nivelCriticidad == 3) {
            mensajeAlerta = String.format("Stock CRÍTICO - Solo %d unidades disponibles (faltan %d)",
                    stockActual, faltante);
        } else if (nivelCriticidad == 2) {
            mensajeAlerta = String.format("Stock BAJO - %d unidades (%.0f%% del mínimo)",
                    stockActual, porcentajeStock);
        } else {
            mensajeAlerta = String.format("Atención - Stock bajo: %d unidades", stockActual);
        }
    }
}