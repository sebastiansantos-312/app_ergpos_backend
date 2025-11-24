    package com.ergpos.app.controller;

    import java.util.List;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import jakarta.validation.Valid;
    import com.ergpos.app.dto.usuarios.CambiarPasswordRequestDTO;
    import com.ergpos.app.dto.usuarios.UsuarioRequestDTO;
    import com.ergpos.app.dto.usuarios.UsuarioResponseDTO;
    import com.ergpos.app.service.UsuarioService;

    @RestController
    @RequestMapping("/api/usuarios")
    @CrossOrigin(origins = "*")
    public class UsuarioController {

        private final UsuarioService usuarioService;

        public UsuarioController(UsuarioService usuarioService) {
            this.usuarioService = usuarioService;
        }

        // Crear usuario
        @PostMapping
        public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO request) {
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
        }

        // Listar con filtros dinámicos
        @GetMapping
        public ResponseEntity<List<UsuarioResponseDTO>> listar(
                @RequestParam(required = false) String nombre,
                @RequestParam(required = false) String email,
                @RequestParam(required = false) String rol,
                @RequestParam(required = false) Boolean activo) {
            return ResponseEntity.ok(usuarioService.listar(nombre, email, rol, activo));
        }

        // Obtener por email
        @GetMapping("/{email}")
        public ResponseEntity<UsuarioResponseDTO> obtener(@PathVariable String email) {
            return ResponseEntity.ok(usuarioService.obtenerPorEmail(email));
        }

        // Actualizar por email
        @PutMapping("/{email}")
        public ResponseEntity<UsuarioResponseDTO> actualizar(
                @PathVariable String email,
                @Valid @RequestBody UsuarioRequestDTO request) {
            return ResponseEntity.ok(usuarioService.actualizar(email, request));
        }

        // Activar por email
        @PatchMapping("/{email}/activar")
        public ResponseEntity<UsuarioResponseDTO> activar(@PathVariable String email) {
            return ResponseEntity.ok(usuarioService.activar(email));
        }

        // Desactivar por email
        @PatchMapping("/{email}/desactivar")
        public ResponseEntity<UsuarioResponseDTO> desactivar(@PathVariable String email) {
            return ResponseEntity.ok(usuarioService.desactivar(email));
        }

        // Cambiar contraseña
        @PatchMapping("/{email}/cambiar-password")
        public ResponseEntity<Void> cambiarPassword(
                @PathVariable String email,
                @Valid @RequestBody CambiarPasswordRequestDTO request) {
            usuarioService.cambiarPassword(email, request);
            return ResponseEntity.noContent().build();
        }
    }