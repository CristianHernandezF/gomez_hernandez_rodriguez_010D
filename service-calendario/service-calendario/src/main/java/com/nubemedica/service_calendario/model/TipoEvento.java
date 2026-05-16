package com.nubemedica.service_calendario.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tipo_evento")
public class TipoEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoEvento;

    @NotBlank
    private String nombreTipo; // "Cita Médica", "Actividad Personal"

    @NotBlank
    private String colorTipo; // "#FF0000"
}
