package com.biblioteca.digital.controller;

import com.biblioteca.digital.model.User;
import com.biblioteca.digital.model.UserRole;
import com.biblioteca.digital.service.UserService;
import com.biblioteca.digital.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * CONTROLADOR PARA GESTION ADMINISTRATIVA DE USUARIOS DEL SISTEMA
 *
 * IMPLEMENTO TODAS LAS OPERACIONES CRUD PARA LA ADMINISTRACION DE USUARIOS
 * EN EL SISTEMA DE BIBLIOTECA DIGITAL. ESTE CONTROLADOR MANEJA LAS PANTALLAS
 * ADMINISTRATIVAS QUE PERMITEN A ADMINISTRADORES Y BIBLIOTECARIOS GESTIONAR
 * LAS CUENTAS DE USUARIO DEL SISTEMA.
 *
 * PROPORCIONO FUNCIONALIDADES COMPLETAS DE CREACION, LECTURA, ACTUALIZACION
 * Y CONTROL DE ESTADO DE USUARIOS, CON VALIDACIONES DE PERMISOS Y MANEJO
 * ROBUSTO DE ERRORES PARA GARANTIZAR LA INTEGRIDAD DEL SISTEMA.
 *
 * TODAS LAS OPERACIONES REQUIEREN PERMISOS ADMINISTRATIVOS Y SE REGISTRAN
 * ADECUADAMENTE PARA AUDITORIA DEL SISTEMA.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-27
 *
 * @see User
 * @see UserService
 * @see LoanService
 * @see org.springframework.stereotype.Controller
 */
@Controller
@RequestMapping("/admin")
public class UserController {

    /**
     * SERVICIO DE NEGOCIO PARA GESTION DE USUARIOS
     *
     * INYECTO EL SERVICIO QUE CONTIENE TODA LA LOGICA DE NEGOCIO
     * RELACIONADA CON USUARIOS. UTILIZO ESTE SERVICIO PARA TODAS
     * LAS OPERACIONES DE CREACION, ACTUALIZACION Y CONSULTA DE USUARIOS.
     */
    @Autowired
    private UserService userService;

    /**
     * SERVICIO DE PRESTAMOS PARA ESTADISTICAS DE USUARIO
     *
     * INYECTO EL SERVICIO DE PRESTAMOS PARA OBTENER INFORMACION
     * RELACIONADA CON LA ACTIVIDAD DE CADA USUARIO, COMO PRESTAMOS
     * ACTIVOS Y HISTORIAL DE ACTIVIDAD.
     */
    @Autowired
    private LoanService loanService;

