package com.ergpos.app.controller;

import java.util.List;
//import org.springframework.security.access.prepost.PreAuthorize;
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

    // Solo ADMINISTRADOR o SUPER_ADMIN pueden ver los roles
    //@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_ADMIN')")
    @GetMapping
    public List<RolResponseDTO> listarActivos() {
        return rolService.listarActivos();
    }

    // Solo ADMINISTRADOR o SUPER_ADMIN pueden ver roles inactivos
    //@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_ADMIN')")
    @GetMapping("/inactivos")
    public List<RolResponseDTO> listarInactivos() {
        return rolService.listarInactivos();
    }

    // Solo SUPER_ADMIN puede crear roles
   // @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public RolResponseDTO crearRol(@RequestBody RolRequestDTO request) {
        return rolService.crearRol(request);
    }

    // Solo SUPER_ADMIN puede activar roles
   // @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{nombre}/activar")
    public RolResponseDTO activarRol(@PathVariable String nombre) {
        return rolService.activarRol(nombre);
    }

    // Solo SUPER_ADMIN puede desactivar roles
  //  @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{nombre}/desactivar")
    public RolResponseDTO desactivarRol(@PathVariable String nombre) {
        return rolService.desactivarRol(nombre);
    }
}
