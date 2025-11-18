package com.ergpos.app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ergpos.app.dto.roles.RolRequestDTO;
import com.ergpos.app.dto.roles.RolResponseDTO;
import com.ergpos.app.model.Rol;
import com.ergpos.app.repository.RolRepository;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    private RolResponseDTO toDTO(Rol rol) {
        RolResponseDTO dto = new RolResponseDTO();
        dto.setNombre(rol.getNombre());
        dto.setActivo(rol.getActivo());
        return dto;
    }

    public List<RolResponseDTO> listarActivos() {
        return rolRepository.findByActivoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RolResponseDTO> listarInactivos() {
        return rolRepository.findByActivoFalse()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RolResponseDTO crearRol(RolRequestDTO request) {

        if (rolRepository.findByNombreIgnoreCase(request.getNombre()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol ya existe");
        }

        Rol rol = new Rol();
        rol.setNombre(request.getNombre().toUpperCase());
        rol.setActivo(true);

        return toDTO(rolRepository.save(rol));
    }

    public RolResponseDTO activarRol(String nombre) {
        Rol rol = rolRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        rol.setActivo(true);
        return toDTO(rolRepository.save(rol));
    }

    public RolResponseDTO desactivarRol(String nombre) {
        Rol rol = rolRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        rol.setActivo(false);
        return toDTO(rolRepository.save(rol));
    }

    public RolResponseDTO obtenerPorNombre(String nombre) {
        Rol rol = rolRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        return toDTO(rol);
    }
}
