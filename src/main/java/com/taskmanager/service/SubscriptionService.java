package com.taskmanager.service;

import com.taskmanager.model.*;
import com.taskmanager.repository.SubscriptionPlanRepository;
import com.taskmanager.repository.UserSubscriptionRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.TaskLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SERVICIO QUE MANEJA TODA LA LÓGICA DE NEGOCIO DE LAS SUSCRIPCIONES
 * GESTIONA PLANES, LÍMITES Y VALIDACIONES DEL SISTEMA PREMIUM
 *
 * @author Mario Flores
 * @version 1.0
 */
@Service
@Transactional
public class SubscriptionService {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskLocationRepository taskLocationRepository;

    // ==================== GESTIÓN DE PLANES ====================

    /**
     * OBTENGO TODOS LOS PLANES ACTIVOS DISPONIBLES PARA CONTRATAR
     *
     * @return lista de planes activos ordenados por precio
     */
    public List<SubscriptionPlan> getAllActivePlans() {
        return subscriptionPlanRepository.findAllActivePlans();
    }

    /**
     * OBTENGO EL PLAN GRATUITO POR DEFECTO
     *
     * @return el plan gratuito
     */
    public SubscriptionPlan getFreePlan() {
        return subscriptionPlanRepository.findFreePlan()
                .orElseThrow(() -> new RuntimeException("Plan gratuito no encontrado"));
    }

    /**
     * OBTENGO PLANES PREMIUM (DE PAGO)
     *
     * @return lista de planes premium
     */
    public List<SubscriptionPlan> getPremiumPlans() {
        return subscriptionPlanRepository.findPremiumPlans();
    }

    /**
     * BUSCO UN PLAN POR SU ID
     *
     * @param planId ID del plan
     * @return el plan si existe
     */
    public Optional<SubscriptionPlan> getPlanById(Long planId) {
        return subscriptionPlanRepository.findById(planId);
    }

    /**
     * BUSCO UN PLAN POR SU NOMBRE
     *
     * @param planName nombre del plan
     * @return el plan si existe
     */
    public Optional<SubscriptionPlan> getPlanByName(String planName) {
        return subscriptionPlanRepository.findByNameIgnoreCase(planName);
    }

    // ==================== GESTIÓN DE SUSCRIPCIONES ====================

    /**
     * OBTENGO LA SUSCRIPCIÓN ACTIVA DE UN USUARIO
     *
     * @param user el usuario
     * @return su suscripción activa o null si no tiene
     */
    public UserSubscription getUserActiveSubscription(User user) {
        return userSubscriptionRepository.findActiveSubscriptionByUser(user).orElse(null);
    }

    /**
     * VERIFICO SI UN USUARIO TIENE SUSCRIPCIÓN PREMIUM ACTIVA
     *
     * @param user el usuario
     * @return true si tiene plan premium
     */
    public boolean hasActivePremiumSubscription(User user) {
        return userSubscriptionRepository.hasPremiumSubscription(user);
    }

    /**
     * CREO UNA SUSCRIPCIÓN GRATUITA PARA UN NUEVO USUARIO
     *
     * @param user usuario recién registrado
     * @return la suscripción creada
     */
    public UserSubscription createFreeSubscription(User user) {
        SubscriptionPlan freePlan = getFreePlan();

        UserSubscription subscription = new UserSubscription(user, freePlan);
        subscription.setPaymentReference("FREE_PLAN");

        return userSubscriptionRepository.save(subscription);
    }

    /**
     * UPGRADE DE USUARIO A PLAN PREMIUM
     *
     * @param user usuario a hacer upgrade
     * @param premiumPlan plan premium a activar
     * @param paymentReference referencia del pago
     * @return la nueva suscripción premium
     */
    public UserSubscription upgradeToPremium(User user, SubscriptionPlan premiumPlan, String paymentReference) {
        // CANCELAR SUSCRIPCIÓN ACTUAL SI EXISTE
        UserSubscription currentSubscription = getUserActiveSubscription(user);
        if (currentSubscription != null) {
            currentSubscription.setStatus(SubscriptionStatus.CANCELLED);
            currentSubscription.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(currentSubscription);
        }

        // CREAR NUEVA SUSCRIPCIÓN PREMIUM
        UserSubscription premiumSubscription = new UserSubscription(user, premiumPlan);
        premiumSubscription.setPaymentReference(paymentReference);
        premiumSubscription.setAutoRenew(true);

        return userSubscriptionRepository.save(premiumSubscription);
    }

