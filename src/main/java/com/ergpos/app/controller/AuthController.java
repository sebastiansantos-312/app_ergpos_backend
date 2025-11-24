package com.ergpos.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.ergpos.app.dto.auth.LoginRequestDTO;
import com.ergpos.app.dto.auth.LoginResponseDTO;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.UsuarioRepository;
import com.ergpos.app.security.JwtUtils;

import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final JwtUtils jwtUtils;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtils jwtUtils, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtils = jwtUtils;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            // Buscar usuario por email o código
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(loginRequest.getUsername());

            // Si no encuentra por email, intentar por código
            if (usuarioOpt.isEmpty()) {
                usuarioOpt = usuarioRepository.findByCodigo(loginRequest.getUsername());
            }

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar si el usuario está activo
            if (!usuario.getActivo()) {
                return ResponseEntity.badRequest().body("Error: Usuario inactivo");
            }

            // VERIFICAR CONTRASEÑA
            if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPasswordHash())) {
                return ResponseEntity.badRequest().body("Error: Contraseña incorrecta");
            }

            // Generar token JWT
            String jwt = jwtUtils.generateTokenFromUsername(usuario.getEmail());

            // Crear respuesta
            LoginResponseDTO response = new LoginResponseDTO(
                    jwt,
                    usuario.getId(),
                    usuario.getCodigo(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    usuario.getRol() != null ? usuario.getRol().getNombre() : null,
                    usuario.getActivo());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok("Logout exitoso");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        return ResponseEntity.ok("Endpoint de validación - funciona correctamente");
    }

    // Endpoint para verificar si el token es válido
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtils.validateJwtToken(token)) {
                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    return ResponseEntity.ok("Token válido para usuario: " + username);
                }
            }
            return ResponseEntity.badRequest().body("Token inválido");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error verificando token");
        }
    }

    // 🔥 NUEVO ENDPOINT - Obtener módulos por rol
    @GetMapping("/modules")
    public ResponseEntity<?> getUserModules(@RequestHeader("Authorization") String authHeader) {
        try {
            // Validar token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Token no proporcionado");
            }

            String token = authHeader.substring(7);
            
            if (!jwtUtils.validateJwtToken(token)) {
                return ResponseEntity.badRequest().body("Token inválido");
            }

            // Obtener usuario desde token
            String username = jwtUtils.getUserNameFromJwtToken(token);
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar que el usuario esté activo
            if (!usuario.getActivo()) {
                return ResponseEntity.badRequest().body("Usuario inactivo");
            }

            // Obtener módulos según rol
            List<String> modules = getModulesByRole(usuario.getRol().getNombre());
            
            return ResponseEntity.ok(modules);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 🔥 MÉTODO PRIVADO - Lógica de módulos por rol
    private List<String> getModulesByRole(String rolNombre) {
        List<String> modules = new ArrayList<>();
        
        switch(rolNombre.toUpperCase()) {
            case "ADMINISTRADOR":
                modules.addAll(Arrays.asList("dashboard", "usuarios", "roles", "productos", 
                                           "categorias", "proveedores", "movimientos", "reportes", "auditoria"));
                break;
            case "SUPERVISOR":
                modules.addAll(Arrays.asList("dashboard", "productos", "categorias", 
                                           "proveedores", "movimientos", "reportes"));
                break;
            case "OPERADOR":
                modules.addAll(Arrays.asList("dashboard", "movimientos", "productos", "categorias", "proveedores"));
                break;
            default:
                modules.add("dashboard");
        }
        
        return modules;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // Validar token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Token no proporcionado");
            }

            String token = authHeader.substring(7);
            
            if (!jwtUtils.validateJwtToken(token)) {
                return ResponseEntity.badRequest().body("Token inválido");
            }

            // Obtener usuario desde token
            String username = jwtUtils.getUserNameFromJwtToken(token);
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Crear respuesta con información del usuario
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", usuario.getId());
            userInfo.put("codigo", usuario.getCodigo());
            userInfo.put("nombre", usuario.getNombre());
            userInfo.put("email", usuario.getEmail());
            userInfo.put("rol", usuario.getRol() != null ? usuario.getRol().getNombre() : null);
            userInfo.put("activo", usuario.getActivo());
            userInfo.put("modules", getModulesByRole(usuario.getRol().getNombre()));

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}