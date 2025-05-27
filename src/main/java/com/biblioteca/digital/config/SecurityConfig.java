package com.biblioteca.digital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * CONFIGURACION DE SEGURIDAD PARA EL SISTEMA DE BIBLIOTECA DIGITAL
 *
 * ESTA CLASE DEFINE TODAS LAS POLITICAS DE SEGURIDAD Y ACCESO DEL SISTEMA.
 * CONFIGURO LAS RUTAS QUE REQUIEREN AUTENTICACION, LAS QUE SON PUBLICAS
 * Y LOS MECANISMOS DE CIFRADO DE CONTRASEÑAS QUE UTILIZO EN EL SISTEMA.
 *
 * IMPLEMENTO UNA CONFIGURACION FLEXIBLE QUE PERMITE ACCESO PUBLICO A LAS
 * PAGINAS PRINCIPALES MIENTRAS PROTEJO LAS FUNCIONALIDADES SENSIBLES QUE
 * REQUIEREN AUTENTICACION DE USUARIO COMO REALIZAR PRESTAMOS O ADMINISTRAR
 * EL CATALOGO DE LIBROS.
 *
 * LA CONFIGURACION INCLUYE MANEJO DE SESIONES, PROTECCION CSRF Y
 * REDIRECCIONAMIENTOS PERSONALIZADOS PARA UNA EXPERIENCIA DE USUARIO OPTIMA.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see org.springframework.security.web.SecurityFilterChain
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * CONFIGURO EL FILTRO DE SEGURIDAD PRINCIPAL DEL SISTEMA
     *
     * ESTE METODO DEFINE TODAS LAS REGLAS DE ACCESO Y AUTENTICACION
     * QUE APLICO EN EL SISTEMA. ESTABLEZCO QUE RUTAS SON PUBLICAS
     * Y CUALES REQUIEREN AUTENTICACION, ASI COMO LOS FORMULARIOS
     * DE LOGIN Y LOGOUT QUE UTILIZO.
     *
     * CONFIGURO EL SISTEMA PARA PERMITIR ACCESO LIBRE A LAS PAGINAS
     * PRINCIPALES COMO EL CATALOGO Y LA INFORMACION GENERAL, MIENTRAS
     * PROTEJO LAS FUNCIONALIDADES ADMINISTRATIVAS Y DE GESTION DE PRESTAMOS.
     *
     * @param http OBJETO HttpSecurity PARA CONFIGURAR LAS POLITICAS DE SEGURIDAD
     * @return SecurityFilterChain CONFIGURADO CON TODAS LAS REGLAS DE ACCESO
     * @throws Exception SI OCURRE ERROR EN LA CONFIGURACION DE SEGURIDAD
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CONFIGURO LAS REGLAS DE AUTORIZACION DE REQUESTS
                .authorizeHttpRequests(authz -> authz
                        // RUTAS PUBLICAS QUE NO REQUIEREN AUTENTICACION
                        .requestMatchers("/", "/home", "/catalog", "/contact").permitAll()
                        .requestMatchers("/register", "/login").permitAll()

                        // RECURSOS ESTATICOS PUBLICOS (CSS, JS, IMAGENES)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // CONSOLA H2 PARA DESARROLLO (SOLO EN PERFIL DE DESARROLLO)
                        .requestMatchers("/h2-console/**").permitAll()

                        // RUTAS ADMINISTRATIVAS QUE REQUIEREN ROL ADMIN O LIBRARIAN
                        .requestMatchers("/admin/**", "/management/**").hasAnyRole("ADMIN", "LIBRARIAN")

                        // RUTAS DE USUARIO QUE REQUIEREN AUTENTICACION BASICA
                        .requestMatchers("/user/**", "/loans/**", "/profile/**").authenticated()

                        // CUALQUIER OTRA RUTA REQUIERE AUTENTICACION
                        .anyRequest().authenticated()
                )

                // CONFIGURO EL FORMULARIO DE LOGIN PERSONALIZADO
                .formLogin(form -> form
                        .loginPage("/login")                    // PAGINA DE LOGIN PERSONALIZADA
                        .loginProcessingUrl("/perform_login")   // URL DONDE SE PROCESA EL LOGIN
                        .defaultSuccessUrl("/", true)           // REDIRECCION DESPUES DE LOGIN EXITOSO
                        .failureUrl("/login?error=true")        // REDIRECCION EN CASO DE ERROR
                        .usernameParameter("username")          // NOMBRE DEL PARAMETRO DE USUARIO
                        .passwordParameter("password")          // NOMBRE DEL PARAMETRO DE CONTRASEÑA
                        .permitAll()                            // PERMITIR ACCESO A TODOS AL LOGIN
                )

                // CONFIGURO EL LOGOUT DEL SISTEMA
                .logout(logout -> logout
                        .logoutUrl("/logout")                   // URL PARA HACER LOGOUT
                        .logoutSuccessUrl("/?logout=true")      // REDIRECCION DESPUES DE LOGOUT
                        .invalidateHttpSession(true)           // INVALIDO LA SESION HTTP
                        .deleteCookies("JSESSIONID")           // ELIMINO LAS COOKIES DE SESION
                        .permitAll()                            // PERMITIR LOGOUT A TODOS
                )

                // CONFIGURO LA GESTION DE SESIONES
                .sessionManagement(session -> session
                        .maximumSessions(1)                     // MAXIMO UNA SESION POR USUARIO
                        .maxSessionsPreventsLogin(false)        // PERMITIR LOGIN EXPULSANDO SESION ANTERIOR
                )

                // DESABILITO CSRF PARA H2 CONSOLE EN DESARROLLO
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )

                // CONFIGURO HEADERS DE SEGURIDAD PARA H2 CONSOLE
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

    /**
     * CONFIGURO EL ENCODER DE CONTRASEÑAS DEL SISTEMA
     *
     * DEFINO EL ALGORITMO DE CIFRADO QUE UTILIZO PARA PROTEGER LAS
     * CONTRASEÑAS DE LOS USUARIOS EN LA BASE DE DATOS. UTILIZO BCRYPT
     * QUE ES UN ALGORITMO ROBUSTO Y AMPLIAMENTE ACEPTADO PARA EL
     * CIFRADO SEGURO DE CONTRASEÑAS.
     *
     * BCRYPT INCLUYE SALT AUTOMATICO Y ES RESISTENTE A ATAQUES DE
     * FUERZA BRUTA Y RAINBOW TABLES, PROPORCIONANDO SEGURIDAD ADECUADA
     * PARA UN SISTEMA DE BIBLIOTECA DIGITAL.
     *
     * @return PasswordEncoder CONFIGURADO CON ALGORITMO BCRYPT
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // UTILIZO BCRYPT CON CONFIGURACION POR DEFECTO (STRENGTH 10)
        // PROPORCIONA UN BALANCE OPTIMO ENTRE SEGURIDAD Y RENDIMIENTO
        return new BCryptPasswordEncoder();
    }
}