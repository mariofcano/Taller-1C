package com.taskmanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ENTIDAD QUE REPRESENTA LOS PLANES DE SUSCRIPCIÓN DISPONIBLES
 * DEFINE QUÉ FUNCIONALIDADES Y LÍMITES TIENE CADA PLAN
 *
 * @author Mario Flores
 * @version 1.0
 */
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    /**
     * ID ÚNICO DEL PLAN
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * NOMBRE DEL PLAN (FREE, PREMIUM)
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * DESCRIPCIÓN DEL PLAN
     */
    @Column(length = 200)
    private String description;

    /**
     * PRECIO MENSUAL DEL PLAN
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * MÁXIMO DE TAREAS PERMITIDAS (NULL = ILIMITADO)
     */
    @Column(name = "max_tasks")
    private Integer maxTasks;

    /**
     * MÁXIMO DE UBICACIONES PERMITIDAS (NULL = ILIMITADO)
     */
    @Column(name = "max_locations")
    private Integer maxLocations;

    /**
     * SI ESTÁ ACTIVO PARA CONTRATAR
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * CARACTERÍSTICAS ADICIONALES DEL PLAN (JSON STRING)
     */
    @Column(length = 1000)
    private String features;

    /**
     * FECHA DE CREACIÓN DEL PLAN
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * SUSCRIPCIONES ASOCIADAS A ESTE PLAN
     */
    @OneToMany(mappedBy = "subscriptionPlan", fetch = FetchType.LAZY)
    private List<UserSubscription> subscriptions;

    // CONSTRUCTORES

    /**
     * CONSTRUCTOR VACÍO PARA JPA
     */
    public SubscriptionPlan() {
    }

    /**
     * CONSTRUCTOR CON DATOS BÁSICOS
     */
    public SubscriptionPlan(String name, String description, BigDecimal price,
                            Integer maxTasks, Integer maxLocations) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.maxTasks = maxTasks;
        this.maxLocations = maxLocations;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * SE EJECUTA ANTES DE GUARDAR EN LA BD
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // GETTERS Y SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getMaxTasks() {
        return maxTasks;
    }

    public void setMaxTasks(Integer maxTasks) {
        this.maxTasks = maxTasks;
    }

    public Integer getMaxLocations() {
        return maxLocations;
    }

    public void setMaxLocations(Integer maxLocations) {
        this.maxLocations = maxLocations;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<UserSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<UserSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    // MÉTODOS ÚTILES

    /**
     * VERIFICO SI ES EL PLAN GRATUITO
     */
    public boolean isFree() {
        return this.price.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * VERIFICO SI TIENE LÍMITE DE TAREAS
     */
    public boolean hasTaskLimit() {
        return this.maxTasks != null;
    }

    /**
     * VERIFICO SI TIENE LÍMITE DE UBICACIONES
     */
    public boolean hasLocationLimit() {
        return this.maxLocations != null;
    }

    /**
     * REPRESENTACIÓN EN TEXTO DEL PLAN
     */
    @Override
    public String toString() {
        return "SubscriptionPlan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", maxTasks=" + maxTasks +
                ", maxLocations=" + maxLocations +
                '}';
    }
}