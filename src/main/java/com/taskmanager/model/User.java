package com.taskmanager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ENTIDAD QUE REPRESENTA UN USUARIO DEL SISTEMA
 *
 * @author Mario Flores
 * @version 1.0
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * ID ÚNICO DEL USUARIO
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * NOMBRE DE USUARIO PARA LOGIN
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * EMAIL DEL USUARIO
     */
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /**
     * CONTRASEÑA CIFRADA
     */
    @Column(nullable = false)
    private String password;

    /**
     * ROL DEL USUARIO EN EL SISTEMA
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * SI EL USUARIO ESTÁ ACTIVO O NO
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * FECHA DE REGISTRO
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * TAREAS QUE PERTENECEN A ESTE USUARIO
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks;

    // CONSTRUCTORES

    /**
     * CONSTRUCTOR VACÍO PARA JPA
     */
    public User() {
    }

    /**
     * CONSTRUCTOR CON DATOS BÁSICOS
     *
     * @param username nombre de usuario
     * @param email correo electrónico
     * @param password contraseña
     * @param role rol del usuario
     */
    public User(String username, String email, String password, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = true;
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

    /**
     * @return el ID del usuario
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return el nombre de usuario
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return el email del usuario
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return la contraseña cifrada
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return el rol del usuario
     */
    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * @return si el usuario está habilitado
     */
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return la fecha de creación
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return la lista de tareas del usuario
     */
    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    // MÉTODOS ÚTILES

    /**
     * VERIFICO SI EL USUARIO ES ADMINISTRADOR
     * @return true si es admin
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /**
     * REPRESENTACIÓN EN TEXTO DEL USUARIO
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }
}