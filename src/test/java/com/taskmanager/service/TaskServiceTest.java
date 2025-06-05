package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test unitario para TaskService donde pruebo todas las funcionalidades
 * del servicio de gestión de tareas que he implementado.
 *
 * Utilizo Mockito para simular las dependencias y así testear
 * únicamente la lógica de mi TaskService sin depender de la base de datos.
 *
 * @author Mario Flores
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    /**
     * Mock del repositorio de tareas que utilizo para simular
     * las operaciones de base de datos sin conectar realmente.
     * Esto me permite controlar exactamente qué datos devuelve
     * y así testear diferentes escenarios.
     */
    @Mock
    private TaskRepository taskRepository;

    /**
     * Instancia real de TaskService donde inyecto automáticamente
     * mis mocks. Esta es la clase que estoy testeando y quiero
     * verificar que funciona correctamente.
     */
    @InjectMocks
    private TaskService taskService;

    /**
     * Test donde verifico que puedo crear una tarea correctamente.
     *
     * Este es mi test más básico donde compruebo que cuando llamo
     * al método createTask de mi servicio, se crea una tarea con
     * los datos que yo proporciono y se guarda en el repositorio.
     *
     * Utilizo el patrón AAA (Arrange-Act-Assert) que me gusta
     * porque hace los tests muy claros y fáciles de entender.
     */
    @Test
    void shouldCreateTaskSuccessfully() {
        // ARRANGE - Aquí preparo todos los datos que necesito para mi test
        // Creo un usuario mock que será el propietario de la tarea
        User user = new User();
        user.setId(1L);
        // Le asigno un ID para simular que viene de la base de datos

        // Creo la tarea que espero que me devuelva el repositorio
        Task expectedTask = new Task("Test Task", "Description", user);
        // Configuro mi mock para que cuando guarde cualquier tarea, me devuelva esta
        when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);

        // ACT - Aquí ejecuto el método que quiero testear
        // Llamo al método createTask de mi servicio con los parámetros de prueba
        Task result = taskService.createTask("Test Task", "Description", user);

        // ASSERT - Aquí verifico que el resultado es el que espero
        // Primero me aseguro de que el resultado no sea null
        assertNotNull(result, "El resultado no debería ser null - mi servicio debe devolver una tarea");

        // Verifico que el título de la tarea creada sea correcto
        assertEquals("Test Task", result.getTitle(),
                "El título de la tarea debe ser exactamente el que yo pasé como parámetro");

        // Verifico que la descripción se haya guardado correctamente
        assertEquals("Description", result.getDescription(),
                "La descripción debe coincidir con la que proporcioné");

        // Verifico que el usuario asignado sea el correcto
        assertEquals(user, result.getUser(),
                "El usuario propietario debe ser el que yo especifiqué");

        // Verifico que las tareas nuevas se crean como pendientes por defecto
        assertFalse(result.getCompleted(),
                "Las tareas nuevas que creo siempre deben empezar como pendientes");
    }

    /**
     * Test donde verifico que mi servicio maneja correctamente
     * los casos donde intento crear una tarea con datos inválidos.
     *
     * Este test me asegura de que mi aplicación es robusta
     * y no se rompe cuando recibe datos incorrectos.
     */
    @Test
    void shouldHandleInvalidDataWhenCreatingTask() {
        // ARRANGE - Preparo un escenario con datos inválidos
        User user = new User();
        user.setId(1L);

        // ACT & ASSERT - Verifico que se lance excepción con título null
        assertThrows(IllegalArgumentException.class, () -> {
            // Intento crear una tarea sin título - esto debe fallar
            taskService.createTask(null, "Description", user);
        }, "Mi servicio debe lanzar excepción cuando el título es null");

        // Verifico que se lance excepción con título vacío
        assertThrows(IllegalArgumentException.class, () -> {
            // Intento crear una tarea con título vacío - esto también debe fallar
            taskService.createTask("", "Description", user);
        }, "Mi servicio debe lanzar excepción cuando el título está vacío");

        // Verifico que se lance excepción sin usuario
        assertThrows(IllegalArgumentException.class, () -> {
            // Intento crear una tarea sin usuario - esto es inválido
            taskService.createTask("Valid Title", "Description", null);
        }, "Mi servicio debe lanzar excepción cuando no hay usuario propietario");
    }

    /**
     * Test donde verifico que puedo contar correctamente las tareas pendientes
     * de un usuario específico.
     *
     * Esta funcionalidad es importante para mi dashboard donde muestro
     * estadísticas al usuario sobre sus tareas.
     */
    @Test
    void shouldCountPendingTasksCorrectly() {
        // ARRANGE - Preparo el escenario de test
        User user = new User();
        user.setId(1L);

        // Configuro mi mock para devolver 5 tareas pendientes
        long expectedPendingCount = 5L;
        when(taskRepository.countPendingTasksByUser(user)).thenReturn(expectedPendingCount);

        // ACT - Ejecuto el método que quiero testear
        long actualCount = taskService.countPendingTasks(user);

        // ASSERT - Verifico que el resultado sea correcto
        assertEquals(expectedPendingCount, actualCount,
                "El contador de tareas pendientes debe devolver exactamente lo que está en la BD");
    }

    /**
     * Test donde verifico que puedo alternar correctamente el estado
     * de completado de una tarea existente.
     *
     * Esta es una funcionalidad clave de mi aplicación donde los usuarios
     * pueden marcar tareas como completadas o volver a marcarlas como pendientes.
     */
    @Test
    void shouldToggleTaskCompletedStatus() {
        // ARRANGE - Preparo una tarea existente
        Long taskId = 1L;
        User user = new User();
        user.setId(1L);

        // Creo una tarea que inicialmente está pendiente
        Task existingTask = new Task("Test Task", "Description", user);
        existingTask.setId(taskId);
        existingTask.setCompleted(false); // Inicialmente pendiente

        // Configuro mis mocks
        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // ACT - Ejecuto el toggle
        boolean result = taskService.toggleTaskCompleted(taskId);

        // ASSERT - Verifico que el estado cambió correctamente
        assertTrue(result, "El método debe devolver true indicando que el toggle fue exitoso");
        assertTrue(existingTask.getCompleted(), "La tarea debe estar marcada como completada después del toggle");
    }
}