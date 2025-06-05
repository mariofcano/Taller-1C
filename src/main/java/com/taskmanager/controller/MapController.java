package com.taskmanager.controller;

import com.taskmanager.model.TaskLocation;
import com.taskmanager.model.User;
import com.taskmanager.service.TaskLocationService;
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
 * CONTROLADOR WEB QUE MANEJA LAS PÁGINAS HTML DE MAPAS Y UBICACIONES
 * RENDERIZA VISTAS THYMELEAF PARA LA INTERFAZ DE USUARIO
 * PROPORCIONA NAVEGACIÓN Y FORMULARIOS PARA GESTIÓN DE UBICACIONES
 *
 * @author Mario Flores
 * @version 1.0
 */
@Controller
@RequestMapping("/maps")
public class MapController {

    @Autowired
    private TaskLocationService taskLocationService;

    @Autowired
    private UserService userService;

    /**
     * MUESTRO LA PÁGINA PRINCIPAL DE MAPAS CON TODAS LAS UBICACIONES
     * INCLUYE GOOGLE MAPS INTERACTIVO Y ESTADÍSTICAS DEL USUARIO
     *
     * @param model objeto para pasar datos a la vista Thymeleaf
     * @param auth información del usuario autenticado
     * @return nombre de la plantilla a renderizar
     */
    @GetMapping
    public String mapIndex(Model model, Authentication auth) {
        // OBTENGO EL USUARIO ACTUAL DESDE LA SESIÓN
        User currentUser = getUserFromAuth(auth);

        // CARGO TODAS LAS UBICACIONES DEL USUARIO
        List<TaskLocation> locations = taskLocationService.getAllLocationsByUser(currentUser);

        // CALCULO ESTADÍSTICAS PARA MOSTRAR EN LA VISTA
        long activeCount = taskLocationService.countActiveLocations(currentUser);
        long inactiveCount = taskLocationService.countInactiveLocations(currentUser);
        long totalCount = activeCount + inactiveCount;

        // OBTENGO LAS UBICACIONES MÁS RECIENTES
        List<TaskLocation> recentLocations = taskLocationService.getRecentLocations(currentUser);

        // PASO LOS DATOS A LA VISTA THYMELEAF
        model.addAttribute("locations", locations);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("recentLocations", recentLocations);
        model.addAttribute("currentUser", currentUser);

        return "maps/index";
    }

