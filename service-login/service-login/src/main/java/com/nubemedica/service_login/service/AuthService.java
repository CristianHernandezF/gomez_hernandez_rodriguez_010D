package com.nubemedica.service_login.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.nubemedica.service_login.config.HashUtils;
import com.nubemedica.service_login.config.JwtUtils;
import com.nubemedica.service_login.dto.AuthResponse;
import com.nubemedica.service_login.dto.LoginUsuarioRequest;
import com.nubemedica.service_login.dto.LoginUsuarioResponse;
import com.nubemedica.service_login.dto.RegisterRequest;
import com.nubemedica.service_login.exceptions.TokenRevokedException;
import com.nubemedica.service_login.model.LoginUsuario;
import com.nubemedica.service_login.exceptions.AuthException;
import com.nubemedica.service_login.model.Token;
import com.nubemedica.service_login.repository.TokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    @Autowired
    private LoginUsuarioService loginUsuarioService;

    @Autowired TokenService tokenService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private HashUtils hashUtils;

    public AuthResponse registrar(RegisterRequest datos) {
        var correoUsuario = loginUsuarioService.buscarPorCorreo(datos.correo());
        if (correoUsuario.isPresent()) {
            throw new AuthException("El email ya está registrado");
        }

        var telefonoUsuario = loginUsuarioService.buscarPorTelefono(datos.numTelefono());
        if (telefonoUsuario.isPresent()) {
            throw new AuthException("El número ya está registrado");
        }

        if (loginUsuarioService.buscarPorRunDoctor(datos.runDoctor()).isPresent()) {
            throw new AuthException("El RUN ya está registrado");
        }

        var usuario = new LoginUsuario(datos, passwordEncoder);
        loginUsuarioService.guardarUsuario(usuario);

        String accessToken = jwtUtils.generateToken(usuario.getCorreo(), usuario.getRunDoctor(), usuario.getRole());
        String refreshToken = jwtUtils.generateRefreshToken();

        tokenService.guardarTokens(usuario, accessToken, refreshToken);

        return new AuthResponse(usuario.getRunDoctor(), usuario.getCorreo(), accessToken, refreshToken);
    }

    public AuthResponse login(LoginUsuarioRequest datos) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(datos.correo(), datos.contrasena()));

        var usuario = loginUsuarioService.buscarPorCorreo(datos.correo())
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        String accessToken = jwtUtils.generateToken(usuario.getCorreo(), usuario.getRunDoctor(), usuario.getRole());
        String refreshToken = jwtUtils.generateRefreshToken();

        tokenService.guardarTokens(usuario, accessToken, refreshToken);

        return new AuthResponse(usuario.getRunDoctor(), usuario.getCorreo(), accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refrescarToken(String refreshTokenRequest) {
        Token tokenDB = tokenRepository.findByRefreshToken(refreshTokenRequest)
                .orElseThrow(() -> new TokenRevokedException("Refresh token no encontrado"));

        if (!tokenDB.isActivo() || tokenDB.getFechaExpRefresh().isBefore(LocalDateTime.now())) {
            tokenDB.setActivo(false);
            tokenRepository.save(tokenDB);
            throw new TokenRevokedException("Refresh token expirado o inactivo");
        }

        String correo = tokenDB.getCorreoUsuario();
        String runDoctor = tokenDB.getRunDoctor();

        tokenDB.setActivo(false);
        tokenRepository.save(tokenDB);

        var usuario = loginUsuarioService.buscarPorCorreo(correo).get();

        String nuevoToken = jwtUtils.generateToken(correo, runDoctor, usuario.getRole());
        String nuevoRefreshToken = jwtUtils.generateRefreshToken();

        tokenService.guardarTokens(usuario, nuevoToken, nuevoRefreshToken);

        return new AuthResponse(usuario.getRunDoctor(), correo, nuevoToken, nuevoRefreshToken);
    }

    public LoginUsuarioResponse actualizarContrasena(Long id, String nuevaContrasena) {
        String hash = passwordEncoder.encode(nuevaContrasena);

        return loginUsuarioService.actualizarContrasenaUsuario(id, hash);
    }

    @Transactional
    public void cerrarSesion(String token) {
        tokenRepository.findByTokenGenerado(hashUtils.hash(token)).ifPresent(t -> {
            t.setActivo(false);
            tokenRepository.save(t);
        });
    }
}