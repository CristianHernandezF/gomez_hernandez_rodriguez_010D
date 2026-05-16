package com.nubemedica.service_estadocita.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="tipo_estadocita")
@AllArgsConstructor
@NoArgsConstructor
public class TipoEstadoCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoEstado;
    private String nombreEstado;

}
