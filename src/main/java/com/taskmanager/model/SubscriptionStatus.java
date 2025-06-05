package com.taskmanager.model;

/**
 * ENUM QUE DEFINE LOS ESTADOS POSIBLES DE UNA SUSCRIPCIÓN
 *
 * @author Mario Flores
 * @version 1.0
 */
public enum SubscriptionStatus {

    /**
     * SUSCRIPCIÓN ACTIVA Y VÁLIDA
     */
    ACTIVE("Activa"),

    /**
     * SUSCRIPCIÓN EXPIRADA POR FALTA DE PAGO
     */
    EXPIRED("Expirada"),

    /**
     * SUSCRIPCIÓN CANCELADA POR EL USUARIO
     */
    CANCELLED("Cancelada"),

    /**
     * SUSCRIPCIÓN SUSPENDIDA POR ADMINISTRADOR
     */
    SUSPENDED("Suspendida"),

    /**
     * SUSCRIPCIÓN PENDIENTE DE ACTIVACIÓN
     */
    PENDING("Pendiente"),

    /**
     * SUSCRIPCIÓN EN PERÍODO DE GRACIA (POCOS DÍAS PARA RENOVAR)
     */
    GRACE_PERIOD("Período de Gracia");

    // DESCRIPCIÓN LEGIBLE DEL ESTADO
    private final String displayName;

    /**
     * CONSTRUCTOR DEL ENUM
     *
     * @param displayName nombre mostrable del estado
     */
    SubscriptionStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * OBTENGO EL NOMBRE PARA MOSTRAR EN LA UI
     *
     * @return descripción del estado
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * VERIFICO SI EL ESTADO PERMITE USAR FUNCIONALIDADES PREMIUM
     *
     * @return true si puede usar funcionalidades premium
     */
    public boolean allowsPremiumFeatures() {
        return this == ACTIVE || this == GRACE_PERIOD;
    }

    /**
     * VERIFICO SI EL ESTADO REQUIERE RENOVACIÓN
     *
     * @return true si necesita renovar
     */
    public boolean requiresRenewal() {
        return this == EXPIRED || this == GRACE_PERIOD;
    }

    /**
     * VERIFICO SI LA SUSCRIPCIÓN ESTÁ ACTIVA
     *
     * @return true si está funcionalmente activa
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * VERIFICO SI LA SUSCRIPCIÓN ESTÁ INACTIVA
     *
     * @return true si no está funcionando
     */
    public boolean isInactive() {
        return this == EXPIRED || this == CANCELLED || this == SUSPENDED;
    }
}