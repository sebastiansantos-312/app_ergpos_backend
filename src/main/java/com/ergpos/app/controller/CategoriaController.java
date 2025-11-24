package com.ergpos.app.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.ergpos.app.dto.categorias.CategoriaRequestDTO;
import com.ergpos.app.dto.categorias.CategoriaResponseDTO;
import com.ergpos.app.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // Crear categoría
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(request));
    }

    // Listar todas con filtros opcionales
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(categoriaService.listar(buscar, activo));
    }

    // Obtener por ID o Código
    @GetMapping("/{identificador}")
    public ResponseEntity<CategoriaResponseDTO> obtener(@PathVariable String identificador) {
        return ResponseEntity.ok(categoriaService.obtener(identificador));
    }

    // Actualizar por ID o Código
    @PutMapping("/{identificador}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable String identificador,
            @Valid @RequestBody CategoriaRequestDTO request) {
        return ResponseEntity.ok(categoriaService.actualizar(identificador, request));
    }

    // Activar por ID o Código
    @PatchMapping("/{identificador}/activar")
    public ResponseEntity<CategoriaResponseDTO> activar(@PathVariable String identificador) {
        return ResponseEntity.ok(categoriaService.activar(identificador));
    }

    // Desactivar por ID o Código
    @PatchMapping("/{identificador}/desactivar")
    public ResponseEntity<CategoriaResponseDTO> desactivar(@PathVariable String identificador) {
        return ResponseEntity.ok(categoriaService.desactivar(identificador));
    }
}