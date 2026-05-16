package com.nubemedica.service_login.controller;

import com.nubemedica.service_login.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/token")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/todos")
    public ResponseEntity<?> obtenerTokens() {
        return ResponseEntity.ok(tokenService.obtenerTokens());
    }
}
