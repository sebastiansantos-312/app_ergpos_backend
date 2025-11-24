package com.ergpos.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ergpos.app.model.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

        // Búsqueda dinámica (por nombre, código o ambos)
        @Query("""
                        SELECT c FROM Categoria c
                        WHERE (:buscar IS NULL OR :buscar = '' OR
                               LOWER(c.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
                               LOWER(c.codigo) LIKE LOWER(CONCAT('%', :buscar, '%')))
                        AND (:activo IS NULL OR c.activo = :activo)
                        ORDER BY c.nombre ASC
                        """)
        List<Categoria> buscar(
                        @Param("buscar") String buscar,
                        @Param("activo") Boolean activo);

        // Validaciones de existencia
        boolean existsByNombreIgnoreCase(String nombre);

        boolean existsByCodigoIgnoreCase(String codigo);

        // Búsqueda por nombre
        Optional<Categoria> findByNombreIgnoreCase(String nombre);

        // Búsqueda por código
        Optional<Categoria> findByCodigoIgnoreCase(String codigo);
}