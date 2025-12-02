package com.ergpos.app.controller;

import com.ergpos.app.dto.auth.ErrorResponseDTO;
import com.ergpos.app.dto.auth.LoginRequestDTO;
import com.ergpos.app.dto.auth.LoginResponseDTO;
import com.ergpos.app.exception.UnauthorizedException;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.UsuarioRepository;
import com.ergpos.app.security.JwtUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
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
            String username = loginRequest.getUsername();
            logger.debug("Intento de login para: {}", username);

            // Buscar usuario por email O código
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(username);

            if (usuarioOpt.isEmpty()) {
                usuarioOpt = usuarioRepository.findByCodigo(username);
            }

            //Respuesta genérica si no existe
            if (usuarioOpt.isEmpty()) {
                logger.warn("Login fallido - Usuario no encontrado: {}", username);
                return buildErrorResponse("INVALID_CREDENTIALS", "Credenciales inválidas", HttpStatus.UNAUTHORIZED);
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar contraseña ANTES de revisar estado
            if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPasswordHash())) {
                logger.warn("Login fallido - Contraseña incorrecta para: {}", username);
                return buildErrorResponse("INVALID_CREDENTIALS", "Credenciales inválidas", HttpStatus.UNAUTHORIZED);
            }

            //Mantener mensaje genérico para usuarios inactivos
            if (!usuario.getActivo()) {
                logger.warn("Login fallido - Usuario inactivo: {}", username);
                return buildErrorResponse("INVALID_CREDENTIALS", "Credenciales inválidas", HttpStatus.UNAUTHORIZED);
            }

            // Generar token JWT
            String jwt = jwtUtils.generateTokenFromUsername(usuario.getEmail());
            logger.info("Login exitoso para usuario: {}", username);

            // Respuesta MÍNIMA (sin información sensible)
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
            logger.error("Error en login: {}", e.getMessage(), e);
            return buildErrorResponse("SERVER_ERROR", "Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // NOTA: Con JWT stateless, el logout es principalmente frontend
        // El frontend debe eliminar el token del localStorage
        logger.info("Logout solicitado");
        return ResponseEntity.ok(Map.of(
                "message", "Logout exitoso",
                "timestamp", LocalDateTime.now()));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        logger.debug("Validación de token solicitada");
        return ResponseEntity.ok(Map.of(
                "message", "Token válido",
                "timestamp", LocalDateTime.now()));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            logger.debug("Verificación de token solicitada");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return buildErrorResponse("INVALID_TOKEN_FORMAT", "Formato de token inválido", HttpStatus.BAD_REQUEST);
            }

            String token = authHeader.substring(7);

            if (!jwtUtils.validateJwtToken(token)) {
                return buildErrorResponse("INVALID_TOKEN", "Token inválido o expirado", HttpStatus.UNAUTHORIZED);
            }

            String username = jwtUtils.getUserNameFromJwtToken(token);
            logger.debug("Token válido para usuario: {}", username);

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username,
                    "timestamp", LocalDateTime.now()));

        } catch (Exception e) {
            logger.error("Error al verificar token: {}", e.getMessage(), e);
            return buildErrorResponse("TOKEN_VERIFICATION_ERROR", "Error al verificar el token",
                    HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/modules")
    public ResponseEntity<?> getUserModules(@RequestHeader("Authorization") String authHeader) {
        try {
            logger.debug("Obtención de módulos solicitada");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return buildErrorResponse("INVALID_TOKEN_FORMAT", "Token no proporcionado", HttpStatus.BAD_REQUEST);
            }

            String token = authHeader.substring(7);

            if (!jwtUtils.validateJwtToken(token)) {
                return buildErrorResponse("INVALID_TOKEN", "Token inválido", HttpStatus.UNAUTHORIZED);
            }

            String username = jwtUtils.getUserNameFromJwtToken(token);
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(username)
                    .or(() -> usuarioRepository.findByCodigo(username))
                    .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

            //Manejo específico para usuarios desactivados DESPUÉS de
            // autenticación
            if (!usuario.getActivo()) {
                logger.warn("Usuario desactivado intentando acceder a módulos: {}", username);
                return buildErrorResponse("ACCOUNT_DISABLED", "Tu cuenta ha sido desactivada", HttpStatus.FORBIDDEN);
            }

            List<String> modules = getModulesByRole(usuario.getRol().getNombre());
            logger.debug("Módulos obtenidos para usuario {}: {}", username, modules);

            return ResponseEntity.ok(Map.of(
                    "modules", modules,
                    "timestamp", LocalDateTime.now()));

        } catch (Exception e) {
            logger.error("Error al obtener módulos: {}", e.getMessage(), e);
            return buildErrorResponse("SERVER_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            logger.debug("Obtención de información de usuario solicitada");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return buildErrorResponse("INVALID_TOKEN_FORMAT", "Token no proporcionado", HttpStatus.BAD_REQUEST);
            }

            String token = authHeader.substring(7);

            if (!jwtUtils.validateJwtToken(token)) {
                return buildErrorResponse("INVALID_TOKEN", "Token inválido", HttpStatus.UNAUTHORIZED);
            }

            String username = jwtUtils.getUserNameFromJwtToken(token);
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(username)
                    .or(() -> usuarioRepository.findByCodigo(username))
                    .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

            //Manejo específico para usuarios desactivados
            if (!usuario.getActivo()) {
                logger.warn("Usuario desactivado intentando acceder a información: {}", username);
                return buildErrorResponse("ACCOUNT_DISABLED", "Tu cuenta ha sido desactivada", HttpStatus.FORBIDDEN);
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", usuario.getId());
            userInfo.put("codigo", usuario.getCodigo());
            userInfo.put("nombre", usuario.getNombre());
            userInfo.put("email", usuario.getEmail());
            userInfo.put("rol", usuario.getRol() != null ? usuario.getRol().getNombre() : null);
            userInfo.put("activo", usuario.getActivo());
            userInfo.put("modules", getModulesByRole(usuario.getRol().getNombre()));
            userInfo.put("timestamp", LocalDateTime.now());

            logger.debug("Información de usuario obtenida: {}", username);
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            logger.error("Error al obtener información de usuario: {}", e.getMessage(), e);
            return buildErrorResponse("SERVER_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> getModulesByRole(String rolNombre) {
        List<String> modules = new ArrayList<>();

        switch (rolNombre.toUpperCase()) {
            case "ADMINISTRADOR":
                modules.addAll(Arrays.asList("dashboard", "usuarios", "roles", "productos",
                        "auditoria", "categorias", "proveedores", "movimientos", "reportes"));
                break;
            case "SUPERVISOR":
                modules.addAll(Arrays.asList("dashboard", "productos", "categorias", "proveedores", "movimientos",
                        "reportes"));
                break;
            case "OPERADOR":
                modules.addAll(Arrays.asList("dashboard", "productos", "movimientos"));
                break;
            default:
                modules.add("dashboard");
        }

        return modules;
    }

    //Método helper para construir respuestas de error consistentes
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String code, String message, HttpStatus status) {
        ErrorResponseDTO error = new ErrorResponseDTO(code, message, status.value());
        return ResponseEntity.status(status).body(error);
    }
}