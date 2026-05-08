package com.hospital.service_direccion.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "DIRECCION")
@AllArgsConstructor
@NoArgsConstructor

public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comuna_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Comuna comuna;


}
