package com.taskmanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ENTIDAD QUE REPRESENTA UNA TRANSACCIÓN DE PAGO SIMULADA
 * GUARDA EL HISTORIAL DE TODOS LOS PAGOS REALIZADOS EN EL SISTEMA
 *
 * @author Mario Flores
 * @version 1.0
 */
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    /**
     * ID ÚNICO DE LA TRANSACCIÓN
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * USUARIO QUE REALIZÓ EL PAGO
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * PLAN POR EL QUE SE ESTÁ PAGANDO
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    /**
     * MONTO DEL PAGO
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * MONEDA DEL PAGO
     */
    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    /**
     * ESTADO DEL PAGO
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /**
     * MÉTODO DE PAGO UTILIZADO
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * CÓDIGO DE REFERENCIA ÚNICO DE LA TRANSACCIÓN
     */
    @Column(name = "reference_code", unique = true, length = 100)
    private String referenceCode;

    /**
     * ÚLTIMOS 4 DÍGITOS DE LA TARJETA (PARA MOSTRAR AL USUARIO)
     */
    @Column(name = "card_last_digits", length = 4)
    private String cardLastDigits;

    /**
     * FECHA Y HORA DE LA TRANSACCIÓN
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    /**
     * MENSAJE DE ERROR SI EL PAGO FALLÓ
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * DATOS ADICIONALES DE LA TRANSACCIÓN (JSON)
     */
    @Column(name = "transaction_data", length = 1000)
    private String transactionData;

    /**
     * FECHA DE CREACIÓN DEL REGISTRO
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // CONSTRUCTORES

    /**
     * CONSTRUCTOR VACÍO PARA JPA
     */
    public PaymentTransaction() {
    }

    /**
     * CONSTRUCTOR PARA CREAR UNA NUEVA TRANSACCIÓN
     */
    public PaymentTransaction(User user, SubscriptionPlan subscriptionPlan,
                              BigDecimal amount, String paymentMethod) {
        this.user = user;
        this.subscriptionPlan = subscriptionPlan;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.currency = "EUR";
        this.status = PaymentStatus.PENDING;
        this.transactionDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.referenceCode = generateReferenceCode();
    }

    /**
     * SE EJECUTA ANTES DE GUARDAR EN LA BD
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
        if (this.referenceCode == null) {
            this.referenceCode = generateReferenceCode();
        }
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getCardLastDigits() {
        return cardLastDigits;
    }

    public void setCardLastDigits(String cardLastDigits) {
        this.cardLastDigits = cardLastDigits;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTransactionData() {
        return transactionData;
    }

    public void setTransactionData(String transactionData) {
        this.transactionData = transactionData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // MÉTODOS ÚTILES

    /**
     * VERIFICO SI EL PAGO FUE EXITOSO
     */
    public boolean isSuccessful() {
        return this.status == PaymentStatus.COMPLETED;
    }

    /**
     * VERIFICO SI EL PAGO FALLÓ
     */
    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED || this.status == PaymentStatus.REJECTED;
    }

    /**
     * VERIFICO SI EL PAGO ESTÁ PENDIENTE
     */
    public boolean isPending() {
        return this.status == PaymentStatus.PENDING || this.status == PaymentStatus.PROCESSING;
    }

    /**
     * OBTENGO EL MONTO FORMATEADO PARA MOSTRAR
     */
    public String getFormattedAmount() {
        return String.format("%.2f %s", this.amount, this.currency);
    }

    /**
     * GENERO UN CÓDIGO DE REFERENCIA ÚNICO
     */
    private String generateReferenceCode() {
        return "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    /**
     * REPRESENTACIÓN EN TEXTO DE LA TRANSACCIÓN
     */
    @Override
    public String toString() {
        return "PaymentTransaction{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", amount=" + amount + " " + currency +
                ", status=" + status +
                ", referenceCode='" + referenceCode + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}