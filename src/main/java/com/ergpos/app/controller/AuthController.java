package com.ergpos.app.controller;

import com.ergpos.app.dto.usuarios.LoginRequest;
import com.ergpos.app.security.JwtUtils;
import com.ergpos.app.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            String username = loginRequest.getUsername();

            // Determinar si es email o código y obtener el email real
            String email = determinarEmail(username);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Obtener información completa del usuario
            var usuarioResponse = usuarioService.obtenerPorEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("user", usuarioResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Credenciales inválidas");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    private String determinarEmail(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("El campo username no puede estar vacío");
        }

        if (username.contains("@")) {
            System.out.println("Login con email: " + username);
            return username;
        }

        var usuario = usuarioService.obtenerPorCodigo(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Código de usuario no existe: " + username);
        }

        System.out.println("Login con código: " + username + ", email: " + usuario.getEmail());
        return usuario.getEmail();
    }

}