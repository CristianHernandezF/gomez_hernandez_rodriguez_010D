package com.nubemedica.service_fichamedica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ficha_medica",
        uniqueConstraints = {@UniqueConstraint
            (columnNames = {"run_paciente", "run_doctor"})})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FichaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFichaMedica;

    private String historialFamiliar;
    private String hipotesisDiagnostica;

    @Column(name = "run_paciente", nullable = false)
    private String runPaciente; 
    @Column(name = "run_doctor", nullable = false)
    private String runDoctor;
    private Long idReporte;

    //
    @OneToMany
    @JoinColumn(name = "telefonoemergencia_id")
    private Long idTelefonoEmergencia;

    @OneToMany
    @JoinColumn(name = "contactoprofesional_id")
    private Long idContactoProfesional;

    @OneToMany
    @JoinColumn(name = "farmacosrecetados_id")
    private Long idFarmacosRecetados;

    @Transient
    private Object datosReportes;

    @Transient
    private Object datosPaciente;

}
