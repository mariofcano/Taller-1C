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
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long id;

    /**
     * USUARIO QUE REALIZA EL PRESTAMO
     */
    @NotNull(message = "EL USUARIO ES OBLIGATORIO PARA EL PRESTAMO")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_LOAN_USER"))
    private User user;

    /**
     * LIBRO QUE SE PRESTA
     */
    @NotNull(message = "EL LIBRO ES OBLIGATORIO PARA EL PRESTAMO")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "FK_LOAN_BOOK"))
    private Book book;

    /**
     * FECHA EN QUE SE REALIZO EL PRESTAMO
     */
    @NotNull(message = "LA FECHA DE PRESTAMO ES OBLIGATORIA")
    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    /**
     * FECHA LIMITE PARA LA DEVOLUCION DEL LIBRO
     */
    @NotNull(message = "LA FECHA DE VENCIMIENTO ES OBLIGATORIA")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * FECHA Y HORA REAL DE DEVOLUCION DEL LIBRO
     */
    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    /**
     * ESTADO ACTUAL DEL PRESTAMO
     */
    @NotNull(message = "EL ESTADO DEL PRESTAMO ES OBLIGATORIO")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status = LoanStatus.ACTIVE;

    /**
     * NUMERO DE RENOVACIONES REALIZADAS
     */
    @Min(value = 0, message = "EL NUMERO DE RENOVACIONES NO PUEDE SER NEGATIVO")
    @Max(value = 10, message = "EL NUMERO DE RENOVACIONES NO PUEDE EXCEDER 10")
    @Column(name = "renewals", nullable = false)
    private Integer renewals = 0;

    /**
     * MULTA APLICADA POR RETRASO EN LA DEVOLUCION
     */
    @DecimalMin(value = "0.0", message = "LA MULTA NO PUEDE SER NEGATIVA")
    @DecimalMax(value = "9999.99", message = "LA MULTA NO PUEDE EXCEDER 9999.99")
    @Column(name = "fine_amount", precision = 6, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    /**
     * INDICA SI LA MULTA HA SIDO PAGADA
     */
    @Column(name = "fine_paid", nullable = false)
    private Boolean finePaid = true;

    /**
     * OBSERVACIONES O NOTAS ADICIONALES DEL PRESTAMO
     */
    @Size(max = 500, message = "LAS OBSERVACIONES NO PUEDEN EXCEDER 500 CARACTERES")
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * FECHA Y HORA DE CREACION DEL REGISTRO
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * FECHA Y HORA DE ULTIMA ACTUALIZACION
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * TARIFA DIARIA POR RETRASO
     */
    @Column(name = "fine_per_day", precision = 5, scale = 2)
    private BigDecimal finePerDay = new BigDecimal("0.50");

    /**
     * LIMITE MAXIMO DE RENOVACIONES
     */
    @Column(name = "max_renewals", nullable = false)
    private Integer maxRenewals = 3;

    /**
     * CONSTRUCTOR VACIO REQUERIDO POR JPA
     */
    public Loan() {
        // CONSTRUCTOR VACIO REQUERIDO POR JPA
    }

    /**
     * CONSTRUCTOR PARA CREAR UN NUEVO PRESTAMO
     */
    public Loan(User user, Book book, LocalDate loanDate, LocalDate dueDate, LocalDateTime returnedAt) {
        this.user = user;
        this.book = book;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnedAt = returnedAt;
        this.status = LoanStatus.ACTIVE;
        this.renewals = 0;
        this.fineAmount = BigDecimal.ZERO;
        this.finePaid = true;
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

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public Integer getRenewals() {
        return renewals;
    }

    public void setRenewals(Integer renewals) {
        this.renewals = renewals;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public Boolean getFinePaid() {
        return finePaid;
    }

    public void setFinePaid(Boolean finePaid) {
        this.finePaid = finePaid;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getFinePerDay() {
        return finePerDay;
    }

    public void setFinePerDay(BigDecimal finePerDay) {
        this.finePerDay = finePerDay;
    }

    public Integer getMaxRenewals() {
        return maxRenewals;
    }

    public void setMaxRenewals(Integer maxRenewals) {
        this.maxRenewals = maxRenewals;
    }

    // MÉTODOS DE NEGOCIO

    /**
     * PROCESA LA DEVOLUCION DEL LIBRO
     */
    public void processReturn(LocalDateTime returnDateTime) {
        this.returnedAt = returnDateTime;

        // CALCULAR MULTA SI ESTÁ ATRASADO
        if (isOverdue()) {
            calculateFine();
            this.status = LoanStatus.RETURNED_LATE;
        } else {
            this.status = LoanStatus.RETURNED;
        }

        updateStatus();
    }

    /**
     * VERIFICA SI EL PRESTAMO PUEDE SER RENOVADO
     */
    public boolean canBeRenewed() {
        return this.status == LoanStatus.ACTIVE &&
                this.renewals < this.maxRenewals &&
                !isOverdue() &&
                !hasUnpaidFines();
    }

    /**
     * RENUEVA EL PRESTAMO
     */
    public boolean renew() {
        if (canBeRenewed()) {
            this.renewals++;
            this.dueDate = this.dueDate.plusDays(14); // Extensión de 14 días
            this.status = LoanStatus.RENEWED;
            updateStatus();
            return true;
        }
        return false;
    }

    /**
     * VERIFICA SI EL PRESTAMO ESTÁ VENCIDO
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(this.dueDate) && this.returnedAt == null;
    }

    /**
     * VERIFICA SI HAY MULTAS SIN PAGAR
     */
    public boolean hasUnpaidFines() {
        return this.fineAmount.compareTo(BigDecimal.ZERO) > 0 && !this.finePaid;
    }

    /**
     * PAGA LA MULTA
     */
    public void payFine() {
        this.finePaid = true;
        updateStatus();
    }

    /**
     * CALCULA LA MULTA POR RETRASO
     */
    public BigDecimal calculateFine() {
        if (this.dueDate != null && (LocalDate.now().isAfter(this.dueDate) || this.returnedAt != null)) {
            LocalDate compareDate = this.returnedAt != null ?
                    this.returnedAt.toLocalDate() : LocalDate.now();

            if (compareDate.isAfter(this.dueDate)) {
                long daysOverdue = ChronoUnit.DAYS.between(this.dueDate, compareDate);
                this.fineAmount = this.finePerDay.multiply(BigDecimal.valueOf(daysOverdue));
                this.finePaid = false;
            }
        }
        return this.fineAmount;
    }

    /**
     * VERIFICA SI EL PRESTAMO ESTÁ ACTIVO
     */
    public boolean isActive() {
        return this.status == LoanStatus.ACTIVE || this.status == LoanStatus.RENEWED;
    }

    /**
     * ACTUALIZA EL ESTADO DEL PRESTAMO
     */
    public void updateStatus() {
        if (this.returnedAt != null) {
            if (this.returnedAt.toLocalDate().isAfter(this.dueDate)) {
                this.status = LoanStatus.RETURNED_LATE;
            } else if (this.status != LoanStatus.RETURNED_LATE) {
                this.status = LoanStatus.RETURNED;
            }
        } else if (isOverdue()) {
            this.status = LoanStatus.OVERDUE;
        } else if (this.status == LoanStatus.OVERDUE && !isOverdue()) {
            this.status = LoanStatus.ACTIVE;
        }
    }

    // MÉTODOS AUXILIARES PARA COMPATIBILIDAD CON LOANSERVICE

    /**
     * Verifica si el préstamo está finalizado (alias para compatibilidad)
     */
    public boolean isFinePaid() {
        return finePaid;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", loanDate=" + loanDate +
                ", dueDate=" + dueDate +
                ", returnedAt=" + returnedAt +
                ", status=" + status +
                ", renewals=" + renewals +
                ", fineAmount=" + fineAmount +
                ", finePaid=" + finePaid +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Loan loan = (Loan) obj;

        if (id != null && loan.id != null) {
            return id.equals(loan.id);
        }

        return user != null && user.equals(loan.user) &&
                book != null && book.equals(loan.book) &&
                loanDate != null && loanDate.equals(loan.loanDate);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (book != null ? book.hashCode() : 0);
        result = 31 * result + (loanDate != null ? loanDate.hashCode() : 0);
        return result;
    }
}