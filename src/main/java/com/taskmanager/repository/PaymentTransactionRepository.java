package com.taskmanager.repository;

import com.taskmanager.model.PaymentTransaction;
import com.taskmanager.model.PaymentStatus;
import com.taskmanager.model.User;
import com.taskmanager.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO PARA MANEJAR LAS TRANSACCIONES DE PAGO EN LA BASE DE DATOS
 * PROPORCIONA MÉTODOS PARA GESTIONAR EL HISTORIAL DE PAGOS SIMULADOS
 *
 * @author Mario Flores
 * @version 1.0
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    /**
     * BUSCO UNA TRANSACCIÓN POR SU CÓDIGO DE REFERENCIA ÚNICO
     * PARA CONSULTAS Y CONFIRMACIONES DE PAGO
     *
     * @param referenceCode código único de la transacción
     * @return la transacción si existe
     */
    Optional<PaymentTransaction> findByReferenceCode(String referenceCode);

    /**
     * BUSCO TODAS LAS TRANSACCIONES DE UN USUARIO
     * HISTORIAL COMPLETO DE PAGOS
     *
     * @param user el usuario
     * @return transacciones ordenadas por fecha (más recientes primero)
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.user = :user ORDER BY pt.transactionDate DESC")
    List<PaymentTransaction> findAllTransactionsByUser(@Param("user") User user);

    /**
     * BUSCO TRANSACCIONES DE UN USUARIO POR ESTADO
     * PARA FILTRAR PAGOS EXITOSOS, FALLIDOS, ETC.
     *
     * @param user el usuario
     * @param status estado del pago
     * @return transacciones filtradas por estado
     */
    List<PaymentTransaction> findByUserAndStatus(User user, PaymentStatus status);

    /**
     * BUSCO TRANSACCIONES POR ESTADO GENERAL
     * PARA ADMINISTRACIÓN Y MONITOREO
     *
     * @param status estado a filtrar
     * @return todas las transacciones con ese estado
     */
    List<PaymentTransaction> findByStatus(PaymentStatus status);

    /**
     * BUSCO TRANSACCIONES EXITOSAS DE UN USUARIO
     * HISTORIAL DE PAGOS COMPLETADOS
     *
     * @param user el usuario
     * @return solo transacciones completadas exitosamente
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.user = :user AND pt.status = 'COMPLETED' ORDER BY pt.transactionDate DESC")
    List<PaymentTransaction> findSuccessfulTransactionsByUser(@Param("user") User user);

    /**
     * BUSCO TRANSACCIONES FALLIDAS DE UN USUARIO
     * PARA ANÁLISIS DE PROBLEMAS DE PAGO
     *
     * @param user el usuario
     * @return solo transacciones fallidas
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.user = :user AND pt.status IN ('FAILED', 'REJECTED') ORDER BY pt.transactionDate DESC")
    List<PaymentTransaction> findFailedTransactionsByUser(@Param("user") User user);

    /**
     * BUSCO TRANSACCIONES POR PLAN DE SUSCRIPCIÓN
     * ESTADÍSTICAS DE INGRESOS POR PLAN
     *
     * @param subscriptionPlan el plan
     * @return transacciones para ese plan
     */
    List<PaymentTransaction> findBySubscriptionPlan(SubscriptionPlan subscriptionPlan);

    /**
     * BUSCO TRANSACCIONES EN UN RANGO DE FECHAS
     * PARA REPORTES FINANCIEROS
     *
     * @param startDate fecha inicio
     * @param endDate fecha fin
     * @return transacciones en ese período
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.transactionDate BETWEEN :startDate AND :endDate ORDER BY pt.transactionDate DESC")
    List<PaymentTransaction> findTransactionsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    /**
     * BUSCO TRANSACCIONES PENDIENTES ANTIGUAS
     * PARA LIMPIEZA Y CANCELACIÓN AUTOMÁTICA
     *
     * @param cutoffDate fecha límite (ej: más de 1 hora pendiente)
     * @return transacciones pendientes muy antiguas
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status IN ('PENDING', 'PROCESSING') AND pt.transactionDate < :cutoffDate")
    List<PaymentTransaction> findOldPendingTransactions(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * CALCULO INGRESOS TOTALES EXITOSOS
     * ESTADÍSTICA FINANCIERA PRINCIPAL
     *
     * @return suma total de pagos completados
     */
    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.status = 'COMPLETED'")
    BigDecimal calculateTotalSuccessfulRevenue();

    /**
     * CALCULO INGRESOS EN UN RANGO DE FECHAS
     * REPORTES FINANCIEROS PERIÓDICOS
     *
     * @param startDate fecha inicio
     * @param endDate fecha fin
     * @return ingresos en ese período
     */
    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.status = 'COMPLETED' AND pt.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueInPeriod(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * CUENTO TRANSACCIONES POR ESTADO
     * ESTADÍSTICAS DE ÉXITO/FALLO DE PAGOS
     *
     * @param status estado a contar
     * @return número de transacciones con ese estado
     */
    long countByStatus(PaymentStatus status);

    /**
     * CUENTO TRANSACCIONES EXITOSAS DE UN USUARIO
     * HISTORIAL DE PAGOS EXITOSOS
     *
     * @param user el usuario
     * @return número de pagos exitosos
     */
    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.user = :user AND pt.status = 'COMPLETED'")
    long countSuccessfulTransactionsByUser(@Param("user") User user);

    /**
     * BUSCO TRANSACCIONES POR MÉTODO DE PAGO
     * ANÁLISIS DE PREFERENCIAS DE PAGO
     *
     * @param paymentMethod método usado (ej: "VISA", "MASTERCARD")
     * @return transacciones con ese método
     */
    List<PaymentTransaction> findByPaymentMethod(String paymentMethod);

    /**
     * BUSCO TRANSACCIONES RECIENTES DEL SISTEMA
     * PARA DASHBOARD ADMINISTRATIVO
     *
     * @param limit número máximo de resultados
     * @return transacciones más recientes
     */
    @Query("SELECT pt FROM PaymentTransaction pt ORDER BY pt.transactionDate DESC LIMIT :limit")
    List<PaymentTransaction> findRecentTransactions(@Param("limit") int limit);

    /**
     * BUSCO TRANSACCIONES POR MONTO ESPECÍFICO
     * PARA INVESTIGACIONES Y AUDITORÍAS
     *
     * @param amount monto exacto
     * @return transacciones con ese monto
     */
    List<PaymentTransaction> findByAmount(BigDecimal amount);

    /**
     * BUSCO TRANSACCIONES EN RANGO DE MONTOS
     * ANÁLISIS DE DISTRIBUCIÓN DE PAGOS
     *
     * @param minAmount monto mínimo
     * @param maxAmount monto máximo
     * @return transacciones en ese rango
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.amount BETWEEN :minAmount AND :maxAmount")
    List<PaymentTransaction> findTransactionsByAmountRange(@Param("minAmount") BigDecimal minAmount,
                                                           @Param("maxAmount") BigDecimal maxAmount);

    /**
     * VERIFICO SI EXISTE TRANSACCIÓN CON ESE CÓDIGO DE REFERENCIA
     * VALIDACIÓN DE UNICIDAD
     *
     * @param referenceCode código a verificar
     * @return true si ya existe
     */
    boolean existsByReferenceCode(String referenceCode);

    /**
     * BUSCO LA ÚLTIMA TRANSACCIÓN EXITOSA DE UN USUARIO
     * PARA MOSTRAR ÚLTIMO PAGO REALIZADO
     *
     * @param user el usuario
     * @return su transacción exitosa más reciente
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.user = :user AND pt.status = 'COMPLETED' ORDER BY pt.transactionDate DESC LIMIT 1")
    Optional<PaymentTransaction> findLastSuccessfulTransactionByUser(@Param("user") User user);

    /**
     * CALCULO TASA DE ÉXITO DE PAGOS
     * PORCENTAJE DE TRANSACCIONES EXITOSAS
     *
     * @return porcentaje de éxito (0-100)
     */
    @Query("SELECT (COUNT(pt) * 100.0 / (SELECT COUNT(pt2) FROM PaymentTransaction pt2)) FROM PaymentTransaction pt WHERE pt.status = 'COMPLETED'")
    Double calculateSuccessRate();

    /**
     * BUSCO TRANSACCIONES SOSPECHOSAS (MÚLTIPLES FALLOS)
     * PARA DETECCIÓN DE FRAUDE
     *
     * @param user usuario a analizar
     * @param hours horas a analizar hacia atrás
     * @param maxFailures número máximo de fallos permitidos
     * @return transacciones fallidas recientes
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.user = :user AND pt.status IN ('FAILED', 'REJECTED') " +
            "AND pt.transactionDate > :cutoffTime")
    List<PaymentTransaction> findRecentFailedTransactionsByUser(@Param("user") User user,
                                                                @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * ELIMINO TRANSACCIONES ANTIGUAS FALLIDAS
     * LIMPIEZA AUTOMÁTICA DE BD
     *
     * @param cutoffDate fecha límite para eliminar
     * @return número de registros eliminados
     */
    @Query("DELETE FROM PaymentTransaction pt WHERE pt.status IN ('FAILED', 'CANCELLED') AND pt.transactionDate < :cutoffDate")
    int cleanupOldFailedTransactions(@Param("cutoffDate") LocalDateTime cutoffDate);
}