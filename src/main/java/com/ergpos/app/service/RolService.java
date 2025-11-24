package com.ergpos.app.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ergpos.app.dto.roles.RolRequestDTO;
import com.ergpos.app.dto.roles.RolResponseDTO;
import com.ergpos.app.model.Rol;
import com.ergpos.app.repository.RolRepository;

@Service
@Transactional(readOnly = true)
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    private RolResponseDTO toDTO(Rol rol) {
        RolResponseDTO dto = new RolResponseDTO();
        dto.setId(rol.getId());
        dto.setNombre(rol.getNombre());
        dto.setActivo(rol.getActivo());
        dto.setCreatedAt(rol.getCreatedAt());
        dto.setUpdatedAt(rol.getUpdatedAt());
        return dto;
    }

    // Búsqueda dinámica con filtros
    public List<RolResponseDTO> listar(String buscar, Boolean activo) {
        return rolRepository.buscar(buscar, activo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener por ID
    public RolResponseDTO obtenerPorId(UUID id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol no encontrado con ID: " + id));
        return toDTO(rol);
    }

    // Obtener por nombre
    public RolResponseDTO obtenerPorNombre(String nombre) {
        String nombreNormalizado = nombre.trim().toUpperCase();
        Rol rol = rolRepository.findByNombreIgnoreCase(nombreNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol no encontrado con nombre: " + nombreNormalizado));
        return toDTO(rol);
    }

    // Crear rol
    @Transactional
    public RolResponseDTO crear(RolRequestDTO request) {
        String nombreRol = request.getNombre().trim().toUpperCase();

        if (rolRepository.existsByNombreIgnoreCase(nombreRol)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un rol con el nombre: " + nombreRol);
        }

        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        rol.setActivo(true);

        Rol saved = rolRepository.save(rol);
        return toDTO(saved);
    }

    // Actualizar rol
    @Transactional
    public RolResponseDTO actualizar(String nombre, RolRequestDTO request) {
        String nombreActual = nombre.trim().toUpperCase();
        Rol rol = rolRepository.findByNombreIgnoreCase(nombreActual)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol no encontrado con nombre: " + nombreActual));

        String nuevoNombre = request.getNombre().trim().toUpperCase();

        if (!rol.getNombre().equalsIgnoreCase(nuevoNombre) &&
                rolRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un rol con el nombre: " + nuevoNombre);
        }

        rol.setNombre(nuevoNombre);
        Rol updated = rolRepository.save(rol);
        return toDTO(updated);
    }

    // Activar rol
    @Transactional
    public RolResponseDTO activar(String nombre) {
        String nombreNormalizado = nombre.trim().toUpperCase();
        Rol rol = rolRepository.findByNombreIgnoreCase(nombreNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol no encontrado con nombre: " + nombreNormalizado));

        if (rol.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El rol ya está activo");
        }

        rol.setActivo(true);
        Rol updated = rolRepository.save(rol);
        return toDTO(updated);
    }

    // Desactivar rol
    @Transactional
    public RolResponseDTO desactivar(String nombre) {
        String nombreNormalizado = nombre.trim().toUpperCase();
        Rol rol = rolRepository.findByNombreIgnoreCase(nombreNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol no encontrado con nombre: " + nombreNormalizado));

        if (!rol.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El rol ya está inactivo");
        }

        rol.setActivo(false);
        Rol updated = rolRepository.save(rol);
        return toDTO(updated);
    }
}