    /**
     * MUESTRA LA LISTA DE TODOS LOS USUARIOS DEL SISTEMA
     *
     * RENDERIZO LA PANTALLA PRINCIPAL DE GESTION DE USUARIOS QUE MUESTRA
     * UNA TABLA CON TODOS LOS USUARIOS REGISTRADOS EN EL SISTEMA.
     * INCLUYO ESTADISTICAS AGREGADAS PARA PROPORCIONAR UNA VISION GENERAL
     * DE LA DISTRIBUCION DE USUARIOS POR ROLES.
     *
     * IMPLEMENTO MANEJO DE ERRORES ROBUSTO PARA GARANTIZAR QUE LA PANTALLA
     * SE MUESTRE INCLUSO SI HAY PROBLEMAS AL CARGAR ALGUNOS DATOS.
     * PROPORCIONO VALORES POR DEFECTO PARA MANTENER LA FUNCIONALIDAD.
     *
     * @param model OBJETO MODEL PARA PASAR DATOS A LA VISTA THYMELEAF
     * @return STRING NOMBRE DE LA PLANTILLA A RENDERIZAR
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        try {
            // OBTENGO TODOS LOS USUARIOS ACTIVOS DEL SISTEMA
            List<User> users = userService.findAllActiveUsers();

            // CALCULO ESTADISTICAS POR ROLES PARA EL DASHBOARD
            // UTILIZO STREAMS PARA CONTAR EFICIENTEMENTE POR CATEGORIA
            long totalUsers = users.size();
            long adminUsers = users.stream()
                    .mapToLong(u -> u.getRole() == UserRole.ADMIN ? 1 : 0)
                    .sum();
            long librarianUsers = users.stream()
                    .mapToLong(u -> u.getRole() == UserRole.LIBRARIAN ? 1 : 0)
                    .sum();
            long regularUsers = users.stream()
                    .mapToLong(u -> u.getRole() == UserRole.USER ? 1 : 0)
                    .sum();

            // PASO TODOS LOS DATOS A LA VISTA
            model.addAttribute("users", users);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("adminUsers", adminUsers);
            model.addAttribute("librarianUsers", librarianUsers);
            model.addAttribute("regularUsers", regularUsers);
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "Gestión de Usuarios");

            System.out.println("CARGANDO LISTA DE USUARIOS - Total: " + totalUsers);

        } catch (Exception e) {
            // MANEJO ERRORES GRACEFULMENTE SIN ROMPER LA INTERFAZ
            System.err.println("ERROR AL CARGAR USUARIOS: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "ERROR AL CARGAR USUARIOS: " + e.getMessage());
            model.addAttribute("users", List.of());
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("adminUsers", 0L);
            model.addAttribute("librarianUsers", 0L);
            model.addAttribute("regularUsers", 0L);
            model.addAttribute("userRoles", UserRole.values());
        }

        return "users/list";
    }

    /**
     * MUESTRA EL FORMULARIO PARA CREAR UN NUEVO USUARIO
     *
     * RENDERIZO LA PANTALLA DE CREACION DE USUARIO CON UN FORMULARIO
     * VACIO LISTO PARA RECIBIR LOS DATOS DEL NUEVO USUARIO.
     * PROPORCIONO TODOS LOS ROLES DISPONIBLES PARA SELECCION.
     *
     * @param model OBJETO MODEL PARA PASAR DATOS A LA VISTA
     * @return STRING NOMBRE DE LA PLANTILLA DEL FORMULARIO DE CREACION
     */
    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        // CREO UN OBJETO USER VACIO PARA EL FORMULARIO
        model.addAttribute("user", new User());
        model.addAttribute("userRoles", UserRole.values());
        model.addAttribute("pageTitle", "Crear Nuevo Usuario");
        model.addAttribute("formAction", "/admin/users/create");
        model.addAttribute("submitButtonText", "Crear Usuario");

        System.out.println("MOSTRANDO FORMULARIO DE CREACION DE USUARIO");

