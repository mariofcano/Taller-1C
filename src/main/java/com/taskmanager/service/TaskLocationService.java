package com.taskmanager.service;

import com.taskmanager.model.TaskLocation;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SERVICIO QUE MANEJA TODA LA LÓGICA DE NEGOCIO DE LAS UBICACIONES DE TAREAS
 * INCLUYE VALIDACIONES DE LÍMITES SEGÚN EL PLAN DE SUSCRIPCIÓN
 *
 * @author Mario Flores
 * @version 1.0
 */
@Service
public class TaskLocationService {

    @Autowired
    private TaskLocationRepository taskLocationRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * OBTENGO TODAS LAS UBICACIONES DE UN USUARIO ESPECÍFICO
     * INCLUYE TANTO UBICACIONES ACTIVAS COMO INACTIVAS
     *
     * @param user el usuario propietario de las ubicaciones
     * @return lista completa de ubicaciones del usuario
     */
    public List<TaskLocation> getAllLocationsByUser(User user) {
        return taskLocationRepository.findByUser(user);
    }

    /**
     * OBTENGO SOLO LAS UBICACIONES ACTIVAS DE UN USUARIO
     * ORDENADAS ALFABÉTICAMENTE POR NOMBRE
     *
     * @param user el usuario propietario
     * @return lista de ubicaciones activas ordenadas
     */
    public List<TaskLocation> getActiveLocationsByUser(User user) {
        return taskLocationRepository.findActiveLocationsByUserOrderByName(user);
    }

    /**
     * BUSCO UNA UBICACIÓN POR SU ID ÚNICO
     * MÉTODO BÁSICO PARA OPERACIONES DE LECTURA Y EDICIÓN
     *
     * @param id identificador único de la ubicación
     * @return la ubicación si existe, vacío si no existe
     */
    public Optional<TaskLocation> getLocationById(Long id) {
        return taskLocationRepository.findById(id);
    }

    /**
     * GUARDO UNA UBICACIÓN NUEVA O ACTUALIZO UNA EXISTENTE
     * MÉTODO GENÉRICO PARA PERSISTENCIA
     *
     * @param location la ubicación a guardar
     * @return la ubicación guardada con ID asignado
     */
    public TaskLocation saveLocation(TaskLocation location) {
        return taskLocationRepository.save(location);
    }

    /**
     * CREO UNA NUEVA UBICACIÓN CON LOS DATOS BÁSICOS
     * INCLUYE VALIDACIONES DE COORDENADAS, DUPLICADOS Y LÍMITES DEL PLAN
     *
     * @param name nombre descriptivo de la ubicación
     * @param description descripción adicional opcional
     * @param latitude coordenada de latitud GPS
     * @param longitude coordenada de longitud GPS
     * @param address dirección física opcional
     * @param user usuario propietario de la ubicación
     * @return la ubicación creada
     * @throws IllegalArgumentException si las coordenadas son inválidas
     * @throws RuntimeException si ya existe una ubicación en esas coordenadas o se excede el límite
     */
    public TaskLocation createLocation(String name, String description, Double latitude,
                                       Double longitude, String address, User user) {

        // VALIDACIONES BÁSICAS DE COORDENADAS
        if (!isValidLatitude(latitude)) {
            throw new IllegalArgumentException("Latitud debe estar entre -90.0 y 90.0");
        }

        if (!isValidLongitude(longitude)) {
            throw new IllegalArgumentException("Longitud debe estar entre -180.0 y 180.0");
        }

        // VALIDACIÓN DE LÍMITES DEL PLAN DE SUSCRIPCIÓN
        if (!subscriptionService.canCreateMoreLocations(user)) {
            // OBTENGO INFORMACIÓN DEL LÍMITE PARA EL MENSAJE DE ERROR
            SubscriptionService.SubscriptionUsageStats stats = subscriptionService.getUserUsageStats(user);

            String errorMessage = String.format(
                    "Has alcanzado el límite de ubicaciones de tu plan (%d/%d). " +
                            "Upgrade a Premium para crear ubicaciones ilimitadas.",
                    stats.getCurrentLocations(),
                    stats.getMaxLocations()
            );

            throw new RuntimeException(errorMessage);
        }

        // VERIFICO QUE NO EXISTA YA UNA UBICACIÓN EN ESAS COORDENADAS EXACTAS
        if (taskLocationRepository.existsByCoordinatesAndUser(latitude, longitude, user)) {
            throw new RuntimeException("Ya existe una ubicación en estas coordenadas");
        }

        // SI PASA TODAS LAS VALIDACIONES, CREAR LA UBICACIÓN
        TaskLocation location = new TaskLocation(name, description, latitude, longitude, user);
        location.setAddress(address);

        return taskLocationRepository.save(location);
    }

