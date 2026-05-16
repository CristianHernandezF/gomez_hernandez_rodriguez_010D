package com.nubemedica.service_login.controller;

import com.nubemedica.service_login.service.ConsultaTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consulta/token")
public class ConsultaToken {

    @Autowired
    private ConsultaTokenService consultaTokenService;

    @GetMapping()
    public ResponseEntity<?> obtenerTokens() {
        return ResponseEntity.ok(consultaTokenService.obtenerConsulta());
    }
}
