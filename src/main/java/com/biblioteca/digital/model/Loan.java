package com.biblioteca.digital.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * ENTIDAD JPA QUE REPRESENTA UN PRESTAMO EN EL SISTEMA DE BIBLIOTECA DIGITAL
 *
 * ESTA CLASE MAPEA LA TABLA 'loans' EN LA BASE DE DATOS Y GESTIONA TODA
 * LA INFORMACION RELACIONADA CON LOS PRESTAMOS DE LIBROS REALIZADOS POR
 * LOS USUARIOS DE LA BIBLIOTECA.
 *
 * CADA PRESTAMO VINCULA UN USUARIO ESPECIFICO CON UN LIBRO ESPECIFICO
 * DURANTE UN PERIODO DETERMINADO, CONTROLANDO FECHAS DE PRESTAMO,
 * DEVOLUCION ESPERADA, DEVOLUCION REAL Y PENALIZACIONES APLICABLES.
 *
 * LA ENTIDAD MANEJA EL CICLO COMPLETO DEL PRESTAMO DESDE SU CREACION
 * HASTA SU FINALIZACION, INCLUYENDO EL CALCULO DE MULTAS POR RETRASO.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see User
 * @see Book
 * @see jakarta.persistence.Entity
 * @see jakarta.validation.constraints
 */
@Entity
@Table(name = "loans", indexes = {
        @Index(name = "IDX_LOAN_USER", columnList = "user_id"),
        @Index(name = "IDX_LOAN_BOOK", columnList = "book_id"),
        @Index(name = "IDX_LOAN_STATUS", columnList = "status"),
        @Index(name = "IDX_LOAN_DUE_DATE", columnList = "due_date"),
        @Index(name = "IDX_LOAN_RETURNED", columnList = "returned_at")
})
public class Loan {

    /**
     * CLAVE PRIMARIA DE LA ENTIDAD LOAN
     *
     * UTILIZO ESTRATEGIA DE GENERACION IDENTITY PARA QUE LA BASE DE DATOS
     * ASIGNE AUTOMATICAMENTE LOS VALORES INCREMENTALES. ESTE IDENTIFICADOR
     * UNICO PERMITE REFERENCIAR CADA PRESTAMO DE FORMA INEQUIVOCA.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long id;

    /**
     * USUARIO QUE REALIZA EL PRESTAMO
     *
     * RELACION MANY-TO-ONE CON LA ENTIDAD USER QUE ESTABLECE QUIEN
     * ES EL RESPONSABLE DEL PRESTAMO. CADA PRESTAMO PERTENECE A UN
     * UNICO USUARIO, PERO UN USUARIO PUEDE TENER MULTIPLES PRESTAMOS.
     */
    @NotNull(message = "EL USUARIO ES OBLIGATORIO PARA EL PRESTAMO")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_LOAN_USER"))
    private User user;

