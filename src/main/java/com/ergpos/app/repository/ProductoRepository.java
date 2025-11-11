package com.ergpos.app.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ergpos.app.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    Optional<Producto> findByCodigo(String codigo);
}