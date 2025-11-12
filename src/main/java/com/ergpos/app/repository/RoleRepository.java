package com.ergpos.app.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ergpos.app.model.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNombreIgnoreCase(String nombre);
}
