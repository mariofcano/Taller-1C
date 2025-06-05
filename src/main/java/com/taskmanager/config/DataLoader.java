package com.taskmanager.config;

import com.taskmanager.model.User;
import com.taskmanager.model.UserRole;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * CLASE QUE SE EJECUTA AL ARRANCAR LA APLICACIÓN
 * CREO USUARIOS DE PRUEBA PARA NO TENER QUE REGISTRARLOS MANUALMENTE
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

    /**
     * ESTE MÉTODO SE EJECUTA AL ARRANCAR SPRING BOOT
     * AQUÍ CREO LOS USUARIOS INICIALES SI NO EXISTEN
     */
    @Override
    public void run(String... args) throws Exception {
        // VERIFICO SI YA HAY USUARIOS EN LA BD
        if (userRepository.count() == 0) {
            System.out.println("=== CREANDO USUARIOS DE PRUEBA ===");
            createInitialUsers();
            System.out.println("=== USUARIOS CREADOS ===");
        } else {
            System.out.println("=== USUARIOS YA EXISTEN ===");
        }
    }

    /**
     * CREO LOS USUARIOS INICIALES DEL SISTEMA
     * UN ADMINISTRADOR Y UN USUARIO NORMAL
     */
    private void createInitialUsers() {
        // CREO EL USUARIO ADMINISTRADOR
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@taskmanager.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);

        userRepository.save(admin);
        System.out.println("✅ ADMIN CREADO: admin@taskmanager.com / admin123");

        // CREO UN USUARIO NORMAL
        User normalUser = new User();
        normalUser.setUsername("mario");
        normalUser.setEmail("mario@taskmanager.com");
        normalUser.setPassword(passwordEncoder.encode("mario123"));
        normalUser.setRole(UserRole.USER);
        normalUser.setEnabled(true);

        userRepository.save(normalUser);
        System.out.println("✅ USUARIO CREADO: mario@taskmanager.com / mario123");

        // CREO OTRO USUARIO DE PRUEBA
        User testUser = new User();
        testUser.setUsername("test");
        testUser.setEmail("test@taskmanager.com");
        testUser.setPassword(passwordEncoder.encode("test123"));
        testUser.setRole(UserRole.USER);
        testUser.setEnabled(true);

        userRepository.save(testUser);
        System.out.println("✅ USUARIO TEST CREADO: test@taskmanager.com / test123");
    }
}