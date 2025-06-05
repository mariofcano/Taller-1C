package com.taskmanager.service;

import com.taskmanager.model.User;
import com.taskmanager.model.UserRole;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SERVICIO QUE MANEJA TODA LA LÓGICA DE NEGOCIO DE LOS USUARIOS
 *
 * @author Mario Flores
 * @version 1.0
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * OBTENGO TODOS LOS USUARIOS
     * @return lista de todos los usuarios
     */
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * BUSCO UN USUARIO POR ID
     * @param id identificador del usuario
     * @return el usuario si existe
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * BUSCO UN USUARIO POR USERNAME
     * @param username nombre de usuario
     * @return el usuario si existe
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * BUSCO UN USUARIO POR EMAIL
     * @param email correo electrónico
     * @return el usuario si existe
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * CREO UN NUEVO USUARIO
     * @param username nombre de usuario
     * @param email correo electrónico
     * @param password contraseña sin cifrar
     * @param role rol del usuario
     * @return el usuario creado
     */
    public User createUser(String username, String email, String password, UserRole role) {
        // VERIFICO QUE NO EXISTA YA
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Ya existe un usuario con ese nombre");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        // CREO EL USUARIO
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * ACTUALIZO UN USUARIO EXISTENTE
     * @param id identificador del usuario
     * @param username nuevo nombre de usuario
     * @param email nuevo email
     * @param role nuevo rol
     * @return el usuario actualizado
     */
    public User updateUser(Long id, String username, String email, UserRole role) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // VERIFICO QUE NO EXISTAN OTROS USUARIOS CON ESE USERNAME/EMAIL
            if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
                throw new RuntimeException("Ya existe un usuario con ese nombre");
            }
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("Ya existe un usuario con ese email");
            }

            user.setUsername(username);
            user.setEmail(email);
            user.setRole(role);

            return userRepository.save(user);
        }
        return null;
    }

    /**
     * CAMBIO LA CONTRASEÑA DE UN USUARIO
     * @param id identificador del usuario
     * @param newPassword nueva contraseña sin cifrar
     * @return true si se cambió correctamente
     */
    public boolean changePassword(Long id, String newPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * HABILITO O DESHABILITO UN USUARIO
     * @param id identificador del usuario
     * @param enabled true para habilitar, false para deshabilitar
     * @return true si se cambió correctamente
     */
    public boolean toggleUserEnabled(Long id, boolean enabled) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(enabled);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * ELIMINO UN USUARIO
     * @param id identificador del usuario a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * BUSCO USUARIOS POR TEXTO
     * @param searchTerm texto a buscar en username o email
     * @return usuarios que coincidan
     */
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByUsernameContainingOrEmailContaining(searchTerm);
    }

    /**
     * OBTENGO USUARIOS POR ROL
     * @param role el rol a filtrar
     * @return usuarios con ese rol
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * CUENTO USUARIOS POR ROL
     * @param role el rol a contar
     * @return número de usuarios
     */
    public long countUsersByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    /**
     * VERIFICO SI UN USERNAME ESTÁ DISPONIBLE
     * @param username el nombre a verificar
     * @return true si está disponible
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * VERIFICO SI UN EMAIL ESTÁ DISPONIBLE
     * @param email el email a verificar
     * @return true si está disponible
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}