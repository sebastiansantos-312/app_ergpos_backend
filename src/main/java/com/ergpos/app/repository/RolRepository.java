package com.ergpos.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ergpos.app.model.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {

  // Búsqueda dinámica por nombre y estado
  @Query("""
      SELECT r FROM Rol r
      WHERE (:buscar IS NULL OR :buscar = '' OR
             LOWER(r.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')))
      AND (:activo IS NULL OR r.activo = :activo)
      ORDER BY r.nombre ASC
      """)
  List<Rol> buscar(
      @Param("buscar") String buscar,
      @Param("activo") Boolean activo);

  // Búsqueda por nombre
  Optional<Rol> findByNombreIgnoreCase(String nombre);

  // Validación de existencia
  boolean existsByNombreIgnoreCase(String nombre);
}