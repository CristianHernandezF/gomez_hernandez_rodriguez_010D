package com.nubemedica.service_fichamedica.model;

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
@Table(name = "farmacos_recetados")
@AllArgsConstructor
@NoArgsConstructor
public class FarmacosRecetados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFarmaco;

    private String nomFarmaco;

    private float dosis;

    @ManyToOne
    @JoinColumn(name = "fichamedica_id")
    private Long idFichaMedica;

}
