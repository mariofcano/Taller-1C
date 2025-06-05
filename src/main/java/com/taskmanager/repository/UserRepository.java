package com.taskmanager.repository;

import com.taskmanager.model.User;
import com.taskmanager.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO PARA MANEJAR LOS USUARIOS EN LA BASE DE DATOS
 *
 * @author Mario Flores
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * BUSCO UN USUARIO POR SU NOMBRE DE USUARIO
     *
     * @param username el nombre de usuario
     * @return el usuario si existe
     */
    Optional<User> findByUsername(String username);

    /**
     * BUSCO UN USUARIO POR SU EMAIL
     *
     * @param email el correo electrónico
     * @return el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * VERIFICO SI EXISTE UN USUARIO CON ESE USERNAME
     *
     * @param username el nombre de usuario
     * @return true si existe
     */
    boolean existsByUsername(String username);

    /**
     * VERIFICO SI EXISTE UN USUARIO CON ESE EMAIL
     *
     * @param email el correo electrónico
     * @return true si existe
     */
    boolean existsByEmail(String email);

    /**
     * BUSCO USUARIOS POR ROL
     *
     * @param role el rol a buscar
     * @return lista de usuarios con ese rol
     */
    List<User> findByRole(UserRole role);

    /**
     * BUSCO USUARIOS HABILITADOS
     *
     * @param enabled true para habilitados, false para deshabilitados
     * @return lista de usuarios filtrados
     */
    List<User> findByEnabled(Boolean enabled);

    /**
     * BUSCO USUARIOS QUE CONTENGAN UN TEXTO EN USERNAME O EMAIL
     *
     * @param searchTerm texto a buscar
     * @return usuarios que coincidan
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    List<User> findByUsernameContainingOrEmailContaining(@Param("searchTerm") String searchTerm);

    /**
     * CUENTO CUÁNTOS USUARIOS HAY POR ROL
     *
     * @param role el rol a contar
     * @return número de usuarios con ese rol
     */
    long countByRole(UserRole role);

    /**
     * OBTENGO TODOS LOS USUARIOS ORDENADOS POR FECHA DE CREACIÓN
     *
     * @return usuarios ordenados por fecha
     */
    List<User> findAllByOrderByCreatedAtDesc();
}