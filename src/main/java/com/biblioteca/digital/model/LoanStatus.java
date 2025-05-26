package com.biblioteca.digital.model;

/**
 * ENUMERACION QUE DEFINE LOS ESTADOS POSIBLES DE UN PRESTAMO EN EL SISTEMA
 *
 * ESTA ENUMERACION ESTABLECE TODOS LOS ESTADOS POR LOS QUE PUEDE PASAR
 * UN PRESTAMO DURANTE SU CICLO DE VIDA EN EL SISTEMA DE BIBLIOTECA DIGITAL.
 * CADA ESTADO REPRESENTA UNA SITUACION ESPECIFICA QUE DETERMINA LAS
 * ACCIONES DISPONIBLES Y EL TRATAMIENTO DEL PRESTAMO.
 *
 * LOS ESTADOS PERMITEN UN CONTROL GRANULAR DEL FLUJO DE PRESTAMOS
 * Y FACILITAN LA GENERACION DE REPORTES Y ESTADISTICAS PRECISAS.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see Loan
 */
public enum LoanStatus {

    /**
     * PRESTAMO ACTIVO
     *
     * EL LIBRO HA SIDO PRESTADO Y ESTA EN PODER DEL USUARIO.
     * EL PRESTAMO SE ENCUENTRA DENTRO DEL PERIODO PERMITIDO
     * SIN HABER ALCANZADO LA FECHA LIMITE DE DEVOLUCION.
     *
     * ACCIONES DISPONIBLES:
     * - RENOVAR EL PRESTAMO
     * - PROCESAR DEVOLUCION
     * - CONSULTAR INFORMACION
     */
    ACTIVE("Activo"),

    /**
     * PRESTAMO VENCIDO
     *
     * EL LIBRO NO HA SIDO DEVUELTO DENTRO DEL PLAZO ESTABLECIDO.
     * SE HA SUPERADO LA FECHA LIMITE Y PUEDEN APLICARSE MULTAS
     * POR RETRASO SEGUN LAS POLITICAS DE LA BIBLIOTECA.
     *
     * ACCIONES DISPONIBLES:
     * - PROCESAR DEVOLUCION CON MULTA
     * - ENVIAR RECORDATORIOS
     * - APLICAR PENALIZACIONES
     */
    OVERDUE("Vencido"),

    /**
     * PRESTAMO DEVUELTO A TIEMPO
     *
     * EL LIBRO HA SIDO DEVUELTO DENTRO DEL PLAZO ESTABLECIDO.
     * EL PRESTAMO SE COMPLETO EXITOSAMENTE SIN PENALIZACIONES
     * Y EL LIBRO ESTA NUEVAMENTE DISPONIBLE PARA OTROS USUARIOS.
     *
     * ACCIONES DISPONIBLES:
     * - CONSULTAR HISTORIAL
     * - GENERAR ESTADISTICAS
     */
    RETURNED("Devuelto"),

    /**
     * PRESTAMO DEVUELTO CON RETRASO
     *
     * EL LIBRO HA SIDO DEVUELTO DESPUES DE LA FECHA LIMITE.
     * SE HAN APLICADO MULTAS POR RETRASO Y EL USUARIO DEBE
     * PAGARLAS ANTES DE PODER REALIZAR NUEVOS PRESTAMOS.
     *
     * ACCIONES DISPONIBLES:
     * - PROCESAR PAGO DE MULTA
     * - CONSULTAR HISTORIAL
     * - GENERAR REPORTES DE MULTAS
     */
    RETURNED_LATE("Devuelto Tarde"),

    /**
     * PRESTAMO RENOVADO
     *
     * EL PERIODO DE PRESTAMO HA SIDO EXTENDIDO A SOLICITUD DEL USUARIO.
     * SE HA ESTABLECIDO UNA NUEVA FECHA LIMITE MANTENIENDO EL MISMO
     * PRESTAMO ACTIVO CON CONDICIONES ACTUALIZADAS.
     *
     * ACCIONES DISPONIBLES:
     * - RENOVAR NUEVAMENTE (SI SE PERMITE)
     * - PROCESAR DEVOLUCION
     * - CONSULTAR INFORMACION
     */
    RENEWED("Renovado"),

