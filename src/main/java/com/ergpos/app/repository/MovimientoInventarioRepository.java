package com.ergpos.app.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ergpos.app.model.MovimientoInventario;
import com.ergpos.app.model.MovimientoInventario.TipoMovimiento;
import com.ergpos.app.model.MovimientoInventario.EstadoMovimiento;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, UUID> {

        // Búsqueda dinámica con todos los filtros
        @Query("""
                        SELECT m FROM MovimientoInventario m
                        WHERE (:productoId IS NULL OR m.producto.id = :productoId)
                        AND (:tipo IS NULL OR m.tipo = :tipo)
                        AND (:estado IS NULL OR m.estado = :estado)
                        AND (:usuarioId IS NULL OR m.usuario.id = :usuarioId)
                        AND (:proveedorId IS NULL OR m.proveedor.id = :proveedorId)
                        ORDER BY m.fecha DESC
                        """)
        List<MovimientoInventario> buscarMovimientos(
                        @Param("productoId") UUID productoId,
                        @Param("tipo") TipoMovimiento tipo,
                        @Param("estado") EstadoMovimiento estado,
                        @Param("usuarioId") UUID usuarioId,
                        @Param("proveedorId") UUID proveedorId);
}