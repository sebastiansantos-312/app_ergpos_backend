package com.ergpos.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ergpos.app.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    // Búsqueda dinámica
    @Query("""
            SELECT u FROM Usuario u
            WHERE (:nombre IS NULL OR :nombre = '' OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
            AND (:email IS NULL OR :email = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
            AND (:rolNombre IS NULL OR :rolNombre = '' OR LOWER(u.rol.nombre) LIKE LOWER(CONCAT('%', :rolNombre, '%')))
            AND (:activo IS NULL OR u.activo = :activo)
            ORDER BY u.nombre ASC
            """)
    List<Usuario> buscarUsuarios(
            @Param("nombre") String nombre,
            @Param("email") String email,
            @Param("rolNombre") String rolNombre,
            @Param("activo") Boolean activo);

    // Búsquedas por campos únicos
    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByCodigo(String codigo);

    // Validaciones de existencia
    boolean existsByEmail(String email);

    boolean existsByCodigo(String codigo);

    // Métodos básicos
    List<Usuario> findByActivoTrue();

    List<Usuario> findByActivoFalse();

    long countByActivoTrue();

    long countByActivoFalse();
}