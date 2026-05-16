package com.nubemedica.service_login.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.nubemedica.service_login.model.LoginUsuario;
import com.nubemedica.service_login.repository.TokenRepository;
import com.nubemedica.service_login.service.ConsultaTokenService;
import com.nubemedica.service_login.service.LoginUsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private LoginUsuarioService loginUsuarioService;

    @Autowired
    private ConsultaTokenService consultaTokenService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private HashUtils hashUtils; // 1. Inyectamos HashUtils para procesar el token entrante

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // 2. Validar firma y expiración del JWT (Matemáticamente)
            DecodedJWT decodedJWT = jwtUtils.validateToken(jwt);
            String correo = decodedJWT.getSubject();

            // 3. NUEVA FUNCIONALIDAD: Validar contra la base de datos
            // Primero hasheamos el JWT que viene de la petición para poder buscarlo
            String jwtHasheado = hashUtils.hash(jwt);
            boolean tokenValidoEnDB = tokenRepository.existsByTokenGeneradoAndActivoTrue(jwtHasheado);

            // 4. AGREGAR tokenValidoEnDB a la condición
            if (correo != null && tokenValidoEnDB && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(correo);
                LoginUsuario usuario = loginUsuarioService.buscarPorCorreo(correo).orElse(null);

                // 5. Guardar log de consulta (Aquí puedes guardar el JWT original o el Hash)
                if (usuario != null) {
                    String apiConsultada = request.getRequestURI();
                    consultaTokenService.guardarLog(jwt, usuario, apiConsultada);
                }

                // 6. Autenticar en Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else if (!tokenValidoEnDB) {
                // Opcional: Si el token existe pero está inactivo (Logout), podrías loguear un intento fallido
                logger.warn("Intento de acceso con un token revocado (Logout hecho).");
            }

        } catch (Exception e) {
            logger.error("Error al validar el token JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}