        return "users/create";
    }

    /**
     * PROCESA LA CREACION DE UN NUEVO USUARIO
     *
     * RECIBO LOS DATOS DEL FORMULARIO DE CREACION Y PROCESO LA CREACION
     * DEL NUEVO USUARIO EN EL SISTEMA. APLICO TODAS LAS VALIDACIONES
     * NECESARIAS Y MANEJO ERRORES DE FORMA APROPIADA.
     *
     * UTILIZO RedirectAttributes PARA ENVIAR MENSAJES DE EXITO O ERROR
     * A LA PANTALLA DE DESTINO SIN PERDER LA INFORMACION AL REDIRIGIR.
     *
     * @param username NOMBRE DE USUARIO UNICO
     * @param email DIRECCION DE CORREO ELECTRONICO
     * @param password CONTRASEÑA EN TEXTO PLANO
     * @param fullName NOMBRE COMPLETO DEL USUARIO
     * @param phone TELEFONO OPCIONAL
     * @param address DIRECCION OPCIONAL
     * @param role ROL A ASIGNAR AL USUARIO
     * @param redirectAttributes OBJETO PARA MENSAJES DE REDIRECCION
     * @return STRING URL DE REDIRECCION DESPUES DEL PROCESAMIENTO
     */
    @PostMapping("/users/create")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam("fullName") String fullName,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "address", required = false) String address,
                             @RequestParam("role") UserRole role,
                             RedirectAttributes redirectAttributes) {
        try {
            // REGISTRO EL NUEVO USUARIO CON DATOS BASICOS
            User newUser = userService.registerUser(username, email, password, fullName);

            // ACTUALIZO CAMPOS OPCIONALES SI SE PROPORCIONARON
            if (phone != null && !phone.trim().isEmpty()) {
                newUser.setPhone(phone);
            }
            if (address != null && !address.trim().isEmpty()) {
                newUser.setAddress(address);
            }

            // OBTENGO EL USUARIO ACTUAL PARA OPERACIONES ADMINISTRATIVAS
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> currentUser = userService.findUserByUsername(auth.getName());

            if (currentUser.isPresent()) {
                // ASIGNO EL ROL ESPECIFICADO SI ES DIFERENTE DE USER
                if (role != UserRole.USER) {
                    userService.changeUserRole(newUser.getId(), role, currentUser.get().getId());
                }
            }

            // MENSAJE DE EXITO
            redirectAttributes.addFlashAttribute("successMessage",
                    "USUARIO '" + username + "' CREADO EXITOSAMENTE");

            System.out.println("USUARIO CREADO EXITOSAMENTE: " + username + " - ROL: " + role);

        } catch (Exception e) {
            // MANEJO ERRORES Y PROPORCIONO RETROALIMENTACION CLARA
            System.err.println("ERROR AL CREAR USUARIO: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("errorMessage",
                    "ERROR AL CREAR USUARIO: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * MUESTRA EL FORMULARIO PARA EDITAR UN USUARIO EXISTENTE
     *
     * CARGO LOS DATOS DEL USUARIO ESPECIFICADO Y RENDERIZO EL FORMULARIO
     * DE EDICION PRE-POBLADO CON LA INFORMACION ACTUAL.
     * INCLUYO ESTADISTICAS ADICIONALES DEL USUARIO COMO PRESTAMOS ACTIVOS.
     *
     * @param id IDENTIFICADOR DEL USUARIO A EDITAR
     * @param model OBJETO MODEL PARA PASAR DATOS A LA VISTA
     * @return STRING NOMBRE DE LA PLANTILLA DE EDICION
     */
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        try {
            // BUSCO EL USUARIO POR ID
            Optional<User> userOptional = userService.findUserById(id);

            if (!userOptional.isPresent()) {
                model.addAttribute("error", "USUARIO NO ENCONTRADO CON ID: " + id);
                return "redirect:/admin/users";
            }

            User user = userOptional.get();

            // CARGO DATOS DEL USUARIO Y CONFIGURACION DEL FORMULARIO
            model.addAttribute("user", user);
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "EDITAR USUARIO: " + user.getUsername());
            model.addAttribute("formAction", "/admin/users/edit/" + id);
            model.addAttribute("submitButtonText", "Actualizar Usuario");

            // OBTENGO ESTADISTICAS ADICIONALES DEL USUARIO
            try {
                long activeLoans = loanService.findActiveLoansForUser(id).size();
                model.addAttribute("activeLoans", activeLoans);

                // VERIFICO SI TIENE PRESTAMOS VENCIDOS
                long overdueLoans = loanService.findOverdueLoansForUser(id).size();
                model.addAttribute("overdueLoans", overdueLoans);

            } catch (Exception e) {
                System.err.println("ERROR AL CARGAR ESTADISTICAS DE USUARIO: " + e.getMessage());
                model.addAttribute("activeLoans", 0L);
                model.addAttribute("overdueLoans", 0L);
            }

            System.out.println("MOSTRANDO FORMULARIO DE EDICION PARA USUARIO: " + user.getUsername());

        } catch (Exception e) {
            System.err.println("ERROR AL CARGAR USUARIO PARA EDICION: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "ERROR AL CARGAR USUARIO: " + e.getMessage());
            return "redirect:/admin/users";
        }

        return "users/edit";
    }

    /**
     * PROCESA LA ACTUALIZACION DE UN USUARIO EXISTENTE
     *
     * RECIBO LOS DATOS MODIFICADOS DEL FORMULARIO DE EDICION Y ACTUALIZO
     * EL USUARIO EN EL SISTEMA. MANEJO CAMBIOS DE ROL, ESTADO ACTIVO
     * Y INFORMACION PERSONAL DE FORMA COORDINADA.
     *
     * @param id IDENTIFICADOR DEL USUARIO A ACTUALIZAR
     * @param fullName NUEVO NOMBRE COMPLETO
     * @param phone NUEVO TELEFONO
     * @param address NUEVA DIRECCION
     * @param role NUEVO ROL A ASIGNAR
     * @param active NUEVO ESTADO DE ACTIVACION
     * @param redirectAttributes OBJETO PARA MENSAJES DE REDIRECCION
     * @return STRING URL DE REDIRECCION DESPUES DE LA ACTUALIZACION
     */
    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam("fullName") String fullName,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "address", required = false) String address,
                             @RequestParam("role") UserRole role,
                             @RequestParam(value = "active") Boolean active,
                             RedirectAttributes redirectAttributes) {
        try {
            // OBTENGO EL USUARIO ACTUAL PARA VALIDAR PERMISOS
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> currentUser = userService.findUserByUsername(auth.getName());

            if (!currentUser.isPresent()) {
                throw new RuntimeException("USUARIO ACTUAL NO ENCONTRADO EN LA SESION");
            }

            // ACTUALIZO EL PERFIL DEL USUARIO
            userService.updateUserProfile(id, fullName, phone, address);

            // OBTENGO EL USUARIO ACTUALIZADO PARA VERIFICAR CAMBIOS
            Optional<User> userToUpdate = userService.findUserById(id);
            if (!userToUpdate.isPresent()) {
                throw new RuntimeException("USUARIO A ACTUALIZAR NO ENCONTRADO");
            }

            User user = userToUpdate.get();

            // CAMBIO EL ROL SI ES DIFERENTE AL ACTUAL
            if (user.getRole() != role) {
                userService.changeUserRole(id, role, currentUser.get().getId());
                System.out.println("ROL CAMBIADO PARA USUARIO " + user.getUsername() + ": " +
                        user.getRole() + " -> " + role);
            }

            // CAMBIO EL ESTADO ACTIVO SI ES DIFERENTE AL ACTUAL
            if (!user.getActive().equals(active)) {
                userService.toggleUserActiveStatus(id, active, currentUser.get().getId());
                System.out.println("ESTADO CAMBIADO PARA USUARIO " + user.getUsername() + ": " +
                        user.getActive() + " -> " + active);
            }

            // MENSAJE DE EXITO
            redirectAttributes.addFlashAttribute("successMessage",
                    "USUARIO '" + user.getUsername() + "' ACTUALIZADO EXITOSAMENTE");

            System.out.println("USUARIO ACTUALIZADO EXITOSAMENTE: " + user.getUsername());

        } catch (Exception e) {
            // MANEJO ERRORES CON RETROALIMENTACION DETALLADA
            System.err.println("ERROR AL ACTUALIZAR USUARIO: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("errorMessage",
                    "ERROR AL ACTUALIZAR USUARIO: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * ACTIVA O DESACTIVA UN USUARIO MEDIANTE AJAX
     *
     * PROPORCIONO UN ENDPOINT PARA CAMBIOS RAPIDOS DE ESTADO DE USUARIO
     * SIN NECESIDAD DE CARGAR EL FORMULARIO COMPLETO DE EDICION.
     * UTILIZO ESTE METODO PARA TOGGLES RAPIDOS EN LA INTERFAZ.
     *
     * @param id IDENTIFICADOR DEL USUARIO
     * @param active NUEVO ESTADO DE ACTIVACION
     * @return ResponseEntity CON EL RESULTADO DE LA OPERACION
     */
    @PostMapping("/users/{id}/toggle-status")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        try {
            // OBTENGO EL USUARIO ACTUAL PARA PERMISOS
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> currentUser = userService.findUserByUsername(auth.getName());

            if (!currentUser.isPresent()) {
                return org.springframework.http.ResponseEntity.badRequest()
                        .body("USUARIO ACTUAL NO ENCONTRADO");
            }

            // CAMBIO EL ESTADO DEL USUARIO
            User updatedUser = userService.toggleUserActiveStatus(id, active, currentUser.get().getId());

            String message = "USUARIO " + updatedUser.getUsername() +
                    " " + (active ? "ACTIVADO" : "DESACTIVADO") + " EXITOSAMENTE";

            System.out.println(message);

            return org.springframework.http.ResponseEntity.ok(message);

        } catch (Exception e) {
            System.err.println("ERROR AL CAMBIAR ESTADO DE USUARIO: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest()
                    .body("ERROR: " + e.getMessage());
        }
    }

    /**
     * MUESTRA LA VISTA DETALLADA DE UN USUARIO
     *
     * RENDERIZO UNA PANTALLA CON INFORMACION COMPLETA DEL USUARIO
     * INCLUYENDO ESTADISTICAS DE ACTIVIDAD, HISTORIAL DE PRESTAMOS
     * Y OTRA INFORMACION RELEVANTE PARA ADMINISTRADORES.
     *
     * @param id IDENTIFICADOR DEL USUARIO
     * @param model OBJETO MODEL PARA PASAR DATOS A LA VISTA
     * @return STRING NOMBRE DE LA PLANTILLA DE VISTA DETALLADA
     */
    @GetMapping("/users/view/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        try {
            // BUSCO EL USUARIO
            Optional<User> userOptional = userService.findUserById(id);

            if (!userOptional.isPresent()) {
                model.addAttribute("error", "USUARIO NO ENCONTRADO");
                return "redirect:/admin/users";
            }

            User user = userOptional.get();
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "DETALLES DE USUARIO: " + user.getUsername());

            // CARGO ESTADISTICAS COMPLETAS DEL USUARIO
            try {
                long activeLoans = loanService.findActiveLoansForUser(id).size();
                long overdueLoans = loanService.findOverdueLoansForUser(id).size();
                long totalLoans = loanService.findLoanHistoryForUser(id,
                        org.springframework.data.domain.Pageable.unpaged()).getTotalElements();

                model.addAttribute("activeLoans", activeLoans);
                model.addAttribute("overdueLoans", overdueLoans);
                model.addAttribute("totalLoans", totalLoans);

                // OBTENGO PRESTAMOS RECIENTES
                List<com.biblioteca.digital.model.Loan> recentLoans =
                        loanService.findLoanHistoryForUser(id,
                                org.springframework.data.domain.PageRequest.of(0, 5)).getContent();
                model.addAttribute("recentLoans", recentLoans);

            } catch (Exception e) {
                System.err.println("ERROR AL CARGAR ESTADISTICAS DE USUARIO: " + e.getMessage());
                model.addAttribute("activeLoans", 0L);
                model.addAttribute("overdueLoans", 0L);
                model.addAttribute("totalLoans", 0L);
                model.addAttribute("recentLoans", List.of());
            }

            System.out.println("MOSTRANDO DETALLES DEL USUARIO: " + user.getUsername());

        } catch (Exception e) {
            System.err.println("ERROR AL CARGAR DETALLES DE USUARIO: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "ERROR AL CARGAR DETALLES: " + e.getMessage());
        }

        return "users/view";
    }

    /**
     * BUSCA USUARIOS SEGUN CRITERIOS ESPECIFICOS
     *
     * PROPORCIONO FUNCIONALIDAD DE BUSQUEDA AVANZADA QUE PERMITE
     * A LOS ADMINISTRADORES ENCONTRAR USUARIOS RAPIDAMENTE USANDO
     * DIFERENTES CRITERIOS COMO NOMBRE, EMAIL O ROL.
     *
     * @param searchTerm TERMINO DE BUSQUEDA
     * @param role ROL A FILTRAR (OPCIONAL)
     * @param active ESTADO ACTIVO A FILTRAR (OPCIONAL)
     * @param model OBJETO MODEL PARA PASAR DATOS A LA VISTA
     * @return STRING NOMBRE DE LA PLANTILLA CON RESULTADOS
     */
    @GetMapping("/users/search")
    public String searchUsers(@RequestParam(value = "searchTerm", required = false) String searchTerm,
                              @RequestParam(value = "role", required = false) UserRole role,
                              @RequestParam(value = "active", required = false) Boolean active,
                              Model model) {
        try {
            List<User> users;

            // DETERMINO EL TIPO DE BUSQUEDA A REALIZAR
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                // BUSQUEDA POR TERMINO GLOBAL
                users = userService.searchUsers(searchTerm.trim());
                model.addAttribute("searchTerm", searchTerm);
                System.out.println("BUSQUEDA DE USUARIOS CON TERMINO: " + searchTerm);

            } else if (role != null) {
                // FILTRO POR ROL ESPECIFICO
                users = userService.findUsersByRole(role);
                model.addAttribute("filterRole", role);
                System.out.println("FILTRO DE USUARIOS POR ROL: " + role);

            } else if (active != null) {
                // FILTRO POR ESTADO ACTIVO
                users = userService.findAllActiveUsers().stream()
                        .filter(u -> u.getActive().equals(active))
                        .collect(java.util.stream.Collectors.toList());
                model.addAttribute("filterActive", active);
                System.out.println("FILTRO DE USUARIOS POR ESTADO: " + active);

            } else {
                // SIN FILTROS - MOSTRAR TODOS
                users = userService.findAllActiveUsers();
                System.out.println("MOSTRANDO TODOS LOS USUARIOS");
            }

            // CALCULO ESTADISTICAS DE LOS RESULTADOS
            long totalResults = users.size();
            model.addAttribute("users", users);
            model.addAttribute("totalResults", totalResults);
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("pageTitle", "BUSQUEDA DE USUARIOS");

            // MENSAJE DESCRIPTIVO DE LOS RESULTADOS
            if (totalResults == 0) {
                model.addAttribute("resultMessage", "NO SE ENCONTRARON USUARIOS CON LOS CRITERIOS ESPECIFICADOS");
            } else {
                model.addAttribute("resultMessage",
                        "SE ENCONTRARON " + totalResults + " USUARIO(S)");
            }

        } catch (Exception e) {
            System.err.println("ERROR EN BUSQUEDA DE USUARIOS: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "ERROR EN LA BUSQUEDA: " + e.getMessage());
            model.addAttribute("users", List.of());
            model.addAttribute("totalResults", 0L);
        }

        return "users/search-results";
    }

    /**
     * EXPORTA LA LISTA DE USUARIOS A FORMATO CSV
     *
     * GENERO UN ARCHIVO CSV CON LA INFORMACION DE TODOS LOS USUARIOS
     * PARA RESPALDO, ANALISIS O INTEGRACION CON OTROS SISTEMAS.
     * INCLUYO SOLO INFORMACION NO SENSIBLE.
     *
     * @param response OBJETO HttpServletResponse PARA CONFIGURAR LA DESCARGA
     */
    @GetMapping("/users/export")
    public void exportUsers(jakarta.servlet.http.HttpServletResponse response) {
        try {
            // CONFIGURO LA RESPUESTA PARA DESCARGA DE ARCHIVO
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"usuarios.csv\"");

            // OBTENGO TODOS LOS USUARIOS
            List<User> users = userService.findAllActiveUsers();

            // ESCRIBO EL ARCHIVO CSV
            java.io.PrintWriter writer = response.getWriter();

            // ESCRIBO EL ENCABEZADO
            writer.println("USERNAME,EMAIL,NOMBRE_COMPLETO,ROL,ACTIVO,TELEFONO,FECHA_REGISTRO");

            // ESCRIBO LOS DATOS DE CADA USUARIO
            for (User user : users) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().getDescription(),
                        user.getActive() ? "SI" : "NO",
                        user.getPhone() != null ? user.getPhone() : "",
                        user.getCreatedAt().toString()
                );
            }

            writer.flush();
            System.out.println("EXPORTACION DE USUARIOS COMPLETADA: " + users.size() + " REGISTROS");

        } catch (Exception e) {
            System.err.println("ERROR AL EXPORTAR USUARIOS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * MUESTRA ESTADISTICAS DETALLADAS DE USUARIOS
     *
     * RENDERIZO UNA PANTALLA CON GRAFICOS Y METRICAS AVANZADAS
     * SOBRE LA DISTRIBUCION Y ACTIVIDAD DE LOS USUARIOS EN EL SISTEMA.
     *
     * @param model OBJETO MODEL PARA PASAR DATOS A LA VISTA
     * @return STRING NOMBRE DE LA PLANTILLA DE ESTADISTICAS
     */
    @GetMapping("/users/statistics")
    public String userStatistics(Model model) {
        try {
            // OBTENGO ESTADISTICAS GENERALES
            List<Object[]> roleStats = userService.getUserStatisticsByRole();
            long usersRegisteredToday = userService.countUsersRegisteredToday();

            // USUARIOS CON PRESTAMOS ACTIVOS Y VENCIDOS
            List<User> usersWithActiveLoans = userService.findUsersWithActiveLoans();
            List<User> usersWithOverdueLoans = userService.findUsersWithOverdueLoans();

            // PASO DATOS A LA VISTA
            model.addAttribute("roleStats", roleStats);
            model.addAttribute("usersRegisteredToday", usersRegisteredToday);
            model.addAttribute("usersWithActiveLoans", usersWithActiveLoans.size());
            model.addAttribute("usersWithOverdueLoans", usersWithOverdueLoans.size());
            model.addAttribute("pageTitle", "ESTADISTICAS DE USUARIOS");

            // CALCULO PORCENTAJES PARA GRAFICOS
            long totalUsers = userService.findAllActiveUsers().size();
            if (totalUsers > 0) {
                double activeLoanPercentage = (usersWithActiveLoans.size() * 100.0) / totalUsers;
                double overdueLoanPercentage = (usersWithOverdueLoans.size() * 100.0) / totalUsers;

                model.addAttribute("activeLoanPercentage", Math.round(activeLoanPercentage * 100.0) / 100.0);
                model.addAttribute("overdueLoanPercentage", Math.round(overdueLoanPercentage * 100.0) / 100.0);
            }

            System.out.println("MOSTRANDO ESTADISTICAS DE USUARIOS");

        } catch (Exception e) {
            System.err.println("ERROR AL CARGAR ESTADISTICAS: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "ERROR AL CARGAR ESTADISTICAS: " + e.getMessage());
        }

        return "users/statistics";
    }

    /**
     * RESETEA LA CONTRASEÑA DE UN USUARIO
     *
     * PERMITO A LOS ADMINISTRADORES RESETEAR LA CONTRASEÑA DE UN USUARIO
     * CUANDO ESTE NO PUEDE ACCEDER A SU CUENTA. GENERO UNA CONTRASEÑA
     * TEMPORAL QUE EL USUARIO DEBE CAMBIAR EN SU PRIMER LOGIN.
     *
     * @param id IDENTIFICADOR DEL USUARIO
     * @param redirectAttributes OBJETO PARA MENSAJES DE REDIRECCION
     * @return STRING URL DE REDIRECCION
     */
    @PostMapping("/users/{id}/reset-password")
    public String resetUserPassword(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            // BUSCO EL USUARIO
            Optional<User> userOptional = userService.findUserById(id);

            if (!userOptional.isPresent()) {
                throw new RuntimeException("USUARIO NO ENCONTRADO");
            }

            User user = userOptional.get();

            // GENERO UNA CONTRASEÑA TEMPORAL SEGURA
            String tempPassword = generateTemporaryPassword();

            // OBTENGO EL USUARIO ACTUAL PARA LA OPERACION
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> currentUser = userService.findUserByUsername(auth.getName());

            if (!currentUser.isPresent()) {
                throw new RuntimeException("USUARIO ACTUAL NO ENCONTRADO");
            }

            // CAMBIO LA CONTRASEÑA
            userService.changePassword(id, user.getPassword(), tempPassword);

            // MENSAJE CON LA CONTRASEÑA TEMPORAL
            redirectAttributes.addFlashAttribute("successMessage",
                    "CONTRASEÑA RESETEADA PARA " + user.getUsername() +
                            ". CONTRASEÑA TEMPORAL: " + tempPassword);

            System.out.println("CONTRASEÑA RESETEADA PARA USUARIO: " + user.getUsername());

        } catch (Exception e) {
            System.err.println("ERROR AL RESETEAR CONTRASEÑA: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("errorMessage",
                    "ERROR AL RESETEAR CONTRASEÑA: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * GENERA UNA CONTRASEÑA TEMPORAL SEGURA
     *
     * CREO UNA CONTRASEÑA TEMPORAL QUE CUMPLE CON LOS REQUISITOS
     * DE SEGURIDAD DEL SISTEMA Y ES FACIL DE COMUNICAR AL USUARIO.
     *
     * @return STRING CONTRASEÑA TEMPORAL GENERADA
     */
    private String generateTemporaryPassword() {
        // CARACTERES PERMITIDOS PARA LA CONTRASEÑA TEMPORAL
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String allChars = upperCase + lowerCase + numbers;

        // GENERO UNA CONTRASEÑA DE 8 CARACTERES
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        // GARANTIZO AL MENOS UN CARACTER DE CADA TIPO
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));

        // COMPLETO CON CARACTERES ALEATORIOS
        for (int i = 3; i < 8; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // MEZCLO LOS CARACTERES
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }
}