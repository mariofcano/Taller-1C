package com.taskmanager.controller;

import com.taskmanager.model.User;
import com.taskmanager.model.UserRole;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * CONTROLADOR QUE MANEJA TODAS LAS OPERACIONES WEB DE USUARIOS
 *
 * @author Mario Flores
 * @version 1.0
 */
@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * MUESTRO LA LISTA DE TODOS LOS USUARIOS (CATÁLOGO)
     *
     * @param model objeto para pasar datos a la vista
     * @return nombre de la vista del catálogo
     */
    @GetMapping("/catalog")
    public String catalog(Model model) {
        List<User> users = userService.getAllUsers();
        long totalUsers = users.size();
        long adminCount = userService.countUsersByRole(UserRole.ADMIN);
        long userCount = userService.countUsersByRole(UserRole.USER);

        model.addAttribute("users", users);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("userCount", userCount);

        return "users/catalog";
    }

    /**
     * MUESTRO EL FORMULARIO PARA CREAR UN NUEVO USUARIO
     *
     * @param model objeto para pasar datos a la vista
     * @return nombre de la vista del formulario
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", UserRole.values());
        return "users/register";
    }

    /**
     * PROCESO EL FORMULARIO DE CREACIÓN DE USUARIO
     *
     * @param username nombre de usuario
     * @param email correo electrónico
     * @param password contraseña
     * @param role rol del usuario
     * @param redirectAttributes para mensajes flash
     * @return redirección al catálogo
     */
    @PostMapping("/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam UserRole role,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(username, email, password, role);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear usuario: " + e.getMessage());
        }

        return "redirect:/users/catalog";
    }

    /**
     * MUESTRO EL FORMULARIO PARA EDITAR UN USUARIO
     *
     * @param id identificador del usuario
     * @param model objeto para pasar datos a la vista
     * @return nombre de la vista del formulario
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            model.addAttribute("roles", UserRole.values());
            return "users/edit";
        }
        return "redirect:/users/catalog";
    }

    /**
     * PROCESO EL FORMULARIO DE EDICIÓN DE USUARIO
     *
     * @param id identificador del usuario
     * @param username nuevo nombre de usuario
     * @param email nuevo email
     * @param role nuevo rol
     * @param redirectAttributes para mensajes flash
     * @return redirección al catálogo
     */
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String email,
                             @RequestParam UserRole role,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, username, email, role);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar usuario: " + e.getMessage());
        }

        return "redirect:/users/catalog";
    }

    /**
     * ELIMINO UN USUARIO
     *
     * @param id identificador del usuario
     * @param redirectAttributes para mensajes flash
     * @return redirección al catálogo
     */
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (userService.deleteUser(id)) {
                redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar usuario: " + e.getMessage());
        }

        return "redirect:/users/catalog";
    }

    /**
     * BUSCO USUARIOS POR TEXTO
     *
     * @param query texto de búsqueda
     * @param model objeto para pasar datos a la vista
     * @return nombre de la vista de resultados
     */
    @GetMapping("/search")
    public String searchUsers(@RequestParam String query, Model model) {
        List<User> users = userService.searchUsers(query);
        model.addAttribute("users", users);
        model.addAttribute("searchQuery", query);
        model.addAttribute("searchResults", true);

        return "users/search-results";
    }

    /**
     * HABILITO O DESHABILITO UN USUARIO
     *
     * @param id identificador del usuario
     * @param enabled nuevo estado
     * @param redirectAttributes para mensajes flash
     * @return redirección al catálogo
     */
    @PostMapping("/toggle/{id}")
    public String toggleUser(@PathVariable Long id,
                             @RequestParam boolean enabled,
                             RedirectAttributes redirectAttributes) {
        try {
            if (userService.toggleUserEnabled(id, enabled)) {
                String status = enabled ? "habilitado" : "deshabilitado";
                redirectAttributes.addFlashAttribute("successMessage", "Usuario " + status + " exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cambiar estado: " + e.getMessage());
        }

        return "redirect:/users/catalog";
    }
}