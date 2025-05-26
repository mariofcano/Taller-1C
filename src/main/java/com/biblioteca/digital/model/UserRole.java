package com.biblioteca.digital.model;

/**
 * ENUMERACION QUE DEFINE LOS ROLES DISPONIBLES EN EL SISTEMA DE BIBLIOTECA DIGITAL
 *
 * ESTA ENUMERACION ESTABLECE LOS DIFERENTES NIVELES DE ACCESO Y PERMISOS
 * QUE PUEDEN TENER LOS USUARIOS EN EL SISTEMA. CADA ROL TIENE ASOCIADAS
 * DIFERENTES CAPACIDADES Y RESTRICCIONES PARA GARANTIZAR LA SEGURIDAD
 * Y EL CONTROL ADECUADO DE LAS OPERACIONES.
 *
 * @author SISTEMA BIBLIOTECA DIGITAL
 * @version 1.0
 * @since 2025-05-26
 *
 * @see User
 */
public enum UserRole {

    /**
     * ADMINISTRADOR DEL SISTEMA
     *
     * USUARIO CON PERMISOS COMPLETOS QUE PUEDE:
     * - GESTIONAR TODOS LOS USUARIOS DEL SISTEMA
     * - CREAR, MODIFICAR Y ELIMINAR BIBLIOTECARIOS
     * - ACCEDER A TODAS LAS FUNCIONALIDADES ADMINISTRATIVAS
     * - CONFIGURAR PARAMETROS DEL SISTEMA
     * - GENERAR REPORTES COMPLETOS
     * - GESTIONAR COPIAS DE SEGURIDAD
     */
    ADMIN("Administrador"),

    /**
     * BIBLIOTECARIO DEL SISTEMA
     *
     * USUARIO ESPECIALIZADO QUE PUEDE:
     * - GESTIONAR EL CATALOGO DE LIBROS
     * - REALIZAR Y GESTIONAR PRESTAMOS
     * - ADMINISTRAR DEVOLUCIONES
     * - GENERAR REPORTES DE PRESTAMOS
     * - GESTIONAR USUARIOS FINALES
     * - ACCEDER A ESTADISTICAS DE LA BIBLIOTECA
     */
    LIBRARIAN("Bibliotecario"),

    /**
     * USUARIO FINAL DEL SISTEMA
     *
     * USUARIO BASICO QUE PUEDE:
     * - CONSULTAR EL CATALOGO DE LIBROS
     * - REALIZAR SOLICITUDES DE PRESTAMO
     * - VER SU HISTORIAL DE PRESTAMOS
     * - ACTUALIZAR SU PERFIL PERSONAL
     * - RECIBIR NOTIFICACIONES DE VENCIMIENTOS
     */
    USER("Usuario");

    /**
     * DESCRIPCION LEGIBLE DEL ROL
     *
     * ALMACENO UNA DESCRIPCION EN TEXTO PLANO QUE SE PUEDE MOSTRAR
     * EN LA INTERFAZ DE USUARIO PARA MAYOR CLARIDAD.
     */
    private final String description;

    /**
     * CONSTRUCTOR DEL ENUM UserRole
     *
     * ESTABLEZCO LA DESCRIPCION ASOCIADA A CADA ROL PARA SU USO
     * EN INTERFACES DE USUARIO Y REPORTES.
     *
     * @param description DESCRIPCION LEGIBLE DEL ROL
     */
    UserRole(String description) {
        this.description = description;
    }

    /**
     * OBTIENE LA DESCRIPCION LEGIBLE DEL ROL
     *
     * @return DESCRIPCION DEL ROL EN TEXTO PLANO
     */
    public String getDescription() {
        return description;
    }

    /**
     * VERIFICA SI EL ROL TIENE PERMISOS ADMINISTRATIVOS
     *
     * @return TRUE SI ES ADMIN O LIBRARIAN, FALSE PARA USER
     */
    public boolean hasAdminPrivileges() {
        return this == ADMIN || this == LIBRARIAN;
    }

    /**
     * VERIFICA SI EL ROL PUEDE GESTIONAR LIBROS
     *
     * @return TRUE SI ES ADMIN O LIBRARIAN, FALSE PARA USER
     */
    public boolean canManageBooks() {
        return this == ADMIN || this == LIBRARIAN;
    }

    /**
     * VERIFICA SI EL ROL PUEDE GESTIONAR USUARIOS
     *
     * @return TRUE SOLO SI ES ADMIN
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }

    /**
     * OBTIENE TODOS LOS ROLES DISPONIBLES COMO ARRAY
     *
     * @return ARRAY CON TODOS LOS VALORES DEL ENUM
     */
    public static UserRole[] getAllRoles() {
        return values();
    }

    /**
     * BUSCA UN ROL POR SU DESCRIPCION
     *
     * @param description DESCRIPCION A BUSCAR
     * @return ROL CORRESPONDIENTE O NULL SI NO SE ENCUENTRA
     */
    public static UserRole findByDescription(String description) {
        for (UserRole role : values()) {
            if (role.getDescription().equalsIgnoreCase(description)) {
                return role;
            }
        }
        return null;
    }
}