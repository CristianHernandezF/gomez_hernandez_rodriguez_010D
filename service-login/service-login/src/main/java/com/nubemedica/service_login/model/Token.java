package com.nubemedica.service_login.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idToken;
    private String correoUsuario;

    @Column(length = 512)
    private String tokenGenerado;

    @Column(length = 512)
    private String refreshToken;

    private LocalDateTime fechaExp;
    private LocalDateTime fechaExpRefresh;

    private boolean activo;
}