package com.ergpos.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ergpos.app.model.Rol;

public interface RolRepository extends JpaRepository<Rol, UUID> {
  Optional<Rol> findByNombreIgnoreCase(String nombre);
  List<Rol> findByActivoTrue();
  List<Rol> findByActivoFalse();
}
