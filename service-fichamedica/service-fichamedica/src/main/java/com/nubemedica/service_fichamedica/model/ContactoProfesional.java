package com.nubemedica.service_fichamedica.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "contactoProfesional")
@AllArgsConstructor
@NoArgsConstructor
public class ContactoProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idContactoPro;    
    private String correo;
    private String nombres;
    private String apellidos;

    @ManyToOne
    @JoinColumn(name = "id_ficha_medica")
    @JsonIgnore
    private FichaMedica fichaMedica;
    
}
