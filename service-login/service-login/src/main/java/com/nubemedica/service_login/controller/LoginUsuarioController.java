package com.nubemedica.service_login.controller;

import com.nubemedica.service_login.dto.LoginUsuarioRequest;
import com.nubemedica.service_login.dto.LoginUsuarioResponse;
import com.nubemedica.service_login.dto.MensajeResponse;
import com.nubemedica.service_login.service.AuthService;
import com.nubemedica.service_login.service.LoginUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class LoginUsuarioController {

    @Autowired
    private LoginUsuarioService loginUsuarioService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<List<LoginUsuarioResponse>> listar() {
        return ResponseEntity.ok(loginUsuarioService.listarUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoginUsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(loginUsuarioService.obtenerUsuario(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeResponse> actualizarContrasena(@PathVariable Long id, @RequestBody @Valid LoginUsuarioRequest request) {
        var usuario = authService.actualizarContrasena(id, request.contrasena());

        return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada exitosamente al usuario con id: " + usuario.idUsuario()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        loginUsuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}