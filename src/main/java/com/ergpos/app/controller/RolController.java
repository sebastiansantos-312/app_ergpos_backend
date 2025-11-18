package com.ergpos.app.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
    public List<RolResponseDTO> listarActivos() {
        return rolService.listarActivos();
    }

    @GetMapping("/inactivos")
    public List<RolResponseDTO> listarInactivos() {
        return rolService.listarInactivos();
    }

    @PostMapping
    public RolResponseDTO crearRol(@RequestBody RolRequestDTO request) {
        return rolService.crearRol(request);
    }

    @PutMapping("/{nombre}/activar")
    public RolResponseDTO activarRol(@PathVariable String nombre) {
        return rolService.activarRol(nombre);
    }

    @PutMapping("/{nombre}/desactivar")
    public RolResponseDTO desactivarRol(@PathVariable String nombre) {
        return rolService.desactivarRol(nombre);
    }
}
