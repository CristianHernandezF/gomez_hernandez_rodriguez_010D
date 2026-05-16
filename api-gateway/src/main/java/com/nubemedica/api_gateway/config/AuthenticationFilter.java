package com.nubemedica.api_gateway.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtils jwtUtils;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 1. Verificar si existe el header Authorization
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado: Falta Token");
            }

            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // 2. Validar Token y extraer Claim "runDoctor"
                    DecodedJWT jwt = jwtUtils.validateToken(token);
                    String runDoctor = jwt.getClaim("runDoctor").asString();

                    if (runDoctor == null || runDoctor.isBlank()) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Token no contiene RUN del doctor");
                    }
                    // 3. Mutar la petición: Agregar el RUN como un Header nuevo
                    // Los microservicios internos leerán este header "X-Doctor-Run"
                    request = request.mutate()
                            .header("X-Doctor-Run", runDoctor)
                            .build();

                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Inválido o Expirado");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Formato de Token inválido");
            }

            return chain.filter(exchange.mutate().request(request).build());
        };
    }
}
