package com.taskmanager.config;

import com.taskmanager.model.User;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SERVICIO PERSONALIZADO PARA CARGAR USUARIOS EN SPRING SECURITY
 * CONECTA MI SISTEMA DE USUARIOS CON LA AUTENTICACIÓN DE SPRING
 *
 * @author Mario Flores
 * @version 1.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * CARGO UN USUARIO POR SU USERNAME PARA SPRING SECURITY
     * ESTE MÉTODO SE LLAMA AUTOMÁTICAMENTE AL HACER LOGIN
     *
     * @param username el nombre de usuario
     * @return los detalles del usuario para Spring Security
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // BUSCO EL USUARIO EN MI BASE DE DATOS
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // VERIFICO QUE ESTÉ HABILITADO
        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("Usuario deshabilitado: " + username);
        }

        // CONVIERTO MI USUARIO A UN OBJETO QUE ENTIENDA SPRING SECURITY
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user)
        );
    }

    /**
     * CONVIERTO EL ROL DE MI USUARIO A AUTHORITIES DE SPRING SECURITY
     *
     * @param user el usuario del sistema
     * @return lista de authorities/permisos
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // AÑADO EL ROL CON EL PREFIJO ROLE_ QUE ESPERA SPRING SECURITY
        authorities.add(new SimpleGrantedAuthority(user.getRole().getAuthority()));

        return authorities;
    }
}