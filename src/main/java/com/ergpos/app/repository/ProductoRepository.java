package com.ergpos.app.repository;

import com.ergpos.app.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID>, JpaSpecificationExecutor<Producto> {

        Optional<Producto> findByCodigo(String codigo);

        boolean existsByCodigo(String codigo);

        @Query("SELECT p FROM Producto p WHERE p.id = :id")
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        Optional<Producto> findByIdWithLock(@Param("id") UUID id);

        @Query("SELECT p FROM Producto p WHERE p.codigo = :codigo")
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        Optional<Producto> findByCodigoWithLock(@Param("codigo") String codigo);
}