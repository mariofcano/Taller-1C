package com.taskmanager.controller;

import com.taskmanager.model.User;
import com.taskmanager.model.UserRole;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador principal para el manejo de rutas básicas del sistema.
 *
 * <p>Este controlador maneja las operaciones fundamentales de navegación,
 * incluyendo el dashboard principal, login Y registro. Implemento
 * la lógica de autenticación y estadísticas del usuario autenticado.</p>
 *
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Dashboard con estadísticas de tareas del usuario</li>
 *   <li>Panel administrativo para usuarios con rol ADMIN</li>
 *   <li>Gestión de rutas de login y registro</li>
 *   <li>Redirección automática según estado de autenticación</li>
 * </ul>
 *
 * @author Mario Flores
 * @version 1.0
 * @since 2025
 */
@Controller
public class MainController {

    /**
     * Servicio para operaciones relacionadas con usuarios.
     * Utilizo este servicio para obtener información del usuario autenticado
     * y estadísticas de usuarios registrados en el sistema.
     */
    @Autowired
    private UserService userService;

    /**
     * Servicio para operaciones relacionadas con tareas.
     * Implemento este servicio para obtener estadísticas de tareas
     * del usuario y datos para el dashboard personal.
     */
    @Autowired
    private TaskService taskService;

    /**
     * Renderiza el dashboard principal del sistema con estadísticas personalizadas.
     *
     * <p>Esta función implementa la página de inicio del usuario autenticado.
     * Cargo estadísticas personales de tareas, información administrativa
     * para usuarios ADMIN, y datos para generar gráficos de progreso.</p>
     *
     * <p>Funcionalidades implementadas:</p>
     * <ul>
     *   <li>Cálculo de tareas pendientes y completadas</li>
     *   <li>Obtención de tareas recientes del usuario</li>
     *   <li>Panel administrativo con estadísticas globales</li>
     *   <li>Cálculo de porcentaje de completitud</li>
     *   <li>Redirección automática a login si no está autenticado</li>
     * </ul>
     *
     * @param model objeto Model de Spring MVC para pasar datos a la vista.
     *              Utilizo este objeto para transferir todas las estadísticas
     *              calculadas y información del usuario a la plantilla Thymeleaf.
     * @param auth objeto Authentication de Spring Security que contiene
     *             la información del usuario autenticado. Uso este parámetro
     *             para verificar el estado de autenticación y obtener el username.
     * @return String con el nombre de la vista a renderizar.
     *         Retorno "index" para el dashboard principal o "redirect:/login"
     *         si el usuario no está autenticado correctamente.
     */
    @GetMapping("/")
    public String dashboard(Model model, Authentication auth) {
        // Verifico el estado de autenticación del usuario
        if (auth != null && auth.isAuthenticated()) {
            // Obtengo el usuario actual desde el contexto de seguridad
            User currentUser = userService.findByUsername(auth.getName());

            // Valido que el usuario existe en la base de datos
            if (currentUser != null) {
                // Calculo estadísticas de tareas del usuario autenticado
                long pendingTasks = taskService.countPendingTasks(currentUser);
                long completedTasks = taskService.countCompletedTasks(currentUser);
                long totalTasks = pendingTasks + completedTasks;

                // Obtengo las tareas más recientes para mostrar en el dashboard
                var recentTasks = taskService.getRecentTasks(currentUser);

                // Genero estadísticas administrativas si el usuario tiene rol ADMIN
                if (currentUser.isAdmin()) {
                    // Calculo estadísticas globales del sistema
                    long totalUsers = userService.countUsersByRole(UserRole.USER) +
                            userService.countUsersByRole(UserRole.ADMIN);
                    long adminUsers = userService.countUsersByRole(UserRole.ADMIN);
                    long normalUsers = userService.countUsersByRole(UserRole.USER);

                    // Añado estadísticas administrativas al modelo
                    model.addAttribute("totalUsers", totalUsers);
                    model.addAttribute("adminUsers", adminUsers);
                    model.addAttribute("normalUsers", normalUsers);
                    model.addAttribute("isAdmin", true);
                } else {
                    // Marco que el usuario no tiene privilegios administrativos
                    model.addAttribute("isAdmin", false);
                }

                // Transfiero todos los datos calculados al modelo de la vista
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("pendingTasks", pendingTasks);
                model.addAttribute("completedTasks", completedTasks);
                model.addAttribute("totalTasks", totalTasks);
                model.addAttribute("recentTasks", recentTasks);

                // Calculo el porcentaje de completitud para el gráfico de progreso
                double completionPercentage = totalTasks > 0 ?
                        (double) completedTasks / totalTasks * 100 : 0;
                model.addAttribute("completionPercentage", Math.round(completionPercentage));

                // Retorno la vista del dashboard principal
                return "index";
            }
        }

        // Redirijo al login si el usuario no está autenticado
        return "redirect:/login";
    }

    /**
     * Renderiza la página de inicio de sesión del sistema.
     *
     * <p>Esta función maneja la ruta GET para mostrar el formulario de login.
     * La página incluye campos para username y password, mensajes de error
     * y enlaces de registro. Spring Security procesa automáticamente
     * el envío del formulario de login.</p>
     *
     * @param model objeto Model de Spring MVC para pasar datos a la vista.
     *              Aunque no utilizo parámetros adicionales en esta vista,
     *              mantengo la consistencia con otros métodos del controlador.
     * @return String con el nombre de la plantilla Thymeleaf a renderizar.
     *         Retorno "login" que corresponde a templates/login.html.
     */
    @GetMapping("/login")
    public String login(Model model) {
        // Retorno la vista del formulario de login
        return "login";
    }

    /**
     * Renderiza la página de registro de nuevos usuarios.
     *
     * <p>Esta función muestra el formulario para crear nuevos usuarios
     * en el sistema. Incluyo validaciones JavaScript y campos para
     * username, email, password y rol del usuario.</p>
     *
     * @param model objeto Model de Spring MVC para pasar datos a la vista.
     *              Utilizo este parámetro para mantener consistencia,
     *              aunque en esta implementación no paso datos adicionales.
     * @return String con el nombre de la plantilla Thymeleaf a renderizar.
     *         Retorno "register" que corresponde a templates/register.html.
     */
    @GetMapping("/register")
    public String register(Model model) {
        // Retorno la vista del formulario de registro
        return "register";
    }


}