package com.nubemedica.service_login.controller;

import com.nubemedica.service_login.dto.AuthResponse;
import com.nubemedica.service_login.dto.LoginUsuarioRequest;
import com.nubemedica.service_login.dto.RegisterRequest;
import com.nubemedica.service_login.dto.TokenRefreshRequest;
import com.nubemedica.service_login.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:8081")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar a un doctor")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest datos) {
        return ResponseEntity.ok(authService.registrar(datos));
    }

    @PostMapping("/login")
    @Operation(summary = "Entrar en una sesion y obtener token")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginUsuarioRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar un token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refrescarToken(request.refreshToken()));
    }

    @PostMapping("/signout")
    @Operation(summary = "Cerrar sesion de un doctor")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            authService.cerrarSesion(jwt);
            return ResponseEntity.ok("Sesión cerrada exitosamente");
        }
        return ResponseEntity.badRequest().body("No se encontró el token");
    }

}
