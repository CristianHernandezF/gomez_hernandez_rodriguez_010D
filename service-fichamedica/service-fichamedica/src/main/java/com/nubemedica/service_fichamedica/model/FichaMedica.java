package com.nubemedica.service_fichamedica.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ficha_medica",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"run_paciente", "run_doctor"})})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FichaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFichaMedica;

    // Son nulos al principio, se llenan en la cita
    @Column(name = "historial_familiar", nullable = true)
    private String historialFamiliar;
    
    @Column(name = "Diagnostico", nullable = true)
    private String Diagnostico;

    @Column(name = "run_paciente", nullable = false)
    private String runPaciente; 
    
    @Column(name = "run_doctor", nullable = false)
    private String runDoctor;

    // Inicializamos como listas vacías para evitar errores de puntero nulo
    @OneToMany(mappedBy = "fichaMedica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContactoProfesional> contactoPro = new ArrayList<>();

    @OneToMany(mappedBy = "fichaMedica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TelefonoEmergencia> telefonos = new ArrayList<>();

    @OneToMany(mappedBy = "fichaMedica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FarmacosRecetados> farmacos = new ArrayList<>();

    @Transient
    private Object datosPaciente;
}
