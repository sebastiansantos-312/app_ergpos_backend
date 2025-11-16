package com.ergpos.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ergpos.app.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    Optional<Producto> findByCodigo(String codigo);
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    List<Producto> findByActivoTrue();
    List<Producto> findByActivoFalse();
}
