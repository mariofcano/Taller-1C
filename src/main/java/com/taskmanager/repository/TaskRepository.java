package com.taskmanager.repository;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * REPOSITORIO PARA MANEJAR LAS TAREAS EN LA BASE DE DATOS
 *
 * @author Mario Flores
 * @version 1.0
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * BUSCO TODAS LAS TAREAS DE UN USUARIO ESPECÍFICO
     *
     * @param user el usuario propietario
     * @return lista de tareas del usuario
     */
    List<Task> findByUser(User user);

    /**
     * BUSCO TAREAS POR ESTADO DE COMPLETADO Y USUARIO
     *
     * @param completed true para completadas, false para pendientes
     * @param user el usuario propietario
     * @return lista filtrada de tareas
     */
    List<Task> findByCompletedAndUser(Boolean completed, User user);

    /**
     * BUSCO TAREAS QUE CONTENGAN UN TEXTO EN EL TÍTULO
     *
     * @param title texto a buscar en el título
     * @param user el usuario propietario
     * @return tareas que coincidan con la búsqueda
     */
    List<Task> findByTitleContainingIgnoreCaseAndUser(String title, User user);

    /**
     * CUENTO CUÁNTAS TAREAS PENDIENTES TIENE UN USUARIO
     *
     * @param user el usuario
     * @return número de tareas pendientes
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.completed = false")
    long countPendingTasksByUser(@Param("user") User user);

    /**
     * CUENTO CUÁNTAS TAREAS COMPLETADAS TIENE UN USUARIO
     *
     * @param user el usuario
     * @return número de tareas completadas
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.completed = true")
    long countCompletedTasksByUser(@Param("user") User user);

    /**
     * OBTENGO LAS ÚLTIMAS TAREAS CREADAS POR UN USUARIO
     *
     * @param user el usuario
     * @return las 5 tareas más recientes
     */
    @Query("SELECT t FROM Task t WHERE t.user = :user ORDER BY t.createdAt DESC")
    List<Task> findTop5ByUserOrderByCreatedAtDesc(@Param("user") User user);
}