package com.taskmanager.model;

/**
 * ENUM QUE DEFINE LOS ROLES DE USUARIO EN EL SISTEMA
 *
 * @author Mario Flores
 * @version 1.0
 */
public enum UserRole {

    /**
     * USUARIO ADMINISTRADOR
     * PUEDE GESTIONAR USUARIOS Y TIENE ACCESO COMPLETO
     */
    ADMIN("Administrador"),

    /**
     * USUARIO NORMAL
     * PUEDE GESTIONAR SUS PROPIAS TAREAS
     */
    USER("Usuario");

    // DESCRIPCIÓN LEGIBLE DEL ROL
    private final String displayName;

    /**
     * CONSTRUCTOR DEL ENUM
     *
     * @param displayName nombre mostrable del rol
     */
    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * OBTENGO EL NOMBRE PARA MOSTRAR EN LA UI
     *
     * @return descripción del rol
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * CONVIERTO EL ROL A STRING PARA SPRING SECURITY
     *
     * @return nombre del rol con prefijo ROLE_
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}