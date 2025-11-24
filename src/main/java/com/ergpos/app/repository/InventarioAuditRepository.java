package com.ergpos.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ergpos.app.model.InventarioAudit;

@Repository
public interface InventarioAuditRepository extends JpaRepository<InventarioAudit, Long> {

    List<InventarioAudit> findByEventoTipo(String eventoTipo);

    List<InventarioAudit> findByTablaNombre(String tablaNombre);

    List<InventarioAudit> findByRegistroId(UUID registroId);

    List<InventarioAudit> findByUsuarioId(UUID usuarioId);

    List<InventarioAudit> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("""
            SELECT a FROM InventarioAudit a
            WHERE a.createdAt >= :desde
            AND a.createdAt <= :hasta
            ORDER BY a.createdAt DESC
            """)
    List<InventarioAudit> findAuditoriaPorFecha(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("""
            SELECT a FROM InventarioAudit a
            WHERE a.tablaNombre = :tablaNombre
            AND a.registroId = :registroId
            ORDER BY a.createdAt DESC
            """)
    List<InventarioAudit> findAuditoriaPorRegistro(
            @Param("tablaNombre") String tablaNombre,
            @Param("registroId") UUID registroId);

    @Query("""
            SELECT a FROM InventarioAudit a
            WHERE a.usuarioId = :usuarioId
            AND a.createdAt >= :desde
            AND a.createdAt <= :hasta
            ORDER BY a.createdAt DESC
            """)
    List<InventarioAudit> findAuditoriaPorUsuarioYFecha(
            @Param("usuarioId") UUID usuarioId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    long countByEventoTipo(String eventoTipo);

    long countByTablaNombre(String tablaNombre);

    long countByUsuarioId(UUID usuarioId);

    @Query("""
            SELECT a.tablaNombre, COUNT(a) as total
            FROM InventarioAudit a
            WHERE a.createdAt >= :desde
            AND a.createdAt <= :hasta
            GROUP BY a.tablaNombre
            ORDER BY total DESC
            """)
    List<Object[]> resumenAuditoriaPorTabla(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    List<InventarioAudit> findTop100ByOrderByCreatedAtDesc();

    @Query("""
            DELETE FROM InventarioAudit a WHERE a.createdAt < :fecha
            """)
    void deleteRegistrosAntiguos(@Param("fecha") LocalDateTime fecha);
}