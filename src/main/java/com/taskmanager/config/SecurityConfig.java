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
 * CONFIGURACIÓN DE SEGURIDAD PARA LA APLICACIÓN
 * DEFINE QUÉ RUTAS ESTÁN PROTEGIDAS Y CÓMO FUNCIONA EL LOGIN
 *
 * @author Mario Flores
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * CONFIGURO EL CIFRADO DE CONTRASEÑAS
     * USO BCRYPT QUE ES SEGURO Y ESTÁNDAR
     *
     * @return el encoder de contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CONFIGURO LAS REGLAS DE SEGURIDAD DE LA APLICACIÓN
     *
     * @param http configuración de seguridad HTTP
     * @return la cadena de filtros de seguridad
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CONFIGURO LAS AUTORIZACIONES
                .authorizeHttpRequests(auth -> auth
                        // RUTAS PÚBLICAS (NO NECESITAN LOGIN)
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // CONSOLA H2 PARA DESARROLLO

                        // RUTAS DE ADMINISTRADOR
                        .requestMatchers("/users/create", "/users/edit/**", "/users/delete/**").hasRole("ADMIN")

                        // RUTAS DE USUARIOS NORMALES
                        .requestMatchers("/tasks/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/users/catalog").hasAnyRole("USER", "ADMIN")

                        // CUALQUIER OTRA RUTA NECESITA AUTENTICACIÓN
                        .anyRequest().authenticated()
                )

                // CONFIGURO EL FORMULARIO DE LOGIN
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )

                // CONFIGURO EL LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // CONFIGURO EL MANEJO DE SESIONES
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )

                // DESABILITO CSRF PARA H2 CONSOLE
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )

                // PERMITO FRAMES PARA H2 CONSOLE
                .headers(headers -> headers
                        .frameOptions().sameOrigin()
                );

        return http.build();
    }

    /**
     * CONFIGURO EL MANAGER DE AUTENTICACIÓN
     * LE DIGO QUE USE MI SERVICIO PERSONALIZADO Y EL ENCODER
     *
     * @param http configuración de seguridad
     * @return el manager de autenticación
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());

        return authBuilder.build();
    }
}