package com.biblioteca.digital.controller;

import com.biblioteca.digital.model.User;
import com.biblioteca.digital.model.UserRole;
import com.biblioteca.digital.service.UserService;
import com.biblioteca.digital.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Optional;

/**
 * CONTROLADOR PARA LA GESTION COMPLETA DE USUARIOS
 *
 * ESTE CONTROLADOR MANEJA TODAS LAS OPERACIONES CRUD Y DE ADMINISTRACION
 * DE USUARIOS EN EL SISTEMA DE BIBLIOTECA DIGITAL. INCLUYE FUNCIONALIDADES
 * PARA LISTAR, CREAR, EDITAR, VER DETALLES Y GESTIONAR USUARIOS.
 *
 * IMPLEMENTA CONTROL DE ACCESO BASADO EN ROLES PARA GARANTIZAR QUE SOLO
 * USUARIOS AUTORIZADOS PUEDAN REALIZAR OPERACIONES ADMINISTRATIVAS.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-27
 */
@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoanService loanService;

    /**
     * LISTA TODOS LOS USUARIOS CON PAGINACION
     *
     * MUESTRA UNA TABLA PAGINADA DE TODOS LOS USUARIOS DEL SISTEMA
     * CON OPCIONES DE BUSQUEDA Y FILTRADO. SOLO ACCESIBLE PARA
     * ADMINISTRADORES Y BIBLIOTECARIOS.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "createdAt") String sortBy,
                            @RequestParam(defaultValue = "desc") String sortDir,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) UserRole role,
                            @RequestParam(required = false) Boolean active,
                            Model model) {
        try {
            // CONFIGURACION DE PAGINACION Y ORDENAMIENTO
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // BUSQUEDA Y FILTRADO
            Page<User> usersPage;
            if (search != null && !search.trim().isEmpty()) {
                // BUSQUEDA GLOBAL POR TERMINO
                usersPage = userService.searchUsers(search).stream()
                        .collect(java.util.stream.Collectors.toList())
                        .stream()
                        .skip((long) page * size)
                        .limit(size)
                        .collect(java.util.stream.Collectors.toList())
                        .stream()
                        .collect(java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toList(),
                                list -> new org.springframework.data.domain.PageImpl<>(
                                        list, pageable, userService.searchUsers(search).size())
                        ));
            } else {
                // LISTADO NORMAL CON FILTROS
                usersPage = getAllUsersWithFilters(pageable, role, active);
            }

            // ESTADISTICAS PARA EL DASHBOARD
            long totalUsers = userService.findAllActiveUsers().size();
            long todayRegistrations = userService.countUsersRegisteredToday();
            long usersWithActiveLoans = userService.findUsersWithActiveLoans().size();
            long usersWithOverdueLoans = userService.findUsersWithOverdueLoans().size();

            // DATOS PARA LA VISTA
            model.addAttribute("usersPage", usersPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usersPage.getTotalPages());
            model.addAttribute("totalElements", usersPage.getTotalElements());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
            model.addAttribute("search", search);
            model.addAttribute("selectedRole", role);
            model.addAttribute("selectedActive", active);

            // ESTADISTICAS
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("todayRegistrations", todayRegistrations);
            model.addAttribute("usersWithActiveLoans", usersWithActiveLoans);
            model.addAttribute("usersWithOverdueLoans", usersWithOverdueLoans);

            // DATOS PARA FILTROS
            model.addAttribute("userRoles", UserRole.values());

            return "users/list";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la lista de usuarios: " + e.getMessage());
            return "users/list";
        }
    }

    /**
     * MUESTRA EL FORMULARIO PARA CREAR UN NUEVO USUARIO
     */
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("userRoles", UserRole.values());
        model.addAttribute("pageTitle", "Crear Nuevo Usuario");
        return "users/create";
    }

    /**
     * PROCESA LA CREACION DE UN NUEVO USUARIO
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@Valid @ModelAttribute User user,
                             BindingResult result,
                             @RequestParam String confirmPassword,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Principal principal) {
        try {
            // VALIDACIONES
            if (result.hasErrors()) {
                model.addAttribute("userRoles", UserRole.values());
                model.addAttribute("pageTitle", "Crear Nuevo Usuario");
                return "users/create";
            }

            // VERIFICAR CONTRASEÑAS COINCIDEN
            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Las contraseñas no coinciden");
                model.addAttribute("userRoles", UserRole.values());
                model.addAttribute("pageTitle", "Crear Nuevo Usuario");
                return "users/create";
            }

            // VERIFICAR DISPONIBILIDAD
            if (!userService.isUsernameAvailable(user.getUsername())) {
                model.addAttribute("error", "El nombre de usuario ya está en uso");
                model.addAttribute("userRoles", UserRole.values());
                model.addAttribute("pageTitle", "Crear Nuevo Usuario");
                return "users/create";
            }

            if (!userService.isEmailAvailable(user.getEmail())) {
                model.addAttribute("error", "El email ya está registrado");
                model.addAttribute("userRoles", UserRole.values());
                model.addAttribute("pageTitle", "Crear Nuevo Usuario");
                return "users/create";
            }

            // CREAR USUARIO
            User createdUser = userService.registerUser(
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getFullName()
            );

            // ACTUALIZAR PERFIL CON DATOS ADICIONALES
            if (user.getPhone() != null || user.getAddress() != null) {
                userService.updateUserProfile(
                        createdUser.getId(),
                        user.getFullName(),
                        user.getPhone(),
                        user.getAddress()
                );
            }

            // ASIGNAR ROL SI ES DIFERENTE AL DEFAULT
            if (user.getRole() != null && user.getRole() != UserRole.USER) {
                User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null) {
                    userService.changeUserRole(createdUser.getId(), user.getRole(), currentUser.getId());
                }
            }

            redirectAttributes.addFlashAttribute("success",
                    "Usuario '" + createdUser.getUsername() + "' creado exitosamente");
            return "redirect:/users";

        } catch (Exception e) {
            model.addAttribute("error", "Error al crear usuario: " + e.getMessage());
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "Crear Nuevo Usuario");
            return "users/create";
        }
    }

    /**
     * MUESTRA LOS DETALLES DE UN USUARIO ESPECIFICO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or (hasRole('USER') and #id == authentication.principal.id)")
    public String viewUser(@PathVariable Long id, Model model, Principal principal) {
        try {
            Optional<User> userOptional = userService.findUserById(id);
            if (!userOptional.isPresent()) {
                model.addAttribute("error", "Usuario no encontrado");
                return "redirect:/users";
            }

            User user = userOptional.get();

            // OBTENER ESTADISTICAS DEL USUARIO
            long activeLoans = loanService.findActiveLoansForUser(id).size();
            long totalLoans = loanService.findLoanHistoryForUser(id, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
            long overdueLoans = loanService.findOverdueLoansForUser(id).size();

            model.addAttribute("user", user);
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("totalLoans", totalLoans);
            model.addAttribute("overdueLoans", overdueLoans);
            model.addAttribute("pageTitle", "Detalles de " + user.getFullName());

            return "users/view";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar usuario: " + e.getMessage());
            return "redirect:/users";
        }
    }

    /**
     * MUESTRA EL FORMULARIO PARA EDITAR UN USUARIO
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == authentication.principal.id)")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Optional<User> userOptional = userService.findUserById(id);
            if (!userOptional.isPresent()) {
                model.addAttribute("error", "Usuario no encontrado");
                return "redirect:/users";
            }

            User user = userOptional.get();
            model.addAttribute("user", user);
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "Editar " + user.getFullName());

            return "users/edit";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar usuario: " + e.getMessage());
            return "redirect:/users";
        }
    }

    /**
     * PROCESA LA ACTUALIZACION DE UN USUARIO
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == authentication.principal.id)")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute User userForm,
                             BindingResult result,
                             @RequestParam(required = false) String newPassword,
                             @RequestParam(required = false) String confirmPassword,
                             @RequestParam(required = false) String currentPassword,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Principal principal) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("userRoles", UserRole.values());
                model.addAttribute("pageTitle", "Editar Usuario");
                return "users/edit";
            }

            // ACTUALIZAR PERFIL BASICO
            User updatedUser = userService.updateUserProfile(
                    id,
                    userForm.getFullName(),
                    userForm.getPhone(),
                    userForm.getAddress()
            );

            // CAMBIAR CONTRASEÑA SI SE PROPORCIONA
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (currentPassword == null || currentPassword.trim().isEmpty()) {
                    model.addAttribute("error", "Debe proporcionar la contraseña actual");
                    model.addAttribute("userRoles", UserRole.values());
                    return "users/edit";
                }

                if (!newPassword.equals(confirmPassword)) {
                    model.addAttribute("error", "Las nuevas contraseñas no coinciden");
                    model.addAttribute("userRoles", UserRole.values());
                    return "users/edit";
                }

                userService.changePassword(id, currentPassword, newPassword);
            }

            // CAMBIAR ROL SI ES ADMIN Y SE ESPECIFICA
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);
            if (currentUser != null && currentUser.getRole().canManageUsers() &&
                    userForm.getRole() != null && !userForm.getRole().equals(updatedUser.getRole())) {
                userService.changeUserRole(id, userForm.getRole(), currentUser.getId());
            }

            redirectAttributes.addFlashAttribute("success", "Usuario actualizado exitosamente");
            return "redirect:/users/" + id;

        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar usuario: " + e.getMessage());
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "Editar Usuario");
            return "users/edit";
        }
    }

    /**
     * ACTIVA O DESACTIVA UN USUARIO
     */
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserStatus(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        try {
            Optional<User> userOptional = userService.findUserById(id);
            if (!userOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/users";
            }

            User user = userOptional.get();
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);

            if (currentUser != null) {
                Boolean newStatus = !user.getActive();
                userService.toggleUserActiveStatus(id, newStatus, currentUser.getId());

                String statusText = newStatus ? "activado" : "desactivado";
                redirectAttributes.addFlashAttribute("success",
                        "Usuario '" + user.getUsername() + "' " + statusText + " exitosamente");
            }

            return "redirect:/users";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al cambiar estado del usuario: " + e.getMessage());
            return "redirect:/users";
        }
    }

    /**
     * MUESTRA EL PERFIL DEL USUARIO ACTUAL
     */
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        try {
            Optional<User> userOptional = userService.findUserByUsername(principal.getName());
            if (!userOptional.isPresent()) {
                model.addAttribute("error", "Usuario no encontrado");
                return "redirect:/";
            }

            User user = userOptional.get();

            // ESTADISTICAS DEL USUARIO
            long activeLoans = loanService.findActiveLoansForUser(user.getId()).size();
            long totalLoans = loanService.findLoanHistoryForUser(user.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
            long overdueLoans = loanService.findOverdueLoansForUser(user.getId()).size();

            model.addAttribute("user", user);
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("totalLoans", totalLoans);
            model.addAttribute("overdueLoans", overdueLoans);
            model.addAttribute("pageTitle", "Mi Perfil");

            return "users/profile";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar perfil: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * METODO AUXILIAR PARA OBTENER USUARIOS CON FILTROS
     */
    private Page<User> getAllUsersWithFilters(Pageable pageable, UserRole role, Boolean active) {
        // IMPLEMENTACION BASICA - EN UNA VERSION REAL USARIA SPECIFICATIONS O CRITERIA API
        if (role != null && active != null) {
            return convertListToPage(userService.findUsersByRole(role)
                    .stream()
                    .filter(u -> u.getActive().equals(active))
                    .collect(java.util.stream.Collectors.toList()), pageable);
        } else if (role != null) {
            return convertListToPage(userService.findUsersByRole(role), pageable);
        } else if (active != null) {
            return convertListToPage(
                    active ? userService.findAllActiveUsers() :
                            userService.findAllActiveUsers().stream()
                                    .filter(u -> !u.getActive())
                                    .collect(java.util.stream.Collectors.toList()),
                    pageable);
        } else {
            return convertListToPage(userService.findAllActiveUsers(), pageable);
        }
    }

    /**
     * CONVIERTE UNA LISTA A PAGE PARA PAGINACION
     */
    private Page<User> convertListToPage(java.util.List<User> users, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());

        java.util.List<User> pageContent = users.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, users.size());
    }
}