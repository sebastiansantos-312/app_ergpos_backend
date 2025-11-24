package com.ergpos.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ergpos.app.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

        // Búsqueda dinámica (por nombre, código o ambos)
        @Query("""
                        SELECT p FROM Producto p
                        WHERE (:buscar IS NULL OR :buscar = '' OR
                               LOWER(p.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
                               LOWER(p.codigo) LIKE LOWER(CONCAT('%', :buscar, '%')))
                        AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
                        AND (:activo IS NULL OR p.activo = :activo)
                        ORDER BY p.nombre ASC
                        """)
        List<Producto> buscar(
                        @Param("buscar") String buscar,
                        @Param("categoriaId") UUID categoriaId,
                        @Param("activo") Boolean activo);

        // Búsqueda por código
        Optional<Producto> findByCodigo(String codigo);

        // Validación de existencia
        boolean existsByCodigo(String codigo);
}