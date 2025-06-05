package com.taskmanager.repository;

import com.taskmanager.model.TaskLocation;
import com.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * REPOSITORIO PARA MANEJAR LAS UBICACIONES DE TAREAS EN LA BASE DE DATOS
 * PROPORCIONA MÉTODOS PARA REALIZAR OPERACIONES CRUD Y CONSULTAS ESPECÍFICAS
 *
 * @author Mario Flores
 * @version 1.0
 */
@Repository
public interface TaskLocationRepository extends JpaRepository<TaskLocation, Long> {

    /**
     * BUSCO TODAS LAS UBICACIONES DE UN USUARIO ESPECÍFICO
     * DEVUELVE SOLO LAS UBICACIONES QUE PERTENECEN AL USUARIO DADO
     *
     * @param user el usuario propietario de las ubicaciones
     * @return lista de ubicaciones del usuario
     */
    List<TaskLocation> findByUser(User user);

    /**
     * BUSCO UBICACIONES POR ESTADO ACTIVO Y USUARIO
     * PERMITE FILTRAR UBICACIONES ACTIVAS O INACTIVAS DE UN USUARIO
     *
     * @param active true para ubicaciones activas, false para inactivas
     * @param user el usuario propietario
     * @return lista filtrada de ubicaciones
     */
    List<TaskLocation> findByActiveAndUser(Boolean active, User user);

    /**
     * BUSCO UBICACIONES QUE CONTENGAN UN TEXTO EN EL NOMBRE
     * BÚSQUEDA INSENSIBLE A MAYÚSCULAS/MINÚSCULAS PARA UN USUARIO ESPECÍFICO
     *
     * @param name texto a buscar en el nombre de la ubicación
     * @param user el usuario propietario
     * @return ubicaciones que coincidan con la búsqueda
     */
    List<TaskLocation> findByNameContainingIgnoreCaseAndUser(String name, User user);

    /**
     * BUSCO UBICACIONES QUE CONTENGAN TEXTO EN NOMBRE O DESCRIPCIÓN
     * BÚSQUEDA AMPLIA PARA UN USUARIO ESPECÍFICO
     *
     * @param searchTerm texto a buscar en nombre o descripción
     * @param user el usuario propietario
     * @return ubicaciones que coincidan en nombre o descripción
     */
    @Query("SELECT tl FROM TaskLocation tl WHERE tl.user = :user AND " +
            "(LOWER(tl.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tl.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<TaskLocation> findByNameOrDescriptionContaining(@Param("searchTerm") String searchTerm,
                                                         @Param("user") User user);

    /**
     * BUSCO UBICACIONES DENTRO DE UN ÁREA GEOGRÁFICA RECTANGULAR
     * ÚTIL PARA MOSTRAR UBICACIONES EN UN MAPA CON LÍMITES ESPECÍFICOS
     *
     * @param minLat latitud mínima del área
     * @param maxLat latitud máxima del área
     * @param minLng longitud mínima del área
     * @param maxLng longitud máxima del área
     * @param user el usuario propietario
     * @return ubicaciones dentro del área especificada
     */
    @Query("SELECT tl FROM TaskLocation tl WHERE tl.user = :user AND " +
            "tl.latitude BETWEEN :minLat AND :maxLat AND " +
            "tl.longitude BETWEEN :minLng AND :maxLng AND " +
            "tl.active = true")
    List<TaskLocation> findLocationsInBounds(@Param("minLat") Double minLat,
                                             @Param("maxLat") Double maxLat,
                                             @Param("minLng") Double minLng,
                                             @Param("maxLng") Double maxLng,
                                             @Param("user") User user);

    /**
     * CUENTO CUÁNTAS UBICACIONES ACTIVAS TIENE UN USUARIO
     * ESTADÍSTICA ÚTIL PARA EL DASHBOARD
     *
     * @param user el usuario a contar
     * @return número de ubicaciones activas
     */
    @Query("SELECT COUNT(tl) FROM TaskLocation tl WHERE tl.user = :user AND tl.active = true")
    long countActiveLocationsByUser(@Param("user") User user);

    /**
     * CUENTO CUÁNTAS UBICACIONES INACTIVAS TIENE UN USUARIO
     * ESTADÍSTICA COMPLEMENTARIA PARA ADMINISTRACIÓN
     *
     * @param user el usuario a contar
     * @return número de ubicaciones inactivas
     */
    @Query("SELECT COUNT(tl) FROM TaskLocation tl WHERE tl.user = :user AND tl.active = false")
    long countInactiveLocationsByUser(@Param("user") User user);

    /**
     * OBTENGO LAS UBICACIONES MÁS RECIENTES DE UN USUARIO
     * PARA MOSTRAR EN DASHBOARD O HISTORIAL RECIENTE
     *
     * @param user el usuario propietario
     * @return las 5 ubicaciones más recientes
     */
    @Query("SELECT tl FROM TaskLocation tl WHERE tl.user = :user " +
            "ORDER BY tl.createdAt DESC")
    List<TaskLocation> findTop5ByUserOrderByCreatedAtDesc(@Param("user") User user);

    /**
     * OBTENGO TODAS LAS UBICACIONES ACTIVAS DE UN USUARIO ORDENADAS POR NOMBRE
     * ÚTIL PARA LISTADOS ORGANIZADOS ALFABÉTICAMENTE
     *
     * @param user el usuario propietario
     * @return ubicaciones activas ordenadas por nombre
     */
    @Query("SELECT tl FROM TaskLocation tl WHERE tl.user = :user AND tl.active = true " +
            "ORDER BY tl.name ASC")
    List<TaskLocation> findActiveLocationsByUserOrderByName(@Param("user") User user);

    /**
     * VERIFICO SI EXISTE UNA UBICACIÓN CON COORDENADAS EXACTAS PARA UN USUARIO
     * PREVIENE DUPLICADOS EN LA MISMA POSICIÓN GPS
     *
     * @param latitude coordenada de latitud
     * @param longitude coordenada de longitud
     * @param user el usuario propietario
     * @return true si ya existe una ubicación en esas coordenadas
     */
    @Query("SELECT COUNT(tl) > 0 FROM TaskLocation tl WHERE " +
            "tl.latitude = :latitude AND tl.longitude = :longitude AND tl.user = :user")
    boolean existsByCoordinatesAndUser(@Param("latitude") Double latitude,
                                       @Param("longitude") Double longitude,
                                       @Param("user") User user);

    /**
     * BUSCO UBICACIONES POR PROXIMIDAD A UN PUNTO ESPECÍFICO
     * USA FÓRMULA DE DISTANCIA HAVERSINE APROXIMADA PARA BÚSQUEDA LOCAL
     * RADIO DE BÚSQUEDA EN KILÓMETROS
     *
     * @param latitude latitud del punto central
     * @param longitude longitud del punto central
     * @param radiusKm radio de búsqueda en kilómetros
     * @param user el usuario propietario
     * @return ubicaciones dentro del radio especificado
     */
    @Query("SELECT tl FROM TaskLocation tl WHERE tl.user = :user AND tl.active = true AND " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(tl.latitude)) * " +
            "cos(radians(tl.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(tl.latitude)))) <= :radiusKm")
    List<TaskLocation> findLocationsWithinRadius(@Param("latitude") Double latitude,
                                                 @Param("longitude") Double longitude,
                                                 @Param("radiusKm") Double radiusKm,
                                                 @Param("user") User user);
}