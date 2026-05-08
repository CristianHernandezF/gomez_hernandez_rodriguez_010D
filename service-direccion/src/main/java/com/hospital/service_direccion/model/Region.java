package com.hospital.service_direccion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="REGION")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Region {
            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

            @NotNull
            private String nombre;
}