    /**
     * PRESTAMO CANCELADO
     *
     * EL PRESTAMO HA SIDO CANCELADO ANTES DE SU FINALIZACION NATURAL.
     * PUEDE DEBERSE A CIRCUNSTANCIAS ESPECIALES, ERRORES EN EL SISTEMA
     * O DECISIONES ADMINISTRATIVAS EXCEPCIONALES.
     *
     * ACCIONES DISPONIBLES:
     * - CONSULTAR MOTIVO DE CANCELACION
     * - RESTAURAR DISPONIBILIDAD DEL LIBRO
     * - GENERAR REPORTES DE CANCELACIONES
     */
    CANCELLED("Cancelado"),

    /**
     * LIBRO PERDIDO O EXTRAVIADO
     *
     * EL USUARIO HA REPORTADO LA PERDIDA DEL LIBRO O ESTE NO HA SIDO
     * DEVUELTO DESPUES DE MULTIPLES INTENTOS DE RECUPERACION.
     * SE APLICAN PENALIZACIONES ECONOMICAS Y RESTRICCIONES AL USUARIO.
     *
     * ACCIONES DISPONIBLES:
     * - PROCESAR PAGO DE REPOSICION
     * - APLICAR RESTRICCIONES AL USUARIO
     * - ACTUALIZAR INVENTARIO
     */
    LOST("Perdido"),

    /**
     * LIBRO DAÑADO AL MOMENTO DE DEVOLUCION
     *
     * EL LIBRO HA SIDO DEVUELTO CON DAÑOS SIGNIFICATIVOS QUE AFECTAN
     * SU DISPONIBILIDAD PARA FUTUROS PRESTAMOS. SE EVALUAN PENALIZACIONES
     * SEGUN EL GRADO DE DAÑO Y LAS POLITICAS ESTABLECIDAS.
     *
     * ACCIONES DISPONIBLES:
     * - EVALUAR DAÑOS
     * - PROCESAR PAGO DE REPARACION O REPOSICION
     * - ACTUALIZAR ESTADO DEL LIBRO
     */
    DAMAGED("Dañado");

    /**
     * DESCRIPCION LEGIBLE DEL ESTADO
     *
     * ALMACENO UNA DESCRIPCION EN TEXTO PLANO QUE SE PUEDE MOSTRAR
     * EN LA INTERFAZ DE USUARIO PARA MAYOR CLARIDAD Y COMPRENSION.
     */
    private final String description;

    /**
     * CONSTRUCTOR DEL ENUM LoanStatus
     *
     * ESTABLEZCO LA DESCRIPCION ASOCIADA A CADA ESTADO PARA SU USO
     * EN INTERFACES DE USUARIO Y REPORTES DEL SISTEMA.
     *
     * @param description DESCRIPCION LEGIBLE DEL ESTADO
     */
    LoanStatus(String description) {
        this.description = description;
    }

    /**
     * OBTIENE LA DESCRIPCION LEGIBLE DEL ESTADO
     *
     * @return DESCRIPCION DEL ESTADO EN TEXTO PLANO
     */
    public String getDescription() {
        return description;
    }

    /**
     * VERIFICA SI EL ESTADO INDICA UN PRESTAMO ACTIVO
     *
     * DETERMINO SI EL ESTADO CORRESPONDE A UN PRESTAMO QUE ESTA
     * ACTUALMENTE EN PODER DEL USUARIO (ACTIVO, VENCIDO O RENOVADO).
     *
     * @return TRUE SI ES ESTADO ACTIVO, FALSE EN CASO CONTRARIO
     */
    public boolean isActiveStatus() {
        return this == ACTIVE || this == OVERDUE || this == RENEWED;
    }

    /**
     * VERIFICA SI EL ESTADO INDICA UN PRESTAMO FINALIZADO
     *
     * DETERMINO SI EL PRESTAMO HA LLEGADO A UN ESTADO FINAL
     * DONDE NO SE REQUIEREN MAS ACCIONES DEL USUARIO.
     *
     * @return TRUE SI ES ESTADO FINAL, FALSE EN CASO CONTRARIO
     */
    public boolean isFinalStatus() {
        return this == RETURNED || this == RETURNED_LATE ||
                this == CANCELLED || this == LOST || this == DAMAGED;
    }

    /**
     * VERIFICA SI EL ESTADO REQUIERE PAGO DE PENALIZACIONES
     *
     * DETERMINO SI EL ESTADO ACTUAL IMPLICA QUE EL USUARIO
     * DEBE PAGAR MULTAS O PENALIZACIONES ANTES DE CONTINUAR.
     *
     * @return TRUE SI REQUIERE PAGO, FALSE EN CASO CONTRARIO
     */
    public boolean requiresPayment() {
        return this == OVERDUE || this == RETURNED_LATE ||
                this == LOST || this == DAMAGED;
    }

