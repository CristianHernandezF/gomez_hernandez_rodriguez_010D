package com.nubemedica.service_fichamedica.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    
}
