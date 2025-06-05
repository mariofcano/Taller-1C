package com.taskmanager.repository;

import com.taskmanager.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO PARA MANEJAR LOS PLANES DE SUSCRIPCIÓN EN LA BASE DE DATOS
 * PROPORCIONA MÉTODOS PARA REALIZAR OPERACIONES CRUD Y CONSULTAS ESPECÍFICAS
 *
 * @author Mario Flores
 * @version 1.0
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    /**
     * BUSCO UN PLAN POR SU NOMBRE
     * ÚTIL PARA ENCONTRAR EL PLAN GRATUITO O PREMIUM ESPECÍFICO
     *
     * @param name nombre del plan (ej: "FREE", "PREMIUM")
     * @return el plan si existe
     */
    Optional<SubscriptionPlan> findByName(String name);

    /**
     * BUSCO UN PLAN POR SU NOMBRE IGNORANDO MAYÚSCULAS/MINÚSCULAS
     * MÁS FLEXIBLE PARA BÚSQUEDAS
     *
     * @param name nombre del plan
     * @return el plan si existe
     */
    Optional<SubscriptionPlan> findByNameIgnoreCase(String name);

    /**
     * OBTENGO TODOS LOS PLANES ACTIVOS
     * SOLO PLANES QUE SE PUEDEN CONTRATAR
     *
     * @return lista de planes activos ordenados por precio
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.active = true ORDER BY sp.price ASC")
    List<SubscriptionPlan> findAllActivePlans();

    /**
     * BUSCO PLANES POR RANGO DE PRECIO
     * PARA FILTROS DE BÚSQUEDA
     *
     * @param minPrice precio mínimo
     * @param maxPrice precio máximo
     * @return planes en ese rango de precio
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.price BETWEEN :minPrice AND :maxPrice AND sp.active = true")
    List<SubscriptionPlan> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice);

    /**
     * BUSCO EL PLAN GRATUITO
     * PLAN POR DEFECTO PARA NUEVOS USUARIOS
     *
     * @return el plan gratuito (precio = 0)
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.price = 0 AND sp.active = true")
    Optional<SubscriptionPlan> findFreePlan();

    /**
     * BUSCO PLANES PREMIUM (NO GRATUITOS)
     * PLANES DE PAGO DISPONIBLES
     *
     * @return lista de planes premium ordenados por precio
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.price > 0 AND sp.active = true ORDER BY sp.price ASC")
    List<SubscriptionPlan> findPremiumPlans();

    /**
     * VERIFICO SI EXISTE UN PLAN CON ESE NOMBRE
     * PARA VALIDACIONES ANTES DE CREAR NUEVOS PLANES
     *
     * @param name nombre del plan
     * @return true si ya existe
     */
    boolean existsByName(String name);

    /**
     * VERIFICO SI EXISTE UN PLAN CON ESE NOMBRE (IGNORANDO MAYÚSCULAS)
     *
     * @param name nombre del plan
     * @return true si ya existe
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * CUENTO CUÁNTOS PLANES ACTIVOS HAY
     * ESTADÍSTICA PARA ADMINISTRACIÓN
     *
     * @return número de planes activos
     */
    long countByActiveTrue();

    /**
     * BUSCO PLANES QUE TENGAN LÍMITE EN TAREAS
     * PARA ANÁLISIS DE FUNCIONALIDADES
     *
     * @return planes que limitan el número de tareas
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.maxTasks IS NOT NULL AND sp.active = true")
    List<SubscriptionPlan> findPlansWithTaskLimits();

    /**
     * BUSCO PLANES QUE TENGAN LÍMITE EN UBICACIONES
     * PARA ANÁLISIS DE FUNCIONALIDADES
     *
     * @return planes que limitan el número de ubicaciones
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.maxLocations IS NOT NULL AND sp.active = true")
    List<SubscriptionPlan> findPlansWithLocationLimits();

    /**
     * BUSCO PLANES SIN LÍMITES (ILIMITADOS)
     * PLANES PREMIUM CON TODAS LAS FUNCIONALIDADES
     *
     * @return planes sin restricciones
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.maxTasks IS NULL AND sp.maxLocations IS NULL AND sp.active = true")
    List<SubscriptionPlan> findUnlimitedPlans();

    /**
     * BUSCO PLANES ORDENADOS POR POPULARIDAD
     * BASADO EN EL NÚMERO DE SUSCRIPCIONES ACTIVAS
     *
     * @return planes ordenados por número de suscriptores
     */
    @Query("SELECT sp FROM SubscriptionPlan sp LEFT JOIN sp.subscriptions s " +
            "WHERE sp.active = true " +
            "GROUP BY sp " +
            "ORDER BY COUNT(s) DESC")
    List<SubscriptionPlan> findPlansByPopularity();

    /**
     * OBTENGO ESTADÍSTICAS DE SUSCRIPTORES POR PLAN
     * PARA DASHBOARD ADMINISTRATIVO
     *
     * @param planId ID del plan
     * @return número de suscriptores activos
     */
    @Query("SELECT COUNT(s) FROM UserSubscription s WHERE s.subscriptionPlan.id = :planId AND s.status = 'ACTIVE'")
    long countActiveSubscribersByPlan(@Param("planId") Long planId);

    /**
     * BUSCO PLANES QUE CONTENGAN TEXTO EN NOMBRE O DESCRIPCIÓN
     * PARA BÚSQUEDA ADMINISTRATIVA
     *
     * @param searchTerm término a buscar
     * @return planes que coincidan con la búsqueda
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE " +
            "(LOWER(sp.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(sp.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND sp.active = true")
    List<SubscriptionPlan> searchPlans(@Param("searchTerm") String searchTerm);
}