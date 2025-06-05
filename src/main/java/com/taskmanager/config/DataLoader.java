package com.taskmanager.config;

import com.taskmanager.model.SubscriptionPlan;
import com.taskmanager.model.User;
import com.taskmanager.model.UserRole;
import com.taskmanager.model.UserSubscription;
import com.taskmanager.repository.SubscriptionPlanRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.repository.UserSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * CLASE QUE SE EJECUTA AL ARRANCAR LA APLICACIÓN
 * CREO USUARIOS DE PRUEBA Y PLANES DE SUSCRIPCIÓN INICIALES
 *
 * @author Mario Flores
 * @version 1.0
 */
@Component
public class DataLoader implements CommandLineRunner {

    // DEPENDENCIAS QUE NECESITO
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    /**
     * ESTE MÉTODO SE EJECUTA AL ARRANCAR SPRING BOOT
     * AQUÍ CREO LOS DATOS INICIALES SI NO EXISTEN
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INICIANDO CARGA DE DATOS INICIALES ===");

        // CREAR PLANES DE SUSCRIPCIÓN PRIMERO
        createSubscriptionPlans();

        // LUEGO CREAR USUARIOS Y ASIGNAR SUSCRIPCIONES
        createInitialUsers();

        System.out.println("=== CARGA DE DATOS COMPLETADA ===");
    }

    /**
     * CREO LOS PLANES DE SUSCRIPCIÓN POR DEFECTO
     * PLAN GRATUITO Y PLAN PREMIUM
     */
    private void createSubscriptionPlans() {
        // VERIFICO SI YA EXISTEN PLANES
        if (subscriptionPlanRepository.count() == 0) {
            System.out.println("=== CREANDO PLANES DE SUSCRIPCIÓN ===");

            // PLAN GRATUITO
            SubscriptionPlan freePlan = new SubscriptionPlan(
                    "FREE",
                    "Plan Gratuito - Funcionalidades básicas",
                    BigDecimal.ZERO,
                    10,  // Máximo 10 tareas
                    5    // Máximo 5 ubicaciones
            );
            freePlan.setFeatures("Gestión básica de tareas,Mapas interactivos,Soporte por email");
            subscriptionPlanRepository.save(freePlan);
            System.out.println("✅ PLAN GRATUITO CREADO: " + freePlan.getName());

            // PLAN PREMIUM BÁSICO
            SubscriptionPlan premiumPlan = new SubscriptionPlan(
                    "PREMIUM",
                    "Plan Premium - Todas las funcionalidades",
                    new BigDecimal("9.99"),
                    null,  // Tareas ilimitadas
                    null   // Ubicaciones ilimitadas
            );
            premiumPlan.setFeatures("Tareas ilimitadas,Ubicaciones ilimitadas,Estadísticas avanzadas,Soporte prioritario,Sin anuncios,Exportación de datos");
            subscriptionPlanRepository.save(premiumPlan);
            System.out.println("✅ PLAN PREMIUM CREADO: " + premiumPlan.getName() + " - €" + premiumPlan.getPrice());

            // PLAN PREMIUM ANUAL (OPCIONAL)
            SubscriptionPlan premiumAnnualPlan = new SubscriptionPlan(
                    "PREMIUM_ANNUAL",
                    "Plan Premium Anual - 2 meses gratis",
                    new BigDecimal("99.99"),
                    null,  // Tareas ilimitadas
                    null   // Ubicaciones ilimitadas
            );
            premiumAnnualPlan.setFeatures("Todas las funcionalidades Premium,Descuento del 16%,Facturación anual,Soporte telefónico,Acceso beta");
            subscriptionPlanRepository.save(premiumAnnualPlan);
            System.out.println("✅ PLAN PREMIUM ANUAL CREADO: " + premiumAnnualPlan.getName() + " - €" + premiumAnnualPlan.getPrice());

            System.out.println("=== PLANES DE SUSCRIPCIÓN CREADOS ===");
        } else {
            System.out.println("=== PLANES DE SUSCRIPCIÓN YA EXISTEN ===");
        }
    }

