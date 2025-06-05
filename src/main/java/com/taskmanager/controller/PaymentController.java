package com.taskmanager.controller;

import com.taskmanager.model.*;
import com.taskmanager.service.PaymentSimulatorService;
import com.taskmanager.service.SubscriptionService;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
 * CONTROLADOR QUE MANEJA EL PROCESAMIENTO DE PAGOS Y TRANSACCIONES
 * INTEGRA EL SIMULADOR DE TARJETAS CON EL SISTEMA DE SUSCRIPCIONES
 *
 * @author Mario Flores
 * @version 1.0
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentSimulatorService paymentSimulatorService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    /**
     * PROCESO EL FORMULARIO DE PAGO ENVIADO DESDE CHECKOUT
     * MANEJA DATOS DE TARJETA Y CREA LA TRANSACCIÓN
     *
     * @param planId ID del plan a pagar
     * @param cardNumber número de tarjeta
     * @param expiryMonth mes de expiración
     * @param expiryYear año de expiración
     * @param cvv código de seguridad
     * @param cardHolderName nombre del titular
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección a página de procesamiento
     */
    @PostMapping("/process")
    public String processPayment(@RequestParam Long planId,
                                 @RequestParam String cardNumber,
                                 @RequestParam String expiryMonth,
                                 @RequestParam String expiryYear,
                                 @RequestParam String cvv,
                                 @RequestParam String cardHolderName,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = getUserFromAuth(auth);

        try {
            // BUSCAR EL PLAN SELECCIONADO
            Optional<SubscriptionPlan> planOpt = subscriptionService.getPlanById(planId);

            if (planOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Plan no encontrado");
                return "redirect:/subscription/plans";
            }

            SubscriptionPlan selectedPlan = planOpt.get();

            // LIMPIAR NÚMERO DE TARJETA (REMOVER ESPACIOS)
            cardNumber = cardNumber.replaceAll("\\s", "");

            // INICIAR TRANSACCIÓN
            PaymentTransaction transaction = paymentSimulatorService.initiatePayment(
                    currentUser, selectedPlan, cardNumber, expiryMonth, expiryYear, cvv, cardHolderName
            );

            // REDIRECCIONAR A PÁGINA DE PROCESAMIENTO CON REFERENCIA
            return "redirect:/payment/processing?ref=" + transaction.getReferenceCode();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos de tarjeta inválidos: " + e.getMessage());
            return "redirect:/subscription/checkout/" + planId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el pago: " + e.getMessage());
            return "redirect:/subscription/checkout/" + planId;
        }
    }

    /**
     * MUESTRO LA PÁGINA DE PROCESAMIENTO CON ESTADO EN TIEMPO REAL
     * PÁGINA CON LOADING QUE CONSULTA EL ESTADO VÍA AJAX
     *
     * @param referenceCode código de referencia de la transacción
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista de procesamiento
     */
    @GetMapping("/processing")
    public String showProcessing(@RequestParam("ref") String referenceCode,
                                 Model model,
                                 Authentication auth) {

        User currentUser = getUserFromAuth(auth);
        PaymentTransaction transaction = paymentSimulatorService.getTransactionStatus(referenceCode);

        if (transaction == null || !transaction.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/subscription/plans";
        }

        // DATOS PARA LA VISTA
        model.addAttribute("transaction", transaction);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("referenceCode", referenceCode);

        return "payment/processing";
    }

    /**
     * ENDPOINT AJAX PARA CONSULTAR EL ESTADO DE UNA TRANSACCIÓN
     * POLLING DESDE LA PÁGINA DE PROCESAMIENTO
     *
     * @param referenceCode código de referencia
     * @param auth información del usuario autenticado
     * @return JSON con el estado actual
     */
    @GetMapping("/status/{referenceCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String referenceCode,
                                                                Authentication auth) {

        User currentUser = getUserFromAuth(auth);
        PaymentTransaction transaction = paymentSimulatorService.getTransactionStatus(referenceCode);

        Map<String, Object> response = new HashMap<>();

        if (transaction == null || !transaction.getUser().getId().equals(currentUser.getId())) {
            response.put("success", false);
            response.put("error", "Transacción no encontrada");
            return ResponseEntity.notFound().build();
        }

        // DATOS DE LA TRANSACCIÓN
        response.put("success", true);
        response.put("status", transaction.getStatus().name());
        response.put("statusDisplay", transaction.getStatus().getDisplayName());
        response.put("amount", transaction.getFormattedAmount());
        response.put("referenceCode", transaction.getReferenceCode());
        response.put("isFinished", transaction.getStatus().isFinalized());

        // SI ES EXITOSO, ACTIVAR SUSCRIPCIÓN
        if (transaction.getStatus() == PaymentStatus.COMPLETED) {
            try {
                // VERIFICAR SI YA SE ACTIVÓ LA SUSCRIPCIÓN
                UserSubscription existingSubscription = subscriptionService.getUserActiveSubscription(currentUser);

                if (existingSubscription == null ||
                        !transaction.getReferenceCode().equals(existingSubscription.getPaymentReference())) {

                    // ACTIVAR SUSCRIPCIÓN PREMIUM
                    UserSubscription newSubscription = subscriptionService.upgradeToPremium(
                            currentUser,
                            transaction.getSubscriptionPlan(),
                            transaction.getReferenceCode()
                    );

                    response.put("subscriptionActivated", true);
                    response.put("planName", newSubscription.getSubscriptionPlan().getName());
                }

                response.put("redirectUrl", "/subscription/success?transactionRef=" + referenceCode);

            } catch (Exception e) {
                response.put("subscriptionError", e.getMessage());
            }
        }

        // SI FALLÓ, INCLUIR MENSAJE DE ERROR
        if (transaction.isFailed()) {
            response.put("errorMessage", transaction.getErrorMessage());
            response.put("redirectUrl", "/subscription/error?error=" + transaction.getErrorMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * MUESTRO EL HISTORIAL DE PAGOS DEL USUARIO
     * PÁGINA CON TODAS LAS TRANSACCIONES REALIZADAS
     *
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista del historial de pagos
     */
    @GetMapping("/history")
    public String showPaymentHistory(Model model, Authentication auth) {
        User currentUser = getUserFromAuth(auth);

        // OBTENER HISTORIAL COMPLETO
        List<PaymentTransaction> transactions = paymentSimulatorService.getUserPaymentHistory(currentUser);

        // ESTADÍSTICAS
        long successfulPayments = transactions.stream()
                .mapToLong(t -> t.isSuccessful() ? 1 : 0)
                .sum();

        long failedPayments = transactions.stream()
                .mapToLong(t -> t.isFailed() ? 1 : 0)
                .sum();

        // DATOS PARA LA VISTA
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalTransactions", transactions.size());
        model.addAttribute("successfulPayments", successfulPayments);
        model.addAttribute("failedPayments", failedPayments);

        return "payment/history";
    }

    /**
     * MUESTRO INFORMACIÓN DETALLADA DE UNA TRANSACCIÓN ESPECÍFICA
     *
     * @param transactionId ID de la transacción
     * @param model objeto para pasar datos a la vista
     * @param auth información del usuario autenticado
     * @return vista de detalles de transacción
     */
    @GetMapping("/details/{transactionId}")
    public String showTransactionDetails(@PathVariable Long transactionId,
                                         Model model,
                                         Authentication auth) {

        User currentUser = getUserFromAuth(auth);

        // BUSCAR TRANSACCIÓN (aquí asumo que tienes un método en el servicio)
        // PaymentTransaction transaction = paymentSimulatorService.getTransactionById(transactionId);

        // Por ahora redirijo al historial - puedes implementar este método si lo necesitas
        return "redirect:/payment/history";
    }

    /**
     * ENDPOINT PARA OBTENER TARJETAS DE PRUEBA VÍA AJAX
     * AYUDA AL USUARIO CON NÚMEROS DE TARJETA PARA TESTING
     *
     * @return JSON con tarjetas de prueba
     */
    @GetMapping("/test-cards")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getTestCards() {
        Map<String, String> testCards = paymentSimulatorService.getTestCards();
        return ResponseEntity.ok(testCards);
    }

    /**
     * WEBHOOK SIMULADO PARA PROCESAR NOTIFICACIONES DE PAGO
     * SIMULA CALLBACKS DE PASARELAS REALES
     *
     * @param payload datos del webhook
     * @return respuesta de confirmación
     */
    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {

        Map<String, Object> response = new HashMap<>();

        try {
            // PROCESAR WEBHOOK (AQUÍ PUEDES IMPLEMENTAR LÓGICA ADICIONAL)
            String referenceCode = (String) payload.get("reference_code");
            String status = (String) payload.get("status");

            // LOG DEL WEBHOOK RECIBIDO
            System.out.println("🔗 WEBHOOK RECIBIDO - Ref: " + referenceCode + ", Status: " + status);

            response.put("success", true);
            response.put("message", "Webhook procesado correctamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * ENDPOINT PARA CANCELAR UNA TRANSACCIÓN PENDIENTE
     * PERMITE AL USUARIO CANCELAR PAGOS QUE ESTÁN EN PROCESO
     *
     * @param referenceCode código de referencia
     * @param auth información del usuario autenticado
     * @param redirectAttributes para mensajes flash
     * @return redirección apropiada
     */
    @PostMapping("/cancel/{referenceCode}")
    public String cancelPayment(@PathVariable String referenceCode,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {

        User currentUser = getUserFromAuth(auth);
        PaymentTransaction transaction = paymentSimulatorService.getTransactionStatus(referenceCode);

        if (transaction == null || !transaction.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transacción no encontrada");
            return "redirect:/payment/history";
        }

        if (transaction.isPending()) {
            // AQUÍ PODRÍAS IMPLEMENTAR LÓGICA PARA CANCELAR LA TRANSACCIÓN
            redirectAttributes.addFlashAttribute("successMessage", "Pago cancelado exitosamente");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede cancelar una transacción finalizada");
        }

        return "redirect:/payment/history";
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