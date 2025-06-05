package com.taskmanager.repository;

import com.taskmanager.model.User;
import com.taskmanager.model.UserSubscription;
import com.taskmanager.model.SubscriptionStatus;
import com.taskmanager.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO PARA MANEJAR LAS SUSCRIPCIONES DE USUARIOS EN LA BASE DE DATOS
 * PROPORCIONA MÉTODOS PARA GESTIONAR LAS RELACIONES USUARIO-PLAN
 *
 * @author Mario Flores
 * @version 1.0
 */
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    /**
     * BUSCO LA SUSCRIPCIÓN ACTIVA DE UN USUARIO
     * CADA USUARIO SOLO PUEDE TENER UNA SUSCRIPCIÓN ACTIVA
     *
     * @param user el usuario
     * @return su suscripción activa si existe
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.user = :user AND us.status = 'ACTIVE'")
    Optional<UserSubscription> findActiveSubscriptionByUser(@Param("user") User user);

    /**
     * BUSCO TODAS LAS SUSCRIPCIONES DE UN USUARIO (HISTORIAL COMPLETO)
     * INCLUYE ACTIVAS, EXPIRADAS Y CANCELADAS
     *
     * @param user el usuario
     * @return todas sus suscripciones ordenadas por fecha
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.user = :user ORDER BY us.createdAt DESC")
    List<UserSubscription> findAllSubscriptionsByUser(@Param("user") User user);

    /**
     * BUSCO SUSCRIPCIONES POR ESTADO
     * PARA ADMINISTRACIÓN Y ESTADÍSTICAS
     *
     * @param status estado de la suscripción
     * @return suscripciones con ese estado
     */
    List<UserSubscription> findByStatus(SubscriptionStatus status);

    /**
     * BUSCO SUSCRIPCIONES DE UN PLAN ESPECÍFICO
     * PARA ESTADÍSTICAS DE POPULARIDAD DE PLANES
     *
     * @param subscriptionPlan el plan
     * @return suscripciones de ese plan
     */
    List<UserSubscription> findBySubscriptionPlan(SubscriptionPlan subscriptionPlan);

    /**
     * BUSCO SUSCRIPCIONES QUE EXPIRAN PRONTO
     * PARA ENVIAR RECORDATORIOS DE RENOVACIÓN
     *
     * @param dateLimit fecha límite (ej: dentro de 3 días)
     * @return suscripciones que expiran antes de esa fecha
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.endDate <= :dateLimit AND us.status = 'ACTIVE'")
    List<UserSubscription> findSubscriptionsExpiringBefore(@Param("dateLimit") LocalDateTime dateLimit);

    /**
     * BUSCO SUSCRIPCIONES EXPIRADAS QUE AÚN ESTÁN MARCADAS COMO ACTIVAS
     * PARA PROCESO AUTOMÁTICO DE LIMPIEZA
     *
     * @param currentDate fecha actual
     * @return suscripciones que deberían estar expiradas
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.endDate < :currentDate AND us.status = 'ACTIVE'")
    List<UserSubscription> findExpiredActiveSubscriptions(@Param("currentDate") LocalDateTime currentDate);

    /**
     * VERIFICO SI UN USUARIO TIENE SUSCRIPCIÓN ACTIVA
     * MÉTODO RÁPIDO PARA VALIDACIONES
     *
     * @param user el usuario
     * @return true si tiene suscripción activa
     */
    @Query("SELECT COUNT(us) > 0 FROM UserSubscription us WHERE us.user = :user AND us.status = 'ACTIVE'")
    boolean hasActiveSubscription(@Param("user") User user);

    /**
     * VERIFICO SI UN USUARIO TIENE SUSCRIPCIÓN PREMIUM ACTIVA
     * PARA CONTROLAR ACCESO A FUNCIONALIDADES PREMIUM
     *
     * @param user el usuario
     * @return true si tiene plan premium activo
     */
    @Query("SELECT COUNT(us) > 0 FROM UserSubscription us WHERE us.user = :user AND us.status = 'ACTIVE' AND us.subscriptionPlan.price > 0")
    boolean hasPremiumSubscription(@Param("user") User user);

    /**
     * CUENTO SUSCRIPCIONES ACTIVAS POR PLAN
     * ESTADÍSTICA PARA DASHBOARD ADMINISTRATIVO
     *
     * @param plan el plan
     * @return número de suscriptores activos
     */
    @Query("SELECT COUNT(us) FROM UserSubscription us WHERE us.subscriptionPlan = :plan AND us.status = 'ACTIVE'")
    long countActiveSubscriptionsByPlan(@Param("plan") SubscriptionPlan plan);

    /**
     * CUENTO TODAS LAS SUSCRIPCIONES ACTIVAS
     * ESTADÍSTICA GENERAL DEL SISTEMA
     *
     * @return total de suscripciones activas
     */
    long countByStatus(SubscriptionStatus status);

    /**
     * BUSCO SUSCRIPCIONES QUE SE RENUEVAN AUTOMÁTICAMENTE
     * PARA PROCESO DE RENOVACIÓN AUTOMÁTICA
     *
     * @return suscripciones con auto-renovación activada
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.autoRenew = true AND us.status = 'ACTIVE'")
    List<UserSubscription> findAutoRenewableSubscriptions();

    /**
     * BUSCO SUSCRIPCIONES POR REFERENCIA DE PAGO
     * PARA RELACIONAR PAGOS CON SUSCRIPCIONES
     *
     * @param paymentReference código de referencia del pago
     * @return suscripción asociada a ese pago
     */
    Optional<UserSubscription> findByPaymentReference(String paymentReference);

    /**
     * BUSCO LAS SUSCRIPCIONES MÁS RECIENTES
     * PARA DASHBOARD ADMINISTRATIVO
     *
     * @return últimas suscripciones creadas
     */
    @Query("SELECT us FROM UserSubscription us ORDER BY us.createdAt DESC")
    List<UserSubscription> findRecentSubscriptions();

    /**
     * BUSCO SUSCRIPCIONES CREADAS EN UN RANGO DE FECHAS
     * PARA REPORTES Y ANÁLISIS
     *
     * @param startDate fecha inicio
     * @param endDate fecha fin
     * @return suscripciones creadas en ese período
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.createdAt BETWEEN :startDate AND :endDate")
    List<UserSubscription> findSubscriptionsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * CALCULO INGRESOS TOTALES DE SUSCRIPCIONES ACTIVAS
     * ESTADÍSTICA FINANCIERA
     *
     * @return suma de precios de todas las suscripciones activas
     */
    @Query("SELECT SUM(us.subscriptionPlan.price) FROM UserSubscription us WHERE us.status = 'ACTIVE'")
    Double calculateTotalActiveRevenue();

    /**
     * BUSCO USUARIOS QUE NUNCA HAN TENIDO SUSCRIPCIÓN PREMIUM
     * PARA CAMPAÑAS DE MARKETING
     *
     * @return usuarios solo con plan gratuito
     */
    @Query("SELECT DISTINCT u FROM User u WHERE u NOT IN " +
            "(SELECT us.user FROM UserSubscription us WHERE us.subscriptionPlan.price > 0)")
    List<User> findUsersWithoutPremiumHistory();

    /**
     * BUSCO SUSCRIPCIONES EN PERÍODO DE GRACIA
     * USUARIOS QUE EXPIRARON PERO AÚN PUEDEN RENOVAR
     *
     * @return suscripciones en período de gracia
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'GRACE_PERIOD'")
    List<UserSubscription> findSubscriptionsInGracePeriod();

    /**
     * ELIMINO SUSCRIPCIONES ANTIGUAS Y CANCELADAS
     * PARA LIMPIEZA AUTOMÁTICA DE BD
     *
     * @param cutoffDate fecha límite para eliminar
     * @return número de registros eliminados
     */
    @Query("DELETE FROM UserSubscription us WHERE us.status = 'CANCELLED' AND us.updatedAt < :cutoffDate")
    int cleanupOldCancelledSubscriptions(@Param("cutoffDate") LocalDateTime cutoffDate);
}