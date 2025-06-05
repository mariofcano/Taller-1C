package com.taskmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para la aplicación Task Manager.
 *
 * <p>Implemento la configuración completa de Spring Security incluyendo
 * autenticación, autorización, manejo de sesiones y CSRF. Defino las
 * rutas protegidas y públicas del sistema según roles de usuario.</p>
 *
 * @author Mario Flores
 * @version 1.0
 * @since 2025
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Servicio personalizado para cargar usuarios desde la base de datos.
     * Utilizo este servicio para integrar mi sistema de usuarios con
     * el mecanismo de autenticación de Spring Security.
     */
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Configuro el encoder de contraseñas utilizando BCrypt.
     *
     * <p>Implemento BCrypt como algoritmo de hashing por ser considerado
     * seguro y resistente a ataques de fuerza bruta. Este encoder
     * genera un salt único para cada contraseña.</p>
     *
     * @return PasswordEncoder configurado con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defino la cadena de filtros de seguridad principal del sistema.
     *
     * <p>Esta función configura todas las reglas de seguridad, incluyendo
     * autorizaciones por rol, formulario de login, logout y manejo de sesiones.
     * Implemento un sistema granular de permisos basado en rutas y roles.</p>
     *
     * @param http objeto HttpSecurity para configurar la seguridad web
     * @return SecurityFilterChain configurado con todas las reglas de seguridad
     * @throws Exception si ocurre error durante la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuro las reglas de autorización por rutas
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas accesibles sin autenticación
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // Consola H2 para desarrollo

                        // Rutas administrativas restringidas a rol ADMIN
                        .requestMatchers("/users/create", "/users/edit/**", "/users/delete/**").hasRole("ADMIN")

                        // Rutas de funcionalidad general para usuarios autenticados
                        .requestMatchers("/tasks/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/users/catalog").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/maps/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/locations/**").hasAnyRole("USER", "ADMIN")

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )

                // Configuro el formulario de login personalizado
                .formLogin(form -> form
                        .loginPage("/login")                    // Página de login personalizada
                        .loginProcessingUrl("/login")           // URL que procesa el formulario
                        .defaultSuccessUrl("/", true)           // Redirección tras login exitoso
                        .failureUrl("/login?error=true")        // Redirección tras error de login
                        .usernameParameter("username")          // Nombre del campo usuario
                        .passwordParameter("password")          // Nombre del campo contraseña
                        .permitAll()                            // Permitir acceso a todos
                )

                // Configuro el proceso de logout
                .logout(logout -> logout
                        .logoutUrl("/logout")                   // URL para cerrar sesión
                        .logoutSuccessUrl("/login?logout=true") // Redirección tras logout
                        .invalidateHttpSession(true)           // Invalidar sesión HTTP
                        .clearAuthentication(true)             // Limpiar autenticación
                        .deleteCookies("JSESSIONID")           // Eliminar cookies de sesión
                        .permitAll()                           // Permitir acceso a todos
                )

                // Configuro el manejo de sesiones concurrentes
                .sessionManagement(session -> session
                        .maximumSessions(1)                    // Una sesión por usuario
                        .maxSessionsPreventsLogin(false)      // Permitir nueva sesión
                )

                // Deshabilito CSRF temporalmente para debugging
                .csrf(csrf -> csrf.disable())

                // Configuro headers para permitir frames de H2 Console
                .headers(headers -> headers
                        .frameOptions().sameOrigin()
                );

        return http.build();
    }

    /**
     * Configuro el AuthenticationManager con mi servicio personalizado.
     *
     * <p>Integro mi CustomUserDetailsService con el sistema de autenticación
     * de Spring Security. Este manager utiliza el encoder BCrypt para
     * verificar contraseñas durante el proceso de login.</p>
     *
     * @param http objeto HttpSecurity para acceder al builder de autenticación
     * @return AuthenticationManager configurado con mi servicio de usuarios
     * @throws Exception si ocurre error durante la configuración
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        // Configuro mi servicio personalizado y el encoder de contraseñas
        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());

        return authBuilder.build();
    }
}