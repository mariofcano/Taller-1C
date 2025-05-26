package com.biblioteca.digital.repository;

import com.biblioteca.digital.model.Loan;
import com.biblioteca.digital.model.LoanStatus;
import com.biblioteca.digital.model.User;
import com.biblioteca.digital.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO JPA PARA LA GESTION DE ENTIDADES LOAN
 *
 * ESTA INTERFAZ PROPORCIONA OPERACIONES CRUD ESPECIALIZADAS Y CONSULTAS AVANZADAS
 * PARA LA GESTION COMPLETA DEL SISTEMA DE PRESTAMOS DE LA BIBLIOTECA DIGITAL.
 * INCLUYE METODOS OPTIMIZADOS PARA CONTROL DE VENCIMIENTOS, CALCULO DE MULTAS,
 * ESTADISTICAS DE PRESTAMOS Y SEGUIMIENTO DE DISPONIBILIDAD.
 *
 * LAS CONSULTAS ESTAN DISEÑADAS PARA SOPORTAR TODAS LAS OPERACIONES CRITICAS
 * DEL SISTEMA DE PRESTAMOS, DESDE LA GESTION DIARIA HASTA REPORTES GERENCIALES.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see Loan
 * @see LoanStatus
 * @see User
 * @see Book
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * BUSCA PRESTAMOS POR USUARIO ESPECIFICO
     *
     * LOCALIZO TODOS LOS PRESTAMOS REALIZADOS POR UN USUARIO DETERMINADO.
     * FUNDAMENTAL PARA MOSTRAR EL HISTORIAL PERSONAL DE PRESTAMOS
     * Y GESTIONAR LAS OPERACIONES INDIVIDUALES DE CADA USUARIO.
     *
     * @param user USUARIO PARA FILTRAR PRESTAMOS
     * @return LISTA DE PRESTAMOS DEL USUARIO ESPECIFICADO
     */
    List<Loan> findByUser(User user);

    /**
     * BUSCA PRESTAMOS POR LIBRO ESPECIFICO
     *
     * OBTENGO EL HISTORIAL COMPLETO DE PRESTAMOS DE UN LIBRO DETERMINADO.
     * UTIL PARA ANALIZAR LA POPULARIDAD DE OBRAS ESPECIFICAS
     * Y GESTIONAR EL INVENTARIO DE COPIAS.
     *
     * @param book LIBRO PARA FILTRAR PRESTAMOS
     * @return LISTA DE PRESTAMOS DEL LIBRO ESPECIFICADO
     */
    List<Loan> findByBook(Book book);

    /**
     * BUSCA PRESTAMOS POR ESTADO ESPECIFICO
     *
     * FILTRO PRESTAMOS SEGUN SU ESTADO ACTUAL (ACTIVO, VENCIDO, DEVUELTO, ETC.).
     * ESENCIAL PARA OPERACIONES DE GESTION Y CONTROL DE FLUJO
     * DE PRESTAMOS EN EL SISTEMA.
     *
     * @param status ESTADO DE PRESTAMO A FILTRAR
     * @return LISTA DE PRESTAMOS CON EL ESTADO ESPECIFICADO
     */
    List<Loan> findByStatus(LoanStatus status);

    /**
     * BUSCA PRESTAMOS ACTIVOS DE UN USUARIO
     *
     * LOCALIZO PRESTAMOS QUE UN USUARIO TIENE ACTUALMENTE SIN DEVOLVER.
     * FUNDAMENTAL PARA VALIDAR LIMITES DE PRESTAMOS Y MOSTRAR
     * EL ESTADO ACTUAL DE CADA USUARIO.
     *
     * @param user USUARIO A CONSULTAR
     * @return LISTA DE PRESTAMOS ACTIVOS DEL USUARIO
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL")
    List<Loan> findActiveByUser(@Param("user") User user);

    /**
     * CUENTA PRESTAMOS ACTIVOS DE UN USUARIO
     *
     * CALCULO EL NUMERO DE LIBROS QUE UN USUARIO TIENE ACTUALMENTE
     * EN PRESTAMO. NECESARIO PARA VALIDAR LIMITES Y RESTRICCIONES
     * DE PRESTAMOS SIMULTANEOS.
     *
     * @param user USUARIO A CONSULTAR
     * @return NUMERO DE PRESTAMOS ACTIVOS DEL USUARIO
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL")
    long countActiveByUser(@Param("user") User user);

    /**
     * BUSCA PRESTAMOS VENCIDOS EN EL SISTEMA
     *
     * IDENTIFICO PRESTAMOS QUE HAN SUPERADO SU FECHA DE VENCIMIENTO
     * Y NO HAN SIDO DEVUELTOS. CRITICO PARA PROCESOS DE NOTIFICACION
     * Y APLICACION DE MULTAS.
     *
     * @return LISTA DE PRESTAMOS VENCIDOS
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();

    /**
     * BUSCA PRESTAMOS QUE VENCEN HOY
     *
     * LOCALIZO PRESTAMOS CUYA FECHA DE VENCIMIENTO ES LA FECHA ACTUAL.
     * FUNDAMENTAL PARA SISTEMAS DE NOTIFICACION Y ALERTAS
     * DE VENCIMIENTOS INMINENTES.
     *
     * @return LISTA DE PRESTAMOS QUE VENCEN HOY
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.dueDate = CURRENT_DATE")
    List<Loan> findLoansDueToday();

    /**
     * BUSCA PRESTAMOS QUE VENCEN EN LOS PROXIMOS DIAS
     *
     * IDENTIFICO PRESTAMOS QUE VENCERAN DENTRO DE UN NUMERO
     * ESPECIFICADO DE DIAS. UTIL PARA RECORDATORIOS PREVENTIVOS
     * Y NOTIFICACIONES ANTICIPADAS A LOS USUARIOS.
     *
     * @param days NUMERO DE DIAS DE ANTICIPACION
     * @return LISTA DE PRESTAMOS QUE VENCEN PRONTO
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.dueDate BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL :days DAY)")
    List<Loan> findLoansDueSoon(@Param("days") int days);

    /**
     * BUSCA PRESTAMOS DEVUELTOS EN UN RANGO DE FECHAS
     *
     * LOCALIZO PRESTAMOS QUE FUERON DEVUELTOS DENTRO DE UN PERIODO
     * ESPECIFICO. ESENCIAL PARA REPORTES PERIODICOS Y ANALISIS
     * DE ACTIVIDAD DE DEVOLUCIONES.
     *
     * @param startDate FECHA INICIAL DEL RANGO
     * @param endDate FECHA FINAL DEL RANGO
     * @return LISTA DE PRESTAMOS DEVUELTOS EN EL PERIODO
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt BETWEEN :startDate AND :endDate")
    List<Loan> findReturnedBetween(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * BUSCA PRESTAMOS REALIZADOS EN UN RANGO DE FECHAS
     *
     * OBTENGO PRESTAMOS CREADOS DENTRO DE UN PERIODO ESPECIFICO.
     * FUNDAMENTAL PARA ANALISIS DE TENDENCIAS DE PRESTAMOS
     * Y GENERACION DE ESTADISTICAS PERIODICAS.
     *
     * @param startDate FECHA INICIAL DEL RANGO
     * @param endDate FECHA FINAL DEL RANGO
     * @return LISTA DE PRESTAMOS REALIZADOS EN EL PERIODO
     */
    List<Loan> findByLoanDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * BUSCA PRESTAMOS CON MULTAS PENDIENTES DE PAGO
     *
     * IDENTIFICO PRESTAMOS QUE TIENEN MULTAS APLICADAS PERO NO PAGADAS.
     * CRITICO PARA CONTROL FINANCIERO Y GESTION DE COBRANZAS
     * DEL SISTEMA.
     *
     * @return LISTA DE PRESTAMOS CON MULTAS PENDIENTES
     */
    @Query("SELECT l FROM Loan l WHERE l.fineAmount > 0 AND l.finePaid = false")
    List<Loan> findLoansWithUnpaidFines();

    /**
     * CALCULA EL MONTO TOTAL DE MULTAS PENDIENTES
     *
     * SUMO TODAS LAS MULTAS NO PAGADAS EN EL SISTEMA.
     * METRICA FINANCIERA IMPORTANTE PARA REPORTES
     * Y CONTROL DE INGRESOS PENDIENTES.
     *
     * @return MONTO TOTAL DE MULTAS PENDIENTES
     */
    @Query("SELECT SUM(l.fineAmount) FROM Loan l WHERE l.fineAmount > 0 AND l.finePaid = false")
    BigDecimal calculateTotalUnpaidFines();

    /**
     * BUSCA PRESTAMOS POR USUARIO CON PAGINACION
     *
     * OBTENGO PRESTAMOS DE UN USUARIO ESPECIFICO CON SOPORTE
     * PARA PAGINACION. OPTIMIZA LA CARGA DE DATOS CUANDO
     * UN USUARIO TIENE MUCHOS PRESTAMOS.
     *
     * @param user USUARIO A CONSULTAR
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON PRESTAMOS DEL USUARIO
     */
    Page<Loan> findByUser(User user, Pageable pageable);

    /**
     * BUSCA PRESTAMOS RENOVADOS
     *
     * LOCALIZO PRESTAMOS QUE HAN SIDO RENOVADOS AL MENOS UNA VEZ.
     * UTIL PARA ANALIZAR PATRONES DE USO Y DEMANDA
     * DE EXTENSIONES DE PRESTAMO.
     *
     * @return LISTA DE PRESTAMOS RENOVADOS
     */
    @Query("SELECT l FROM Loan l WHERE l.renewals > 0")
    List<Loan> findRenewedLoans();

    /**
     * BUSCA PRESTAMOS CON MULTIPLES RENOVACIONES
     *
     * IDENTIFICO PRESTAMOS QUE HAN SIDO RENOVADOS MAS DE UNA VEZ.
     * IMPORTANTE PARA DETECTAR PATRONES DE USO EXCESIVO
     * Y POSIBLES PROBLEMAS DE DISPONIBILIDAD.
     *
     * @param minRenewals NUMERO MINIMO DE RENOVACIONES
     * @return LISTA DE PRESTAMOS CON MULTIPLES RENOVACIONES
     */
    @Query("SELECT l FROM Loan l WHERE l.renewals >= :minRenewals")
    List<Loan> findLoansWithMultipleRenewals(@Param("minRenewals") int minRenewals);

    /**
     * BUSCA PRESTAMOS DE UN LIBRO EN UN PERIODO
     *
     * LOCALIZO PRESTAMOS DE UN LIBRO ESPECIFICO DENTRO DE UN RANGO
     * DE FECHAS. UTIL PARA ANALIZAR LA POPULARIDAD TEMPORAL
     * DE OBRAS ESPECIFICAS.
     *
     * @param book LIBRO A CONSULTAR
     * @param startDate FECHA INICIAL DEL PERIODO
     * @param endDate FECHA FINAL DEL PERIODO
     * @return LISTA DE PRESTAMOS DEL LIBRO EN EL PERIODO
     */
    @Query("SELECT l FROM Loan l WHERE l.book = :book AND l.loanDate BETWEEN :startDate AND :endDate")
    List<Loan> findByBookAndPeriod(@Param("book") Book book,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * BUSCA PRESTAMOS ACTIVOS POR LIBRO
     *
     * OBTENGO PRESTAMOS ACTIVOS (NO DEVUELTOS) DE UN LIBRO ESPECIFICO.
     * FUNDAMENTAL PARA CONOCER QUE USUARIOS TIENEN ACTUALMENTE
     * COPIAS DE UN LIBRO DETERMINADO.
     *
     * @param book LIBRO A CONSULTAR
     * @return LISTA DE PRESTAMOS ACTIVOS DEL LIBRO
     */
    @Query("SELECT l FROM Loan l WHERE l.book = :book AND l.returnedAt IS NULL")
    List<Loan> findActiveByBook(@Param("book") Book book);

    /**
     * CUENTA PRESTAMOS ACTIVOS POR LIBRO
     *
     * CALCULO EL NUMERO DE COPIAS DE UN LIBRO QUE ESTAN
     * ACTUALMENTE EN PRESTAMO. ESENCIAL PARA CONTROL
     * DE DISPONIBILIDAD E INVENTARIO.
     *
     * @param book LIBRO A CONSULTAR
     * @return NUMERO DE PRESTAMOS ACTIVOS DEL LIBRO
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.book = :book AND l.returnedAt IS NULL")
    long countActiveByBook(@Param("book") Book book);

    /**
     * BUSCA PRESTAMOS POR ESTADO CON PAGINACION
     *
     * OBTENGO PRESTAMOS CON UN ESTADO ESPECIFICO USANDO PAGINACION.
     * OPTIMIZA LA CARGA DE DATOS PARA LISTADOS GRANDES
     * EN INTERFACES ADMINISTRATIVAS.
     *
     * @param status ESTADO A FILTRAR
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON PRESTAMOS DEL ESTADO ESPECIFICADO
     */
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    /**
     * BUSCA PRESTAMOS DEVUELTOS TARDE
     *
     * LOCALIZO PRESTAMOS QUE FUERON DEVUELTOS DESPUES DE SU FECHA
     * DE VENCIMIENTO. IMPORTANTE PARA ANALISIS DE CUMPLIMIENTO
     * Y EFECTIVIDAD DE POLITICAS DE PRESTAMO.
     *
     * @return LISTA DE PRESTAMOS DEVUELTOS CON RETRASO
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NOT NULL AND l.returnedAt > CONCAT(l.dueDate, ' 23:59:59')")
    List<Loan> findLateReturns();

    /**
     * CALCULA LA DURACION PROMEDIO DE PRESTAMOS
     *
     * OBTENGO EL PROMEDIO DE DIAS QUE LOS USUARIOS MANTIENEN
     * LOS LIBROS EN PRESTAMO. METRICA UTIL PARA PLANIFICACION
     * Y OPTIMIZACION DE POLITICAS DE PRESTAMO.
     *
     * @return DURACION PROMEDIO EN DIAS
     */
    @Query("SELECT AVG(DATEDIFF(COALESCE(l.returnedAt, CURRENT_DATE), l.loanDate)) FROM Loan l")
    Double calculateAverageLoanDuration();

    /**
     * BUSCA USUARIOS CON MAS PRESTAMOS REALIZADOS
     *
     * IDENTIFICO LOS USUARIOS MAS ACTIVOS DEL SISTEMA BASANDOME
     * EN EL NUMERO TOTAL DE PRESTAMOS REALIZADOS. UTIL PARA
     * PROGRAMAS DE FIDELIZACION Y ANALISIS DE USO.
     *
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON USUARIOS MAS ACTIVOS
     */
    @Query("SELECT l.user, COUNT(l) as loanCount FROM Loan l " +
            "GROUP BY l.user ORDER BY loanCount DESC")
    Page<Object[]> findMostActiveUsers(Pageable pageable);

    /**
     * BUSCA LIBROS MAS PRESTADOS
     *
     * OBTENGO LOS LIBROS MAS POPULARES DEL SISTEMA BASANDOME
     * EN EL NUMERO TOTAL DE PRESTAMOS. FUNDAMENTAL PARA
     * ANALISIS DE DEMANDA Y PLANIFICACION DE ADQUISICIONES.
     *
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON LIBROS MAS PRESTADOS
     */
    @Query("SELECT l.book, COUNT(l) as loanCount FROM Loan l " +
            "GROUP BY l.book ORDER BY loanCount DESC")
    Page<Object[]> findMostBorrowedBooks(Pageable pageable);

    /**
     * OBTIENE ESTADISTICAS DE PRESTAMOS POR ESTADO
     *
     * GENERO UN RESUMEN ESTADISTICO AGRUPANDO PRESTAMOS POR ESTADO
     * Y CONTANDO LA CANTIDAD EN CADA CATEGORIA. ESENCIAL PARA
     * DASHBOARDS Y REPORTES GERENCIALES.
     *
     * @return LISTA DE ARRAYS CON [ESTADO, CANTIDAD]
     */
    @Query("SELECT l.status, COUNT(l) FROM Loan l GROUP BY l.status")
    List<Object[]> getLoanStatsByStatus();

    /**
     * OBTIENE ESTADISTICAS DE PRESTAMOS POR MES
     *
     * GENERO ESTADISTICAS MENSUALES DE PRESTAMOS PARA ANALISIS
     * DE TENDENCIAS TEMPORALES Y PLANIFICACION DE RECURSOS.
     * AGRUPA POR AÑO Y MES DE CREACION.
     *
     * @return LISTA DE ARRAYS CON [AÑO, MES, CANTIDAD]
     */
    @Query("SELECT YEAR(l.loanDate), MONTH(l.loanDate), COUNT(l) FROM Loan l " +
            "GROUP BY YEAR(l.loanDate), MONTH(l.loanDate) ORDER BY YEAR(l.loanDate), MONTH(l.loanDate)")
    List<Object[]> getLoanStatsByMonth();

    /**
     * BUSCA PRESTAMOS DE USUARIO EN UN RANGO DE FECHAS
     *
     * LOCALIZO PRESTAMOS DE UN USUARIO ESPECIFICO DENTRO DE UN PERIODO
     * DETERMINADO. UTIL PARA GENERAR REPORTES PERSONALIZADOS
     * Y ANALISIS DE ACTIVIDAD INDIVIDUAL.
     *
     * @param user USUARIO A CONSULTAR
     * @param startDate FECHA INICIAL DEL PERIODO
     * @param endDate FECHA FINAL DEL PERIODO
     * @return LISTA DE PRESTAMOS DEL USUARIO EN EL PERIODO
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.loanDate BETWEEN :startDate AND :endDate")
    List<Loan> findByUserAndPeriod(@Param("user") User user,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * BUSCA PRESTAMOS CON OBSERVACIONES ESPECIALES
     *
     * LOCALIZO PRESTAMOS QUE TIENEN NOTAS O COMENTARIOS REGISTRADOS.
     * IMPORTANTE PARA SEGUIMIENTO DE CASOS ESPECIALES
     * Y SITUACIONES QUE REQUIEREN ATENCION PARTICULAR.
     *
     * @return LISTA DE PRESTAMOS CON OBSERVACIONES
     */
    @Query("SELECT l FROM Loan l WHERE l.notes IS NOT NULL AND l.notes != ''")
    List<Loan> findLoansWithNotes();

    /**
     * ACTUALIZA EL ESTADO DE UN PRESTAMO
     *
     * OPERACION DE ACTUALIZACION QUE MODIFICA EL ESTADO DE UN PRESTAMO
     * ESPECIFICO. OPTIMIZA LA OPERACION AL EVITAR CARGAR
     * LA ENTIDAD COMPLETA.
     *
     * @param loanId ID DEL PRESTAMO A ACTUALIZAR
     * @param status NUEVO ESTADO DEL PRESTAMO
     */
    @Modifying
    @Query("UPDATE Loan l SET l.status = :status WHERE l.id = :loanId")
    void updateLoanStatus(@Param("loanId") Long loanId, @Param("status") LoanStatus status);

    /**
     * MARCA UNA MULTA COMO PAGADA
     *
     * OPERACION QUE ACTUALIZA EL ESTADO DE PAGO DE UNA MULTA
     * PARA UN PRESTAMO ESPECIFICO. ESENCIAL PARA CONTROL
     * FINANCIERO Y GESTION DE COBRANZAS.
     *
     * @param loanId ID DEL PRESTAMO A ACTUALIZAR
     */
    @Modifying
    @Query("UPDATE Loan l SET l.finePaid = true WHERE l.id = :loanId")
    void markFineAsPaid(@Param("loanId") Long loanId);

    /**
     * ACTUALIZA LA FECHA DE DEVOLUCION DE UN PRESTAMO
     *
     * OPERACION QUE REGISTRA LA FECHA Y HORA DE DEVOLUCION
     * DE UN PRESTAMO ESPECIFICO. FUNDAMENTAL PARA COMPLETAR
     * EL CICLO DE VIDA DEL PRESTAMO.
     *
     * @param loanId ID DEL PRESTAMO A ACTUALIZAR
     * @param returnDate FECHA Y HORA DE DEVOLUCION
     */
    @Modifying
    @Query("UPDATE Loan l SET l.returnedAt = :returnDate WHERE l.id = :loanId")
    void updateReturnDate(@Param("loanId") Long loanId, @Param("returnDate") LocalDateTime returnDate);

    /**
     * CUENTA PRESTAMOS TOTALES DEL SISTEMA
     *
     * CALCULO EL NUMERO TOTAL DE PRESTAMOS REGISTRADOS EN EL SISTEMA.
     * METRICA FUNDAMENTAL PARA ESTADISTICAS GENERALES
     * Y REPORTES DE ACTIVIDAD.
     *
     * @return NUMERO TOTAL DE PRESTAMOS
     */
    @Query("SELECT COUNT(l) FROM Loan l")
    long countTotalLoans();

    /**
     * CUENTA PRESTAMOS ACTIVOS DEL SISTEMA
     *
     * CALCULO EL NUMERO DE PRESTAMOS QUE ACTUALMENTE ESTAN ACTIVOS
     * (NO DEVUELTOS). INDICADOR CLAVE DEL NIVEL DE ACTIVIDAD
     * ACTUAL DEL SISTEMA.
     *
     * @return NUMERO DE PRESTAMOS ACTIVOS
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnedAt IS NULL")
    long countActiveLoans();

    /**
     * BUSCA PRESTAMOS VENCIDOS DE UN USUARIO ESPECIFICO
     *
     * LOCALIZO PRESTAMOS VENCIDOS DE UN USUARIO DETERMINADO.
     * IMPORTANTE PARA GESTION PERSONALIZADA DE MOROSOS
     * Y APLICACION DE RESTRICCIONES INDIVIDUALES.
     *
     * @param user USUARIO A CONSULTAR
     * @return LISTA DE PRESTAMOS VENCIDOS DEL USUARIO
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueByUser(@Param("user") User user);

    /**
     * BUSCA EL PRESTAMO MAS RECIENTE DE UN USUARIO
     *
     * OBTENGO EL ULTIMO PRESTAMO REALIZADO POR UN USUARIO ESPECIFICO.
     * UTIL PARA VALIDACIONES DE HISTORIAL Y CONTROL DE FRECUENCIA
     * DE PRESTAMOS.
     *
     * @param user USUARIO A CONSULTAR
     * @return OPTIONAL CON EL PRESTAMO MAS RECIENTE
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user ORDER BY l.loanDate DESC")
    Optional<Loan> findMostRecentByUser(@Param("user") User user);

    /**
     * BUSCA EL ULTIMO PRESTAMO DE UN LIBRO
     *
     * LOCALIZO EL PRESTAMO MAS RECIENTE DE UN LIBRO ESPECIFICO.
     * IMPORTANTE PARA RASTREAR EL HISTORIAL DE CIRCULACION
     * Y ESTADO DE CADA EJEMPLAR.
     *
     * @param book LIBRO A CONSULTAR
     * @return OPTIONAL CON EL PRESTAMO MAS RECIENTE DEL LIBRO
     */
    @Query("SELECT l FROM Loan l WHERE l.book = :book ORDER BY l.loanDate DESC")
    Optional<Loan> findMostRecentByBook(@Param("book") Book book);

    /**
     * VERIFICA SI UN USUARIO PUEDE REALIZAR UN NUEVO PRESTAMO
     *
     * COMPRUEBO SI UN USUARIO CUMPLE LAS CONDICIONES PARA REALIZAR
     * UN NUEVO PRESTAMO (NO EXCEDER LIMITE, NO TENER MULTAS PENDIENTES).
     * VALIDACION CRITICA ANTES DE AUTORIZAR PRESTAMOS.
     *
     * @param user USUARIO A VALIDAR
     * @param maxLoans LIMITE MAXIMO DE PRESTAMOS SIMULTANEOS
     * @return TRUE SI PUEDE REALIZAR PRESTAMO, FALSE EN CASO CONTRARIO
     */
    @Query("SELECT CASE WHEN (COUNT(l) < :maxLoans AND " +
            "SUM(CASE WHEN l.fineAmount > 0 AND l.finePaid = false THEN 1 ELSE 0 END) = 0) " +
            "THEN true ELSE false END " +
            "FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL")
    boolean canUserBorrow(@Param("user") User user, @Param("maxLoans") int maxLoans);
}