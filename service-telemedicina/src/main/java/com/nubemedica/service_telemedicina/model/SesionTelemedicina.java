package com.nubemedica.service_telemedicina.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "sesion_telemedicina")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SesionTelemedicina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSesionTelemedicina;

    @Column(nullable = false)
    private String linkAcceso;

    @Column(nullable = false, length = 10)
    private String codigoAcceso;
}