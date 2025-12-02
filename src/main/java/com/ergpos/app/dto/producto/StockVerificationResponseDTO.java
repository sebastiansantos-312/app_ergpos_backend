package com.ergpos.app.dto.producto;

import java.math.BigDecimal;

public class StockVerificationResponseDTO {
    private String codigoProducto;
    private String nombreProducto;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer cantidadSolicitada;
    private BigDecimal precio;
    private Boolean disponible;
    private Integer faltante;
    private Boolean bajoStockMinimo;
    private String mensaje;

    public StockVerificationResponseDTO(String codigoProducto, String nombreProducto,
            Integer stockActual, Integer cantidadSolicitada) {
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.stockActual = stockActual;
        this.cantidadSolicitada = cantidadSolicitada;
        this.disponible = stockActual >= cantidadSolicitada;
        this.faltante = disponible ? 0 : cantidadSolicitada - stockActual;
        this.bajoStockMinimo = stockActual < 10;
        this.mensaje = disponible ? "Stock suficiente" : "Stock insuficiente";
    }

    // Nuevo constructor (6 parámetros)
    public StockVerificationResponseDTO(String codigoProducto, String nombreProducto,
            Integer stockActual, Integer stockMinimo,
            Integer cantidadSolicitada, BigDecimal precio) {
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.cantidadSolicitada = cantidadSolicitada;
        this.precio = precio;
        this.disponible = stockActual >= cantidadSolicitada;
        this.faltante = disponible ? 0 : cantidadSolicitada - stockActual;
        this.bajoStockMinimo = stockActual < stockMinimo;
        this.mensaje = disponible ? "Stock suficiente" : "Stock insuficiente";
    }

    // Getters y Setters
    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
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

    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(Integer cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public Integer getFaltante() {
        return faltante;
    }

    public void setFaltante(Integer faltante) {
        this.faltante = faltante;
    }

    public Boolean getBajoStockMinimo() {
        return bajoStockMinimo;
    }

    public void setBajoStockMinimo(Boolean bajoStockMinimo) {
        this.bajoStockMinimo = bajoStockMinimo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}