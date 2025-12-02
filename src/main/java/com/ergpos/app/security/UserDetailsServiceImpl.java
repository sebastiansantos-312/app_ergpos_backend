package com.ergpos.app.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.UsuarioRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // El username puede ser email o código - buscamos en ambos campos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(username);

        // Si no encuentra por email, buscar por código
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByCodigo(username);
        }

        Usuario usuario = usuarioOpt
                .orElseThrow(() -> {
                    logger.warn("Intento de login fallido - Usuario no encontrado: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado");
                });

        //Manejo consistente de usuarios inactivos
        if (!usuario.getActivo()) {
            logger.warn("Intento de login fallido - Usuario inactivo: {}", username);
            throw new UsernameNotFoundException("Credenciales inválidas");
            // NOTA: Se mantiene mensaje genérico por seguridad
        }

        // Verificar que el usuario tenga un rol asignado
        if (usuario.getRol() == null) {
            logger.error("Usuario sin rol asignado: {}", username);
            throw new UsernameNotFoundException("Configuración de usuario incompleta");
        }

        // Crear authorities basado en el rol del usuario
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre().toUpperCase()));

        logger.info("Login exitoso para usuario: {}", username);

        return new User(
                usuario.getEmail(), // Usamos email como username principal
                usuario.getPasswordHash() != null ? usuario.getPasswordHash() : "",
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities);
    }

    // Método adicional para cargar por email (opcional)
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        logger.debug("Cargando usuario por email: {}", email);
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email));

        return createUserDetails(usuario);
    }

    // Método adicional para cargar por código (opcional)
    @Transactional(readOnly = true)
    public UserDetails loadUserByCodigo(String codigo) throws UsernameNotFoundException {
        logger.debug("Cargando usuario por código: {}", codigo);
        Usuario usuario = usuarioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con código: " + codigo));

        return createUserDetails(usuario);
    }

    // Método helper para crear UserDetails
    private UserDetails createUserDetails(Usuario usuario) {
        // Mensaje genérico para usuarios inactivos (seguridad)
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        if (usuario.getRol() == null) {
            throw new UsernameNotFoundException("El usuario no tiene un rol asignado");
        }

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre().toUpperCase()));

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash() != null ? usuario.getPasswordHash() : "",
                true, true, true, true,
                authorities);
    }
}