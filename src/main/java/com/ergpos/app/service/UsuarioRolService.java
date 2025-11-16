package com.ergpos.app.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.ergpos.app.dto.usuarios.RolCambioRequestDTO;
import com.ergpos.app.dto.usuarios.UsuarioRolResponseDTO;
import com.ergpos.app.model.Rol;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.RolRepository;
import com.ergpos.app.repository.UsuarioRepository;

@Service
public class UsuarioRolService {

        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;

        public UsuarioRolService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
                this.usuarioRepository = usuarioRepository;
                this.rolRepository = rolRepository;
        }

       // @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMINISTRADOR')")
        public UsuarioRolResponseDTO cambiarRolesUsuario(String email, RolCambioRequestDTO request) {

                Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                Set<Rol> roles = request.getRolNombres().stream()
                                .map(nombre -> rolRepository.findByNombreIgnoreCase(nombre)
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Rol no encontrado: " + nombre)))
                                .collect(Collectors.toSet());

                usuario.setRoles(roles);
                usuarioRepository.save(usuario);

                List<String> rolesAsignados = roles.stream()
                                .map(Rol::getNombre)
                                .collect(Collectors.toList());

                return new UsuarioRolResponseDTO(usuario.getNombre(), usuario.getEmail(), rolesAsignados);
        }
}
