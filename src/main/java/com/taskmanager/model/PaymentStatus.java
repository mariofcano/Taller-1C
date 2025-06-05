package com.taskmanager.model;

/**
 * ENUM QUE DEFINE LOS ESTADOS POSIBLES DE UNA TRANSACCIÓN DE PAGO
 *
 * @author Mario Flores
 * @version 1.0
 */
public enum PaymentStatus {

    /**
     * PAGO PENDIENTE DE PROCESAMIENTO
     */
    PENDING("Pendiente"),

    /**
     * PAGO EN PROCESO
     */
    PROCESSING("Procesando"),

    /**
     * PAGO COMPLETADO EXITOSAMENTE
     */
    COMPLETED("Completado"),

    /**
     * PAGO FALLIDO POR ERROR TÉCNICO
     */
    FAILED("Fallido"),

    /**
     * PAGO RECHAZADO POR EL BANCO
     */
    REJECTED("Rechazado"),

    /**
     * PAGO CANCELADO POR EL USUARIO
     */
    CANCELLED("Cancelado"),

    /**
     * PAGO REEMBOLSADO
     */
    REFUNDED("Reembolsado"),

    /**
     * PAGO EN DISPUTA
     */
    DISPUTED("En Disputa");

    // DESCRIPCIÓN LEGIBLE DEL ESTADO
    private final String displayName;

    /**
     * CONSTRUCTOR DEL ENUM
     *
     * @param displayName nombre mostrable del estado
     */
    PaymentStatus(String displayName) {
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
     * VERIFICO SI EL PAGO FUE EXITOSO
     *
     * @return true si el pago se completó correctamente
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    /**
     * VERIFICO SI EL PAGO FALLÓ DEFINITIVAMENTE
     *
     * @return true si el pago no se puede procesar
     */
    public boolean isFailed() {
        return this == FAILED || this == REJECTED || this == CANCELLED;
    }

    /**
     * VERIFICO SI EL PAGO ESTÁ EN PROGRESO
     *
     * @return true si aún se está procesando
     */
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING;
    }

    /**
     * VERIFICO SI EL PAGO ESTÁ FINALIZADO (EXITOSO O FALLIDO)
     *
     * @return true si ya no está en progreso
     */
    public boolean isFinalized() {
        return !isInProgress();
    }

    /**
     * VERIFICO SI EL PAGO PERMITE ACTIVAR SUSCRIPCIÓN
     *
     * @return true si activa la suscripción premium
     */
    public boolean activatesSubscription() {
        return this == COMPLETED;
    }

    /**
     * OBTENGO LA CLASE CSS PARA EL BADGE DEL ESTADO
     *
     * @return clase CSS para mostrar el estado con colores
     */
    public String getCssClass() {
        return switch (this) {
            case COMPLETED -> "bg-success";
            case PENDING, PROCESSING -> "bg-warning";
            case FAILED, REJECTED, CANCELLED -> "bg-danger";
            case REFUNDED -> "bg-info";
            case DISPUTED -> "bg-secondary";
        };
    }

    /**
     * OBTENGO EL ICONO BOOTSTRAP PARA EL ESTADO
     *
     * @return clase de icono de Bootstrap Icons
     */
    public String getIcon() {
        return switch (this) {
            case COMPLETED -> "bi-check-circle-fill";
            case PENDING -> "bi-clock-fill";
            case PROCESSING -> "bi-arrow-repeat";
            case FAILED, REJECTED -> "bi-x-circle-fill";
            case CANCELLED -> "bi-stop-circle-fill";
            case REFUNDED -> "bi-arrow-counterclockwise";
            case DISPUTED -> "bi-exclamation-triangle-fill";
        };
    }
}