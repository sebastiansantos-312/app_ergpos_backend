package com.ergpos.app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.ergpos.app.model.Role;
import com.ergpos.app.repository.RoleRepository;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RoleRepository rolRepository;

    public RolController(RoleRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    // Listar todos los roles
    @GetMapping
    public List<Role> listarRoles() {
        return rolRepository.findAll();
    }

    // Crear un rol
    @PostMapping
    public Role crearRol(@RequestBody Role rol) {
        return rolRepository.save(rol);
    }

    // Obtener un rol por ID
    @GetMapping("/{id}")
    public Role obtenerRol(@PathVariable UUID id) {
        return rolRepository.findById(id).orElse(null);
    }

    // Buscar rol por nombre (ignore case)
    @GetMapping("/buscar/nombre/{nombre}")
    public Role buscarPorNombre(@PathVariable String nombre) {
        return rolRepository.findByNombreIgnoreCase(nombre).orElse(null);
    }

    // Actualizar rol
    @PutMapping("/{id}")
    public Role actualizarRol(@PathVariable UUID id, @RequestBody Role rolDetalles) {
        return rolRepository.findById(id).map(rol -> {
            rol.setNombre(rolDetalles.getNombre());
            rol.setDescripcion(rolDetalles.getDescripcion());
            return rolRepository.save(rol);
        }).orElse(null);
    }

    // Eliminar rol
    @DeleteMapping("/{id}")
    public void eliminarRol(@PathVariable UUID id) {
        rolRepository.deleteById(id);
    }
}
