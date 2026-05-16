package com.nubemedica.service_calendario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "evento")
@Inheritance(strategy = InheritanceType.JOINED)  // Clave: una tabla por clase
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvento;

    private LocalDate fecha;
    private LocalTime hora;

    @ManyToOne
    @JoinColumn(name = "id_tipo_evento")
    private TipoEvento tipoEvento;

    private String runDoctor; // 
}
