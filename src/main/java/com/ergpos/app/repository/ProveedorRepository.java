package com.ergpos.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ergpos.app.model.Proveedor;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, UUID> {

    // Búsqueda dinámica (por nombre, RUC o email)
    @Query("""
            SELECT p FROM Proveedor p
            WHERE (:buscar IS NULL OR :buscar = '' OR
                   LOWER(p.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
                   LOWER(p.ruc) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
                   LOWER(p.email) LIKE LOWER(CONCAT('%', :buscar, '%')))
            AND (:activo IS NULL OR p.activo = :activo)
            ORDER BY p.nombre ASC
            """)
    List<Proveedor> buscar(
            @Param("buscar") String buscar,
            @Param("activo") Boolean activo);

    // Búsqueda por identificadores
    Optional<Proveedor> findByRuc(String ruc);

    Optional<Proveedor> findByEmailIgnoreCase(String email);

    Optional<Proveedor> findByNombreIgnoreCase(String nombre);

    // Validaciones de existencia
    boolean existsByRuc(String ruc);

    boolean existsByNombreIgnoreCase(String nombre);
}