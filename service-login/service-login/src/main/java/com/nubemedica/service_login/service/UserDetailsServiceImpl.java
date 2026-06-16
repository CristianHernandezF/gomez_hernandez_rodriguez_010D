package com.nubemedica.service_login.service;

import com.nubemedica.service_login.model.LoginUsuario;
import com.nubemedica.service_login.repository.LoginUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final LoginUsuarioRepository loginUsuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        LoginUsuario usuario = loginUsuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado con correo: " + correo
                        )
                );

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getContrasena())
                .authorities(usuario.getAuthorities())
                .build();
    }
}