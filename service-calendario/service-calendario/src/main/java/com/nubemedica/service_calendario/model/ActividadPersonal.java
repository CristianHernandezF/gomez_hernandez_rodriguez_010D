package com.nubemedica.service_calendario.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "actividad_personal")
@PrimaryKeyJoinColumn(name = "idEvento")
public class ActividadPersonal extends Evento {

    private String nombreActividad;
    private String descripcion;
}