    /**
     * MUESTRO EL FORMULARIO PARA CREAR UNA NUEVA UBICACIÓN
     * INCLUYE MAPA INTERACTIVO PARA SELECCIONAR COORDENADAS
     *
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return nombre de la vista del formulario de creación
     */
    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication auth) {
        // CREO UNA UBICACIÓN VACÍA PARA EL FORMULARIO
        model.addAttribute("location", new TaskLocation());

        // PASO EL USUARIO ACTUAL PARA PERSONALIZACIÓN
        User currentUser = getUserFromAuth(auth);
        model.addAttribute("currentUser", currentUser);

        return "maps/create";
    }

    /**
     * PROCESO EL FORMULARIO DE CREACIÓN DE UBICACIÓN
     * MANEJA DATOS ENVIADOS DESDE EL FORMULARIO HTML
     *
     * @param name nombre de la nueva ubicación
     * @param description descripción de la ubicación
     * @param latitude coordenada de latitud GPS
     * @param longitude coordenada de longitud GPS
     * @param address dirección física opcional
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección a la página principal de mapas
     */
    @PostMapping("/create")
    public String createLocation(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam Double latitude,
                                 @RequestParam Double longitude,
                                 @RequestParam(required = false) String address,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {

        // OBTENGO EL USUARIO ACTUAL
        User currentUser = getUserFromAuth(auth);

        try {
            // CREO LA NUEVA UBICACIÓN USANDO EL SERVICIO
            taskLocationService.createLocation(name, description, latitude, longitude, address, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Ubicación creada exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos inválidos: " + e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la ubicación");
        }

        return "redirect:/maps";
    }

    /**
     * MUESTRO EL FORMULARIO PARA EDITAR UNA UBICACIÓN EXISTENTE
     * INCLUYE MAPA CON LA UBICACIÓN ACTUAL PRE-CARGADA
     *
     * @param id identificador de la ubicación a editar
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return nombre de la vista del formulario o redirección si hay error
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication auth) {
        User currentUser = getUserFromAuth(auth);
        Optional<TaskLocation> locationOpt = taskLocationService.getLocationById(id);

        // VERIFICO QUE LA UBICACIÓN EXISTE Y PERTENECE AL USUARIO
        if (locationOpt.isPresent()) {
            TaskLocation location = locationOpt.get();

            // VERIFICO PERTENENCIA AL USUARIO
            if (location.getUser().getId().equals(currentUser.getId())) {
                model.addAttribute("location", location);
                model.addAttribute("currentUser", currentUser);
                return "maps/edit";
            } else {
                // SI NO ES SUYA, LO REDIRIJO CON ERROR
                return "redirect:/maps?error=unauthorized";
            }
        }

        // SI NO EXISTE, LO REDIRIJO
        return "redirect:/maps?error=notfound";
    }

    /**
     * PROCESO EL FORMULARIO DE EDICIÓN DE UBICACIÓN
     * ACTUALIZA UNA UBICACIÓN EXISTENTE CON NUEVOS DATOS
     *
     * @param id identificador de la ubicación
     * @param name nuevo nombre
     * @param description nueva descripción
     * @param latitude nueva latitud
     * @param longitude nueva longitud
     * @param address nueva dirección
     * @param active nuevo estado activo
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección a la página principal
     */
    @PostMapping("/edit/{id}")
    public String updateLocation(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam Double latitude,
                                 @RequestParam Double longitude,
                                 @RequestParam(required = false) String address,
                                 @RequestParam(required = false) Boolean active,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = getUserFromAuth(auth);

        try {
            // SI NO VIENE EL CHECKBOX ACTIVO MARCADO, ES FALSE
            if (active == null) active = false;

            // ACTUALIZO LA UBICACIÓN
            TaskLocation updatedLocation = taskLocationService.updateLocation(
                    id, name, description, latitude, longitude, address, active, currentUser
            );

            if (updatedLocation != null) {
                redirectAttributes.addFlashAttribute("successMessage", "Ubicación actualizada exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Ubicación no encontrada o sin permisos");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos inválidos: " + e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar la ubicación");
        }

        return "redirect:/maps";
    }

    /**
     * CAMBIO EL ESTADO ACTIVO/INACTIVO DE UNA UBICACIÓN VÍA FORMULARIO
     * MÉTODO RÁPIDO PARA ACTIVAR O DESACTIVAR UBICACIONES
     *
     * @param id identificador de la ubicación
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección a la página principal
     */
    @PostMapping("/toggle/{id}")
    public String toggleLocationStatus(@PathVariable Long id,
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        User currentUser = getUserFromAuth(auth);

        try {
            boolean success = taskLocationService.toggleLocationActive(id, currentUser);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Estado de ubicación cambiado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Ubicación no encontrada o sin permisos");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cambiar el estado");
        }

        return "redirect:/maps";
    }

    /**
     * ELIMINO UNA UBICACIÓN PERMANENTEMENTE
     * OPERACIÓN IRREVERSIBLE QUE REQUIERE CONFIRMACIÓN
     *
     * @param id identificador de la ubicación a eliminar
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección a la página principal
     */
    @PostMapping("/delete/{id}")
    public String deleteLocation(@PathVariable Long id,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = getUserFromAuth(auth);

        try {
            boolean success = taskLocationService.deleteLocation(id, currentUser);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Ubicación eliminada exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Ubicación no encontrada o sin permisos");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la ubicación");
        }

        return "redirect:/maps";
    }

    /**
     * BUSCO UBICACIONES POR TEXTO EN NOMBRE O DESCRIPCIÓN
     * MUESTRA RESULTADOS FILTRADOS EN LA PÁGINA PRINCIPAL
     *
     * @param query texto a buscar
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista con resultados de la búsqueda
     */
    @GetMapping("/search")
    public String searchLocations(@RequestParam("q") String query,
                                  Model model,
                                  Authentication auth) {
        User currentUser = getUserFromAuth(auth);

        // BUSCO UBICACIONES QUE CONTENGAN EL TEXTO
        List<TaskLocation> locations = taskLocationService.searchLocations(query, currentUser);

        // CALCULO ESTADÍSTICAS DE LOS RESULTADOS
        long activeCount = locations.stream().mapToLong(loc -> loc.getActive() ? 1 : 0).sum();
        long inactiveCount = locations.size() - activeCount;

        // PASO LOS RESULTADOS A LA VISTA
        model.addAttribute("locations", locations);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("totalCount", locations.size());
        model.addAttribute("searchQuery", query);
        model.addAttribute("searchResults", true);
        model.addAttribute("currentUser", currentUser);

        return "maps/index";
    }

    /**
     * FILTRO UBICACIONES POR ESTADO ACTIVO/INACTIVO
     * MUESTRA SOLO UBICACIONES DEL ESTADO SELECCIONADO
     *
     * @param status estado a filtrar (active, inactive, all)
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista con ubicaciones filtradas
     */
    @GetMapping("/filter")
    public String filterLocations(@RequestParam String status,
                                  Model model,
                                  Authentication auth) {
        User currentUser = getUserFromAuth(auth);
        List<TaskLocation> locations;

        // FILTRO SEGÚN EL ESTADO SOLICITADO
        switch (status.toLowerCase()) {
            case "active":
                locations = taskLocationService.getActiveLocationsByUser(currentUser);
                break;
            case "inactive":
                locations = taskLocationService.getAllLocationsByUser(currentUser)
                        .stream()
                        .filter(loc -> !loc.getActive())
                        .toList();
                break;
            default:
                locations = taskLocationService.getAllLocationsByUser(currentUser);
                break;
        }

        // CALCULO ESTADÍSTICAS
        long activeCount = locations.stream().mapToLong(loc -> loc.getActive() ? 1 : 0).sum();
        long inactiveCount = locations.size() - activeCount;

        // PASO LOS DATOS A LA VISTA
        model.addAttribute("locations", locations);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("totalCount", locations.size());
        model.addAttribute("currentFilter", status);
        model.addAttribute("currentUser", currentUser);

        return "maps/index";
    }

    /**
     * MÉTODO AUXILIAR PARA OBTENER EL USUARIO DESDE LA AUTENTICACIÓN
     * CENTRALIZA LA LÓGICA DE OBTENCIÓN DEL USUARIO LOGUEADO
     *
     * @param auth objeto de autenticación de Spring Security
     * @return el usuario logueado
     */
    private User getUserFromAuth(Authentication auth) {
        String username = auth.getName();
        return userService.findByUsername(username);
    }
}