package com.biblioteca.digital.config;

import com.biblioteca.digital.model.User;
import com.biblioteca.digital.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * SERVICIO PERSONALIZADO PARA AUTENTICACION DE USUARIOS CON SPRING SECURITY
 *
 * IMPLEMENTO UserDetailsService PARA INTEGRAR EL SISTEMA DE USUARIOS
 * DE LA BIBLIOTECA DIGITAL CON EL FRAMEWORK DE SEGURIDAD DE SPRING.
 * ESTA CLASE ES EL PUENTE ENTRE LA BASE DE DATOS DE USUARIOS Y SPRING SECURITY.
 *
 * PROPORCIONO AUTENTICACION FLEXIBLE QUE PERMITE LOGIN CON USERNAME O EMAIL,
 * VALIDACION DE ESTADO DE USUARIO ACTIVO Y ASIGNACION CORRECTA DE ROLES
 * PARA CONTROL DE ACCESO GRANULAR EN TODA LA APLICACION.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-27
 *
 * @see UserDetailsService
 * @see User
 * @see UserRepository
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * REPOSITORIO PARA ACCESO A DATOS DE USUARIOS
     *
     * INYECTO EL REPOSITORIO QUE ME PERMITE CONSULTAR LA BASE DE DATOS
     * PARA ENCONTRAR USUARIOS DURANTE EL PROCESO DE AUTENTICACION.
     * UTILIZO ESTE REPOSITORIO PARA BUSCAR POR USERNAME O EMAIL.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * CARGA UN USUARIO POR SU NOMBRE DE USUARIO PARA AUTENTICACION
     *
     * ESTE ES EL METODO PRINCIPAL QUE SPRING SECURITY INVOCA DURANTE
     * EL PROCESO DE LOGIN. RECIBO EL IDENTIFICADOR DEL USUARIO (QUE PUEDE
     * SER USERNAME O EMAIL) Y RETORNO UN OBJETO UserDetails QUE SPRING
     * SECURITY UTILIZA PARA VALIDAR CREDENCIALES Y ASIGNAR PERMISOS.
     *
     * IMPLEMENTO BUSQUEDA FLEXIBLE QUE PERMITE AL USUARIO HACER LOGIN
     * CON SU USERNAME O EMAIL INDISTINTAMENTE, MEJORANDO LA EXPERIENCIA
     * DE USUARIO AL NO FORZAR EL RECUERDO DE UN IDENTIFICADOR ESPECIFICO.
     *
     * VALIDO QUE EL USUARIO ESTE ACTIVO ANTES DE PERMITIR LA AUTENTICACION,
     * PROPORCIONANDO UNA CAPA ADICIONAL DE SEGURIDAD ADMINISTRATIVA.
     *
     * @param username IDENTIFICADOR DEL USUARIO (USERNAME O EMAIL)
     * @return UserDetails OBJETO CON INFORMACION DE AUTENTICACION Y AUTORIZACION
     * @throws UsernameNotFoundException SI EL USUARIO NO EXISTE O ESTA INACTIVO
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // BUSCO EL USUARIO EN LA BASE DE DATOS POR USERNAME O EMAIL
        // UTILIZO EL METODO DEL REPOSITORIO QUE PERMITE BUSQUEDA DUAL
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(username, username);

        // VERIFICO SI EL USUARIO EXISTE EN LA BASE DE DATOS
        if (!userOptional.isPresent()) {
            // LANZO EXCEPCION ESPECIFICA DE SPRING SECURITY
            // NO PROPORCIONO DETALLES ESPECIFICOS POR SEGURIDAD
            throw new UsernameNotFoundException("USUARIO NO ENCONTRADO: " + username);
        }

        // OBTENGO EL USUARIO ENCONTRADO
        User user = userOptional.get();

        // VERIFICO QUE EL USUARIO ESTE ACTIVO EN EL SISTEMA
        // ESTO PERMITE DESACTIVAR USUARIOS SIN ELIMINAR SUS DATOS
        if (!user.getActive()) {
            throw new UsernameNotFoundException("USUARIO DESACTIVADO: " + username);
        }

        // CREO LA AUTORIDAD (ROL) DEL USUARIO PARA SPRING SECURITY
        // CONVIERTO EL ENUM UserRole A GrantedAuthority CON PREFIJO "ROLE_"
        // ESTE PREFIJO ES REQUERIDO POR SPRING SECURITY PARA ROLES
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // CONSTRUYO Y RETORNO EL OBJETO UserDetails PARA SPRING SECURITY
        // UTILIZO EL BUILDER INCORPORADO PARA CREAR UN UserDetails COMPLETO
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())              // USERNAME PARA IDENTIFICACION
                .password(user.getPassword())              // CONTRASEÑA CIFRADA
                .authorities(Collections.singletonList(authority))  // LISTA DE ROLES/PERMISOS
                .accountExpired(false)                     // CUENTA NO EXPIRADA
                .accountLocked(false)                      // CUENTA NO BLOQUEADA
                .credentialsExpired(false)                 // CREDENCIALES NO EXPIRADAS
                .disabled(!user.getActive())               // ESTADO BASADO EN CAMPO ACTIVE
                .build();
    }

    /**
     * METODO UTILITARIO PARA OBTENER USUARIO ACTUAL AUTENTICADO
     *
     * PROPORCIONO UN METODO DE CONVENIENCIA QUE PUEDE SER UTILIZADO
     * POR OTROS COMPONENTES PARA OBTENER EL USUARIO ACTUALMENTE
     * AUTENTICADO SIN TENER QUE MANEJAR SPRING SECURITY DIRECTAMENTE.
     *
     * @param username USERNAME DEL USUARIO AUTENTICADO
     * @return Optional<User> USUARIO ENCONTRADO O VACIO SI NO EXISTE
     */
    public Optional<User> getCurrentUser(String username) {
        // UTILIZO EL MISMO METODO DE BUSQUEDA DUAL
        return userRepository.findByUsernameOrEmail(username, username);
    }

    /**
     * VALIDA SI UN USUARIO TIENE UN ROL ESPECIFICO
     *
     * METODO AUXILIAR QUE PERMITE VERIFICAR RAPIDAMENTE SI UN USUARIO
     * TIENE PERMISOS PARA REALIZAR CIERTAS OPERACIONES SIN CARGAR
     * TODA LA INFORMACION DE SPRING SECURITY.
     *
     * @param username USERNAME DEL USUARIO A VERIFICAR
     * @param roleName NOMBRE DEL ROL A VERIFICAR
     * @return BOOLEAN TRUE SI TIENE EL ROL, FALSE EN CASO CONTRARIO
     */
    public boolean hasRole(String username, String roleName) {
        // BUSCO EL USUARIO
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(username, username);

        // VERIFICO EXISTENCIA Y ROL
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getRole().name().equals(roleName);
        }

        return false;
    }

    /**
     * OBTIENE TODOS LOS ROLES DISPONIBLES COMO STRINGS
     *
     * METODO UTILITARIO QUE RETORNA TODOS LOS ROLES DISPONIBLES
     * EN EL SISTEMA EN FORMATO STRING PARA USO EN INTERFACES
     * Y VALIDACIONES.
     *
     * @return String[] ARRAY CON TODOS LOS NOMBRES DE ROLES
     */
    public String[] getAvailableRoles() {
        // OBTENGO TODOS LOS VALORES DEL ENUM UserRole
        return java.util.Arrays.stream(com.biblioteca.digital.model.UserRole.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}