    /**
     * ACTUALIZO UNA UBICACIÓN EXISTENTE CON NUEVOS DATOS
     * INCLUYE VALIDACIONES Y VERIFICACIÓN DE PERTENENCIA AL USUARIO
     *
     * @param id identificador de la ubicación a actualizar
     * @param name nuevo nombre
     * @param description nueva descripción
     * @param latitude nueva latitud
     * @param longitude nueva longitud
     * @param address nueva dirección
     * @param active nuevo estado activo
     * @param user usuario que realiza la actualización
     * @return la ubicación actualizada o null si no existe o no pertenece al usuario
     * @throws IllegalArgumentException si las coordenadas son inválidas
     */
    public TaskLocation updateLocation(Long id, String name, String description, Double latitude,
                                       Double longitude, String address, Boolean active, User user) {

        Optional<TaskLocation> locationOpt = taskLocationRepository.findById(id);

        if (locationOpt.isPresent()) {
            TaskLocation location = locationOpt.get();

            // VERIFICO QUE LA UBICACIÓN PERTENEZCA AL USUARIO
            if (!location.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("No tienes permisos para modificar esta ubicación");
            }

            // VALIDO LAS NUEVAS COORDENADAS
            if (!isValidLatitude(latitude)) {
                throw new IllegalArgumentException("Latitud debe estar entre -90.0 y 90.0");
            }

            if (!isValidLongitude(longitude)) {
                throw new IllegalArgumentException("Longitud debe estar entre -180.0 y 180.0");
            }

            // VERIFICO DUPLICADOS SOLO SI LAS COORDENADAS CAMBIARON
            if (!location.getLatitude().equals(latitude) || !location.getLongitude().equals(longitude)) {
                if (taskLocationRepository.existsByCoordinatesAndUser(latitude, longitude, user)) {
                    throw new RuntimeException("Ya existe una ubicación en estas coordenadas");
                }
            }

            // ACTUALIZO LOS CAMPOS
            location.setName(name);
            location.setDescription(description);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAddress(address);
            location.setActive(active != null ? active : true);

            return taskLocationRepository.save(location);
        }

        return null;
    }

    /**
     * CAMBIO EL ESTADO ACTIVO/INACTIVO DE UNA UBICACIÓN
     * MÉTODO RÁPIDO PARA ACTIVAR O DESACTIVAR UBICACIONES
     *
     * @param id identificador de la ubicación
     * @param user usuario que realiza el cambio
     * @return true si se cambió correctamente, false si no existe o no pertenece al usuario
     */
    public boolean toggleLocationActive(Long id, User user) {
        Optional<TaskLocation> locationOpt = taskLocationRepository.findById(id);

        if (locationOpt.isPresent()) {
            TaskLocation location = locationOpt.get();

            // VERIFICO PERTENENCIA AL USUARIO
            if (!location.getUser().getId().equals(user.getId())) {
                return false;
            }

            // CAMBIO EL ESTADO
            location.setActive(!location.getActive());
            taskLocationRepository.save(location);
            return true;
        }

        return false;
    }

    /**
     * ELIMINO UNA UBICACIÓN PERMANENTEMENTE
     * INCLUYE VERIFICACIÓN DE PERTENENCIA AL USUARIO
     *
     * @param id identificador de la ubicación a eliminar
     * @param user usuario que realiza la eliminación
     * @return true si se eliminó correctamente, false si no existe o no pertenece al usuario
     */
    public boolean deleteLocation(Long id, User user) {
        Optional<TaskLocation> locationOpt = taskLocationRepository.findById(id);

        if (locationOpt.isPresent()) {
            TaskLocation location = locationOpt.get();

            // VERIFICO PERTENENCIA AL USUARIO
            if (!location.getUser().getId().equals(user.getId())) {
                return false;
            }

            taskLocationRepository.deleteById(id);
            return true;
        }

        return false;
    }

