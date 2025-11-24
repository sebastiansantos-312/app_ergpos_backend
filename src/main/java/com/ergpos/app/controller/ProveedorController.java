package com.ergpos.app.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.ergpos.app.dto.proveedores.ProveedorRequestDTO;
import com.ergpos.app.dto.proveedores.ProveedorResponseDTO;
import com.ergpos.app.service.ProveedorService;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    // Crear proveedor
    @PostMapping
    public ResponseEntity<ProveedorResponseDTO> crear(@Valid @RequestBody ProveedorRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(request));
    }

    // Listar con filtros dinámicos
    @GetMapping
    public ResponseEntity<List<ProveedorResponseDTO>> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(proveedorService.listar(buscar, activo));
    }

    // Obtener por RUC, Email o Nombre
    @GetMapping("/{identificador}")
    public ResponseEntity<ProveedorResponseDTO> obtener(@PathVariable String identificador) {
        return ResponseEntity.ok(proveedorService.obtener(identificador));
    }

    // Actualizar por RUC, Email o Nombre
    @PutMapping("/{identificador}")
    public ResponseEntity<ProveedorResponseDTO> actualizar(
            @PathVariable String identificador,
            @Valid @RequestBody ProveedorRequestDTO request) {
        return ResponseEntity.ok(proveedorService.actualizar(identificador, request));
    }

    // Activar por RUC, Email o Nombre
    @PatchMapping("/{identificador}/activar")
    public ResponseEntity<ProveedorResponseDTO> activar(@PathVariable String identificador) {
        return ResponseEntity.ok(proveedorService.activar(identificador));
    }

    // Desactivar por RUC, Email o Nombre
    @PatchMapping("/{identificador}/desactivar")
    public ResponseEntity<ProveedorResponseDTO> desactivar(@PathVariable String identificador) {
        return ResponseEntity.ok(proveedorService.desactivar(identificador));
    }
}