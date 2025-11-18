package com.ergpos.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ergpos.app.model.Usuario;

// En UsuarioRepository - ELIMINAR el método duplicado
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
        Optional<Usuario> findByEmailIgnoreCase(String email); 
        List<Usuario> findByActivoTrue();
        List<Usuario> findByNombreContainingIgnoreCase(String nombre);
        Optional<Usuario> findByNombreIgnoreCase(String nombre);
        Optional<Usuario> findByCodigo(String codigo);
        List<Usuario> findByActivoFalse();

        @Query("""
                        SELECT u FROM Usuario u
                        WHERE (:nombre IS NULL OR :nombre = '' OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
                        AND (:email IS NULL OR :email = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
                        AND (:departamento IS NULL OR :departamento = '' OR LOWER(u.departamento) LIKE LOWER(CONCAT('%', :departamento, '%')))
                        AND (:puesto IS NULL OR :puesto = '' OR LOWER(u.puesto) LIKE LOWER(CONCAT('%', :puesto, '%')))
                        """)
        List<Usuario> buscarUsuarios(
                        @Param("nombre") String nombre,
                        @Param("email") String email,
                        @Param("departamento") String departamento,
                        @Param("puesto") String puesto);
}