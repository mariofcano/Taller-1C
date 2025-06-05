package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * CONTROLADOR QUE MANEJA TODAS LAS OPERACIONES WEB DE TAREAS
 * RECIBE LAS PETICIONES HTTP Y DEVUELVE LAS VISTAS CORRESPONDIENTES
 *
 * @author Mario Flores
 * @version 1.0
 */
@Controller
@RequestMapping("/tasks")
public class TaskController {

    // SERVICIOS QUE NECESITO PARA LA LÓGICA DE NEGOCIO
    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    /**
     * MUESTRO LA LISTA DE TODAS LAS TAREAS DEL USUARIO LOGUEADO
     *
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return nombre de la vista a renderizar
     */
    @GetMapping
    public String listTasks(Model model, Authentication auth) {
        // OBTENGO EL USUARIO ACTUAL DESDE LA SESIÓN
        User currentUser = getUserFromAuth(auth);

        // CARGO TODAS SUS TAREAS
        List<Task> tasks = taskService.getAllTasksByUser(currentUser);

        // CALCULO ESTADÍSTICAS PARA MOSTRAR EN LA VISTA
        long pendingCount = taskService.countPendingTasks(currentUser);
        long completedCount = taskService.countCompletedTasks(currentUser);

        // PASO LOS DATOS A LA VISTA
        model.addAttribute("tasks", tasks);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalTasks", tasks.size());

        return "tasks/list";
    }

    /**
     * MUESTRO EL FORMULARIO PARA CREAR UNA NUEVA TAREA
     *
     * @param model objeto para pasar datos a la vista
     * @return nombre de la vista del formulario
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // CREO UNA TAREA VACÍA PARA EL FORMULARIO
        model.addAttribute("task", new Task());
        return "tasks/create";
    }

    /**
     * PROCESO EL FORMULARIO DE CREACIÓN DE TAREA
     *
     * @param title título de la nueva tarea
     * @param description descripción de la tarea
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección a la lista de tareas
     */
    @PostMapping("/create")
    public String createTask(@RequestParam String title,
                             @RequestParam String description,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {

        // OBTENGO EL USUARIO ACTUAL
        User currentUser = getUserFromAuth(auth);

        try {
            // CREO LA TAREA
            taskService.createTask(title, description, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Tarea creada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la tarea");
        }

        return "redirect:/tasks";
    }

    /**
     * MUESTRO EL FORMULARIO PARA EDITAR UNA TAREA EXISTENTE
     *
     * @param id identificador de la tarea a editar
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return nombre de la vista del formulario o redirección si hay error
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication auth) {
        User currentUser = getUserFromAuth(auth);
        Optional<Task> taskOpt = taskService.getTaskById(id);

        // VERIFICO QUE LA TAREA EXISTE Y PERTENECE AL USUARIO
        if (taskOpt.isPresent() && taskOpt.get().getUser().getId().equals(currentUser.getId())) {
            model.addAttribute("task", taskOpt.get());
            return "tasks/edit";
        }

        // SI NO EXISTE O NO ES SUYA, LO REDIRIJO
        return "redirect:/tasks";
    }

    /**
     * PROCESO EL FORMULARIO DE EDICIÓN DE TAREA
     *
     * @param id identificador de la tarea
     * @param title nuevo título
     * @param description nueva descripción
     * @param completed nuevo estado de completado
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección a la lista de tareas
     */
    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam(required = false) Boolean completed,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {

        User currentUser = getUserFromAuth(auth);
        Optional<Task> taskOpt = taskService.getTaskById(id);

        // VERIFICO QUE LA TAREA EXISTE Y ES DEL USUARIO
        if (taskOpt.isPresent() && taskOpt.get().getUser().getId().equals(currentUser.getId())) {
            try {
                // SI NO VIENE EL CHECKBOX MARCADO, ES FALSE
                if (completed == null) completed = false;

                taskService.updateTask(id, title, description, completed);
                redirectAttributes.addFlashAttribute("successMessage", "Tarea actualizada exitosamente");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar la tarea");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Tarea no encontrada");
        }

        return "redirect:/tasks";
    }

    /**
     * CAMBIO EL ESTADO DE COMPLETADO DE UNA TAREA VÍA AJAX
     *
     * @param id identificador de la tarea
     * @param auth información del usuario autenticado
     * @return redirección a la lista
     */
    @PostMapping("/toggle/{id}")
    public String toggleTaskStatus(@PathVariable Long id, Authentication auth) {
        User currentUser = getUserFromAuth(auth);
        Optional<Task> taskOpt = taskService.getTaskById(id);

        // VERIFICO QUE LA TAREA EXISTE Y ES DEL USUARIO
        if (taskOpt.isPresent() && taskOpt.get().getUser().getId().equals(currentUser.getId())) {
            taskService.toggleTaskCompleted(id);
        }

        return "redirect:/tasks";
    }

    /**
     * ELIMINO UNA TAREA
     *
     * @param id identificador de la tarea a eliminar
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección a la lista
     */
    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {

        User currentUser = getUserFromAuth(auth);
        Optional<Task> taskOpt = taskService.getTaskById(id);

        // VERIFICO QUE LA TAREA EXISTE Y ES DEL USUARIO
        if (taskOpt.isPresent() && taskOpt.get().getUser().getId().equals(currentUser.getId())) {
            try {
                taskService.deleteTask(id);
                redirectAttributes.addFlashAttribute("successMessage", "Tarea eliminada exitosamente");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la tarea");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Tarea no encontrada");
        }

        return "redirect:/tasks";
    }

    /**
     * BUSCO TAREAS POR TÍTULO
     *
     * @param query texto a buscar
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista con resultados de la búsqueda
     */
    @GetMapping("/search")
    public String searchTasks(@RequestParam String query, Model model, Authentication auth) {
        User currentUser = getUserFromAuth(auth);

        // BUSCO TAREAS QUE CONTENGAN EL TEXTO
        List<Task> tasks = taskService.searchTasksByTitle(query, currentUser);

        // PASO LOS RESULTADOS A LA VISTA
        model.addAttribute("tasks", tasks);
        model.addAttribute("searchQuery", query);
        model.addAttribute("searchResults", true);

        return "tasks/list";
    }

    /**
     * FILTRO TAREAS POR ESTADO
     *
     * @param status estado a filtrar (pending, completed, all)
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista con tareas filtradas
     */
    @GetMapping("/filter")
    public String filterTasks(@RequestParam String status, Model model, Authentication auth) {
        User currentUser = getUserFromAuth(auth);
        List<Task> tasks;

        // FILTRO SEGÚN EL ESTADO SOLICITADO
        switch (status.toLowerCase()) {
            case "pending":
                tasks = taskService.getPendingTasksByUser(currentUser);
                break;
            case "completed":
                tasks = taskService.getCompletedTasksByUser(currentUser);
                break;
            default:
                tasks = taskService.getAllTasksByUser(currentUser);
                break;
        }

        // PASO LOS DATOS A LA VISTA
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentFilter", status);

        return "tasks/list";
    }

    /**
     * MÉTODO AUXILIAR PARA OBTENER EL USUARIO DESDE LA AUTENTICACIÓN
     *
     * @param auth objeto de autenticación de Spring Security
     * @return el usuario logueado
     */
    private User getUserFromAuth(Authentication auth) {
        String username = auth.getName();
        return userService.findByUsername(username);
    }
}