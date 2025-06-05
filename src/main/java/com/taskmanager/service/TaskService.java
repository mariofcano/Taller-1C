package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SERVICIO QUE MANEJA TODA LA LÓGICA DE NEGOCIO DE LAS TAREAS
 * INCLUYE VALIDACIONES DE LÍMITES SEGÚN EL PLAN DE SUSCRIPCIÓN
 *
 * @author Mario Flores
 * @version 1.0
 */
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * OBTENGO TODAS LAS TAREAS DE UN USUARIO
     * @param user el usuario propietario
     * @return lista de todas sus tareas
     */
    public List<Task> getAllTasksByUser(User user) {
        return taskRepository.findByUser(user);
    }

    /**
     * BUSCO UNA TAREA POR SU ID
     * @param id identificador de la tarea
     * @return la tarea si existe, null si no
     */
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * GUARDO UNA TAREA NUEVA O ACTUALIZO UNA EXISTENTE
     * @param task la tarea a guardar
     * @return la tarea guardada
     */
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    /**
     * CREO UNA TAREA NUEVA CON LOS DATOS BÁSICOS
     * INCLUYE VALIDACIÓN DE LÍMITES SEGÚN EL PLAN DE SUSCRIPCIÓN
     *
     * @param title título de la tarea
     * @param description descripción
     * @param user usuario propietario
     * @return la tarea creada
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws RuntimeException si se excede el límite del plan
     */
    public Task createTask(String title, String description, User user) {
        // VALIDACIONES BÁSICAS
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título de la tarea no puede estar vacío");
        }

        if (user == null) {
            throw new IllegalArgumentException("El usuario propietario es obligatorio");
        }

        // VALIDACIÓN DE LÍMITES DEL PLAN DE SUSCRIPCIÓN
        if (!subscriptionService.canCreateMoreTasks(user)) {
            // OBTENGO INFORMACIÓN DEL LÍMITE PARA EL MENSAJE DE ERROR
            SubscriptionService.SubscriptionUsageStats stats = subscriptionService.getUserUsageStats(user);

            String errorMessage = String.format(
                    "Has alcanzado el límite de tareas de tu plan (%d/%d). " +
                            "Upgrade a Premium para crear tareas ilimitadas.",
                    stats.getCurrentTasks(),
                    stats.getMaxTasks()
            );

            throw new RuntimeException(errorMessage);
        }

        // SI PASA TODAS LAS VALIDACIONES, CREAR LA TAREA
        Task task = new Task(title, description, user);
        return taskRepository.save(task);
    }

    /**
     * ACTUALIZO UNA TAREA EXISTENTE
     * @param id identificador de la tarea
     * @param title nuevo título
     * @param description nueva descripción
     * @param completed nuevo estado
     * @return la tarea actualizada o null si no existe
     */
    public Task updateTask(Long id, String title, String description, Boolean completed) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setTitle(title);
            task.setDescription(description);
            task.setCompleted(completed);
            return taskRepository.save(task);
        }
        return null;
    }

    /**
     * CAMBIO EL ESTADO DE COMPLETADO DE UNA TAREA
     * @param id identificador de la tarea
     * @return true si se cambió, false si no existe
     */
    public boolean toggleTaskCompleted(Long id) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setCompleted(!task.getCompleted());
            taskRepository.save(task);
            return true;
        }
        return false;
    }

    /**
     * ELIMINO UNA TAREA
     * @param id identificador de la tarea a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * OBTENGO SOLO LAS TAREAS PENDIENTES DE UN USUARIO
     * @param user el usuario
     * @return lista de tareas no completadas
     */
    public List<Task> getPendingTasksByUser(User user) {
        return taskRepository.findByCompletedAndUser(false, user);
    }

    /**
     * OBTENGO SOLO LAS TAREAS COMPLETADAS DE UN USUARIO
     * @param user el usuario
     * @return lista de tareas completadas
     */
    public List<Task> getCompletedTasksByUser(User user) {
        return taskRepository.findByCompletedAndUser(true, user);
    }

    /**
     * BUSCO TAREAS POR TÍTULO
     * @param title texto a buscar
     * @param user usuario propietario
     * @return tareas que contengan el texto
     */
    public List<Task> searchTasksByTitle(String title, User user) {
        return taskRepository.findByTitleContainingIgnoreCaseAndUser(title, user);
    }

    /**
     * CUENTO LAS TAREAS PENDIENTES DE UN USUARIO
     * @param user el usuario
     * @return número de tareas pendientes
     */
    public long countPendingTasks(User user) {
        return taskRepository.countPendingTasksByUser(user);
    }

    /**
     * CUENTO LAS TAREAS COMPLETADAS DE UN USUARIO
     * @param user el usuario
     * @return número de tareas completadas
     */
    public long countCompletedTasks(User user) {
        return taskRepository.countCompletedTasksByUser(user);
    }

    /**
     * OBTENGO LAS TAREAS MÁS RECIENTES DE UN USUARIO
     * @param user el usuario
     * @return las 5 tareas más nuevas
     */
    public List<Task> getRecentTasks(User user) {
        return taskRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    /**
     * VERIFICO SI UN USUARIO PUEDE CREAR MÁS TAREAS
     * MÉTODO DE CONVENIENCIA QUE DELEGA AL SUBSCRIPTION SERVICE
     *
     * @param user el usuario a verificar
     * @return true si puede crear más tareas, false si ya alcanzó el límite
     */
    public boolean canUserCreateMoreTasks(User user) {
        return subscriptionService.canCreateMoreTasks(user);
    }

    /**
     * OBTENGO INFORMACIÓN SOBRE LOS LÍMITES DE TAREAS DEL USUARIO
     * ÚTIL PARA MOSTRAR EN LA UI
     *
     * @param user el usuario
     * @return estadísticas de uso de tareas
     */
    public SubscriptionService.SubscriptionUsageStats getTaskUsageStats(User user) {
        return subscriptionService.getUserUsageStats(user);
    }
}