    /**
     * CANCELAR SUSCRIPCIÓN PREMIUM DE UN USUARIO
     *
     * @param user el usuario
     * @return true si se canceló correctamente
     */
    public boolean cancelPremiumSubscription(User user) {
        UserSubscription subscription = getUserActiveSubscription(user);

        if (subscription != null && !subscription.isFreeSubscription()) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setAutoRenew(false);
            subscription.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(subscription);

            // CREAR NUEVA SUSCRIPCIÓN GRATUITA
            createFreeSubscription(user);
            return true;
        }

        return false;
    }

    /**
     * RENUEVO AUTOMÁTICAMENTE UNA SUSCRIPCIÓN
     *
     * @param subscription suscripción a renovar
     * @param paymentReference nueva referencia de pago
     * @return suscripción renovada
     */
    public UserSubscription renewSubscription(UserSubscription subscription, String paymentReference) {
        // EXTENDER FECHA DE VENCIMIENTO
        if (subscription.getEndDate() != null) {
            subscription.setEndDate(subscription.getEndDate().plusMonths(1));
        } else {
            subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        }

        subscription.setPaymentReference(paymentReference);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setUpdatedAt(LocalDateTime.now());

        return userSubscriptionRepository.save(subscription);
    }

    // ==================== VALIDACIONES DE LÍMITES ====================

    /**
     * VERIFICO SI UN USUARIO PUEDE CREAR MÁS TAREAS
     *
     * @param user el usuario
     * @return true si puede crear más tareas
     */
    public boolean canCreateMoreTasks(User user) {
        UserSubscription subscription = getUserActiveSubscription(user);

        if (subscription == null || subscription.getSubscriptionPlan() == null) {
            return false; // Sin suscripción no puede crear nada
        }

        SubscriptionPlan plan = subscription.getSubscriptionPlan();

        // PLAN SIN LÍMITES
        if (!plan.hasTaskLimit()) {
            return true;
        }

// VERIFICAR LÍMITE
        long currentTasks = taskRepository.countPendingTasksByUser(user) + taskRepository.countCompletedTasksByUser(user);
        return currentTasks < plan.getMaxTasks();
    }

    /**
     * VERIFICO SI UN USUARIO PUEDE CREAR MÁS UBICACIONES
     *
     * @param user el usuario
     * @return true si puede crear más ubicaciones
     */
    public boolean canCreateMoreLocations(User user) {
        UserSubscription subscription = getUserActiveSubscription(user);

        if (subscription == null || subscription.getSubscriptionPlan() == null) {
            return false;
        }

        SubscriptionPlan plan = subscription.getSubscriptionPlan();

        // PLAN SIN LÍMITES
        if (!plan.hasLocationLimit()) {
            return true;
        }

        // VERIFICAR LÍMITE
        long currentLocations = taskLocationRepository.countActiveLocationsByUser(user);
        return currentLocations < plan.getMaxLocations();
    }

    /**
     * OBTENGO EL NÚMERO DE TAREAS RESTANTES QUE PUEDE CREAR
     *
     * @param user el usuario
     * @return número de tareas restantes o -1 si es ilimitado
     */
    public int getRemainingTasks(User user) {
        UserSubscription subscription = getUserActiveSubscription(user);

        if (subscription == null || subscription.getSubscriptionPlan() == null) {
            return 0;
        }

        SubscriptionPlan plan = subscription.getSubscriptionPlan();

        if (!plan.hasTaskLimit()) {
            return -1; // Ilimitado
        }

        long currentTasks = taskRepository.countPendingTasksByUser(user) + taskRepository.countCompletedTasksByUser(user);
        return Math.max(0, plan.getMaxTasks() - (int)currentTasks);
    }

    /**
     * OBTENGO EL NÚMERO DE UBICACIONES RESTANTES QUE PUEDE CREAR
     *
     * @param user el usuario
     * @return número de ubicaciones restantes o -1 si es ilimitado
     */
    public int getRemainingLocations(User user) {
        UserSubscription subscription = getUserActiveSubscription(user);

        if (subscription == null || subscription.getSubscriptionPlan() == null) {
            return 0;
        }

        SubscriptionPlan plan = subscription.getSubscriptionPlan();

        if (!plan.hasLocationLimit()) {
            return -1; // Ilimitado
        }

        long currentLocations = taskLocationRepository.countActiveLocationsByUser(user);
        return Math.max(0, plan.getMaxLocations() - (int)currentLocations);
    }

    /**
     * OBTENGO ESTADÍSTICAS DE USO DE UN USUARIO
     *
     * @param user el usuario
     * @return estadísticas completas
     */
    public SubscriptionUsageStats getUserUsageStats(User user) {
        UserSubscription subscription = getUserActiveSubscription(user);

        if (subscription == null) {
            return new SubscriptionUsageStats(0, 0, 0, 0, false);
        }

        SubscriptionPlan plan = subscription.getSubscriptionPlan();
        long currentTasks = taskRepository.countPendingTasksByUser(user) + taskRepository.countCompletedTasksByUser(user);
        long currentLocations = taskLocationRepository.countActiveLocationsByUser(user);

        return new SubscriptionUsageStats(
                (int)currentTasks,
                plan.getMaxTasks() != null ? plan.getMaxTasks() : -1,
                (int)currentLocations,
                plan.getMaxLocations() != null ? plan.getMaxLocations() : -1,
                subscription.getSubscriptionPlan().getPrice().intValue() > 0
        );
    }

    // ==================== MANTENIMIENTO AUTOMÁTICO ====================

    /**
     * PROCESO AUTOMÁTICO PARA EXPIRAR SUSCRIPCIONES VENCIDAS
     *
     * @return número de suscripciones expiradas
     */
    @Transactional
    public int expireOverdueSubscriptions() {
        List<UserSubscription> expiredSubscriptions =
                userSubscriptionRepository.findExpiredActiveSubscriptions(LocalDateTime.now());

        int count = 0;
        for (UserSubscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(subscription);

            // CREAR SUSCRIPCIÓN GRATUITA AUTOMÁTICAMENTE
            createFreeSubscription(subscription.getUser());
            count++;
        }

        return count;
    }

    /**
     * OBTENGO SUSCRIPCIONES QUE EXPIRAN PRONTO
     *
     * @param daysAhead días de anticipación
     * @return suscripciones que expiran en X días
     */
    public List<UserSubscription> getSubscriptionsExpiringIn(int daysAhead) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(daysAhead);
        return userSubscriptionRepository.findSubscriptionsExpiringBefore(expirationDate);
    }

    // ==================== CLASE AUXILIAR PARA ESTADÍSTICAS ====================

    /**
     * CLASE PARA DEVOLVER ESTADÍSTICAS DE USO
     */
    public static class SubscriptionUsageStats {
        private final int currentTasks;
        private final int maxTasks;
        private final int currentLocations;
        private final int maxLocations;
        private final boolean isPremium;

        public SubscriptionUsageStats(int currentTasks, int maxTasks,
                                      int currentLocations, int maxLocations, boolean isPremium) {
            this.currentTasks = currentTasks;
            this.maxTasks = maxTasks;
            this.currentLocations = currentLocations;
            this.maxLocations = maxLocations;
            this.isPremium = isPremium;
        }

        // GETTERS
        public int getCurrentTasks() { return currentTasks; }
        public int getMaxTasks() { return maxTasks; }
        public int getCurrentLocations() { return currentLocations; }
        public int getMaxLocations() { return maxLocations; }
        public boolean isPremium() { return isPremium; }

        public boolean hasTasksLimit() { return maxTasks > 0; }
        public boolean hasLocationsLimit() { return maxLocations > 0; }
        public int getRemainingTasks() { return hasTasksLimit() ? Math.max(0, maxTasks - currentTasks) : -1; }
        public int getRemainingLocations() { return hasLocationsLimit() ? Math.max(0, maxLocations - currentLocations) : -1; }
    }
}