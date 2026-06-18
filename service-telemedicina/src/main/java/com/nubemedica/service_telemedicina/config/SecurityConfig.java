// package com.nubemedica.service_telemedicina.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             // 1. Habilitar compatibilidad con CORS
//             .cors(cors -> {}) 
            
//             .csrf(csrf -> csrf.disable())
            
//             // 2. DESHABILITAR el login clásico por formulario y HTTP Basic
//             // Esto evita la redirección automática (HTTP 302) a /login
//             .formLogin(form -> form.disable())
//             .httpBasic(basic -> basic.disable())
            
//             .authorizeHttpRequests(auth -> auth
//                 // 3. Permitir el acceso público a Swagger de Reportes
//                 .requestMatchers(
//                     "/v3/api-docs/**",
//                     "/api/v1/reporte/v3/api-docs/**",
//                     "/swagger-ui/**",
//                     "/swagger-ui.html"
//                 ).permitAll()
                
//                 // Las demás peticiones del microservicio requerirán autenticación
//                 .anyRequest().authenticated()
//             );

//         return http.build();
//     }
// }