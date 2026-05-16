package com.nubemedica.service_login.service;

import com.nubemedica.service_login.config.HashUtils;
import com.nubemedica.service_login.dto.ConsultaTokenResponse;
import com.nubemedica.service_login.model.ConsultaToken;
import com.nubemedica.service_login.model.LoginUsuario;
import com.nubemedica.service_login.repository.ConsultaTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaTokenService {

    @Autowired
    private ConsultaTokenRepository consultaTokenRepository;

    @Autowired
    private HashUtils hashUtils;

    @Async
    public void guardarLog(
            String token,
            LoginUsuario usuario,
            String api
    ) {

        ConsultaToken log = new ConsultaToken();

        log.setToken(hashUtils.hash(token));
        log.setUsuario(usuario);
        log.setNomApi(api);
        log.setFechaConsulta(LocalDateTime.now());

        consultaTokenRepository.save(log);
    }

    public List<ConsultaTokenResponse> obtenerConsulta() {
        return consultaTokenRepository.findAll().stream()
                .map(log -> new ConsultaTokenResponse(
                        log.getIdConsulta(),
                        log.getUsuario().getIdUsuario(),
                        log.getUsuario().getCorreo(),
                        log.getNomApi(),
                        log.getFechaConsulta()
                ))
                .toList();
    }
}