    /**
     * VERIFICA SI EL PRESTAMO PUEDE SER RENOVADO EN ESTE ESTADO
     *
     * DETERMINO SI ES POSIBLE RENOVAR EL PRESTAMO BASANDOME
     * EN EL ESTADO ACTUAL DEL MISMO.
     *
     * @return TRUE SI PUEDE RENOVARSE, FALSE EN CASO CONTRARIO
     */
    public boolean canBeRenewed() {
        return this == ACTIVE || this == RENEWED;
    }

    /**
     * VERIFICA SI EL ESTADO PERMITE PROCESAR LA DEVOLUCION
     *
     * DETERMINO SI ES POSIBLE PROCESAR LA DEVOLUCION DEL LIBRO
     * EN EL ESTADO ACTUAL DEL PRESTAMO.
     *
     * @return TRUE SI PERMITE DEVOLUCION, FALSE EN CASO CONTRARIO
     */
    public boolean allowsReturn() {
        return this == ACTIVE || this == OVERDUE || this == RENEWED;
    }

    /**
     * OBTIENE EL NIVEL DE PRIORIDAD DEL ESTADO
     *
     * ASIGNO UN NIVEL DE PRIORIDAD NUMERICO PARA ORDENAR Y PRIORIZAR
     * LOS PRESTAMOS SEGUN SU ESTADO. VALORES MAS ALTOS INDICAN
     * MAYOR PRIORIDAD DE ATENCION.
     *
     * @return NIVEL DE PRIORIDAD (1-10, DONDE 10 ES MAS URGENTE)
     */
    public int getPriority() {
        switch (this) {
            case LOST:
                return 10; // MAXIMA PRIORIDAD
            case DAMAGED:
                return 9;
            case OVERDUE:
                return 8;
            case RETURNED_LATE:
                return 6;
            case ACTIVE:
                return 5;
            case RENEWED:
                return 4;
            case RETURNED:
                return 2;
            case CANCELLED:
                return 1;
            default:
                return 3;
        }
    }

    /**
     * OBTIENE EL COLOR ASOCIADO AL ESTADO PARA INTERFACES GRAFICAS
     *
     * PROPORCIONO UN CODIGO DE COLOR SUGERIDO PARA REPRESENTAR
     * VISUALMENTE CADA ESTADO EN INTERFACES DE USUARIO Y REPORTES.
     *
     * @return CODIGO DE COLOR EN FORMATO CSS/HEX
     */
    public String getColor() {
        switch (this) {
            case ACTIVE:
                return "#28a745"; // VERDE - ESTADO NORMAL
            case OVERDUE:
                return "#dc3545"; // ROJO - REQUIERE ATENCION URGENTE
            case RETURNED:
                return "#6c757d"; // GRIS - COMPLETADO EXITOSAMENTE
            case RETURNED_LATE:
                return "#fd7e14"; // NARANJA - COMPLETADO CON PROBLEMAS
            case RENEWED:
                return "#17a2b8"; // AZUL CLARO - ESTADO MODIFICADO
            case CANCELLED:
                return "#6f42c1"; // PURPURA - ESTADO ESPECIAL
            case LOST:
                return "#721c24"; // ROJO OSCURO - PROBLEMA GRAVE
            case DAMAGED:
                return "#856404"; // AMARILLO OSCURO - REQUIERE EVALUACION
            default:
                return "#495057"; // GRIS OSCURO - ESTADO DESCONOCIDO
        }
    }

    /**
     * OBTIENE EL ICONO SUGERIDO PARA EL ESTADO
     *
     * PROPORCIONO UN NOMBRE DE ICONO (COMPATIBLE CON FONT AWESOME O BOOTSTRAP)
     * PARA REPRESENTAR VISUALMENTE CADA ESTADO EN LA INTERFAZ.
     *
     * @return NOMBRE DEL ICONO SUGERIDO
     */
    public String getIcon() {
        switch (this) {
            case ACTIVE:
                return "fas fa-book-open";
            case OVERDUE:
                return "fas fa-exclamation-triangle";
            case RETURNED:
                return "fas fa-check-circle";
            case RETURNED_LATE:
                return "fas fa-clock";
            case RENEWED:
                return "fas fa-redo";
            case CANCELLED:
                return "fas fa-times-circle";
            case LOST:
                return "fas fa-search";
            case DAMAGED:
                return "fas fa-tools";
            default:
                return "fas fa-question-circle";
        }
    }

