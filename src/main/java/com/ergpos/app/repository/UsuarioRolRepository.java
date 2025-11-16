package com.ergpos.app.repository;

import com.ergpos.app.model.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UUID> {
    Optional<UsuarioRol> findByUsuarioIdAndRolId(UUID usuarioId, UUID rolId);
}
