package com.taskmanager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ENTIDAD QUE REPRESENTA UNA UBICACIÓN GEOGRÁFICA PARA TAREAS
 * CADA UBICACIÓN TIENE COORDENADAS GPS Y PUEDE SER ASOCIADA A TAREAS
 *
 * @author Mario Flores
 * @version 1.0
 */
@Entity
@Table(name = "task_locations")
public class TaskLocation {

    /**
     * ID ÚNICO DE LA UBICACIÓN
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * NOMBRE DESCRIPTIVO DE LA UBICACIÓN
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * DESCRIPCIÓN ADICIONAL DE LA UBICACIÓN
     */
    @Column(length = 500)
    private String description;

    /**
     * LATITUD GPS DE LA UBICACIÓN
     * RANGO VÁLIDO: -90.0 a 90.0
     */
    @Column(nullable = false)
    private Double latitude;

    /**
     * LONGITUD GPS DE LA UBICACIÓN
     * RANGO VÁLIDO: -180.0 a 180.0
     */
    @Column(nullable = false)
    private Double longitude;

    /**
     * DIRECCIÓN FÍSICA DE LA UBICACIÓN
     */
    @Column(length = 200)
    private String address;

    /**
     * INDICA SI LA UBICACIÓN ESTÁ ACTIVA
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * FECHA Y HORA DE CREACIÓN DE LA UBICACIÓN
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * USUARIO QUE CREÓ ESTA UBICACIÓN
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // CONSTRUCTORES

    /**
     * CONSTRUCTOR VACÍO REQUERIDO POR JPA
     */
    public TaskLocation() {
    }

    /**
     * CONSTRUCTOR CON DATOS BÁSICOS PARA CREAR UNA NUEVA UBICACIÓN
     *
     * @param name nombre descriptivo de la ubicación
     * @param description descripción adicional
     * @param latitude coordenada de latitud GPS
     * @param longitude coordenada de longitud GPS
     * @param user usuario propietario de la ubicación
     */
    public TaskLocation(String name, String description, Double latitude, Double longitude, User user) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.user = user;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // MÉTODOS ESPECIALES DE JPA

    /**
     * SE EJECUTA AUTOMÁTICAMENTE ANTES DE GUARDAR EN LA BASE DE DATOS
     * ESTABLECE LA FECHA DE CREACIÓN ACTUAL
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // GETTERS Y SETTERS

    /**
     * OBTIENE EL ID ÚNICO DE LA UBICACIÓN
     * @return el identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * ESTABLECE EL ID DE LA UBICACIÓN
     * @param id el identificador único
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * OBTIENE EL NOMBRE DE LA UBICACIÓN
     * @return el nombre descriptivo
     */
    public String getName() {
        return name;
    }

    /**
     * ESTABLECE EL NOMBRE DE LA UBICACIÓN
     * @param name el nombre descriptivo
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * OBTIENE LA DESCRIPCIÓN DE LA UBICACIÓN
     * @return la descripción adicional
     */
    public String getDescription() {
        return description;
    }

    /**
     * ESTABLECE LA DESCRIPCIÓN DE LA UBICACIÓN
     * @param description la descripción adicional
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * OBTIENE LA LATITUD GPS
     * @return coordenada de latitud
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * ESTABLECE LA LATITUD GPS
     * @param latitude coordenada de latitud (-90.0 a 90.0)
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * OBTIENE LA LONGITUD GPS
     * @return coordenada de longitud
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * ESTABLECE LA LONGITUD GPS
     * @param longitude coordenada de longitud (-180.0 a 180.0)
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * OBTIENE LA DIRECCIÓN FÍSICA
     * @return la dirección como texto
     */
    public String getAddress() {
        return address;
    }

    /**
     * ESTABLECE LA DIRECCIÓN FÍSICA
     * @param address la dirección como texto
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * VERIFICA SI LA UBICACIÓN ESTÁ ACTIVA
     * @return true si está activa, false si está deshabilitada
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * ESTABLECE EL ESTADO ACTIVO DE LA UBICACIÓN
     * @param active true para activar, false para desactivar
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * OBTIENE LA FECHA DE CREACIÓN
     * @return cuándo se creó la ubicación
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * ESTABLECE LA FECHA DE CREACIÓN
     * @param createdAt la fecha de creación
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * OBTIENE EL USUARIO PROPIETARIO
     * @return el usuario que creó la ubicación
     */
    public User getUser() {
        return user;
    }

    /**
     * ESTABLECE EL USUARIO PROPIETARIO
     * @param user el usuario que creó la ubicación
     */
    public void setUser(User user) {
        this.user = user;
    }

    // MÉTODOS ÚTILES

    /**
     * VERIFICA SI LAS COORDENADAS GPS SON VÁLIDAS
     * @return true si la latitud y longitud están en rangos válidos
     */
    public boolean hasValidCoordinates() {
        return latitude != null && longitude != null &&
                latitude >= -90.0 && latitude <= 90.0 &&
                longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * REPRESENTACIÓN EN TEXTO DE LA UBICACIÓN
     * ÚTIL PARA DEBUGGING Y LOGS
     */
    @Override
    public String toString() {
        return "TaskLocation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}