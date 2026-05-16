package com.nubemedica.service_registrodoctor.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctor")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistroDoctor {

    @Id
    @Column(name="run_doctor" ,unique= true, nullable = false)
    private String runDoctor;
    
    @Column(name="pri_nombre", nullable = false)
    private String priNombre;

    @Column(name="seg_nombre")
    private String segNombre;

    @Column(name="apa_paterno", nullable = false)
    private String apaPaterno;

    @Column(name="apa_materno", nullable = false)
    private String apaMaterno;

    @Column(name="telefono" ,unique= true, nullable = false)
    private String telefono;

    @Column(name="correo" ,unique= true, nullable = false)
    private String correo;
    
    // Referencia al Microservicio 4 (Dirección)
    // Se mantiene para saber dónde vive el paciente (ubicación)
    @Column(name = "id_direccion")
    private Long idDireccion;

    // Se usa para traer datos de la dirección desde MS4 sin acoplar tablas
    @Transient
    private Object datosDireccion; 

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Importante para evitar bucles infinitos en el JSON
    private List<AtencionesDoctor> atenciones;
}