    /**
     * OBTIENE TODOS LOS ESTADOS DISPONIBLES COMO ARRAY
     *
     * METODO UTILITARIO PARA OBTENER TODOS LOS ESTADOS DEL ENUM
     * PARA USO EN FORMULARIOS, FILTROS Y LISTADOS COMPLETOS.
     *
     * @return ARRAY CON TODOS LOS VALORES DEL ENUM
     */
    public static LoanStatus[] getAllStatuses() {
        return values();
    }

    /**
     * BUSCA UN ESTADO POR SU DESCRIPCION
     *
     * LOCALIZO UN ESTADO ESPECIFICO BASANDOME EN SU DESCRIPCION
     * TEXTUAL PARA FACILITAR BUSQUEDAS Y CONVERSIONES.
     *
     * @param description DESCRIPCION A BUSCAR
     * @return ESTADO CORRESPONDIENTE O NULL SI NO SE ENCUENTRA
     */
    public static LoanStatus findByDescription(String description) {
        for (LoanStatus status : values()) {
            if (status.getDescription().equalsIgnoreCase(description)) {
                return status;
            }
        }
        return null;
    }

    /**
     * OBTIENE LOS ESTADOS ACTIVOS DISPONIBLES
     *
     * FILTRO Y RETORNO SOLO LOS ESTADOS QUE CORRESPONDEN
     * A PRESTAMOS ACTIVOS PARA BUSQUEDAS ESPECIALIZADAS.
     *
     * @return ARRAY CON ESTADOS ACTIVOS
     */
    public static LoanStatus[] getActiveStatuses() {
        return new LoanStatus[]{ACTIVE, OVERDUE, RENEWED};
    }

    /**
     * OBTIENE LOS ESTADOS FINALES DISPONIBLES
     *
     * FILTRO Y RETORNO SOLO LOS ESTADOS QUE INDICAN
     * PRESTAMOS COMPLETADOS O FINALIZADOS.
     *
     * @return ARRAY CON ESTADOS FINALES
     */
    public static LoanStatus[] getFinalStatuses() {
        return new LoanStatus[]{RETURNED, RETURNED_LATE, CANCELLED, LOST, DAMAGED};
    }

    /**
     * OBTIENE LOS ESTADOS QUE REQUIEREN PAGO
     *
     * FILTRO Y RETORNO SOLO LOS ESTADOS QUE IMPLICAN
     * PENALIZACIONES O MULTAS PENDIENTES DE PAGO.
     *
     * @return ARRAY CON ESTADOS QUE REQUIEREN PAGO
     */
    public static LoanStatus[] getPaymentRequiredStatuses() {
        return new LoanStatus[]{OVERDUE, RETURNED_LATE, LOST, DAMAGED};
    }

    /**
     * CUENTA EL NUMERO TOTAL DE ESTADOS DISPONIBLES
     *
     * @return CANTIDAD TOTAL DE ESTADOS EN EL ENUM
     */
    public static int getTotalStatusesCount() {
        return values().length;
    }

