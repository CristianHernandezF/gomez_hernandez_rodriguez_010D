package com.nubemedica.service_login.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(String email, String runDoctor) {
        return createToken(email, runDoctor, 3600000);
    }

    public String generateRefreshToken(String email, String runDoctor) {
        return createToken(email, runDoctor, 604800000);
    }

    private String createToken(String email, String runDoctor, long expirationMillis) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withIssuer("NubeMedica-Auth")
                .withSubject(email)
                .withClaim("runDoctor", runDoctor)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMillis))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }
}