    /**
     * CREO LOS USUARIOS INICIALES DEL SISTEMA
     * UN ADMINISTRADOR Y USUARIOS NORMALES CON DIFERENTES PLANES
     */
    private void createInitialUsers() {
        // VERIFICO SI YA HAY USUARIOS EN LA BD
        if (userRepository.count() == 0) {
            System.out.println("=== CREANDO USUARIOS DE PRUEBA ===");

            // OBTENGO LOS PLANES CREADOS
            SubscriptionPlan freePlan = subscriptionPlanRepository.findByName("FREE").orElse(null);
            SubscriptionPlan premiumPlan = subscriptionPlanRepository.findByName("PREMIUM").orElse(null);

            // CREO EL USUARIO ADMINISTRADOR
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@taskmanager.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setEnabled(true);
            admin = userRepository.save(admin);

            // ASIGNO PLAN PREMIUM AL ADMIN
            if (premiumPlan != null) {
                UserSubscription adminSubscription = new UserSubscription(admin, premiumPlan);
                adminSubscription.setPaymentReference("ADMIN_PREMIUM_GRANT");
                userSubscriptionRepository.save(adminSubscription);
                System.out.println("✅ ADMIN CREADO: admin@taskmanager.com / admin123 (PREMIUM)");
            }

            // CREO UN USUARIO NORMAL CON PLAN PREMIUM
            User mario = new User();
            mario.setUsername("mario");
            mario.setEmail("mario@taskmanager.com");
            mario.setPassword(passwordEncoder.encode("mario123"));
            mario.setRole(UserRole.USER);
            mario.setEnabled(true);
            mario = userRepository.save(mario);

            // ASIGNO PLAN PREMIUM A MARIO
            if (premiumPlan != null) {
                UserSubscription marioSubscription = new UserSubscription(mario, premiumPlan);
                marioSubscription.setPaymentReference("DEMO_PREMIUM_USER");
                userSubscriptionRepository.save(marioSubscription);
                System.out.println("✅ USUARIO MARIO CREADO: mario@taskmanager.com / mario123 (PREMIUM)");
            }

            // CREO UN USUARIO DE PRUEBA CON PLAN GRATUITO
            User testUser = new User();
            testUser.setUsername("test");
            testUser.setEmail("test@taskmanager.com");
            testUser.setPassword(passwordEncoder.encode("test123"));
            testUser.setRole(UserRole.USER);
            testUser.setEnabled(true);
            testUser = userRepository.save(testUser);

            // ASIGNO PLAN GRATUITO AL USUARIO TEST
            if (freePlan != null) {
                UserSubscription testSubscription = new UserSubscription(testUser, freePlan);
                testSubscription.setPaymentReference("FREE_PLAN");
                userSubscriptionRepository.save(testSubscription);
                System.out.println("✅ USUARIO TEST CREADO: test@taskmanager.com / test123 (FREE)");
            }

            // CREO UN USUARIO DEMO DESHABILITADO
            User demoUser = new User();
            demoUser.setUsername("demo");
            demoUser.setEmail("demo@taskmanager.com");
            demoUser.setPassword(passwordEncoder.encode("demo123"));
            demoUser.setRole(UserRole.USER);
            demoUser.setEnabled(false); // Usuario deshabilitado para pruebas
            demoUser = userRepository.save(demoUser);

            // ASIGNO PLAN GRATUITO AL USUARIO DEMO
            if (freePlan != null) {
                UserSubscription demoSubscription = new UserSubscription(demoUser, freePlan);
                demoSubscription.setPaymentReference("DEMO_FREE_DISABLED");
                userSubscriptionRepository.save(demoSubscription);
                System.out.println("✅ USUARIO DEMO CREADO: demo@taskmanager.com / demo123 (FREE - DESHABILITADO)");
            }

            // CREO UN USUARIO PREMIUM ADICIONAL
            User premiumUser = new User();
            premiumUser.setUsername("premium");
            premiumUser.setEmail("premium@taskmanager.com");
            premiumUser.setPassword(passwordEncoder.encode("premium123"));
            premiumUser.setRole(UserRole.USER);
            premiumUser.setEnabled(true);
            premiumUser = userRepository.save(premiumUser);

            // ASIGNO PLAN PREMIUM
            if (premiumPlan != null) {
                UserSubscription premiumSubscription = new UserSubscription(premiumUser, premiumPlan);
                premiumSubscription.setPaymentReference("DEMO_PREMIUM_USER_2");
                userSubscriptionRepository.save(premiumSubscription);
                System.out.println("✅ USUARIO PREMIUM CREADO: premium@taskmanager.com / premium123 (PREMIUM)");
            }

            System.out.println("=== USUARIOS CREADOS ===");
        } else {
            System.out.println("=== USUARIOS YA EXISTEN ===");
        }
    }

