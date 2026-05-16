package com.nubemedica.service_login.repository;

import com.nubemedica.service_login.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenGenerado(String tokenGenerado);
    Optional<Token> findByRefreshToken(String refreshToken);

    boolean existsByTokenGeneradoAndActivoTrue(String jwt);
}