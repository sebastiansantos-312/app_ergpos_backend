package com.ergpos.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ergpos.app.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    //MÉTODOS EXISTENTES
    Optional<Producto> findByCodigo(String codigo);

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    List<Producto> findByActivoTrue();

    List<Producto> findByActivoFalse();
    long countByActivoTrue();
    long countByActivoFalse();
    List<Producto> findTop10ByOrderByCreatedAtDesc();
    //MÉTODO PARA VERIFICAR EXISTENCIA (útil para validaciones)
    boolean existsByCodigo(String codigo);

    //BÚSQUEDA MÁS FLEXIBLE (con/sin filtro activo)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    //MÉTODO PARA PRECIO PROMEDIO (si lo necesitas)
    @Query("SELECT AVG(p.precio) FROM Producto p WHERE p.activo = true")
    Optional<Double> findPrecioPromedioActivos();
}