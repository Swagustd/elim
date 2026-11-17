package com.sena.elim;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactivado para solicitudes POST
                .authorizeHttpRequests(auth -> auth
                        // 1. Rutas públicas (no requieren inicio de sesión)
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/registro",
                                "/registro-usuario",
                                "/registrar",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/assets/**"
                        ).permitAll()
                        // Permitir cualquier otra ruta mientras manejas la sesión manualmente por HttpSession
                        .anyRequest().permitAll()
                )
                // Deshabilitar la gestión automática de login de Spring Security
                // para que tu UsuarioController.java controle el POST /login
                .formLogin(form -> form.disable())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    // Bean para encriptar y verificar contraseñas con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}