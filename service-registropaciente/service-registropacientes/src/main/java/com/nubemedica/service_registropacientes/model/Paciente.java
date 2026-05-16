package com.nubemedica.service_registropacientes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "paciente")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Paciente {

    @Id

    @Column(name = "run_paciente", nullable = false, unique = true)
    private String runPaciente; // PK
    
    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "pri_nombre", nullable = false)
    private String priNombre;

    @Column(name = "seg_nombre")
    private String segNombre;


    @Column(name = "apa_paterno", nullable = false)
    private String apaPaterno;


    @Column(name = "apa_materno", nullable = false)
    private String apaMaterno;


    @Column(name = "num_telefono", nullable = false, unique = true)
    private String numTelefono;

    // Referencia al Microservicio 4 (Dirección)
    // Se mantiene para saber dónde vive el paciente (ubicación)
    @Column(name = "id_direccion")
    private Long idDireccion;

    // Se usa para traer datos de la dirección desde MS4 sin acoplar tablas
    @Transient
    private Object datosDireccion; 

}
