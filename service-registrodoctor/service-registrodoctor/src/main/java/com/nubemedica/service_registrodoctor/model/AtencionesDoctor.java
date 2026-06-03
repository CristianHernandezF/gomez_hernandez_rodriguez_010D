package com.nubemedica.service_registrodoctor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nubemedica.service_registrodoctor.dto.PacienteResumen;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// service-registrodoctor -> model -> AtencionesDoctor.java
@Entity
@Table(name = "atenciones_doctor", uniqueConstraints = 
                {@UniqueConstraint(columnNames = {"run_paciente", "run_doctor"})})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtencionesDoctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(name = "run_paciente", nullable = false)
    private String runPaciente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "run_doctor")
    private RegistroDoctor doctor;

    // Campo para mostrar datos del paciente sin persistir en DB
    @Transient
    private PacienteResumen datosPaciente; 
}
