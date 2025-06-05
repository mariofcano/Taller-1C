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
 * CONTROLADOR PRINCIPAL DE LA APLICACIÓN
 * MANEJA LAS RUTAS BÁSICAS COMO LOGIN, DASHBOARD Y PÁGINAS PRINCIPALES
 *
 * @author Mario Flores
 * @version 1.0
 */
@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    /**
     * PÁGINA PRINCIPAL / DASHBOARD
     * MUESTRO ESTADÍSTICAS Y RESUMEN AL USUARIO LOGUEADO
     *
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return nombre de la vista del dashboard
     */
    @GetMapping("/")
    public String dashboard(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            // OBTENGO EL USUARIO ACTUAL
            User currentUser = userService.findByUsername(auth.getName());

            if (currentUser != null) {
                // ESTADÍSTICAS DE TAREAS DEL USUARIO
                long pendingTasks = taskService.countPendingTasks(currentUser);
                long completedTasks = taskService.countCompletedTasks(currentUser);
                long totalTasks = pendingTasks + completedTasks;

                // TAREAS RECIENTES
                var recentTasks = taskService.getRecentTasks(currentUser);

                // ESTADÍSTICAS GENERALES SI ES ADMIN
                if (currentUser.isAdmin()) {
                    long totalUsers = userService.countUsersByRole(UserRole.USER) +
                            userService.countUsersByRole(UserRole.ADMIN);
                    long adminUsers = userService.countUsersByRole(UserRole.ADMIN);
                    long normalUsers = userService.countUsersByRole(UserRole.USER);

                    model.addAttribute("totalUsers", totalUsers);
                    model.addAttribute("adminUsers", adminUsers);
                    model.addAttribute("normalUsers", normalUsers);
                    model.addAttribute("isAdmin", true);
                } else {
                    model.addAttribute("isAdmin", false);
                }

                // PASO TODOS LOS DATOS A LA VISTA
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("pendingTasks", pendingTasks);
                model.addAttribute("completedTasks", completedTasks);
                model.addAttribute("totalTasks", totalTasks);
                model.addAttribute("recentTasks", recentTasks);

                // CALCULO PORCENTAJE DE COMPLETADO
                double completionPercentage = totalTasks > 0 ?
                        (double) completedTasks / totalTasks * 100 : 0;
                model.addAttribute("completionPercentage", Math.round(completionPercentage));

                return "users/index"; // DASHBOARD
            }
        }

        // SI NO ESTÁ LOGUEADO, REDIRIJO AL LOGIN
        return "redirect:/login";
    }

    /**
     * PÁGINA DE LOGIN
     *
     * @param model objeto para pasar datos a la vista
     * @return nombre de la vista de login
     */
    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    /**
     * PÁGINA DE REGISTRO
     *
     * @param model objeto para pasar datos a la vista
     * @return nombre de la vista de registro
     */
    @GetMapping("/register")
    public String register(Model model) {
        return "users/register";
    }

    /**
     * PÁGINA DE CONTACTO
     *
     * @param model objeto para pasar datos a la vista
     * @return nombre de la vista de contacto
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        return "users/contact";
    }
}