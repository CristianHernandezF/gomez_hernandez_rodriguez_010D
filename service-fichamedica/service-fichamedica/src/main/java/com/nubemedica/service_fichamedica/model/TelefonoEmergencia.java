package com.nubemedica.service_fichamedica.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "telefono_emergencia")
public class TelefonoEmergencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTelefono;
    private String numTelefono;
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_ficha_medica")
    @JsonIgnore
    private FichaMedica fichaMedica;
}
