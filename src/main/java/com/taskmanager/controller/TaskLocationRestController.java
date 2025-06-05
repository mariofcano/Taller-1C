package com.taskmanager.controller;

import com.taskmanager.model.TaskLocation;
import com.taskmanager.model.User;
import com.taskmanager.service.TaskLocationService;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CONTROLADOR REST QUE MANEJA LAS OPERACIONES API DE UBICACIONES DE TAREAS
 * DEVUELVE DATOS EN FORMATO JSON PARA SER CONSUMIDOS POR JAVASCRIPT
 * IMPLEMENTA ENDPOINTS COMPLETOS PARA CRUD DE UBICACIONES
 *
 * @author Mario Flores
 * @version 1.0
 */
@RestController
@RequestMapping("/api/locations")
public class TaskLocationRestController {

    @Autowired
    private TaskLocationService taskLocationService;

    @Autowired
    private UserService userService;

    /**
     * OBTENGO TODAS LAS UBICACIONES DEL USUARIO AUTENTICADO
     * ENDPOINT: GET /api/locations
     *
     * @param auth información del usuario autenticado
     * @return ResponseEntity con lista de ubicaciones en JSON
     */
    @GetMapping
    public ResponseEntity<List<TaskLocation>> getAllLocations(Authentication auth) {
        try {
            User currentUser = getUserFromAuth(auth);
            List<TaskLocation> locations = taskLocationService.getAllLocationsByUser(currentUser);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * OBTENGO SOLO LAS UBICACIONES ACTIVAS DEL USUARIO
     * ENDPOINT: GET /api/locations/active
     *
     * @param auth información del usuario autenticado
     * @return ResponseEntity con ubicaciones activas en JSON
     */
    @GetMapping("/active")
    public ResponseEntity<List<TaskLocation>> getActiveLocations(Authentication auth) {
        try {
            User currentUser = getUserFromAuth(auth);
            List<TaskLocation> locations = taskLocationService.getActiveLocationsByUser(currentUser);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * OBTENGO UNA UBICACIÓN ESPECÍFICA POR SU ID
     * ENDPOINT: GET /api/locations/{id}
     *
     * @param id identificador de la ubicación
     * @param auth información del usuario autenticado
     * @return ResponseEntity con la ubicación o error 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskLocation> getLocationById(@PathVariable Long id, Authentication auth) {
        try {
            User currentUser = getUserFromAuth(auth);
            Optional<TaskLocation> locationOpt = taskLocationService.getLocationById(id);

            if (locationOpt.isPresent()) {
                TaskLocation location = locationOpt.get();

                // VERIFICO QUE LA UBICACIÓN PERTENEZCA AL USUARIO
                if (location.getUser().getId().equals(currentUser.getId())) {
                    return ResponseEntity.ok(location);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CREO UNA NUEVA UBICACIÓN
     * ENDPOINT: POST /api/locations
     *
     * @param locationData datos de la nueva ubicación en JSON
     * @param auth información del usuario autenticado
     * @return ResponseEntity con la ubicación creada o errores de validación
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createLocation(@RequestBody Map<String, Object> locationData,
                                                              Authentication auth) {
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = getUserFromAuth(auth);

            // EXTRAIGO LOS DATOS DEL JSON
            String name = (String) locationData.get("name");
            String description = (String) locationData.get("description");
            Double latitude = Double.valueOf(locationData.get("latitude").toString());
            Double longitude = Double.valueOf(locationData.get("longitude").toString());
            String address = (String) locationData.get("address");

            // VALIDACIONES BÁSICAS
            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "El nombre es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }

            // CREO LA UBICACIÓN
            TaskLocation newLocation = taskLocationService.createLocation(
                    name.trim(), description, latitude, longitude, address, currentUser
            );

            response.put("success", true);
            response.put("message", "Ubicación creada exitosamente");
            response.put("location", newLocation);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ACTUALIZO UNA UBICACIÓN EXISTENTE
     * ENDPOINT: PUT /api/locations/{id}
     *
     * @param id identificador de la ubicación a actualizar
     * @param locationData nuevos datos en JSON
     * @param auth información del usuario autenticado
     * @return ResponseEntity con resultado de la actualización
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateLocation(@PathVariable Long id,
                                                              @RequestBody Map<String, Object> locationData,
                                                              Authentication auth) {
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = getUserFromAuth(auth);

            // EXTRAIGO LOS DATOS DEL JSON
            String name = (String) locationData.get("name");
            String description = (String) locationData.get("description");
            Double latitude = Double.valueOf(locationData.get("latitude").toString());
            Double longitude = Double.valueOf(locationData.get("longitude").toString());
            String address = (String) locationData.get("address");
            Boolean active = Boolean.valueOf(locationData.get("active").toString());

            // VALIDACIONES BÁSICAS
            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "El nombre es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }

            // ACTUALIZO LA UBICACIÓN
            TaskLocation updatedLocation = taskLocationService.updateLocation(
                    id, name.trim(), description, latitude, longitude, address, active, currentUser
            );

            if (updatedLocation != null) {
                response.put("success", true);
                response.put("message", "Ubicación actualizada exitosamente");
                response.put("location", updatedLocation);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Ubicación no encontrada");
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * CAMBIO EL ESTADO ACTIVO/INACTIVO DE UNA UBICACIÓN
     * ENDPOINT: PATCH /api/locations/{id}/toggle
     *
     * @param id identificador de la ubicación
     * @param auth información del usuario autenticado
     * @return ResponseEntity con resultado del cambio
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleLocationStatus(@PathVariable Long id,
                                                                    Authentication auth) {
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = getUserFromAuth(auth);

            boolean success = taskLocationService.toggleLocationActive(id, currentUser);

            if (success) {
                response.put("success", true);
                response.put("message", "Estado de ubicación cambiado exitosamente");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Ubicación no encontrada o sin permisos");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ELIMINO UNA UBICACIÓN PERMANENTEMENTE
     * ENDPOINT: DELETE /api/locations/{id}
     *
     * @param id identificador de la ubicación a eliminar
     * @param auth información del usuario autenticado
     * @return ResponseEntity con resultado de la eliminación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLocation(@PathVariable Long id,
                                                              Authentication auth) {
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = getUserFromAuth(auth);

            boolean success = taskLocationService.deleteLocation(id, currentUser);

            if (success) {
                response.put("success", true);
                response.put("message", "Ubicación eliminada exitosamente");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Ubicación no encontrada o sin permisos");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * BUSCO UBICACIONES POR TEXTO EN NOMBRE O DESCRIPCIÓN
     * ENDPOINT: GET /api/locations/search?q={query}
     *
     * @param query texto a buscar
     * @param auth información del usuario autenticado
     * @return ResponseEntity con ubicaciones que coincidan
     */
    @GetMapping("/search")
    public ResponseEntity<List<TaskLocation>> searchLocations(@RequestParam("q") String query,
                                                              Authentication auth) {
        try {
            User currentUser = getUserFromAuth(auth);
            List<TaskLocation> locations = taskLocationService.searchLocations(query, currentUser);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * OBTENGO UBICACIONES DENTRO DE UN ÁREA GEOGRÁFICA
     * ENDPOINT: GET /api/locations/bounds?minLat={}&maxLat={}&minLng={}&maxLng={}
     *
     * @param minLat latitud mínima
     * @param maxLat latitud máxima
     * @param minLng longitud mínima
     * @param maxLng longitud máxima
     * @param auth información del usuario autenticado
     * @return ResponseEntity con ubicaciones en el área
     */
    @GetMapping("/bounds")
    public ResponseEntity<List<TaskLocation>> getLocationsInBounds(
            @RequestParam Double minLat, @RequestParam Double maxLat,
            @RequestParam Double minLng, @RequestParam Double maxLng,
            Authentication auth) {
        try {
            User currentUser = getUserFromAuth(auth);
            List<TaskLocation> locations = taskLocationService.getLocationsInBounds(
                    minLat, maxLat, minLng, maxLng, currentUser
            );
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * OBTENGO ESTADÍSTICAS DE UBICACIONES DEL USUARIO
     * ENDPOINT: GET /api/locations/stats
     *
     * @param auth información del usuario autenticado
     * @return ResponseEntity con estadísticas en JSON
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLocationStats(Authentication auth) {
        try {
            User currentUser = getUserFromAuth(auth);

            Map<String, Object> stats = new HashMap<>();
            stats.put("activeCount", taskLocationService.countActiveLocations(currentUser));
            stats.put("inactiveCount", taskLocationService.countInactiveLocations(currentUser));
            stats.put("totalCount", taskLocationService.countActiveLocations(currentUser) +
                    taskLocationService.countInactiveLocations(currentUser));
            stats.put("recentLocations", taskLocationService.getRecentLocations(currentUser));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * MÉTODO AUXILIAR PARA OBTENER EL USUARIO DESDE LA AUTENTICACIÓN
     *
     * @param auth objeto de autenticación de Spring Security
     * @return el usuario logueado
     */
    private User getUserFromAuth(Authentication auth) {
        String username = auth.getName();
        return userService.findByUsername(username);
    }
}