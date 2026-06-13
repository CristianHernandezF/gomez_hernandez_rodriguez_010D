package com.nubemedica.service_calendario.model;

import com.nubemedica.service_calendario.dto.TelemedicinaResponseDTO;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cita_medica")
@PrimaryKeyJoinColumn(name = "idEvento")  // Enlaza la PK con la tabla padre
public class CitaMedica extends Evento {

    private String runPaciente;  // RUN del paciente (MS-Pacientes)
    private Long idEstadoCitaMedica;   // Referencia a MS-EstadoCitasMedicas
    private String motivoConsulta;
    private Long idSesionTelemedicina;


    @Transient
    private TelemedicinaResponseDTO datosTelemedicina; 
    @Transient
    private Object DatosEstadoCitaMedica;
}
