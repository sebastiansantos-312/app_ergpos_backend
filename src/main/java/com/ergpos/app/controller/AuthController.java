package com.ergpos.app.controller;

import java.util.List;

import com.ergpos.app.dto.auth.LoginRequestDTO;
import com.ergpos.app.dto.auth.LoginResponseDTO;
import com.ergpos.app.model.Usuario;
import com.ergpos.app.repository.UsuarioRepository;
import com.ergpos.app.security.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getActivo()) {
            return ResponseEntity.status(403).body("Usuario desactivado");
        }

        List<String> roles = usuario.getRoles().stream()
                .map(r -> r.getNombre())
                .toList();

        String token = jwtService.generateToken(usuario.getEmail(), roles);

        LoginResponseDTO resp = new LoginResponseDTO(
                token,
                usuario.getNombre(),
                usuario.getEmail(),
                roles,
                usuario.getActivo());

        return ResponseEntity.ok(resp);
    }
}
