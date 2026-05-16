package com.nubemedica.service_estadocita.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "estado_cita")
@NoArgsConstructor
@AllArgsConstructor
public class EstadoCita {

    @Id
    @Column(name = "id_estado_cita_medica") 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstadoCita;
    
    private String observacion;

    @ManyToOne
    @JoinColumn(name = "tipoestadocita_id")
    @JsonIgnore
    private TipoEstadoCita tipoEstadoCita;
}
