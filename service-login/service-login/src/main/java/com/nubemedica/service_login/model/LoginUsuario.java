package com.nubemedica.service_login.model;

import com.nubemedica.service_login.enums.Roles;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nubemedica.service_login.dto.RegisterRequest;

import java.util.Collection;
import java.util.List;

@Entity
@Data
@Table(name = "login_usuario")
@NoArgsConstructor
public class LoginUsuario implements UserDetails {
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles role = Roles.ROLE_DOCTOR;

    public LoginUsuario(RegisterRequest datos, PasswordEncoder passwordEncoder) {
        this.correo = datos.correo();
        this.contrasena = passwordEncoder.encode(datos.contrasena());
        this.numTelefono = datos.numTelefono();
        this.runDoctor = datos.runDoctor();
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

}