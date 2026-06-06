package com.nubemedica.service_reportes.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reporte")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReporte;

    private String nombreReporte;

    private LocalDate fechaReporte;

    // No es @ManyToOne ni FK — es solo el ID de otra BD (ms-fichamedica)
    private Long idFichaMedica;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_detalle_reporte")
    private DetalleReporte detalleReporte;
}