    /**
     * MÉTODO AUXILIAR PARA MOSTRAR ESTADÍSTICAS DE LA CARGA DE DATOS
     */
    private void showDataStatistics() {
        long totalUsers = userRepository.count();
        long totalPlans = subscriptionPlanRepository.count();
        long totalSubscriptions = userSubscriptionRepository.count();
        long adminUsers = userRepository.countByRole(UserRole.ADMIN);
        long normalUsers = userRepository.countByRole(UserRole.USER);

        System.out.println("=== ESTADÍSTICAS DE DATOS CARGADOS ===");
        System.out.println("📊 Total Usuarios: " + totalUsers);
        System.out.println("👨‍💼 Administradores: " + adminUsers);
        System.out.println("👤 Usuarios Normales: " + normalUsers);
        System.out.println("📋 Planes de Suscripción: " + totalPlans);
        System.out.println("🎯 Suscripciones Activas: " + totalSubscriptions);
        System.out.println("=====================================");
    }

    /**
     * MÉTODO PARA CREAR DATOS DE PRUEBA ADICIONALES (OPCIONAL)
     * PUEDES DESCOMENTAR ESTE MÉTODO Y LLAMARLO DESDE run() SI NECESITAS MÁS DATOS
     */
    private void createSampleData() {
        System.out.println("=== CREANDO DATOS DE PRUEBA ADICIONALES ===");

        // AQUÍ PUEDES AGREGAR MÁS USUARIOS, TAREAS, UBICACIONES, ETC.
        // PARA TENER UNA BASE DE DATOS MÁS RICA EN DATOS DE PRUEBA

        // EJEMPLO: Crear más usuarios con diferentes configuraciones
        for (int i = 1; i <= 5; i++) {
            User sampleUser = new User();
            sampleUser.setUsername("user" + i);
            sampleUser.setEmail("user" + i + "@taskmanager.com");
            sampleUser.setPassword(passwordEncoder.encode("user" + i + "123"));
            sampleUser.setRole(UserRole.USER);
            sampleUser.setEnabled(true);

            sampleUser = userRepository.save(sampleUser);

            // ALTERNAR ENTRE PLANES GRATUITO Y PREMIUM
            SubscriptionPlan planToAssign = (i % 2 == 0) ?
                    subscriptionPlanRepository.findByName("PREMIUM").orElse(null) :
                    subscriptionPlanRepository.findByName("FREE").orElse(null);

            if (planToAssign != null) {
                UserSubscription subscription = new UserSubscription(sampleUser, planToAssign);
                subscription.setPaymentReference("SAMPLE_DATA_" + i);
                userSubscriptionRepository.save(subscription);
            }

            System.out.println("✅ Usuario de muestra " + i + " creado: " + sampleUser.getEmail());
        }

        System.out.println("=== DATOS DE PRUEBA ADICIONALES CREADOS ===");
    }
}