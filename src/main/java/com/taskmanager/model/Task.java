package com.taskmanager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ENTIDAD QUE REPRESENTA UNA TAREA EN MI SISTEMA
 *
 * @author Mario Flores
 * @version 1.0
 */
@Entity
@Table(name = "tasks")
public class Task {

    /**
     * ID ÚNICO DE LA TAREA
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * TÍTULO DE LA TAREA
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * DESCRIPCIÓN DE LA TAREA
     */
    @Column(length = 500)
    private String description;

    /**
     * ESTADO DE LA TAREA: TRUE SI ESTÁ COMPLETADA
     */
    @Column(nullable = false)
    private Boolean completed = false;

    /**
     * FECHA Y HORA DE CREACIÓN
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * USUARIO AL QUE PERTENECE LA TAREA
     * CADA TAREA TIENE UN DUEÑO
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // CONSTRUCTORES

    /**
     * CONSTRUCTOR VACÍO
     * LO NECESITA JPA PARA CREAR INSTANCIAS
     */
    public Task() {
    }

    /**
     * CONSTRUCTOR CON LOS DATOS BÁSICOS
     * USO ESTE CUANDO CREO UNA TAREA NUEVA
     *
     * @param title el título de la tarea
     * @param description la descripción
     * @param user el usuario propietario
     */
    public Task(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    // MÉTODOS ESPECIALES DE JPA

    /**
     * SE EJECUTA ANTES DE GUARDAR EN LA BD
     * ME ASEGURO DE QUE SIEMPRE TENGA FECHA DE CREACIÓN
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // GETTERS Y SETTERS

    /**
     * OBTENGO EL ID DE LA TAREA
     * @return el identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * ESTABLEZCO EL ID (NORMALMENTE NO LO USO)
     * @param id el identificador
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * OBTENGO EL TÍTULO DE LA TAREA
     * @return el título
     */
    public String getTitle() {
        return title;
    }

    /**
     * ESTABLEZCO EL TÍTULO
     * @param title el nuevo título
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * OBTENGO LA DESCRIPCIÓN
     * @return la descripción de la tarea
     */
    public String getDescription() {
        return description;
    }

    /**
     * ESTABLEZCO LA DESCRIPCIÓN
     * @param description la nueva descripción
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * VERIFICO SI LA TAREA ESTÁ COMPLETADA
     * @return true si está terminada, false si está pendiente
     */
    public Boolean getCompleted() {
        return completed;
    }

    /**
     * MARCO LA TAREA COMO COMPLETADA O PENDIENTE
     * @param completed el nuevo estado
     */
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    /**
     * OBTENGO LA FECHA DE CREACIÓN
     * @return cuándo se creó la tarea
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * ESTABLEZCO LA FECHA DE CREACIÓN (RARO QUE LO USE)
     * @param createdAt la fecha
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * OBTENGO EL USUARIO PROPIETARIO
     * @return el usuario al que pertenece la tarea
     */
    public User getUser() {
        return user;
    }

    /**
     * ASIGNO LA TAREA A UN USUARIO
     * @param user el propietario de la tarea
     */
    public void setUser(User user) {
        this.user = user;
    }

    // MÉTODOS ÚTILES

    /**
     * REPRESENTACIÓN EN TEXTO DE LA TAREA
     * MUY ÚTIL PARA DEBUGGING
     */
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", createdAt=" + createdAt +
                '}';
    }
}