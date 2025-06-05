package com.taskmanager.controller;

import com.taskmanager.model.SubscriptionPlan;
import com.taskmanager.model.User;
import com.taskmanager.model.UserSubscription;
import com.taskmanager.service.SubscriptionService;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CONTROLADOR WEB QUE MANEJA LAS PÁGINAS DE SUSCRIPCIONES Y PLANES PREMIUM
 * RENDERIZA VISTAS THYMELEAF PARA GESTIÓN DE SUSCRIPCIONES
 *
 * @author Mario Flores
 * @version 1.0
 */
@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    /**
     * MUESTRO EL CATÁLOGO DE PLANES DISPONIBLES
     * PÁGINA PRINCIPAL DEL SISTEMA DE SUSCRIPCIONES
     *
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista del catálogo de planes
     */
    @GetMapping("/plans")
    public String showPlans(Model model, Authentication auth) {
        User currentUser = getUserFromAuth(auth);

        // OBTENGO TODOS LOS PLANES ACTIVOS
        List<SubscriptionPlan> allPlans = subscriptionService.getAllActivePlans();
        SubscriptionPlan freePlan = subscriptionService.getFreePlan();
        List<SubscriptionPlan> premiumPlans = subscriptionService.getPremiumPlans();

        // OBTENGO SUSCRIPCIÓN ACTUAL DEL USUARIO
        UserSubscription currentSubscription = subscriptionService.getUserActiveSubscription(currentUser);
        SubscriptionService.SubscriptionUsageStats usageStats = subscriptionService.getUserUsageStats(currentUser);

        // DATOS PARA LA VISTA
        model.addAttribute("allPlans", allPlans);
        model.addAttribute("freePlan", freePlan);
        model.addAttribute("premiumPlans", premiumPlans);
        model.addAttribute("currentSubscription", currentSubscription);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("usageStats", usageStats);
        model.addAttribute("hasPremium", subscriptionService.hasActivePremiumSubscription(currentUser));

        return "subscription/plans";
    }

    /**
     * MUESTRO LA PÁGINA DE GESTIÓN DE SUSCRIPCIÓN ACTUAL
     * PERMITE VER DETALLES Y GESTIONAR LA SUSCRIPCIÓN ACTIVA
     *
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista de gestión de suscripción
     */
    @GetMapping("/manage")
    public String manageSubscription(Model model, Authentication auth) {
        User currentUser = getUserFromAuth(auth);

        // OBTENGO SUSCRIPCIÓN ACTUAL
        UserSubscription currentSubscription = subscriptionService.getUserActiveSubscription(currentUser);

        if (currentSubscription == null) {
            // CREAR SUSCRIPCIÓN GRATUITA SI NO TIENE NINGUNA
            currentSubscription = subscriptionService.createFreeSubscription(currentUser);
        }

        // ESTADÍSTICAS DE USO
        SubscriptionService.SubscriptionUsageStats usageStats = subscriptionService.getUserUsageStats(currentUser);

        // PRÓXIMAS RENOVACIONES SI ES PREMIUM
        boolean willExpireSoon = false;
        long daysRemaining = 0;
        if (currentSubscription.getEndDate() != null) {
            daysRemaining = currentSubscription.getDaysRemaining();
            willExpireSoon = daysRemaining <= 7 && daysRemaining > 0;
        }

        // DATOS PARA LA VISTA
        model.addAttribute("currentSubscription", currentSubscription);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("usageStats", usageStats);
        model.addAttribute("willExpireSoon", willExpireSoon);
        model.addAttribute("daysRemaining", daysRemaining);
        model.addAttribute("hasPremium", subscriptionService.hasActivePremiumSubscription(currentUser));

        return "subscription/manage";
    }

    /**
     * MUESTRO LA PÁGINA DE CHECKOUT PARA UN PLAN ESPECÍFICO
     * PREPARAR EL PROCESO DE PAGO
     *
     * @param planId ID del plan a contratar
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return vista de checkout o redirección si hay error
     */
    @GetMapping("/checkout/{planId}")
    public String showCheckout(@PathVariable Long planId,
                               Model model,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {

        User currentUser = getUserFromAuth(auth);

        // BUSCO EL PLAN SELECCIONADO
        Optional<SubscriptionPlan> planOpt = subscriptionService.getPlanById(planId);

        if (planOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Plan no encontrado");
            return "redirect:/subscription/plans";
        }

        SubscriptionPlan selectedPlan = planOpt.get();

        // VERIFICO QUE NO SEA EL PLAN GRATUITO
        if (selectedPlan.isFree()) {
            redirectAttributes.addFlashAttribute("errorMessage", "El plan gratuito no requiere pago");
            return "redirect:/subscription/plans";
        }

        // VERIFICO QUE NO TENGA YA ESTE PLAN
        UserSubscription currentSubscription = subscriptionService.getUserActiveSubscription(currentUser);
        if (currentSubscription != null &&
                currentSubscription.getSubscriptionPlan().getId().equals(planId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ya tienes este plan activo");
            return "redirect:/subscription/manage";
        }

        // DATOS PARA LA VISTA
        model.addAttribute("selectedPlan", selectedPlan);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentSubscription", currentSubscription);

        return "subscription/checkout";
    }

    /**
     * PROCESO LA CANCELACIÓN DE UNA SUSCRIPCIÓN PREMIUM
     * DOWNGRADE A PLAN GRATUITO
     *
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección a gestión de suscripción
     */
    @PostMapping("/cancel")
    public String cancelSubscription(Authentication auth, RedirectAttributes redirectAttributes) {
        User currentUser = getUserFromAuth(auth);

        try {
            boolean cancelled = subscriptionService.cancelPremiumSubscription(currentUser);

            if (cancelled) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Suscripción cancelada exitosamente. Has sido cambiado al plan gratuito.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No tienes una suscripción premium activa para cancelar");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al cancelar la suscripción: " + e.getMessage());
        }

        return "redirect:/subscription/manage";
    }

    /**
     * MUESTRO UNA PÁGINA DE CONFIRMACIÓN DESPUÉS DE UN PAGO EXITOSO
     * PÁGINA DE AGRADECIMIENTO Y CONFIRMACIÓN
     *
     * @param transactionRef referencia de la transacción
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista de confirmación
     */
    @GetMapping("/success")
    public String showSuccess(@RequestParam(required = false) String transactionRef,
                              Model model,
                              Authentication auth) {

        User currentUser = getUserFromAuth(auth);
        UserSubscription currentSubscription = subscriptionService.getUserActiveSubscription(currentUser);

        // DATOS PARA LA VISTA
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentSubscription", currentSubscription);
        model.addAttribute("transactionRef", transactionRef);
        model.addAttribute("hasPremium", subscriptionService.hasActivePremiumSubscription(currentUser));

        return "subscription/success";
    }

    /**
     * MUESTRO PÁGINA DE ERROR EN CASO DE PAGO FALLIDO
     *
     * @param error mensaje de error
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista de error de pago
     */
    @GetMapping("/error")
    public String showPaymentError(@RequestParam(required = false) String error,
                                   Model model,
                                   Authentication auth) {

        User currentUser = getUserFromAuth(auth);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("errorMessage", error != null ? error : "Error en el procesamiento del pago");

        return "subscription/payment-error";
    }

    /**
     * ENDPOINT PARA VERIFICAR LÍMITES ANTES DE CREAR CONTENIDO
     * USADO VÍA AJAX DESDE OTRAS PÁGINAS
     *
     * @param type tipo de contenido (tasks, locations)
     * @param auth información del usuario autenticado
     * @return datos JSON sobre límites
     */
    @GetMapping("/limits/check")
    @ResponseBody
    public Map<String, Object> checkLimits(@RequestParam String type, Authentication auth) {
        User currentUser = getUserFromAuth(auth);
        Map<String, Object> response = new HashMap<>();

        try {
            SubscriptionService.SubscriptionUsageStats stats = subscriptionService.getUserUsageStats(currentUser);

            if ("tasks".equals(type)) {
                response.put("canCreate", subscriptionService.canCreateMoreTasks(currentUser));
                response.put("current", stats.getCurrentTasks());
                response.put("max", stats.getMaxTasks());
                response.put("remaining", stats.getRemainingTasks());
            } else if ("locations".equals(type)) {
                response.put("canCreate", subscriptionService.canCreateMoreLocations(currentUser));
                response.put("current", stats.getCurrentLocations());
                response.put("max", stats.getMaxLocations());
                response.put("remaining", stats.getRemainingLocations());
            }

            response.put("isPremium", stats.isPremium());
            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * MUESTRO ESTADÍSTICAS DETALLADAS DE USO
     * PÁGINA PARA USUARIOS PREMIUM CON ANÁLISIS AVANZADO
     *
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista de estadísticas o redirección si no es premium
     */
    @GetMapping("/stats")
    public String showDetailedStats(Model model, Authentication auth) {
        User currentUser = getUserFromAuth(auth);

        // VERIFICAR QUE TENGA PREMIUM
        if (!subscriptionService.hasActivePremiumSubscription(currentUser)) {
            return "redirect:/subscription/plans";
        }

        SubscriptionService.SubscriptionUsageStats usageStats = subscriptionService.getUserUsageStats(currentUser);
        UserSubscription currentSubscription = subscriptionService.getUserActiveSubscription(currentUser);

        // DATOS PARA VISTA PREMIUM
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("usageStats", usageStats);
        model.addAttribute("currentSubscription", currentSubscription);

        return "subscription/premium-stats";
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