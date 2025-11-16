package com.ergpos.app.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ergpos.app.model.MovimientoInventario;
import com.ergpos.app.model.Producto;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, UUID> {
    List<MovimientoInventario> findByProducto(Producto producto);
}
