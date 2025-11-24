package com.ergpos.app.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ergpos.app.dto.proveedores.ProveedorRequestDTO;
import com.ergpos.app.dto.proveedores.ProveedorResponseDTO;
import com.ergpos.app.model.Proveedor;
import com.ergpos.app.repository.ProveedorRepository;

@Service
@Transactional(readOnly = true)
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    private ProveedorResponseDTO toDTO(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.setId(proveedor.getId());
        dto.setNombre(proveedor.getNombre());
        dto.setRuc(proveedor.getRuc());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setDireccion(proveedor.getDireccion());
        dto.setActivo(proveedor.getActivo());
        dto.setCreatedAt(proveedor.getCreatedAt());
        dto.setUpdatedAt(proveedor.getUpdatedAt());
        return dto;
    }

    private Proveedor buscarProveedor(String identificador) {
        // Intentar buscar por RUC primero, luego por Email, y finalmente por Nombre
        return proveedorRepository.findByRuc(identificador)
                .or(() -> proveedorRepository.findByEmailIgnoreCase(identificador))
                .or(() -> proveedorRepository.findByNombreIgnoreCase(identificador))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Proveedor no encontrado"));
    }

    // Listar con búsqueda dinámica
    public List<ProveedorResponseDTO> listar(String buscar, Boolean activo) {
        return proveedorRepository.buscar(buscar, activo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener por RUC, Email o Nombre
    public ProveedorResponseDTO obtener(String identificador) {
        return toDTO(buscarProveedor(identificador));
    }

    // Crear proveedor
    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO request) {
        String nombre = request.getNombre().trim();

        if (proveedorRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un proveedor con ese nombre");
        }

        if (request.getRuc() != null && !request.getRuc().trim().isEmpty()) {
            String ruc = request.getRuc().trim();
            if (proveedorRepository.existsByRuc(ruc)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe un proveedor con ese RUC");
            }
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(nombre);
        proveedor.setRuc(request.getRuc() != null ? request.getRuc().trim() : null);
        proveedor.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        proveedor.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null);
        proveedor.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);
        proveedor.setActivo(true);

        return toDTO(proveedorRepository.save(proveedor));
    }

    // Actualizar por RUC, Email o Nombre
    @Transactional
    public ProveedorResponseDTO actualizar(String identificador, ProveedorRequestDTO request) {
        Proveedor proveedor = buscarProveedor(identificador);

        String nuevoNombre = request.getNombre().trim();

        if (!proveedor.getNombre().equalsIgnoreCase(nuevoNombre) &&
                proveedorRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un proveedor con ese nombre");
        }

        if (request.getRuc() != null && !request.getRuc().trim().isEmpty()) {
            String nuevoRuc = request.getRuc().trim();
            if ((proveedor.getRuc() == null || !nuevoRuc.equals(proveedor.getRuc())) &&
                    proveedorRepository.existsByRuc(nuevoRuc)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe un proveedor con ese RUC");
            }
        }

        proveedor.setNombre(nuevoNombre);
        proveedor.setRuc(request.getRuc() != null ? request.getRuc().trim() : null);
        proveedor.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        proveedor.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null);
        proveedor.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);

        return toDTO(proveedorRepository.save(proveedor));
    }

    // Activar por RUC, Email o Nombre
    @Transactional
    public ProveedorResponseDTO activar(String identificador) {
        Proveedor proveedor = buscarProveedor(identificador);

        if (proveedor.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El proveedor ya está activo");
        }

        proveedor.setActivo(true);
        return toDTO(proveedorRepository.save(proveedor));
    }

    // Desactivar por RUC, Email o Nombre
    @Transactional
    public ProveedorResponseDTO desactivar(String identificador) {
        Proveedor proveedor = buscarProveedor(identificador);

        if (!proveedor.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El proveedor ya está inactivo");
        }

        proveedor.setActivo(false);
        return toDTO(proveedorRepository.save(proveedor));
    }
}