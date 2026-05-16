package com.nubemedica.service_login.service;

import com.nubemedica.service_login.config.HashUtils;
import com.nubemedica.service_login.model.LoginUsuario;
import com.nubemedica.service_login.model.Token;
import com.nubemedica.service_login.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private HashUtils hashUtils;

    public List<Token> obtenerTokens() {
        return tokenRepository.findAll();
    }

    public void guardarTokens(LoginUsuario usuario, String accessJwt, String refreshJwt) {
        Token token = new Token();
        token.setCorreoUsuario(usuario.getCorreo());
        token.setTokenGenerado(hashUtils.hash(accessJwt));
        token.setRefreshToken(hashUtils.hash(refreshJwt));
        token.setFechaExp(LocalDateTime.now().plusHours(1));
        token.setFechaExpRefresh(LocalDateTime.now().plusDays(7));
        token.setActivo(true);
        tokenRepository.save(token);
    }
}