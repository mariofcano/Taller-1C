package com.biblioteca.digital.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD JPA QUE REPRESENTA UN USUARIO DEL SISTEMA DE BIBLIOTECA DIGITAL
 *
 * ESTA CLASE MAPEA LA TABLA 'users' EN LA BASE DE DATOS Y CONTIENE TODA LA INFORMACION
 * NECESARIA PARA GESTIONAR LOS USUARIOS QUE PUEDEN ACCEDER AL SISTEMA, REALIZAR PRESTAMOS
 * DE LIBROS Y MANTENER UN HISTORIAL DE SUS ACTIVIDADES.
 *
 * LA ENTIDAD INCLUYE VALIDACIONES A NIVEL DE CAMPO PARA GARANTIZAR LA INTEGRIDAD
 * DE LOS DATOS Y RELACIONES CON OTRAS ENTIDADES DEL SISTEMA.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see Book
 * @see Loan
 * @see jakarta.persistence.Entity
 * @see jakarta.validation.constraints
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "UK_USER_EMAIL"),
        @UniqueConstraint(columnNames = "username", name = "UK_USER_USERNAME")
})
public class User {

    /**
     * CLAVE PRIMARIA DE LA ENTIDAD USER
     *
     * UTILIZO ESTRATEGIA DE GENERACION IDENTITY PARA QUE LA BASE DE DATOS
     * ASIGNE AUTOMATICAMENTE LOS VALORES INCREMENTALES A ESTE CAMPO.
     * ESTE IDENTIFICADOR UNICO PERMITE REFERENCIAR CADA USUARIO DE FORMA
     * INEQUIVOCA EN TODO EL SISTEMA.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /**
     * NOMBRE DE USUARIO PARA EL LOGIN DEL SISTEMA
     *
     * ESTE CAMPO ALMACENA EL IDENTIFICADOR UNICO QUE CADA USUARIO UTILIZARA
     * PARA AUTENTICARSE EN EL SISTEMA. DEBE SER UNICO EN TODA LA BASE DE DATOS
     * Y CUMPLIR CON LOS REQUISITOS DE LONGITUD ESTABLECIDOS.
     */
    @NotBlank(message = "EL NOMBRE DE USUARIO ES OBLIGATORIO")
    @Size(min = 3, max = 50, message = "EL NOMBRE DE USUARIO DEBE TENER ENTRE 3 Y 50 CARACTERES")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "EL NOMBRE DE USUARIO SOLO PUEDE CONTENER LETRAS, NUMEROS Y GUIONES BAJOS")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * CORREO ELECTRONICO DEL USUARIO
     *
     * ALMACENO LA DIRECCION DE EMAIL DEL USUARIO PARA COMUNICACIONES,
     * NOTIFICACIONES DE PRESTAMOS, RECORDATORIOS DE DEVOLUCION Y OTRAS
     * FUNCIONALIDADES DEL SISTEMA QUE REQUIERAN CONTACTO DIRECTO.
     */
    @NotBlank(message = "EL EMAIL ES OBLIGATORIO")
    @Email(message = "EL EMAIL DEBE TENER UN FORMATO VALIDO")
    @Size(max = 100, message = "EL EMAIL NO PUEDE EXCEDER LOS 100 CARACTERES")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * CONTRASEÑA CIFRADA DEL USUARIO
     *
     * ALMACENO LA CONTRASEÑA EN FORMATO HASH UTILIZANDO BCRYPT PARA GARANTIZAR
     * LA SEGURIDAD. NUNCA ALMACENO CONTRASEÑAS EN TEXTO PLANO POR MOTIVOS
     * DE SEGURIDAD Y CUMPLIMIENTO DE BUENAS PRACTICAS.
     */
    @NotBlank(message = "LA CONTRASEÑA ES OBLIGATORIA")
    @Size(min = 60, max = 100, message = "LA CONTRASEÑA CIFRADA DEBE TENER ENTRE 60 Y 100 CARACTERES")
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /**
     * NOMBRE COMPLETO DEL USUARIO
     *
     * CAMPO QUE ALMACENA EL NOMBRE Y APELLIDOS COMPLETOS DEL USUARIO
     * PARA MOSTRAR EN LA INTERFAZ Y GENERAR REPORTES PERSONALIZADOS.
     */
    @NotBlank(message = "EL NOMBRE COMPLETO ES OBLIGATORIO")
    @Size(min = 2, max = 100, message = "EL NOMBRE COMPLETO DEBE TENER ENTRE 2 Y 100 CARACTERES")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * NUMERO DE TELEFONO DEL USUARIO
     *
     * ALMACENO EL TELEFONO DE CONTACTO PARA COMUNICACIONES URGENTES
     * RELACIONADAS CON PRESTAMOS VENCIDOS O NOTIFICACIONES IMPORTANTES.
     */
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,15}$", message = "EL TELEFONO DEBE TENER UN FORMATO VALIDO")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * DIRECCION FISICA DEL USUARIO
     *
     * REGISTRO LA DIRECCION COMPLETA DEL USUARIO PARA ENVIOS POSTALES,
     * NOTIFICACIONES OFICIALES Y VERIFICACION DE IDENTIDAD CUANDO SEA NECESARIO.
     */
    @Size(max = 200, message = "LA DIRECCION NO PUEDE EXCEDER LOS 200 CARACTERES")
    @Column(name = "address", length = 200)
    private String address;

    /**
     * ROL DEL USUARIO EN EL SISTEMA
     *
     * DEFINO EL NIVEL DE PERMISOS Y ACCESO QUE TIENE EL USUARIO:
     * - ADMIN: ACCESO COMPLETO AL SISTEMA
     * - LIBRARIAN: GESTION DE LIBROS Y PRESTAMOS
     * - USER: USUARIO BASICO QUE PUEDE REALIZAR PRESTAMOS
     */
    @NotNull(message = "EL ROL ES OBLIGATORIO")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    /**
     * ESTADO ACTIVO DEL USUARIO
     *
     * CONTROLO SI EL USUARIO PUEDE ACCEDER AL SISTEMA Y REALIZAR OPERACIONES.
     * LOS USUARIOS INACTIVOS NO PUEDEN HACER LOGIN NI REALIZAR PRESTAMOS.
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * FECHA Y HORA DE CREACION DEL REGISTRO
     *
     * TIMESTAMP AUTOMATICO QUE REGISTRA CUANDO SE CREO LA CUENTA DEL USUARIO
     * EN EL SISTEMA. UTILIZO ANOTACION @CreationTimestamp PARA QUE HIBERNATE
     * GESTIONE AUTOMATICAMENTE ESTE CAMPO.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * FECHA Y HORA DE ULTIMA ACTUALIZACION
     *
     * TIMESTAMP QUE SE ACTUALIZA AUTOMATICAMENTE CADA VEZ QUE SE MODIFICA
     * CUALQUIER CAMPO DE LA ENTIDAD. PERMITE AUDITORIA Y CONTROL DE CAMBIOS.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * LISTA DE PRESTAMOS REALIZADOS POR EL USUARIO
     *
     * RELACION ONE-TO-MANY CON LA ENTIDAD LOAN QUE PERMITE ACCEDER A TODOS
     * LOS PRESTAMOS REALIZADOS POR ESTE USUARIO. UTILIZO FETCH LAZY PARA
     * OPTIMIZAR LAS CONSULTAS Y CARGAR LOS PRESTAMOS SOLO CUANDO SEA NECESARIO.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Loan> loans = new ArrayList<>();

    /**
     * CONSTRUCTOR VACIO REQUERIDO POR JPA
     *
     * JPA NECESITA UN CONSTRUCTOR SIN PARAMETROS PARA PODER INSTANCIAR
     * LA ENTIDAD MEDIANTE REFLECTION. AUNQUE NO LO USO DIRECTAMENTE,
     * ES OBLIGATORIO PARA EL CORRECTO FUNCIONAMIENTO DEL ORM.
     */
    public User() {
        // CONSTRUCTOR VACIO REQUERIDO POR JPA
    }

    /**
     * CONSTRUCTOR PARA CREAR UN USUARIO CON DATOS BASICOS
     *
     * UTILIZO ESTE CONSTRUCTOR PARA CREAR USUARIOS CON LA INFORMACION
     * MINIMA REQUERIDA. LOS CAMPOS OPCIONALES SE PUEDEN ESTABLECER
     * POSTERIORMENTE MEDIANTE LOS SETTERS CORRESPONDIENTES.
     *
     * @param username NOMBRE DE USUARIO UNICO PARA LOGIN
     * @param email DIRECCION DE CORREO ELECTRONICO
     * @param password CONTRASEÑA CIFRADA
     * @param fullName NOMBRE COMPLETO DEL USUARIO
     */
    public User(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = UserRole.USER;
        this.active = true;
    }

    /**
     * CONSTRUCTOR COMPLETO PARA CREAR USUARIO CON TODOS LOS DATOS
     *
     * PERMITE CREAR UN USUARIO ESTABLECIENDO TODOS LOS CAMPOS PRINCIPALES
     * EN UNA SOLA OPERACION. UTIL PARA IMPORTACION DE DATOS O CREACION
     * MASIVA DE USUARIOS.
     *
     * @param username NOMBRE DE USUARIO UNICO
     * @param email DIRECCION DE EMAIL
     * @param password CONTRASEÑA CIFRADA
     * @param fullName NOMBRE COMPLETO
     * @param phone NUMERO DE TELEFONO
     * @param address DIRECCION FISICA
     * @param role ROL DEL USUARIO EN EL SISTEMA
     */
    public User(String username, String email, String password, String fullName,
                String phone, String address, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.active = true;
    }

    // GETTERS Y SETTERS CON DOCUMENTACION JAVADOC

    /**
     * OBTIENE EL IDENTIFICADOR UNICO DEL USUARIO
     *
     * @return ID NUMERICO DEL USUARIO
     */
    public Long getId() {
        return id;
    }

    /**
     * ESTABLECE EL IDENTIFICADOR UNICO DEL USUARIO
     *
     * @param id IDENTIFICADOR NUMERICO A ASIGNAR
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * OBTIENE EL NOMBRE DE USUARIO PARA LOGIN
     *
     * @return NOMBRE DE USUARIO
     */
    public String getUsername() {
        return username;
    }

    /**
     * ESTABLECE EL NOMBRE DE USUARIO PARA LOGIN
     *
     * @param username NOMBRE DE USUARIO A ASIGNAR
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * OBTIENE LA DIRECCION DE EMAIL DEL USUARIO
     *
     * @return EMAIL DEL USUARIO
     */
    public String getEmail() {
        return email;
    }

    /**
     * ESTABLECE LA DIRECCION DE EMAIL DEL USUARIO
     *
     * @param email DIRECCION DE EMAIL A ASIGNAR
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * OBTIENE LA CONTRASEÑA CIFRADA DEL USUARIO
     *
     * @return CONTRASEÑA EN FORMATO HASH
     */
    public String getPassword() {
        return password;
    }

    /**
     * ESTABLECE LA CONTRASEÑA CIFRADA DEL USUARIO
     *
     * @param password CONTRASEÑA CIFRADA A ASIGNAR
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * OBTIENE EL NOMBRE COMPLETO DEL USUARIO
     *
     * @return NOMBRE COMPLETO
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * ESTABLECE EL NOMBRE COMPLETO DEL USUARIO
     *
     * @param fullName NOMBRE COMPLETO A ASIGNAR
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * OBTIENE EL NUMERO DE TELEFONO DEL USUARIO
     *
     * @return TELEFONO DE CONTACTO
     */
    public String getPhone() {
        return phone;
    }

    /**
     * ESTABLECE EL NUMERO DE TELEFONO DEL USUARIO
     *
     * @param phone TELEFONO A ASIGNAR
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * OBTIENE LA DIRECCION FISICA DEL USUARIO
     *
     * @return DIRECCION COMPLETA
     */
    public String getAddress() {
        return address;
    }

    /**
     * ESTABLECE LA DIRECCION FISICA DEL USUARIO
     *
     * @param address DIRECCION A ASIGNAR
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * OBTIENE EL ROL DEL USUARIO EN EL SISTEMA
     *
     * @return ROL ASIGNADO
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * ESTABLECE EL ROL DEL USUARIO EN EL SISTEMA
     *
     * @param role ROL A ASIGNAR
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * VERIFICA SI EL USUARIO ESTA ACTIVO
     *
     * @return TRUE SI ESTA ACTIVO, FALSE EN CASO CONTRARIO
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * ESTABLECE EL ESTADO ACTIVO DEL USUARIO
     *
     * @param active ESTADO A ASIGNAR
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * OBTIENE LA FECHA DE CREACION DEL USUARIO
     *
     * @return TIMESTAMP DE CREACION
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * OBTIENE LA FECHA DE ULTIMA ACTUALIZACION
     *
     * @return TIMESTAMP DE ULTIMA MODIFICACION
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * OBTIENE LA LISTA DE PRESTAMOS DEL USUARIO
     *
     * @return LISTA DE PRESTAMOS REALIZADOS
     */
    public List<Loan> getLoans() {
        return loans;
    }

    /**
     * ESTABLECE LA LISTA DE PRESTAMOS DEL USUARIO
     *
     * @param loans LISTA DE PRESTAMOS A ASIGNAR
     */
    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    /**
     * AGREGA UN PRESTAMO A LA LISTA DEL USUARIO
     *
     * METODO DE CONVENIENCIA PARA AÑADIR UN PRESTAMO Y MANTENER
     * LA CONSISTENCIA BIDIRECCIONAL DE LA RELACION.
     *
     * @param loan PRESTAMO A AGREGAR
     */
    public void addLoan(Loan loan) {
        loans.add(loan);
        loan.setUser(this);
    }

    /**
     * ELIMINA UN PRESTAMO DE LA LISTA DEL USUARIO
     *
     * METODO DE CONVENIENCIA PARA REMOVER UN PRESTAMO Y MANTENER
     * LA CONSISTENCIA BIDIRECCIONAL DE LA RELACION.
     *
     * @param loan PRESTAMO A ELIMINAR
     */
    public void removeLoan(Loan loan) {
        loans.remove(loan);
        loan.setUser(null);
    }

    /**
     * VERIFICA SI EL USUARIO ES ADMINISTRADOR
     *
     * @return TRUE SI EL ROL ES ADMIN, FALSE EN CASO CONTRARIO
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    /**
     * VERIFICA SI EL USUARIO ES BIBLIOTECARIO
     *
     * @return TRUE SI EL ROL ES LIBRARIAN, FALSE EN CASO CONTRARIO
     */
    public boolean isLibrarian() {
        return UserRole.LIBRARIAN.equals(this.role);
    }

    /**
     * VERIFICA SI EL USUARIO TIENE PRESTAMOS ACTIVOS
     *
     * @return TRUE SI TIENE PRESTAMOS SIN DEVOLVER, FALSE EN CASO CONTRARIO
     */
    public boolean hasActiveLoans() {
        return loans.stream().anyMatch(loan -> loan.getReturnedAt() == null);
    }

    /**
     * CUENTA EL NUMERO DE PRESTAMOS ACTIVOS DEL USUARIO
     *
     * @return CANTIDAD DE PRESTAMOS PENDIENTES DE DEVOLUCION
     */
    public long countActiveLoans() {
        return loans.stream().filter(loan -> loan.getReturnedAt() == null).count();
    }

    /**
     * REPRESENTACION EN CADENA DE LA ENTIDAD USER
     *
     * GENERO UNA REPRESENTACION LEGIBLE QUE INCLUYE LOS CAMPOS MAS
     * IMPORTANTES PARA DEBUGGING Y LOGGING.
     *
     * @return CADENA REPRESENTATIVA DEL USUARIO
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * VERIFICA LA IGUALDAD ENTRE DOS OBJETOS USER
     *
     * DOS USUARIOS SON IGUALES SI TIENEN EL MISMO ID O EL MISMO USERNAME
     * (DADO QUE USERNAME ES UNICO EN EL SISTEMA).
     *
     * @param obj OBJETO A COMPARAR
     * @return TRUE SI SON IGUALES, FALSE EN CASO CONTRARIO
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User user = (User) obj;

        if (id != null && user.id != null) {
            return id.equals(user.id);
        }

        return username != null && username.equals(user.username);
    }

    /**
     * GENERA EL CODIGO HASH DEL OBJETO USER
     *
     * UTILIZO EL USERNAME PARA GENERAR EL HASH YA QUE ES UNICO
     * Y INMUTABLE UNA VEZ ESTABLECIDO.
     *
     * @return CODIGO HASH DEL USUARIO
     */
    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}