    /**
     * VERIFICA SI UNA CADENA CORRESPONDE A UN ESTADO VALIDO
     *
     * VALIDO SI UN TEXTO DADO CORRESPONDE AL NOMBRE DE ALGUN
     * ESTADO EXISTENTE EN EL ENUM.
     *
     * @param statusName NOMBRE DEL ESTADO A VERIFICAR
     * @return TRUE SI ES ESTADO VALIDO, FALSE EN CASO CONTRARIO
     */
    public static boolean isValidStatus(String statusName) {
        if (statusName == null || statusName.trim().isEmpty()) {
            return false;
        }

        try {
            valueOf(statusName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * CONVIERTE UNA CADENA EN ESTADO ENUM
     *
     * METODO SEGURO PARA CONVERTIR UNA CADENA EN ESTADO
     * SIN LANZAR EXCEPCION SI EL ESTADO NO EXISTE.
     *
     * @param statusName NOMBRE DEL ESTADO
     * @return ESTADO CORRESPONDIENTE O ACTIVE SI NO SE ENCUENTRA
     */
    public static LoanStatus fromString(String statusName) {
        if (statusName == null || statusName.trim().isEmpty()) {
            return ACTIVE;
        }

        try {
            return valueOf(statusName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }

    /**
     * OBTIENE LOS ESTADOS ORDENADOS POR PRIORIDAD
     *
     * RETORNO TODOS LOS ESTADOS ORDENADOS DE MAYOR A MENOR PRIORIDAD
     * PARA MOSTRAR EN REPORTES O INTERFACES QUE REQUIERAN ORDENACION.
     *
     * @return ARRAY DE ESTADOS ORDENADOS POR PRIORIDAD DESCENDENTE
     */
    public static LoanStatus[] getStatusesByPriority() {
        LoanStatus[] statuses = values();
        java.util.Arrays.sort(statuses, (a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        return statuses;
    }

    /**
     * VERIFICA SI EL ESTADO INDICA PROBLEMAS CON EL PRESTAMO
     *
     * DETERMINO SI EL ESTADO ACTUAL REPRESENTA UNA SITUACION
     * PROBLEMATICA QUE REQUIERE ATENCION ESPECIAL.
     *
     * @return TRUE SI INDICA PROBLEMAS, FALSE EN CASO CONTRARIO
     */
    public boolean isProblematic() {
        return this == OVERDUE || this == LOST || this == DAMAGED || this == RETURNED_LATE;
    }

    /**
     * VERIFICA SI EL ESTADO PERMITE MODIFICACIONES
     *
     * DETERMINO SI ES POSIBLE REALIZAR CAMBIOS EN EL PRESTAMO
     * CUANDO SE ENCUENTRA EN EL ESTADO ACTUAL.
     *
     * @return TRUE SI PERMITE MODIFICACIONES, FALSE EN CASO CONTRARIO
     */
    public boolean allowsModification() {
        return this == ACTIVE || this == RENEWED;
    }

    /**
     * OBTIENE EL SIGUIENTE ESTADO LOGICO EN EL FLUJO
     *
     * DETERMINO CUAL SERIA EL SIGUIENTE ESTADO MAS PROBABLE
     * EN EL FLUJO NORMAL DE UN PRESTAMO.
     *
     * @return SIGUIENTE ESTADO ESPERADO O NULL SI NO HAY TRANSICION LOGICA
     */
    public LoanStatus getNextLogicalStatus() {
        switch (this) {
            case ACTIVE:
                return RETURNED; // DEVOLUCION NORMAL
            case OVERDUE:
                return RETURNED_LATE; // DEVOLUCION CON RETRASO
            case RENEWED:
                return RETURNED; // DEVOLUCION DESPUES DE RENOVACION
            default:
                return null; // ESTADOS FINALES NO TIENEN SIGUIENTE ESTADO
        }
    }

    /**
     * VERIFICA SI ES POSIBLE TRANSITAR DESDE ESTE ESTADO A OTRO
     *
     * VALIDO SI ES LOGICAMENTE POSIBLE CAMBIAR DEL ESTADO ACTUAL
     * AL ESTADO ESPECIFICADO SEGUN LAS REGLAS DE NEGOCIO.
     *
     * @param targetStatus ESTADO DE DESTINO
     * @return TRUE SI LA TRANSICION ES VALIDA, FALSE EN CASO CONTRARIO
     */
    public boolean canTransitionTo(LoanStatus targetStatus) {
        if (targetStatus == null || targetStatus == this) {
            return false;
        }

        switch (this) {
            case ACTIVE:
                return targetStatus == OVERDUE || targetStatus == RETURNED ||
                        targetStatus == RENEWED || targetStatus == CANCELLED ||
                        targetStatus == LOST || targetStatus == DAMAGED;

            case OVERDUE:
                return targetStatus == RETURNED_LATE || targetStatus == LOST ||
                        targetStatus == DAMAGED || targetStatus == CANCELLED;

            case RENEWED:
                return targetStatus == RETURNED || targetStatus == OVERDUE ||
                        targetStatus == CANCELLED || targetStatus == LOST ||
                        targetStatus == DAMAGED;

            default:
                return false; // ESTADOS FINALES NO PERMITEN TRANSICIONES
        }
    }

    /**
     * OBTIENE UNA REPRESENTACION AMIGABLE DEL ESTADO
     *
     * RETORNO LA DESCRIPCION LEGIBLE EN LUGAR DEL NOMBRE DEL ENUM
     * PARA MOSTRAR EN INTERFACES DE USUARIO.
     *
     * @return DESCRIPCION LEGIBLE DEL ESTADO
     */
    @Override
    public String toString() {
        return description;
    }
}