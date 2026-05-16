package com.nubemedica.service_login.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consulta_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Long idConsulta;

    @Column(name = "token", nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private LoginUsuario usuario;

    @Column(name = "nom_api", nullable = false)
    private String nomApi;

    @Column(name = "fecha_consulta")
    private LocalDateTime fechaConsulta;
}