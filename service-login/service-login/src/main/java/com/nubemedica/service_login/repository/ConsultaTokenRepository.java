package com.nubemedica.service_login.repository;

import com.nubemedica.service_login.model.ConsultaToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaTokenRepository extends JpaRepository<ConsultaToken, Long> {
}