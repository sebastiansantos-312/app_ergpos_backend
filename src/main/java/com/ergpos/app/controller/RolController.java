package com.ergpos.app.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.ergpos.app.dto.roles.RolRequestDTO;
import com.ergpos.app.dto.roles.RolResponseDTO;
import com.ergpos.app.service.RolService;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    // Crear rol
    @PostMapping
    public ResponseEntity<RolResponseDTO> crear(@Valid @RequestBody RolRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(request));
    }

    // Listar con filtros dinámicos (búsqueda por nombre y estado)
    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(rolService.listar(buscar, activo));
    }

    // Obtener por nombre
    @GetMapping("/{nombre}")
    public ResponseEntity<RolResponseDTO> obtener(@PathVariable String nombre) {
        return ResponseEntity.ok(rolService.obtenerPorNombre(nombre));
    }

    // Actualizar por nombre
    @PutMapping("/{nombre}")
    public ResponseEntity<RolResponseDTO> actualizar(
            @PathVariable String nombre,
            @Valid @RequestBody RolRequestDTO request) {
        return ResponseEntity.ok(rolService.actualizar(nombre, request));
    }

    // Activar por nombre
    @PatchMapping("/{nombre}/activar")
    public ResponseEntity<RolResponseDTO> activar(@PathVariable String nombre) {
        return ResponseEntity.ok(rolService.activar(nombre));
    }

    // Desactivar por nombre
    @PatchMapping("/{nombre}/desactivar")
    public ResponseEntity<RolResponseDTO> desactivar(@PathVariable String nombre) {
        return ResponseEntity.ok(rolService.desactivar(nombre));
    }
}