    /**
     * LIBRO QUE SE PRESTA
     *
     * RELACION MANY-TO-ONE CON LA ENTIDAD BOOK QUE IDENTIFICA QUE
     * LIBRO ESPECIFICO SE ESTA PRESTANDO. CADA PRESTAMO CORRESPONDE
     * A UN UNICO LIBRO, PERO UN LIBRO PUEDE TENER MULTIPLES PRESTAMOS.
     */
    @NotNull(message = "EL LIBRO ES OBLIGATORIO PARA EL PRESTAMO")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "FK_LOAN_BOOK"))
    private Book book;

    /**
     * FECHA EN QUE SE REALIZO EL PRESTAMO
     *
     * REGISTRO LA FECHA EXACTA EN QUE EL USUARIO SOLICITO Y SE APROBO
     * EL PRESTAMO DEL LIBRO. ESTE CAMPO SE ESTABLECE AUTOMATICAMENTE
     * AL MOMENTO DE CREAR EL PRESTAMO.
     */
    @NotNull(message = "LA FECHA DE PRESTAMO ES OBLIGATORIA")
    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    /**
     * FECHA LIMITE PARA LA DEVOLUCION DEL LIBRO
     *
     * FECHA MAXIMA EN QUE EL USUARIO DEBE DEVOLVER EL LIBRO SIN INCURRIR
     * EN PENALIZACIONES. SE CALCULA AUTOMATICAMENTE SUMANDO EL PERIODO
     * DE PRESTAMO ESTANDAR A LA FECHA DE PRESTAMO.
     */
    @NotNull(message = "LA FECHA DE VENCIMIENTO ES OBLIGATORIA")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * FECHA Y HORA REAL DE DEVOLUCION DEL LIBRO
     *
     * TIMESTAMP QUE REGISTRA CUANDO EL USUARIO DEVOLVIO EFECTIVAMENTE
     * EL LIBRO. ESTE CAMPO ES NULL MIENTRAS EL PRESTAMO ESTE ACTIVO
     * Y SE ESTABLECE AL MOMENTO DE PROCESAR LA DEVOLUCION.
     */
    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    /**
     * ESTADO ACTUAL DEL PRESTAMO
     *
     * ENUMERACION QUE INDICA LA SITUACION ACTUAL DEL PRESTAMO:
     * ACTIVO, VENCIDO, DEVUELTO, CANCELADO O PERDIDO. PERMITE
     * CONTROLAR EL FLUJO Y LAS ACCIONES DISPONIBLES.
     */
    @NotNull(message = "EL ESTADO DEL PRESTAMO ES OBLIGATORIO")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status = LoanStatus.ACTIVE;

    /**
     * NUMERO DE RENOVACIONES REALIZADAS
     *
     * CONTADOR QUE REGISTRA CUANTAS VECES EL USUARIO HA EXTENDIDO
     * EL PERIODO DE PRESTAMO. PERMITE CONTROLAR EL LIMITE MAXIMO
     * DE RENOVACIONES PERMITIDAS POR POLITICA.
     */
    @Min(value = 0, message = "EL NUMERO DE RENOVACIONES NO PUEDE SER NEGATIVO")
    @Max(value = 10, message = "EL NUMERO DE RENOVACIONES NO PUEDE EXCEDER 10")
    @Column(name = "renewals", nullable = false)
    private Integer renewals = 0;

    /**
     * MULTA APLICADA POR RETRASO EN LA DEVOLUCION
     *
     * MONTO MONETARIO CALCULADO CUANDO EL LIBRO SE DEVUELVE DESPUES
     * DE LA FECHA LIMITE. SE CALCULA BASANDOSE EN LOS DIAS DE RETRASO
     * Y LA TARIFA DIARIA ESTABLECIDA EN LAS POLITICAS.
     */
    @DecimalMin(value = "0.0", message = "LA MULTA NO PUEDE SER NEGATIVA")
    @DecimalMax(value = "9999.99", message = "LA MULTA NO PUEDE EXCEDER 9999.99")
    @Column(name = "fine_amount", precision = 6, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    /**
     * INDICA SI LA MULTA HA SIDO PAGADA
     *
     * BANDERA QUE CONTROLA EL ESTADO DE PAGO DE LAS MULTAS APLICADAS.
     * LOS USUARIOS CON MULTAS PENDIENTES PUEDEN TENER RESTRICCIONES
     * PARA REALIZAR NUEVOS PRESTAMOS.
     */
    @Column(name = "fine_paid", nullable = false)
    private Boolean finePaid = true;

    /**
     * OBSERVACIONES O NOTAS ADICIONALES DEL PRESTAMO
     *
     * CAMPO DE TEXTO LIBRE PARA REGISTRAR COMENTARIOS ESPECIALES,
     * CONDICIONES DEL LIBRO AL MOMENTO DEL PRESTAMO, ACUERDOS
     * PARTICULARES O CUALQUIER INFORMACION RELEVANTE.
     */
    @Size(max = 500, message = "LAS OBSERVACIONES NO PUEDEN EXCEDER 500 CARACTERES")
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * FECHA Y HORA DE CREACION DEL REGISTRO
     *
     * TIMESTAMP AUTOMATICO QUE REGISTRA CUANDO SE CREO EL PRESTAMO
     * EN EL SISTEMA. UTILIZO @CreationTimestamp PARA QUE HIBERNATE
     * GESTIONE AUTOMATICAMENTE ESTE CAMPO.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * FECHA Y HORA DE ULTIMA ACTUALIZACION
     *
     * TIMESTAMP QUE SE ACTUALIZA AUTOMATICAMENTE CADA VEZ QUE SE MODIFICA
     * CUALQUIER CAMPO DE LA ENTIDAD. PERMITE AUDITORIA Y CONTROL DE CAMBIOS
     * EN LA INFORMACION DEL PRESTAMO.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * CONSTRUCTOR VACIO REQUERIDO POR JPA
     *
     * JPA NECESITA UN CONSTRUCTOR SIN PARAMETROS PARA PODER INSTANCIAR
     * LA ENTIDAD MEDIANTE REFLECTION. AUNQUE NO LO USO DIRECTAMENTE,
     * ES OBLIGATORIO PARA EL CORRECTO FUNCIONAMIENTO DEL ORM.
     */
    public Loan() {
        // CONSTRUCTOR VACIO REQUERIDO POR JPA
    }

    /**
     * ESTABLECE EL USUARIO QUE REALIZA EL PRESTAMO
     *
     * @param user USUARIO A ASIGNAR
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * ESTABLECE EL LIBRO QUE SE PRESTA
     *
     * @param book LIBRO A ASIGNAR
     */
    public void setBook(Book book) {
        this.book = book;
    }

    /**
     * OBTIENE LA FECHA DE DEVOLUCION REAL
     *
     * @return TIMESTAMP DE DEVOLUCION
     */
    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }
}