    /**
     * BUSCO UBICACIONES POR TEXTO EN NOMBRE O DESCRIPCIÓN
     * BÚSQUEDA AMPLIA PARA UN USUARIO ESPECÍFICO
     *
     * @param searchTerm texto a buscar
     * @param user usuario propietario
     * @return ubicaciones que contengan el texto
     */
    public List<TaskLocation> searchLocations(String searchTerm, User user) {
        return taskLocationRepository.findByNameOrDescriptionContaining(searchTerm, user);
    }

    /**
     * OBTENGO UBICACIONES DENTRO DE UN ÁREA GEOGRÁFICA
     * ÚTIL PARA MAPAS CON LÍMITES ESPECÍFICOS
     *
     * @param minLat latitud mínima
     * @param maxLat latitud máxima
     * @param minLng longitud mínima
     * @param maxLng longitud máxima
     * @param user usuario propietario
     * @return ubicaciones dentro del área
     */
    public List<TaskLocation> getLocationsInBounds(Double minLat, Double maxLat,
                                                   Double minLng, Double maxLng, User user) {
        return taskLocationRepository.findLocationsInBounds(minLat, maxLat, minLng, maxLng, user);
    }

    /**
     * BUSCO UBICACIONES CERCA DE UN PUNTO ESPECÍFICO
     * USA DISTANCIA HAVERSINE PARA CÁLCULO DE PROXIMIDAD
     *
     * @param latitude latitud del punto central
     * @param longitude longitud del punto central
     * @param radiusKm radio de búsqueda en kilómetros
     * @param user usuario propietario
     * @return ubicaciones dentro del radio
     */
    public List<TaskLocation> getLocationsNearby(Double latitude, Double longitude,
                                                 Double radiusKm, User user) {
        return taskLocationRepository.findLocationsWithinRadius(latitude, longitude, radiusKm, user);
    }

    /**
     * CUENTO LAS UBICACIONES ACTIVAS DE UN USUARIO
     * ESTADÍSTICA PARA DASHBOARD
     *
     * @param user el usuario a contar
     * @return número de ubicaciones activas
     */
    public long countActiveLocations(User user) {
        return taskLocationRepository.countActiveLocationsByUser(user);
    }

    /**
     * CUENTO LAS UBICACIONES INACTIVAS DE UN USUARIO
     * ESTADÍSTICA COMPLEMENTARIA
     *
     * @param user el usuario a contar
     * @return número de ubicaciones inactivas
     */
    public long countInactiveLocations(User user) {
        return taskLocationRepository.countInactiveLocationsByUser(user);
    }

    /**
     * OBTENGO LAS UBICACIONES MÁS RECIENTES DE UN USUARIO
     * PARA MOSTRAR EN HISTORIAL O DASHBOARD
     *
     * @param user el usuario propietario
     * @return las 5 ubicaciones más recientes
     */
    public List<TaskLocation> getRecentLocations(User user) {
        return taskLocationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    /**
     * VERIFICO SI UN USUARIO PUEDE CREAR MÁS UBICACIONES
     * MÉTODO DE CONVENIENCIA QUE DELEGA AL SUBSCRIPTION SERVICE
     *
     * @param user el usuario a verificar
     * @return true si puede crear más ubicaciones, false si ya alcanzó el límite
     */
    public boolean canUserCreateMoreLocations(User user) {
        return subscriptionService.canCreateMoreLocations(user);
    }

    /**
     * OBTENGO INFORMACIÓN SOBRE LOS LÍMITES DE UBICACIONES DEL USUARIO
     * ÚTIL PARA MOSTRAR EN LA UI
     *
     * @param user el usuario
     * @return estadísticas de uso de ubicaciones
     */
    public SubscriptionService.SubscriptionUsageStats getLocationUsageStats(User user) {
        return subscriptionService.getUserUsageStats(user);
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * VALIDO QUE LA LATITUD ESTÉ EN EL RANGO CORRECTO
     *
     * @param latitude coordenada a validar
     * @return true si está entre -90.0 y 90.0
     */
    private boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * VALIDO QUE LA LONGITUD ESTÉ EN EL RANGO CORRECTO
     *
     * @param longitude coordenada a validar
     * @return true si está entre -180.0 y 180.0
     */
    private boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0;
    }
}