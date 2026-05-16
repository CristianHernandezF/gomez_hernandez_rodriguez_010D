package com.nubemedica.service_login.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nubemedica.service_login.dto.RegisterRequest;

@Entity
@Data
@Table(name = "login_usuario")
@NoArgsConstructor
public class LoginUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(name = "run_doctor", unique = true, nullable = false)
    private String runDoctor;

    @Column(name = "contrasena")
    private String contrasena;

    @Column(unique = true, nullable = false)
    private String numTelefono;

    public LoginUsuario(RegisterRequest datos, PasswordEncoder passwordEncoder) {
        this.correo = datos.correo();
        this.contrasena = passwordEncoder.encode(datos.contrasena());
        this.numTelefono = datos.numTelefono();
        this.runDoctor = datos.runDoctor();
    }
}