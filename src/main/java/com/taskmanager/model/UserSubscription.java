package com.taskmanager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ENTIDAD QUE REPRESENTA LA SUSCRIPCIÓN ACTIVA DE UN USUARIO
 * RELACIONA UN USUARIO CON SU PLAN DE SUSCRIPCIÓN ACTUAL
 *
 * @author Mario Flores
 * @version 1.0
 */
@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {

    /**
     * ID ÚNICO DE LA SUSCRIPCIÓN
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * USUARIO QUE TIENE ESTA SUSCRIPCIÓN
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * PLAN DE SUSCRIPCIÓN CONTRATADO
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    /**
     * FECHA DE INICIO DE LA SUSCRIPCIÓN
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * FECHA DE VENCIMIENTO DE LA SUSCRIPCIÓN
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * ESTADO DE LA SUSCRIPCIÓN
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    /**
     * REFERENCIA DEL PAGO ASOCIADO
     */
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    /**
     * SI SE RENUEVA AUTOMÁTICAMENTE
     */
    @Column(name = "auto_renew")
    private Boolean autoRenew = false;

    /**
     * FECHA DE CREACIÓN DEL REGISTRO
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * FECHA DE ÚLTIMA ACTUALIZACIÓN
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // CONSTRUCTORES

    /**
     * CONSTRUCTOR VACÍO PARA JPA
     */
    public UserSubscription() {
    }

    /**
     * CONSTRUCTOR PARA CREAR UNA NUEVA SUSCRIPCIÓN
     */
    public UserSubscription(User user, SubscriptionPlan subscriptionPlan) {
        this.user = user;
        this.subscriptionPlan = subscriptionPlan;
        this.startDate = LocalDateTime.now();
        this.status = SubscriptionStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // SI ES PLAN GRATUITO, NO TIENE FECHA DE VENCIMIENTO
        if (!subscriptionPlan.isFree()) {
            this.endDate = LocalDateTime.now().plusMonths(1);
            this.autoRenew = true;
        }
    }

    // MÉTODOS DE JPA

    /**
     * SE EJECUTA ANTES DE GUARDAR EN LA BD
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * SE EJECUTA ANTES DE ACTUALIZAR EN LA BD
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // GETTERS Y SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Boolean getAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // MÉTODOS ÚTILES

    /**
     * VERIFICO SI LA SUSCRIPCIÓN ESTÁ ACTIVA
     */
    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE &&
                (this.endDate == null || this.endDate.isAfter(LocalDateTime.now()));
    }

    /**
     * VERIFICO SI LA SUSCRIPCIÓN HA EXPIRADO
     */
    public boolean isExpired() {
        return this.endDate != null && this.endDate.isBefore(LocalDateTime.now());
    }

    /**
     * VERIFICO SI ES UNA SUSCRIPCIÓN GRATUITA
     */
    public boolean isFreeSubscription() {
        return this.subscriptionPlan != null && this.subscriptionPlan.isFree();
    }

    /**
     * OBTENGO LOS DÍAS RESTANTES DE LA SUSCRIPCIÓN
     */
    public long getDaysRemaining() {
        if (this.endDate == null) {
            return Long.MAX_VALUE; // Ilimitado para plan gratuito
        }

        LocalDateTime now = LocalDateTime.now();
        if (this.endDate.isBefore(now)) {
            return 0; // Ya expiró
        }

        return java.time.Duration.between(now, this.endDate).toDays();
    }

    /**
     * REPRESENTACIÓN EN TEXTO DE LA SUSCRIPCIÓN
     */
    @Override
    public String toString() {
        return "UserSubscription{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", plan=" + (subscriptionPlan != null ? subscriptionPlan.getName() : null) +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}